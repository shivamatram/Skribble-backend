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
 * Thread-safe rate limiter for various game actions.
 * Uses sliding window algorithm for accurate rate limiting.
 */
@Component
public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    // Rate limit configurations
    public static final int GUESS_MAX_PER_SECOND = 3;
    public static final int DRAW_STROKE_MAX_PER_SECOND = 60;
    public static final int CONNECTION_MAX_PER_MINUTE = 5;
    public static final int CHAT_MAX_PER_SECOND = 2;

    // Time windows in milliseconds
    private static final long SECOND_MS = 1000;
    private static final long MINUTE_MS = 60000;
    private static final long CLEANUP_INTERVAL_MS = 30000;

    // Rate limit tracking - uses sliding window counters
    // Map: identifier -> WindowedCounter
    private final Map<String, WindowedCounter> guessLimits = new ConcurrentHashMap<>();
    private final Map<String, WindowedCounter> drawStrokeLimits = new ConcurrentHashMap<>();
    private final Map<String, WindowedCounter> connectionLimits = new ConcurrentHashMap<>();
    private final Map<String, WindowedCounter> chatLimits = new ConcurrentHashMap<>();

    // Violation tracking
    private final Map<String, AtomicInteger> violationCounts = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public RateLimiter() {
        startCleanupTask();
    }

    /**
     * Check if a guess submission is allowed for a player.
     * @param playerId The player's ID
     * @return true if allowed, false if rate limited
     */
    public boolean allowGuess(String playerId) {
        return checkAndRecord(guessLimits, playerId, GUESS_MAX_PER_SECOND, SECOND_MS, "GUESS");
    }

    /**
     * Check if a draw stroke is allowed for a player.
     * @param playerId The player's ID
     * @return true if allowed, false if rate limited
     */
    public boolean allowDrawStroke(String playerId) {
        return checkAndRecord(drawStrokeLimits, playerId, DRAW_STROKE_MAX_PER_SECOND, SECOND_MS, "DRAW_STROKE");
    }

    /**
     * Check if a connection attempt is allowed for an IP address.
     * @param ipAddress The IP address
     * @return true if allowed, false if rate limited
     */
    public boolean allowConnection(String ipAddress) {
        return checkAndRecord(connectionLimits, ipAddress, CONNECTION_MAX_PER_MINUTE, MINUTE_MS, "CONNECTION");
    }

    /**
     * Check if a chat message is allowed for a player.
     * @param playerId The player's ID
     * @return true if allowed, false if rate limited
     */
    public boolean allowChat(String playerId) {
        return checkAndRecord(chatLimits, playerId, CHAT_MAX_PER_SECOND, SECOND_MS, "CHAT");
    }

    /**
     * Get the number of rate limit violations for an identifier.
     */
    public int getViolationCount(String identifier) {
        AtomicInteger count = violationCounts.get(identifier);
        return count != null ? count.get() : 0;
    }

    /**
     * Reset violation count for an identifier.
     */
    public void resetViolations(String identifier) {
        violationCounts.remove(identifier);
    }

    /**
     * Remove all rate limit data for an identifier (e.g., on disconnect).
     */
    public void removeIdentifier(String identifier) {
        guessLimits.remove(identifier);
        drawStrokeLimits.remove(identifier);
        chatLimits.remove(identifier);
        // Keep connection limits keyed by IP
    }

    /**
     * Check rate limit and record action if allowed.
     */
    private boolean checkAndRecord(Map<String, WindowedCounter> limits, String identifier,
                                    int maxCount, long windowMs, String actionType) {
        WindowedCounter counter = limits.computeIfAbsent(identifier,
                k -> new WindowedCounter(windowMs));

        if (counter.incrementIfAllowed(maxCount)) {
            return true;
        }

        // Rate limit exceeded - record violation
        violationCounts.computeIfAbsent(identifier, k -> new AtomicInteger(0)).incrementAndGet();
        logger.warn("Rate limit exceeded: type={}, identifier={}, limit={}/{}ms",
                actionType, identifier, maxCount, windowMs);
        return false;
    }

    /**
     * Start cleanup task to remove stale entries.
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                long now = System.currentTimeMillis();
                cleanupStaleEntries(guessLimits, now);
                cleanupStaleEntries(drawStrokeLimits, now);
                cleanupStaleEntries(connectionLimits, now);
                cleanupStaleEntries(chatLimits, now);
            } catch (Exception e) {
                logger.error("Error in rate limiter cleanup: {}", e.getMessage(), e);
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Remove entries that haven't been accessed recently.
     */
    private void cleanupStaleEntries(Map<String, WindowedCounter> limits, long now) {
        limits.entrySet().removeIf(entry -> entry.getValue().isStale(now, MINUTE_MS * 5));
    }

    /**
     * Shutdown the rate limiter.
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
    }

    /**
     * Sliding window counter for rate limiting.
     */
    private static class WindowedCounter {
        private final long windowMs;
        private final AtomicLong windowStart = new AtomicLong(0);
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong lastAccess = new AtomicLong(System.currentTimeMillis());

        public WindowedCounter(long windowMs) {
            this.windowMs = windowMs;
        }

        /**
         * Increment counter if under limit, using sliding window.
         * @return true if increment succeeded (under limit)
         */
        public synchronized boolean incrementIfAllowed(int maxCount) {
            long now = System.currentTimeMillis();
            lastAccess.set(now);

            long currentWindowStart = windowStart.get();

            // Check if we need to slide the window
            if (now - currentWindowStart >= windowMs) {
                // Reset for new window
                windowStart.set(now);
                count.set(1);
                return true;
            }

            // Within current window - check limit
            if (count.get() < maxCount) {
                count.incrementAndGet();
                return true;
            }

            return false;
        }

        /**
         * Check if this counter is stale (not accessed recently).
         */
        public boolean isStale(long now, long staleThresholdMs) {
            return now - lastAccess.get() > staleThresholdMs;
        }
    }

    /**
     * Result of a rate limit check with details.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int currentCount;
        private final int maxCount;
        private final long retryAfterMs;

        public RateLimitResult(boolean allowed, int currentCount, int maxCount, long retryAfterMs) {
            this.allowed = allowed;
            this.currentCount = currentCount;
            this.maxCount = maxCount;
            this.retryAfterMs = retryAfterMs;
        }

        public boolean isAllowed() { return allowed; }
        public int getCurrentCount() { return currentCount; }
        public int getMaxCount() { return maxCount; }
        public long getRetryAfterMs() { return retryAfterMs; }
    }
}
