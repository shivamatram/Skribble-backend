package com.skribble.performance;

import com.skribble.room.RoomManager;
import com.skribble.room.RoomState;
import com.skribble.room.RoomStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled cleanup service for removing stale rooms and preventing memory leaks.
 * Runs periodically to clean up:
 * - Empty rooms
 * - Rooms that have been ENDED for too long
 * - Orphaned session data
 */
@Component
public class RoomCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RoomCleanupScheduler.class);

    // Cleanup configuration
    private static final long CLEANUP_INTERVAL_SECONDS = 60;
    private static final long ENDED_ROOM_TTL_MS = 5 * 60 * 1000; // 5 minutes after game ends
    private static final long EMPTY_ROOM_TTL_MS = 2 * 60 * 1000; // 2 minutes if empty
    private static final long STALE_WAITING_ROOM_TTL_MS = 10 * 60 * 1000; // 10 minutes in WAITING

    private final RoomManager roomManager;
    private final PerformanceMonitor performanceMonitor;
    private final ConnectionLimiter connectionLimiter;
    private final StrokeBatcher strokeBatcher;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "room-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RoomCleanupScheduler(RoomManager roomManager, PerformanceMonitor performanceMonitor,
                                 ConnectionLimiter connectionLimiter, StrokeBatcher strokeBatcher) {
        this.roomManager = roomManager;
        this.performanceMonitor = performanceMonitor;
        this.connectionLimiter = connectionLimiter;
        this.strokeBatcher = strokeBatcher;
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::performCleanup, 
                CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
        logger.info("RoomCleanupScheduler initialized, interval={}s", CLEANUP_INTERVAL_SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        logger.info("RoomCleanupScheduler shutdown");
    }

    /**
     * Perform cleanup of stale rooms.
     */
    private void performCleanup() {
        long now = System.currentTimeMillis();
        List<String> roomsToRemove = new ArrayList<>();

        for (RoomState room : roomManager.getAllRooms()) {
            String roomId = room.getRoomId();
            boolean shouldRemove = false;
            String reason = "";

            // Check various cleanup conditions
            if (room.getPlayerCount() == 0) {
                // Empty room - check if it's been empty long enough
                // Use room creation/modification tracking if available
                shouldRemove = true;
                reason = "empty room";
            } else if (room.getStatus() == RoomStatus.ENDED) {
                // Game ended - check TTL
                long gameEndTime = room.getGameEndTime();
                if (gameEndTime > 0 && (now - gameEndTime) > ENDED_ROOM_TTL_MS) {
                    shouldRemove = true;
                    reason = "ended room TTL expired";
                }
            } else if (room.getStatus() == RoomStatus.WAITING) {
                // Stale waiting room
                long createdTime = room.getCreatedTime();
                if (createdTime > 0 && (now - createdTime) > STALE_WAITING_ROOM_TTL_MS 
                    && room.getPlayerCount() < room.getMinPlayers()) {
                    shouldRemove = true;
                    reason = "stale waiting room";
                }
            }

            if (shouldRemove) {
                roomsToRemove.add(roomId);
                logger.info("Marking room for cleanup: roomId={}, reason={}, players={}", 
                        roomId, reason, room.getPlayerCount());
            }
        }

        // Remove marked rooms
        for (String roomId : roomsToRemove) {
            cleanupRoom(roomId);
        }

        if (!roomsToRemove.isEmpty()) {
            logger.info("Cleanup completed: removed {} rooms", roomsToRemove.size());
        }
    }

    /**
     * Clean up a specific room and release resources.
     */
    public void cleanupRoom(String roomId) {
        // Unregister from stroke batcher
        strokeBatcher.unregisterRoom(roomId);

        // Remove from room manager
        roomManager.removeRoom(roomId);

        // Release room slot
        connectionLimiter.releaseRoomSlot();

        // Update metrics
        performanceMonitor.roomDestroyed(roomId);

        logger.debug("Room cleaned up: roomId={}", roomId);
    }

    /**
     * Force immediate cleanup of a specific room.
     */
    public void forceCleanup(String roomId) {
        RoomState room = roomManager.getRoom(roomId).orElse(null);
        if (room != null) {
            logger.info("Forcing cleanup of room: roomId={}, status={}, players={}", 
                    roomId, room.getStatus(), room.getPlayerCount());
            cleanupRoom(roomId);
        }
    }

    /**
     * Get cleanup statistics.
     */
    public CleanupStats getStats() {
        int totalRooms = 0;
        int emptyRooms = 0;
        int endedRooms = 0;
        int waitingRooms = 0;
        int inProgressRooms = 0;

        for (RoomState room : roomManager.getAllRooms()) {
            totalRooms++;
            if (room.getPlayerCount() == 0) {
                emptyRooms++;
            }
            switch (room.getStatus()) {
                case ENDED -> endedRooms++;
                case WAITING -> waitingRooms++;
                case IN_PROGRESS -> inProgressRooms++;
                default -> {}
            }
        }

        return new CleanupStats(totalRooms, emptyRooms, endedRooms, waitingRooms, inProgressRooms);
    }

    /**
     * Cleanup statistics.
     */
    public record CleanupStats(
            int totalRooms,
            int emptyRooms,
            int endedRooms,
            int waitingRooms,
            int inProgressRooms
    ) {}
}
