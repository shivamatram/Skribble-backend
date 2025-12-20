package com.skribble.reconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages player disconnections and reconnections.
 */
@Component
public class ReconnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ReconnectionManager.class);

    public static final long RECONNECT_WINDOW_MS = 15000; // 15 seconds
    private static final long CLEANUP_INTERVAL_MS = 5000; // 5 seconds

    // Map playerId -> DisconnectedPlayerInfo
    private final Map<String, DisconnectedPlayerInfo> disconnectedPlayers = new ConcurrentHashMap<>();

    // Map playerId -> sessionToken for active players (prevents impersonation)
    private final Map<String, String> activeSessionTokens = new ConcurrentHashMap<>();

    // Map sessionId -> playerId for reverse lookup
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    // Callback for when a player's reconnection window expires
    private Consumer<DisconnectedPlayerInfo> onReconnectExpired;

    public ReconnectionManager() {
        startCleanupTask();
    }

    /**
     * Generate a unique session token for a new player.
     */
    public String generateSessionToken(String playerId, String sessionId) {
        String token = UUID.randomUUID().toString();
        activeSessionTokens.put(playerId, token);
        sessionToPlayer.put(sessionId, playerId);
        logger.debug("Generated session token for player: playerId={}", playerId);
        return token;
    }

    /**
     * Validate that a reconnecting player has the correct token.
     */
    public boolean validateSessionToken(String playerId, String token) {
        DisconnectedPlayerInfo info = disconnectedPlayers.get(playerId);
        if (info == null) {
            return false;
        }
        return info.getSessionToken().equals(token);
    }

    /**
     * Check if a player ID is already in an active session.
     */
    public boolean isPlayerActive(String playerId) {
        return activeSessionTokens.containsKey(playerId) && !disconnectedPlayers.containsKey(playerId);
    }

    /**
     * Record a player disconnect.
     */
    public void recordDisconnect(String playerId, String playerName, String roomId,
                                  int score, boolean wasDrawer, boolean hadGuessedCorrectly) {
        String sessionToken = activeSessionTokens.get(playerId);
        if (sessionToken == null) {
            sessionToken = UUID.randomUUID().toString();
        }

        DisconnectedPlayerInfo info = new DisconnectedPlayerInfo(
                playerId, playerName, roomId, score, wasDrawer, hadGuessedCorrectly, sessionToken
        );

        disconnectedPlayers.put(playerId, info);
        logger.info("Player disconnected - reconnection window started: playerId={}, roomId={}, wasDrawer={}, windowMs={}",
                playerId, roomId, wasDrawer, RECONNECT_WINDOW_MS);
    }

    /**
     * Attempt to reconnect a player.
     * Returns the disconnected player info if reconnection is valid.
     */
    public Optional<DisconnectedPlayerInfo> attemptReconnect(String playerId, String sessionToken) {
        DisconnectedPlayerInfo info = disconnectedPlayers.get(playerId);

        if (info == null) {
            logger.warn("Reconnection failed - no disconnect record: playerId={}", playerId);
            return Optional.empty();
        }

        if (info.isExpired(RECONNECT_WINDOW_MS)) {
            logger.warn("Reconnection failed - window expired: playerId={}, expiredMs={}",
                    playerId, System.currentTimeMillis() - info.getDisconnectTime() - RECONNECT_WINDOW_MS);
            disconnectedPlayers.remove(playerId);
            return Optional.empty();
        }

        // Validate session token to prevent impersonation
        if (sessionToken != null && !info.getSessionToken().equals(sessionToken)) {
            logger.warn("Reconnection failed - invalid session token: playerId={}", playerId);
            return Optional.empty();
        }

        // Successful reconnection
        disconnectedPlayers.remove(playerId);
        logger.info("Player reconnected successfully: playerId={}, roomId={}, remainingWindowMs={}",
                playerId, info.getRoomId(), info.getRemainingTimeMs(RECONNECT_WINDOW_MS));

        return Optional.of(info);
    }

    /**
     * Check if a player is in reconnection window.
     */
    public boolean isInReconnectionWindow(String playerId) {
        DisconnectedPlayerInfo info = disconnectedPlayers.get(playerId);
        return info != null && !info.isExpired(RECONNECT_WINDOW_MS);
    }

    /**
     * Get disconnected player info.
     */
    public Optional<DisconnectedPlayerInfo> getDisconnectedPlayer(String playerId) {
        return Optional.ofNullable(disconnectedPlayers.get(playerId));
    }

    /**
     * Remove a player from active sessions.
     */
    public void removeActiveSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            logger.debug("Removed active session mapping: sessionId={}, playerId={}", sessionId, playerId);
        }
    }

    /**
     * Permanently remove a player (after reconnect window expires).
     */
    public void permanentlyRemovePlayer(String playerId) {
        disconnectedPlayers.remove(playerId);
        activeSessionTokens.remove(playerId);
        logger.info("Player permanently removed: playerId={}", playerId);
    }

    /**
     * Update session mapping for a reconnected player.
     */
    public void updateSessionMapping(String newSessionId, String playerId) {
        // Remove old session mappings for this player
        sessionToPlayer.entrySet().removeIf(entry -> entry.getValue().equals(playerId));
        sessionToPlayer.put(newSessionId, playerId);
        logger.debug("Updated session mapping: newSessionId={}, playerId={}", newSessionId, playerId);
    }

    /**
     * Set callback for when reconnection window expires.
     */
    public void setOnReconnectExpired(Consumer<DisconnectedPlayerInfo> callback) {
        this.onReconnectExpired = callback;
    }

    /**
     * Start the cleanup task to remove expired disconnections.
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredDisconnections();
            } catch (Exception e) {
                logger.error("Error in cleanup task: {}", e.getMessage(), e);
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Clean up expired disconnections.
     */
    private void cleanupExpiredDisconnections() {
        disconnectedPlayers.entrySet().removeIf(entry -> {
            DisconnectedPlayerInfo info = entry.getValue();
            if (info.isExpired(RECONNECT_WINDOW_MS)) {
                logger.info("Reconnection window expired - removing player: playerId={}, roomId={}",
                        info.getPlayerId(), info.getRoomId());

                // Notify callback
                if (onReconnectExpired != null) {
                    try {
                        onReconnectExpired.accept(info);
                    } catch (Exception e) {
                        logger.error("Error in reconnect expired callback: {}", e.getMessage(), e);
                    }
                }

                // Clean up session token
                activeSessionTokens.remove(info.getPlayerId());
                return true;
            }
            return false;
        });
    }

    /**
     * Shutdown the manager.
     */
    public void shutdown() {
        cleanupScheduler.shutdown();
    }
}
