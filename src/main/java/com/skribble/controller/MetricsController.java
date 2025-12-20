package com.skribble.controller;

import com.skribble.performance.ConnectionLimiter;
import com.skribble.performance.PerformanceMonitor;
import com.skribble.performance.RoomCleanupScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for server metrics and health monitoring.
 * Provides endpoints for monitoring system health and performance.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final PerformanceMonitor performanceMonitor;
    private final ConnectionLimiter connectionLimiter;
    private final RoomCleanupScheduler roomCleanupScheduler;

    public MetricsController(PerformanceMonitor performanceMonitor,
                              ConnectionLimiter connectionLimiter,
                              RoomCleanupScheduler roomCleanupScheduler) {
        this.performanceMonitor = performanceMonitor;
        this.connectionLimiter = connectionLimiter;
        this.roomCleanupScheduler = roomCleanupScheduler;
    }

    /**
     * Get current server metrics snapshot.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {
        PerformanceMonitor.MetricsSnapshot snapshot = performanceMonitor.getSnapshot();
        RoomCleanupScheduler.CleanupStats cleanupStats = roomCleanupScheduler.getStats();

        Map<String, Object> metrics = Map.of(
                "active", Map.of(
                        "rooms", snapshot.getActiveRooms(),
                        "connections", snapshot.getActiveConnections(),
                        "players", snapshot.getActivePlayers()
                ),
                "peak", Map.of(
                        "rooms", snapshot.getPeakRooms(),
                        "connections", snapshot.getPeakConnections(),
                        "players", snapshot.getPeakPlayers()
                ),
                "throughput", Map.of(
                        "messagesReceived", snapshot.getMessagesReceived(),
                        "messagesSent", snapshot.getMessagesSent(),
                        "drawStrokes", snapshot.getDrawStrokesProcessed(),
                        "guesses", snapshot.getGuessesProcessed(),
                        "dropped", snapshot.getDroppedMessages(),
                        "batched", snapshot.getBatchedMessages()
                ),
                "performance", Map.of(
                        "avgProcessingTimeMs", snapshot.getAvgProcessingTimeMs()
                ),
                "rooms", Map.of(
                        "total", cleanupStats.totalRooms(),
                        "empty", cleanupStats.emptyRooms(),
                        "ended", cleanupStats.endedRooms(),
                        "waiting", cleanupStats.waitingRooms(),
                        "inProgress", cleanupStats.inProgressRooms()
                ),
                "limits", Map.of(
                        "connectionUtilization", connectionLimiter.getConnectionUtilization(),
                        "roomUtilization", connectionLimiter.getRoomUtilization(),
                        "overloaded", connectionLimiter.isOverloaded()
                )
        );

        return ResponseEntity.ok(metrics);
    }

    /**
     * Get server health status.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        boolean healthy = !connectionLimiter.isOverloaded() 
                && connectionLimiter.getConnectionUtilization() < 0.95;

        String status = healthy ? "UP" : "DEGRADED";

        Map<String, Object> health = Map.of(
                "status", status,
                "timestamp", System.currentTimeMillis(),
                "connections", performanceMonitor.getActiveConnectionCount(),
                "rooms", performanceMonitor.getActiveRoomCount(),
                "overloaded", connectionLimiter.isOverloaded()
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Get connection limiter status.
     */
    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimits() {
        Map<String, Object> limits = Map.of(
                "maxConnections", connectionLimiter.getMaxConnections(),
                "currentConnections", connectionLimiter.getCurrentConnections(),
                "connectionUtilization", connectionLimiter.getConnectionUtilization(),
                "maxRooms", connectionLimiter.getMaxRooms(),
                "currentRooms", connectionLimiter.getCurrentRooms(),
                "roomUtilization", connectionLimiter.getRoomUtilization(),
                "overloaded", connectionLimiter.isOverloaded(),
                "status", connectionLimiter.getStatusSummary()
        );

        return ResponseEntity.ok(limits);
    }
}
