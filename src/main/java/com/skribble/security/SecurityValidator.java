package com.skribble.security;

import com.skribble.room.RoomState;
import com.skribble.room.RoomStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates game actions for security and anti-cheat purposes.
 * Provides centralized validation logic for all game actions.
 */
@Component
public class SecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(SecurityValidator.class);

    private final RateLimiter rateLimiter;
    private final AbuseTracker abuseTracker;

    public SecurityValidator(RateLimiter rateLimiter, AbuseTracker abuseTracker) {
        this.rateLimiter = rateLimiter;
        this.abuseTracker = abuseTracker;
    }

    /**
     * Validate a draw stroke action.
     */
    public ValidationResult validateDrawStroke(String playerId, RoomState room) {
        // Check if player is the current drawer
        if (!room.isCurrentDrawer(playerId)) {
            logger.warn("Unauthorized draw attempt: playerId={}, currentDrawer={}",
                    playerId, room.getCurrentDrawerId());
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.UNAUTHORIZED_DRAW);
            return ValidationResult.error("UNAUTHORIZED_DRAW", "You are not the current drawer");
        }

        // Check if game is in progress
        if (!room.isGameInProgress()) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.ACTION_AFTER_GAME_END);
            return ValidationResult.error("GAME_NOT_ACTIVE", "Game is not in progress");
        }

        // Check if game has ended
        if (room.getStatus() == RoomStatus.ENDED) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.ACTION_AFTER_GAME_END);
            return ValidationResult.error("GAME_ENDED", "Game has ended");
        }

        // Check rate limit
        if (!rateLimiter.allowDrawStroke(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.RATE_LIMIT_EXCEEDED);
            return ValidationResult.error("RATE_LIMITED", "Drawing too fast, please slow down");
        }

        return ValidationResult.success();
    }

    /**
     * Validate a guess submission.
     */
    public ValidationResult validateGuess(String playerId, RoomState room) {
        // Check if player is muted
        if (abuseTracker.isMuted(playerId)) {
            long remaining = abuseTracker.getMuteRemainingMs(playerId);
            return ValidationResult.error("MUTED",
                    String.format("You are muted for %d more seconds", remaining / 1000));
        }

        // Check if player is the drawer (drawer can't guess)
        if (room.isCurrentDrawer(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.INVALID_ACTION);
            return ValidationResult.error("DRAWER_CANNOT_GUESS", "The drawer cannot submit guesses");
        }

        // Check if player already guessed correctly
        if (room.hasPlayerGuessedCorrectly(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.GUESS_AFTER_CORRECT);
            return ValidationResult.error("ALREADY_GUESSED", "You have already guessed correctly");
        }

        // Check if game is in progress
        if (!room.isGameInProgress()) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.ACTION_AFTER_GAME_END);
            return ValidationResult.error("GAME_NOT_ACTIVE", "Game is not in progress");
        }

        // Check if game has ended
        if (room.getStatus() == RoomStatus.ENDED) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.ACTION_AFTER_GAME_END);
            return ValidationResult.error("GAME_ENDED", "Game has ended");
        }

        // Check if round time expired
        if (room.isRoundTimeExpired()) {
            return ValidationResult.error("ROUND_EXPIRED", "Round time has expired");
        }

        // Check rate limit
        if (!rateLimiter.allowGuess(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.SPAM_GUESS);
            return ValidationResult.error("RATE_LIMITED", "Too many guesses, please slow down");
        }

        return ValidationResult.success();
    }

    /**
     * Validate a chat message.
     */
    public ValidationResult validateChat(String playerId, RoomState room) {
        // Check if player is muted
        if (abuseTracker.isMuted(playerId)) {
            long remaining = abuseTracker.getMuteRemainingMs(playerId);
            return ValidationResult.error("MUTED",
                    String.format("You are muted for %d more seconds", remaining / 1000));
        }

        // Check if game has ended
        if (room.getStatus() == RoomStatus.ENDED) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.ACTION_AFTER_GAME_END);
            return ValidationResult.error("GAME_ENDED", "Game has ended");
        }

        // Check rate limit
        if (!rateLimiter.allowChat(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.SPAM_CHAT);
            return ValidationResult.error("RATE_LIMITED", "Too many messages, please slow down");
        }

        return ValidationResult.success();
    }

    /**
     * Validate a connection attempt.
     */
    public ValidationResult validateConnection(String ipAddress) {
        // Check if IP is blocked
        if (abuseTracker.isIpBlocked(ipAddress)) {
            logger.warn("Blocked IP attempted connection: ip={}", ipAddress);
            return ValidationResult.error("IP_BLOCKED", "Connection blocked due to abuse");
        }

        // Check rate limit
        if (!rateLimiter.allowConnection(ipAddress)) {
            abuseTracker.recordIpViolation(ipAddress);
            logger.warn("Connection rate limited: ip={}", ipAddress);
            return ValidationResult.error("RATE_LIMITED", "Too many connection attempts");
        }

        return ValidationResult.success();
    }

    /**
     * Check if a player should be disconnected due to abuse.
     */
    public boolean shouldDisconnectPlayer(String playerId) {
        return abuseTracker.shouldDisconnect(playerId);
    }

    /**
     * Get the abuse action for a player.
     */
    public AbuseTracker.AbuseAction checkAbuseLevel(String playerId) {
        int violations = abuseTracker.getViolationCount(playerId);
        if (violations >= AbuseTracker.DISCONNECT_THRESHOLD) {
            return AbuseTracker.AbuseAction.DISCONNECT;
        } else if (violations >= AbuseTracker.MUTE_THRESHOLD) {
            return AbuseTracker.AbuseAction.MUTE;
        } else if (violations >= AbuseTracker.WARN_THRESHOLD) {
            return AbuseTracker.AbuseAction.WARN;
        }
        return AbuseTracker.AbuseAction.NONE;
    }

    /**
     * Record a violation and get the resulting action.
     */
    public AbuseTracker.AbuseAction recordViolation(String playerId, AbuseTracker.ViolationType type) {
        return abuseTracker.recordViolation(playerId, type);
    }

    /**
     * Clear player data on disconnect or game end.
     */
    public void clearPlayer(String playerId) {
        rateLimiter.removeIdentifier(playerId);
        abuseTracker.clearPlayer(playerId);
    }

    /**
     * Result of a validation check.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorCode;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorCode, String errorMessage) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult error(String code, String message) {
            return new ValidationResult(false, code, message);
        }

        public boolean isValid() { return valid; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }
}
