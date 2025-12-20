package com.skribble.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection limiter to cap server load and gracefully reject new connections.
 * Thread-safe with lock-free operations for minimal overhead.
 */
@Component
public class ConnectionLimiter {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionLimiter.class);

    // Default limits - can be configured via application.properties
    private static final int DEFAULT_MAX_CONNECTIONS = 1000;
    private static final int DEFAULT_MAX_ROOMS = 100;
    private static final int DEFAULT_MAX_PLAYERS_PER_ROOM = 8;
    
    // Soft limit triggers warnings (90% of max)
    private static final double SOFT_LIMIT_RATIO = 0.9;

    private volatile int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private volatile int maxRooms = DEFAULT_MAX_ROOMS;
    private volatile int maxPlayersPerRoom = DEFAULT_MAX_PLAYERS_PER_ROOM;

    private final AtomicInteger currentConnections = new AtomicInteger(0);
    private final AtomicInteger currentRooms = new AtomicInteger(0);

    // Track if we're in overload state
    private volatile boolean overloaded = false;
    private volatile long overloadStartTime = 0;

    /**
     * Result of connection attempt.
     */
    public enum ConnectionResult {
        ALLOWED,
        REJECTED_MAX_CONNECTIONS,
        REJECTED_SERVER_OVERLOADED
    }

    /**
     * Result of room creation attempt.
     */
    public enum RoomCreationResult {
        ALLOWED,
        REJECTED_MAX_ROOMS
    }

    // ==================== Connection Management ====================

    /**
     * Try to acquire a connection slot.
     * @return ConnectionResult indicating success or rejection reason
     */
    public ConnectionResult tryAcquireConnection() {
        if (overloaded) {
            return ConnectionResult.REJECTED_SERVER_OVERLOADED;
        }

        int current = currentConnections.get();
        
        // Check soft limit for warning
        if (current >= (int)(maxConnections * SOFT_LIMIT_RATIO) && !overloaded) {
            logger.warn("Connection soft limit reached: current={}, max={}", current, maxConnections);
        }

        // Atomic increment with limit check
        while (true) {
            current = currentConnections.get();
            if (current >= maxConnections) {
                logger.warn("Connection rejected: max connections reached ({})", maxConnections);
                return ConnectionResult.REJECTED_MAX_CONNECTIONS;
            }
            if (currentConnections.compareAndSet(current, current + 1)) {
                return ConnectionResult.ALLOWED;
            }
            // CAS failed, retry
        }
    }

    /**
     * Release a connection slot.
     */
    public void releaseConnection() {
        int current = currentConnections.decrementAndGet();
        
        // Clear overload state if we're back under soft limit
        if (overloaded && current < (int)(maxConnections * SOFT_LIMIT_RATIO * 0.8)) {
            overloaded = false;
            logger.info("Server recovered from overload state, connections={}", current);
        }
    }

    // ==================== Room Management ====================

    /**
     * Try to acquire a room slot.
     * @return RoomCreationResult indicating success or rejection reason
     */
    public RoomCreationResult tryAcquireRoomSlot() {
        while (true) {
            int current = currentRooms.get();
            if (current >= maxRooms) {
                logger.warn("Room creation rejected: max rooms reached ({})", maxRooms);
                return RoomCreationResult.REJECTED_MAX_ROOMS;
            }
            if (currentRooms.compareAndSet(current, current + 1)) {
                return RoomCreationResult.ALLOWED;
            }
        }
    }

    /**
     * Release a room slot.
     */
    public void releaseRoomSlot() {
        currentRooms.decrementAndGet();
    }

    // ==================== Overload Management ====================

    /**
     * Set server to overloaded state (e.g., triggered by high CPU or memory).
     */
    public void setOverloaded(boolean overloaded) {
        if (overloaded && !this.overloaded) {
            this.overloadStartTime = System.currentTimeMillis();
            logger.error("Server entering overloaded state");
        } else if (!overloaded && this.overloaded) {
            long duration = System.currentTimeMillis() - overloadStartTime;
            logger.info("Server exiting overloaded state after {}ms", duration);
        }
        this.overloaded = overloaded;
    }

    public boolean isOverloaded() {
        return overloaded;
    }

    // ==================== Configuration ====================

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        logger.info("Max connections set to {}", maxConnections);
    }

    public void setMaxRooms(int maxRooms) {
        this.maxRooms = maxRooms;
        logger.info("Max rooms set to {}", maxRooms);
    }

    public void setMaxPlayersPerRoom(int maxPlayersPerRoom) {
        this.maxPlayersPerRoom = maxPlayersPerRoom;
        logger.info("Max players per room set to {}", maxPlayersPerRoom);
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMaxRooms() {
        return maxRooms;
    }

    public int getMaxPlayersPerRoom() {
        return maxPlayersPerRoom;
    }

    // ==================== Status ====================

    public int getCurrentConnections() {
        return currentConnections.get();
    }

    public int getCurrentRooms() {
        return currentRooms.get();
    }

    public double getConnectionUtilization() {
        return (double) currentConnections.get() / maxConnections;
    }

    public double getRoomUtilization() {
        return (double) currentRooms.get() / maxRooms;
    }

    /**
     * Get a status summary for monitoring.
     */
    public String getStatusSummary() {
        return String.format("connections=%d/%d (%.1f%%), rooms=%d/%d (%.1f%%), overloaded=%s",
                currentConnections.get(), maxConnections, getConnectionUtilization() * 100,
                currentRooms.get(), maxRooms, getRoomUtilization() * 100,
                overloaded);
    }
}
