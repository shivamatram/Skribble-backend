package com.skribble.performance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Batches small drawing stroke messages to reduce network overhead.
 * Per-room batching ensures isolation between rooms.
 * 
 * Uses fine-grained locking per room to minimize contention.
 */
@Component
public class StrokeBatcher {

    private static final Logger logger = LoggerFactory.getLogger(StrokeBatcher.class);

    // Batch configuration
    private static final int MAX_BATCH_SIZE = 10;
    private static final long BATCH_FLUSH_INTERVAL_MS = 50; // 50ms = 20 flushes/sec max
    private static final long STALE_STROKE_THRESHOLD_MS = 500; // Drop strokes older than 500ms

    private final ObjectMapper objectMapper;
    private final PerformanceMonitor performanceMonitor;

    // Per-room batch queues with their own locks
    private final ConcurrentHashMap<String, RoomBatch> roomBatches = new ConcurrentHashMap<>();

    // Flush scheduler
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "stroke-batcher");
        t.setDaemon(true);
        t.setPriority(Thread.MAX_PRIORITY - 1); // High priority for low latency
        return t;
    });

    public StrokeBatcher(ObjectMapper objectMapper, PerformanceMonitor performanceMonitor) {
        this.objectMapper = objectMapper;
        this.performanceMonitor = performanceMonitor;
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::flushAllBatches, 
                BATCH_FLUSH_INTERVAL_MS, BATCH_FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        logger.info("StrokeBatcher initialized, flush interval={}ms, maxBatch={}", 
                BATCH_FLUSH_INTERVAL_MS, MAX_BATCH_SIZE);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("StrokeBatcher shutdown");
    }

    /**
     * Stroke data to be batched.
     */
    public static class StrokeData {
        private final String playerId;
        private final List<Map<String, Object>> points;
        private final String color;
        private final float strokeWidth;
        private final long timestamp;

        public StrokeData(String playerId, List<Map<String, Object>> points, 
                         String color, float strokeWidth) {
            this.playerId = playerId;
            this.points = points;
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPlayerId() { return playerId; }
        public List<Map<String, Object>> getPoints() { return points; }
        public String getColor() { return color; }
        public float getStrokeWidth() { return strokeWidth; }
        public long getTimestamp() { return timestamp; }

        public boolean isStale() {
            return System.currentTimeMillis() - timestamp > STALE_STROKE_THRESHOLD_MS;
        }
    }

    /**
     * Per-room batch with its own lock for isolation.
     */
    private static class RoomBatch {
        private final String roomId;
        private final List<StrokeData> strokes = new ArrayList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private Consumer<String> flushCallback;

        RoomBatch(String roomId) {
            this.roomId = roomId;
        }

        void setFlushCallback(Consumer<String> callback) {
            this.flushCallback = callback;
        }
    }

    /**
     * Register a room for batching.
     */
    public void registerRoom(String roomId, Consumer<String> flushCallback) {
        RoomBatch batch = new RoomBatch(roomId);
        batch.setFlushCallback(flushCallback);
        roomBatches.put(roomId, batch);
    }

    /**
     * Unregister a room (cleanup).
     */
    public void unregisterRoom(String roomId) {
        RoomBatch batch = roomBatches.remove(roomId);
        if (batch != null) {
            // Flush any remaining strokes
            flushBatch(batch);
        }
    }

    /**
     * Add a stroke to the batch. May trigger immediate flush if batch is full.
     * 
     * @param roomId The room ID
     * @param stroke The stroke data
     * @return true if stroke was accepted, false if dropped (stale or no room)
     */
    public boolean addStroke(String roomId, StrokeData stroke) {
        // Drop stale strokes immediately
        if (stroke.isStale()) {
            performanceMonitor.messageDropped();
            return false;
        }

        RoomBatch batch = roomBatches.get(roomId);
        if (batch == null) {
            return false;
        }

        boolean shouldFlush = false;

        batch.lock.lock();
        try {
            batch.strokes.add(stroke);
            if (batch.strokes.size() >= MAX_BATCH_SIZE) {
                shouldFlush = true;
            }
        } finally {
            batch.lock.unlock();
        }

        // Flush outside lock to minimize lock hold time
        if (shouldFlush) {
            flushBatch(batch);
        }

        return true;
    }

    /**
     * Flush a specific room's batch.
     */
    private void flushBatch(RoomBatch batch) {
        List<StrokeData> toFlush;

        batch.lock.lock();
        try {
            if (batch.strokes.isEmpty()) {
                return;
            }
            toFlush = new ArrayList<>(batch.strokes);
            batch.strokes.clear();
        } finally {
            batch.lock.unlock();
        }

        // Drop stale strokes from batch
        int originalSize = toFlush.size();
        toFlush.removeIf(StrokeData::isStale);
        int droppedCount = originalSize - toFlush.size();
        if (droppedCount > 0) {
            performanceMonitor.messageDropped();
        }

        if (toFlush.isEmpty()) {
            return;
        }

        // Build batched message
        String batchedMessage = buildBatchedMessage(batch.roomId, toFlush);
        if (batchedMessage != null && batch.flushCallback != null) {
            batch.flushCallback.accept(batchedMessage);
            performanceMonitor.messagesBatched(toFlush.size());
        }
    }

    /**
     * Flush all room batches (called periodically).
     */
    private void flushAllBatches() {
        for (RoomBatch batch : roomBatches.values()) {
            flushBatch(batch);
        }
    }

    /**
     * Build a batched stroke broadcast message.
     */
    private String buildBatchedMessage(String roomId, List<StrokeData> strokes) {
        try {
            // Single stroke - send as regular STROKE_BROADCAST
            if (strokes.size() == 1) {
                StrokeData stroke = strokes.get(0);
                Map<String, Object> message = Map.of(
                        "type", "STROKE_BROADCAST",
                        "roomId", roomId,
                        "playerId", stroke.getPlayerId(),
                        "points", stroke.getPoints(),
                        "color", stroke.getColor(),
                        "strokeWidth", stroke.getStrokeWidth(),
                        "timestamp", stroke.getTimestamp()
                );
                return objectMapper.writeValueAsString(message);
            }

            // Multiple strokes - send as STROKE_BATCH
            List<Map<String, Object>> strokeList = new ArrayList<>();
            for (StrokeData stroke : strokes) {
                strokeList.add(Map.of(
                        "playerId", stroke.getPlayerId(),
                        "points", stroke.getPoints(),
                        "color", stroke.getColor(),
                        "strokeWidth", stroke.getStrokeWidth(),
                        "timestamp", stroke.getTimestamp()
                ));
            }

            Map<String, Object> batchMessage = Map.of(
                    "type", "STROKE_BATCH",
                    "roomId", roomId,
                    "strokes", strokeList,
                    "count", strokes.size(),
                    "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(batchMessage);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize batched strokes: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Force flush a specific room (e.g., on round end).
     */
    public void forceFlush(String roomId) {
        RoomBatch batch = roomBatches.get(roomId);
        if (batch != null) {
            flushBatch(batch);
        }
    }

    /**
     * Get pending stroke count for a room.
     */
    public int getPendingCount(String roomId) {
        RoomBatch batch = roomBatches.get(roomId);
        if (batch == null) {
            return 0;
        }
        batch.lock.lock();
        try {
            return batch.strokes.size();
        } finally {
            batch.lock.unlock();
        }
    }
}
