package com.skribble.session;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Thread-safe session manager for WebSocket connections.
 * Tracks active sessions and provides utilities for session operations.
 */
@Component
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    // Thread-safe map of session ID to WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // Track session metadata (e.g., player ID, room code)
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    /**
     * Register a new WebSocket session.
     * 
     * @param session The WebSocket session to register
     */
    public void registerSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionMetadata.put(sessionId, new SessionMetadata(sessionId));
        logger.info("Session registered: sessionId={}, totalSessions={}", sessionId, sessions.size());
    }

    /**
     * Remove a WebSocket session.
     * 
     * @param sessionId The session ID to remove
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        sessionMetadata.remove(sessionId);
        logger.info("Session removed: sessionId={}, totalSessions={}", sessionId, sessions.size());
    }

    /**
     * Get a session by its ID.
     * 
     * @param sessionId The session ID
     * @return Optional containing the session if found
     */
    public Optional<WebSocketSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Get session metadata by session ID.
     * 
     * @param sessionId The session ID
     * @return Optional containing the metadata if found
     */
    public Optional<SessionMetadata> getSessionMetadata(String sessionId) {
        return Optional.ofNullable(sessionMetadata.get(sessionId));
    }

    /**
     * Update session metadata with player information.
     * 
     * @param sessionId The session ID
     * @param playerId The player ID
     * @param playerName The player name
     * @param roomCode The room code the player is in
     */
    public void updateSessionMetadata(String sessionId, String playerId, String playerName, String roomCode) {
        SessionMetadata metadata = sessionMetadata.get(sessionId);
        if (metadata != null) {
            metadata.setPlayerId(playerId);
            metadata.setPlayerName(playerName);
            metadata.setRoomCode(roomCode);
            logger.debug("Session metadata updated: sessionId={}, playerId={}, roomCode={}", 
                    sessionId, playerId, roomCode);
        }
    }

    /**
     * Get all active session IDs.
     * 
     * @return Collection of active session IDs
     */
    public Collection<String> getAllSessionIds() {
        return Collections.unmodifiableCollection(sessions.keySet());
    }

    /**
     * Get count of active sessions.
     * 
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Check if a session exists and is open.
     * 
     * @param sessionId The session ID to check
     * @return true if session exists and is open
     */
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }

    /**
     * Get all sessions in a specific room.
     * 
     * @param roomCode The room code
     * @return Collection of sessions in the room
     */
    public Collection<WebSocketSession> getSessionsInRoom(String roomCode) {
        return sessionMetadata.entrySet().stream()
                .filter(entry -> roomCode.equals(entry.getValue().getRoomCode()))
                .map(entry -> sessions.get(entry.getKey()))
                .filter(session -> session != null && session.isOpen())
                .collect(Collectors.toList());
    }

    /**
     * Get all session IDs in a specific room.
     * 
     * @param roomCode The room code
     * @return Collection of session IDs in the room
     */
    public Collection<String> getSessionIdsInRoom(String roomCode) {
        return sessionMetadata.entrySet().stream()
                .filter(entry -> roomCode.equals(entry.getValue().getRoomCode()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Send a message to a specific session.
     * 
     * @param sessionId The target session ID
     * @param message The message to send
     * @return true if message was sent successfully
     */
    public boolean sendToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                logger.debug("Message sent to session: sessionId={}", sessionId);
                return true;
            } catch (IOException e) {
                logger.error("Failed to send message to session: sessionId={}, error={}", 
                        sessionId, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Broadcast a message to all sessions in a room.
     * 
     * @param roomCode The room code
     * @param message The message to broadcast
     * @param excludeSessionId Session ID to exclude (optional, can be null)
     * @return Number of sessions the message was sent to
     */
    public int broadcastToRoom(String roomCode, String message, String excludeSessionId) {
        int sentCount = 0;
        Collection<WebSocketSession> roomSessions = getSessionsInRoom(roomCode);
        
        for (WebSocketSession session : roomSessions) {
            if (excludeSessionId != null && excludeSessionId.equals(session.getId())) {
                continue;
            }
            
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    sentCount++;
                }
            } catch (IOException e) {
                logger.error("Failed to broadcast to session: sessionId={}, error={}", 
                        session.getId(), e.getMessage());
            }
        }
        
        logger.debug("Broadcast to room: roomCode={}, sentCount={}, excludedSession={}", 
                roomCode, sentCount, excludeSessionId);
        return sentCount;
    }

    /**
     * Broadcast a message to all connected sessions.
     * 
     * @param message The message to broadcast
     * @return Number of sessions the message was sent to
     */
    public int broadcastToAll(String message) {
        int sentCount = 0;
        
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    sentCount++;
                }
            } catch (IOException e) {
                logger.error("Failed to broadcast to session: sessionId={}, error={}", 
                        session.getId(), e.getMessage());
            }
        }
        
        logger.debug("Broadcast to all: sentCount={}", sentCount);
        return sentCount;
    }

    /**
     * Clear the room code for a session (when player leaves room).
     * 
     * @param sessionId The session ID
     */
    public void clearRoomForSession(String sessionId) {
        SessionMetadata metadata = sessionMetadata.get(sessionId);
        if (metadata != null) {
            metadata.setRoomCode(null);
            logger.debug("Room cleared for session: sessionId={}", sessionId);
        }
    }

    /**
     * Find session ID by player ID.
     * 
     * @param playerId The player ID to search for
     * @return Optional containing the session ID if found
     */
    public Optional<String> findSessionByPlayerId(String playerId) {
        return sessionMetadata.entrySet().stream()
                .filter(entry -> playerId.equals(entry.getValue().getPlayerId()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Metadata class to track additional session information.
     */
    public static class SessionMetadata {
        private final String sessionId;
        private final long connectedAt;
        private String playerId;
        private String playerName;
        private String roomCode;
        private long lastActivityAt;

        public SessionMetadata(String sessionId) {
            this.sessionId = sessionId;
            this.connectedAt = System.currentTimeMillis();
            this.lastActivityAt = this.connectedAt;
        }

        public String getSessionId() { return sessionId; }
        public long getConnectedAt() { return connectedAt; }
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
        public long getLastActivityAt() { return lastActivityAt; }
        public void updateLastActivity() { this.lastActivityAt = System.currentTimeMillis(); }
    }
}
