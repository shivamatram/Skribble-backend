package com.skribble.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe performance monitoring for tracking system metrics.
 * Uses lock-free data structures for minimal overhead in hot paths.
 */
@Component
public class PerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    // Metrics reporting interval
    private static final long REPORT_INTERVAL_SECONDS = 60;

    // Active counts (using AtomicInteger for precise counts)
    private final AtomicInteger activeRooms = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger activePlayers = new AtomicInteger(0);

    // Throughput counters (using LongAdder for high-contention scenarios)
    private final LongAdder messagesReceived = new LongAdder();
    private final LongAdder messagesSent = new LongAdder();
    private final LongAdder drawStrokesProcessed = new LongAdder();
    private final LongAdder guessesProcessed = new LongAdder();
    private final LongAdder droppedMessages = new LongAdder();
    private final LongAdder batchedMessages = new LongAdder();

    // Per-room message counts for isolation monitoring
    private final ConcurrentHashMap<String, LongAdder> roomMessageCounts = new ConcurrentHashMap<>();

    // Latency tracking (last N samples for percentile calculation)
    private final LongAdder totalProcessingTimeNanos = new LongAdder();
    private final LongAdder processingCount = new LongAdder();

    // Peak values
    private volatile int peakRooms = 0;
    private volatile int peakConnections = 0;
    private volatile int peakPlayers = 0;

    // Last report values (for rate calculation)
    private volatile long lastReportTime = System.currentTimeMillis();
    private volatile long lastMessagesReceived = 0;
    private volatile long lastMessagesSent = 0;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "perf-monitor");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::reportMetrics, 
                REPORT_INTERVAL_SECONDS, REPORT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        logger.info("PerformanceMonitor initialized, reporting every {}s", REPORT_INTERVAL_SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("PerformanceMonitor shutdown");
    }

    // ==================== Room Tracking ====================

    public void roomCreated(String roomId) {
        int current = activeRooms.incrementAndGet();
        roomMessageCounts.put(roomId, new LongAdder());
        updatePeakRooms(current);
    }

    public void roomDestroyed(String roomId) {
        activeRooms.decrementAndGet();
        roomMessageCounts.remove(roomId);
    }

    public int getActiveRoomCount() {
        return activeRooms.get();
    }

    // ==================== Connection Tracking ====================

    public void connectionOpened() {
        int current = activeConnections.incrementAndGet();
        updatePeakConnections(current);
    }

    public void connectionClosed() {
        activeConnections.decrementAndGet();
    }

    public int getActiveConnectionCount() {
        return activeConnections.get();
    }

    // ==================== Player Tracking ====================

    public void playerJoined() {
        int current = activePlayers.incrementAndGet();
        updatePeakPlayers(current);
    }

    public void playerLeft() {
        activePlayers.decrementAndGet();
    }

    public int getActivePlayerCount() {
        return activePlayers.get();
    }

    // ==================== Message Throughput ====================

    public void messageReceived() {
        messagesReceived.increment();
    }

    public void messageReceived(String roomId) {
        messagesReceived.increment();
        LongAdder roomCounter = roomMessageCounts.get(roomId);
        if (roomCounter != null) {
            roomCounter.increment();
        }
    }

    public void messageSent() {
        messagesSent.increment();
    }

    public void messageSent(int count) {
        messagesSent.add(count);
    }

    public void drawStrokeProcessed() {
        drawStrokesProcessed.increment();
    }

    public void guessProcessed() {
        guessesProcessed.increment();
    }

    public void messageDropped() {
        droppedMessages.increment();
    }

    public void messagesBatched(int count) {
        batchedMessages.add(count);
    }

    // ==================== Latency Tracking ====================

    public void recordProcessingTime(long nanos) {
        totalProcessingTimeNanos.add(nanos);
        processingCount.increment();
    }

    public double getAverageProcessingTimeMs() {
        long count = processingCount.sum();
        if (count == 0) return 0.0;
        return (totalProcessingTimeNanos.sum() / (double) count) / 1_000_000.0;
    }

    // ==================== Peak Value Updates ====================

    private void updatePeakRooms(int current) {
        if (current > peakRooms) {
            peakRooms = current;
        }
    }

    private void updatePeakConnections(int current) {
        if (current > peakConnections) {
            peakConnections = current;
        }
    }

    private void updatePeakPlayers(int current) {
        if (current > peakPlayers) {
            peakPlayers = current;
        }
    }

    // ==================== Metrics Snapshot ====================

    public MetricsSnapshot getSnapshot() {
        return new MetricsSnapshot(
                activeRooms.get(),
                activeConnections.get(),
                activePlayers.get(),
                messagesReceived.sum(),
                messagesSent.sum(),
                drawStrokesProcessed.sum(),
                guessesProcessed.sum(),
                droppedMessages.sum(),
                batchedMessages.sum(),
                getAverageProcessingTimeMs(),
                peakRooms,
                peakConnections,
                peakPlayers
        );
    }

    // ==================== Periodic Reporting ====================

    private void reportMetrics() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastReportTime;
        double elapsedSec = elapsed / 1000.0;

        long currentReceived = messagesReceived.sum();
        long currentSent = messagesSent.sum();

        double receiveRate = (currentReceived - lastMessagesReceived) / elapsedSec;
        double sendRate = (currentSent - lastMessagesSent) / elapsedSec;

        logger.info("PERF: rooms={}, connections={}, players={}, recv/s={}, send/s={}, dropped={}, avgMs={}",
                activeRooms.get(),
                activeConnections.get(),
                activePlayers.get(),
                String.format("%.1f", receiveRate),
                String.format("%.1f", sendRate),
                droppedMessages.sum(),
                String.format("%.2f", getAverageProcessingTimeMs()));

        lastReportTime = now;
        lastMessagesReceived = currentReceived;
        lastMessagesSent = currentSent;
    }

    /**
     * Immutable metrics snapshot for thread-safe reading.
     */
    public static class MetricsSnapshot {
        private final int activeRooms;
        private final int activeConnections;
        private final int activePlayers;
        private final long messagesReceived;
        private final long messagesSent;
        private final long drawStrokesProcessed;
        private final long guessesProcessed;
        private final long droppedMessages;
        private final long batchedMessages;
        private final double avgProcessingTimeMs;
        private final int peakRooms;
        private final int peakConnections;
        private final int peakPlayers;

        public MetricsSnapshot(int activeRooms, int activeConnections, int activePlayers,
                               long messagesReceived, long messagesSent, long drawStrokesProcessed,
                               long guessesProcessed, long droppedMessages, long batchedMessages,
                               double avgProcessingTimeMs, int peakRooms, int peakConnections, int peakPlayers) {
            this.activeRooms = activeRooms;
            this.activeConnections = activeConnections;
            this.activePlayers = activePlayers;
            this.messagesReceived = messagesReceived;
            this.messagesSent = messagesSent;
            this.drawStrokesProcessed = drawStrokesProcessed;
            this.guessesProcessed = guessesProcessed;
            this.droppedMessages = droppedMessages;
            this.batchedMessages = batchedMessages;
            this.avgProcessingTimeMs = avgProcessingTimeMs;
            this.peakRooms = peakRooms;
            this.peakConnections = peakConnections;
            this.peakPlayers = peakPlayers;
        }

        public int getActiveRooms() { return activeRooms; }
        public int getActiveConnections() { return activeConnections; }
        public int getActivePlayers() { return activePlayers; }
        public long getMessagesReceived() { return messagesReceived; }
        public long getMessagesSent() { return messagesSent; }
        public long getDrawStrokesProcessed() { return drawStrokesProcessed; }
        public long getGuessesProcessed() { return guessesProcessed; }
        public long getDroppedMessages() { return droppedMessages; }
        public long getBatchedMessages() { return batchedMessages; }
        public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
        public int getPeakRooms() { return peakRooms; }
        public int getPeakConnections() { return peakConnections; }
        public int getPeakPlayers() { return peakPlayers; }
    }
}
