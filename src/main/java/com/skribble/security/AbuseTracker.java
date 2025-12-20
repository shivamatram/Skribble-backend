package com.skribble.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks player abuse patterns and enforces penalties.
 * Handles muting, warnings, and disconnection recommendations.
 */
@Component
public class AbuseTracker {

    private static final Logger logger = LoggerFactory.getLogger(AbuseTracker.class);

    // Thresholds for penalties
    public static final int WARN_THRESHOLD = 3;           // Warnings after this many violations
    public static final int MUTE_THRESHOLD = 5;           // Mute after this many violations
    public static final int DISCONNECT_THRESHOLD = 10;    // Disconnect after this many violations

    // Durations
    public static final long MUTE_DURATION_MS = 30000;    // 30 seconds mute
    public static final long VIOLATION_DECAY_MS = 60000;  // Violations decay after 1 minute of good behavior
    public static final long CLEANUP_INTERVAL_MS = 30000; // Cleanup every 30 seconds

    // Violation types
    public enum ViolationType {
        UNAUTHORIZED_DRAW,      // Non-drawer tried to draw
        GUESS_AFTER_CORRECT,    // Guessed after already correct
        ACTION_AFTER_GAME_END,  // Action after game ended
        RATE_LIMIT_EXCEEDED,    // Too many requests
        INVALID_ACTION,         // General invalid action
        SPAM_GUESS,             // Spamming guesses
        SPAM_CHAT,              // Spamming chat
        INVALID_PAYLOAD,        // Malformed data
        SCORE_MANIPULATION,     // Attempted score manipulation
        TIMER_MANIPULATION      // Attempted timer manipulation
    }

    // Player tracking data
    private final Map<String, PlayerAbuseData> playerData = new ConcurrentHashMap<>();

    // IP-based tracking for connection abuse
    private final Map<String, AtomicInteger> ipViolations = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public AbuseTracker() {
        startCleanupTask();
    }

    /**
     * Record a violation for a player.
     * @return The action that should be taken
     */
    public AbuseAction recordViolation(String playerId, ViolationType type) {
        PlayerAbuseData data = playerData.computeIfAbsent(playerId, k -> new PlayerAbuseData());
        int totalViolations = data.recordViolation(type);

        logger.warn("Abuse violation recorded: playerId={}, type={}, totalViolations={}",
                playerId, type, totalViolations);

        // Determine action based on violation count
        if (totalViolations >= DISCONNECT_THRESHOLD) {
            logger.error("Player exceeded disconnect threshold: playerId={}, violations={}",
                    playerId, totalViolations);
            return AbuseAction.DISCONNECT;
        } else if (totalViolations >= MUTE_THRESHOLD) {
            data.mute(MUTE_DURATION_MS);
            logger.warn("Player muted for abuse: playerId={}, violations={}, muteMs={}",
                    playerId, totalViolations, MUTE_DURATION_MS);
            return AbuseAction.MUTE;
        } else if (totalViolations >= WARN_THRESHOLD) {
            return AbuseAction.WARN;
        }

        return AbuseAction.NONE;
    }

    /**
     * Record a violation from an IP address (for connection-level abuse).
     */
    public void recordIpViolation(String ipAddress) {
        ipViolations.computeIfAbsent(ipAddress, k -> new AtomicInteger(0)).incrementAndGet();
        logger.warn("IP violation recorded: ip={}", ipAddress);
    }

    /**
     * Check if a player is currently muted.
     */
    public boolean isMuted(String playerId) {
        PlayerAbuseData data = playerData.get(playerId);
        return data != null && data.isMuted();
    }

    /**
     * Get remaining mute time in milliseconds.
     */
    public long getMuteRemainingMs(String playerId) {
        PlayerAbuseData data = playerData.get(playerId);
        return data != null ? data.getMuteRemainingMs() : 0;
    }

    /**
     * Check if a player should be disconnected.
     */
    public boolean shouldDisconnect(String playerId) {
        PlayerAbuseData data = playerData.get(playerId);
        return data != null && data.getViolationCount() >= DISCONNECT_THRESHOLD;
    }

    /**
     * Get violation count for a player.
     */
    public int getViolationCount(String playerId) {
        PlayerAbuseData data = playerData.get(playerId);
        return data != null ? data.getViolationCount() : 0;
    }

    /**
     * Get violation count for a specific type.
     */
    public int getViolationCount(String playerId, ViolationType type) {
        PlayerAbuseData data = playerData.get(playerId);
        return data != null ? data.getViolationCount(type) : 0;
    }

    /**
     * Clear all data for a player (e.g., on game end or room leave).
     */
    public void clearPlayer(String playerId) {
        playerData.remove(playerId);
    }

    /**
     * Check if an IP is blocked.
     */
    public boolean isIpBlocked(String ipAddress) {
        AtomicInteger violations = ipViolations.get(ipAddress);
        return violations != null && violations.get() >= DISCONNECT_THRESHOLD;
    }

    /**
     * Start cleanup task to decay old violations.
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                long now = System.currentTimeMillis();
                // Remove entries with no recent violations
                playerData.entrySet().removeIf(entry -> entry.getValue().shouldCleanup(now, VIOLATION_DECAY_MS));
                // Decay IP violations
                ipViolations.entrySet().removeIf(entry -> {
                    int current = entry.getValue().get();
                    if (current > 0) {
                        entry.getValue().decrementAndGet();
                        return false;
                    }
                    return true;
                });
            } catch (Exception e) {
                logger.error("Error in abuse tracker cleanup: {}", e.getMessage(), e);
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown the tracker.
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
    }

    /**
     * Actions to take based on abuse level.
     */
    public enum AbuseAction {
        NONE,       // No action needed
        WARN,       // Send warning to player
        MUTE,       // Mute the player temporarily
        DISCONNECT  // Disconnect the player
    }

    /**
     * Internal class to track per-player abuse data.
     */
    private static class PlayerAbuseData {
        private final Map<ViolationType, AtomicInteger> violationsByType = new ConcurrentHashMap<>();
        private final AtomicInteger totalViolations = new AtomicInteger(0);
        private final AtomicLong lastViolationTime = new AtomicLong(0);
        private final AtomicLong muteUntil = new AtomicLong(0);

        public int recordViolation(ViolationType type) {
            violationsByType.computeIfAbsent(type, k -> new AtomicInteger(0)).incrementAndGet();
            lastViolationTime.set(System.currentTimeMillis());
            return totalViolations.incrementAndGet();
        }

        public int getViolationCount() {
            return totalViolations.get();
        }

        public int getViolationCount(ViolationType type) {
            AtomicInteger count = violationsByType.get(type);
            return count != null ? count.get() : 0;
        }

        public void mute(long durationMs) {
            muteUntil.set(System.currentTimeMillis() + durationMs);
        }

        public boolean isMuted() {
            return System.currentTimeMillis() < muteUntil.get();
        }

        public long getMuteRemainingMs() {
            long remaining = muteUntil.get() - System.currentTimeMillis();
            return Math.max(0, remaining);
        }

        public boolean shouldCleanup(long now, long decayThreshold) {
            // Clean up if no violations recently and no active mute
            return totalViolations.get() == 0 ||
                    (now - lastViolationTime.get() > decayThreshold && !isMuted());
        }
    }
}
