package com.skribble.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import com.skribble.dto.CorrectGuessBroadcast;
import com.skribble.dto.DrawStrokeMessage;
import com.skribble.dto.FinalResultsBroadcast;
import com.skribble.dto.GameEndedBroadcast;
import com.skribble.dto.GameStartedBroadcast;
import com.skribble.dto.GameStartingBroadcast;
import com.skribble.dto.GuessFeedback;
import com.skribble.dto.LeaderboardUpdateBroadcast;
import com.skribble.dto.LeaderboardUpdateBroadcast.LeaderboardEntry;
import com.skribble.dto.PlayerDisconnectedBroadcast;
import com.skribble.dto.PlayerJoinedBroadcast;
import com.skribble.dto.PlayerReconnectedBroadcast;
import com.skribble.dto.PlayerRemovedBroadcast;
import com.skribble.dto.RoomAssignedEvent;
import com.skribble.dto.RoomStatusUpdateBroadcast;
import com.skribble.dto.RoundEndedBroadcast;
import com.skribble.dto.ScoreUpdateBroadcast;
import com.skribble.dto.SendWordOptionsEvent;
import com.skribble.dto.StateSyncEvent;
import com.skribble.dto.StrokeBroadcast;
import com.skribble.dto.SubmitGuessMessage;
import com.skribble.dto.WordConfirmedBroadcast;
import com.skribble.dto.WordSelectedMessage;
import com.skribble.dto.WordSelectionStartedBroadcast;
import com.skribble.performance.ConnectionLimiter;
import com.skribble.performance.PerformanceMonitor;
import com.skribble.performance.StrokeBatcher;
import com.skribble.reconnect.DisconnectedPlayerInfo;
import com.skribble.reconnect.ReconnectionManager;
import com.skribble.room.RoomManager;
import com.skribble.room.RoomState;
import com.skribble.room.RoomStatus;
import com.skribble.score.ScoreCalculator;
import com.skribble.security.AbuseTracker;
import com.skribble.security.RateLimiter;
import com.skribble.security.SecurityValidator;
import com.skribble.session.SessionManager;
import com.skribble.word.WordSelectionManager;
import com.skribble.word.WordSelectionResult;
import com.skribble.word.WordSelectionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket handler for Skribble game messages.
 * Handles connection lifecycle and message routing.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);
    
    // Game end timing constants
    private static final long RESULTS_DELAY_MS = 3000; // Delay before sending final results
    private static final long ROOM_CLOSE_DELAY_MS = 30000; // Grace period before room cleanup
    
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final RoomManager roomManager;
    private final ScoreCalculator scoreCalculator;
    private final ReconnectionManager reconnectionManager;
    private final SecurityValidator securityValidator;
    private final RateLimiter rateLimiter;
    private final AbuseTracker abuseTracker;
    private final PerformanceMonitor performanceMonitor;
    private final ConnectionLimiter connectionLimiter;
    private final StrokeBatcher strokeBatcher;
    private final WordSelectionManager wordSelectionManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Track player info associated with each session
    private final Map<String, PlayerSessionInfo> playerSessions = new ConcurrentHashMap<>();
    
    // Track pending game starts (to avoid duplicate starts)
    private final Map<String, Boolean> pendingGameStarts = new ConcurrentHashMap<>();
    
    // Track pending game ends (to avoid duplicate end processing)
    private final Map<String, Boolean> pendingGameEnds = new ConcurrentHashMap<>();
    
    // Track total correct guesses per player across all rounds
    private final Map<String, Map<String, Integer>> playerCorrectGuesses = new ConcurrentHashMap<>();

    public GameWebSocketHandler(ObjectMapper objectMapper, SessionManager sessionManager, 
                                 RoomManager roomManager, ScoreCalculator scoreCalculator,
                                 ReconnectionManager reconnectionManager,
                                 SecurityValidator securityValidator,
                                 RateLimiter rateLimiter, AbuseTracker abuseTracker,
                                 PerformanceMonitor performanceMonitor,
                                 ConnectionLimiter connectionLimiter,
                                 StrokeBatcher strokeBatcher,
                                 WordSelectionManager wordSelectionManager) {
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.roomManager = roomManager;
        this.scoreCalculator = scoreCalculator;
        this.reconnectionManager = reconnectionManager;
        this.securityValidator = securityValidator;
        this.rateLimiter = rateLimiter;
        this.abuseTracker = abuseTracker;
        this.performanceMonitor = performanceMonitor;
        this.connectionLimiter = connectionLimiter;
        this.strokeBatcher = strokeBatcher;
        this.wordSelectionManager = wordSelectionManager;
    }

    @PostConstruct
    public void init() {
        // Set up callback for when reconnection window expires
        reconnectionManager.setOnReconnectExpired(this::handleReconnectionExpired);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        String ipAddress = getClientIpAddress(session);
        
        // Check connection limit first (fast path rejection)
        ConnectionLimiter.ConnectionResult connResult = connectionLimiter.tryAcquireConnection();
        if (connResult != ConnectionLimiter.ConnectionResult.ALLOWED) {
            logger.warn("Connection rejected - server limit: ip={}, reason={}", ipAddress, connResult);
            sendError(session, "SERVER_FULL", "Server is at capacity, please try again later");
            closeSession(session, CloseStatus.SERVICE_OVERLOAD);
            return;
        }
        
        // Validate connection rate limit
        SecurityValidator.ValidationResult validation = securityValidator.validateConnection(ipAddress);
        if (!validation.isValid()) {
            connectionLimiter.releaseConnection(); // Release the slot we acquired
            logger.warn("Connection rejected - rate limited: ip={}, sessionId={}", ipAddress, sessionId);
            sendError(session, validation.getErrorCode(), validation.getErrorMessage());
            closeSession(session, CloseStatus.POLICY_VIOLATION);
            return;
        }
        
        sessionManager.registerSession(session);
        performanceMonitor.connectionOpened();
        
        // Send welcome message
        sendMessage(session, createWelcomeMessage(sessionId));
        logger.debug("WebSocket connection established: sessionId={}, ip={}", sessionId, ipAddress);
    }
    
    /**
     * Extract client IP address from session.
     */
    private String getClientIpAddress(WebSocketSession session) {
        if (session.getRemoteAddress() != null) {
            return session.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
    
    /**
     * Close a WebSocket session safely.
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            logger.error("Error closing session: sessionId={}, error={}", session.getId(), e.getMessage());
        }
    }

    /**
     * Check if player should be disconnected due to repeated abuse.
     * @return true if player was disconnected, false otherwise
     */
    private boolean handleAbuseCheck(WebSocketSession session, String playerId) {
        if (securityValidator.shouldDisconnectPlayer(playerId)) {
            logger.warn("Disconnecting player for repeated violations: playerId={}", playerId);
            sendError(session, "ABUSE_DISCONNECT", "Disconnected due to repeated policy violations");
            closeSession(session, CloseStatus.POLICY_VIOLATION);
            return true;
        }
        return false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        logger.debug("WebSocket connection closed: sessionId={}, status={}", sessionId, status);
        
        // Release connection slot
        connectionLimiter.releaseConnection();
        performanceMonitor.connectionClosed();
        
        // Clean up player session info and handle reconnection
        PlayerSessionInfo playerInfo = playerSessions.remove(sessionId);
        if (playerInfo != null) {
            String playerId = playerInfo.getPlayerId();
            // Clean up security tracking data
            securityValidator.clearPlayer(playerId);
            performanceMonitor.playerLeft();
            handlePlayerDisconnect(playerInfo, sessionId);
        }
        
        sessionManager.removeSession(sessionId);
        reconnectionManager.removeActiveSession(sessionId);
    }

    /**
     * Handle player disconnect - record for potential reconnection.
     */
    private void handlePlayerDisconnect(PlayerSessionInfo playerInfo, String sessionId) {
        String playerId = playerInfo.getPlayerId();
        String roomId = playerInfo.getRoomCode();
        String playerName = playerInfo.getPlayerName();
        
        logger.info("Player disconnected: playerId={}, roomId={}", playerId, roomId);
        
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            logger.warn("Room not found for disconnected player: roomId={}", roomId);
            return;
        }
        
        RoomState room = roomOpt.get();
        boolean wasDrawer = room.isCurrentDrawer(playerId);
        boolean hadGuessedCorrectly = room.hasPlayerGuessedCorrectly(playerId);
        int score = room.getPlayerScore(playerId);
        
        // Record disconnect for reconnection window
        reconnectionManager.recordDisconnect(playerId, playerName, roomId, score, wasDrawer, hadGuessedCorrectly);
        
        // Broadcast disconnect to room
        PlayerDisconnectedBroadcast disconnectBroadcast = PlayerDisconnectedBroadcast.create(
                roomId, playerId, playerName, wasDrawer,
                ReconnectionManager.RECONNECT_WINDOW_MS, room.getPlayerCount()
        );
        broadcastToRoom(roomId, toJson(disconnectBroadcast));
        
        // If drawer disconnected during game, schedule round end check
        if (wasDrawer && room.isGameInProgress()) {
            scheduleDrawerDisconnectCheck(roomId, playerId);
        }
    }

    /**
     * Schedule check for drawer reconnection - end round if drawer doesn't reconnect.
     */
    private void scheduleDrawerDisconnectCheck(String roomId, String drawerId) {
        scheduler.schedule(() -> {
            // Check if drawer reconnected
            if (reconnectionManager.isInReconnectionWindow(drawerId)) {
                // Still waiting for reconnection
                return;
            }
            
            Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
            if (roomOpt.isEmpty()) {
                return;
            }
            
            RoomState room = roomOpt.get();
            
            // If drawer didn't reconnect and is still the current drawer, end round
            if (room.isCurrentDrawer(drawerId) && room.isGameInProgress()) {
                logger.info("Drawer didn't reconnect in time, ending round: roomId={}, drawerId={}", roomId, drawerId);
                endRoundDueToDrawerDisconnect(room);
            }
        }, ReconnectionManager.RECONNECT_WINDOW_MS + 500, TimeUnit.MILLISECONDS);
    }

    /**
     * End round early due to drawer disconnect.
     */
    private void endRoundDueToDrawerDisconnect(RoomState room) {
        String roomId = room.getRoomId();
        String word = room.getCurrentWord();
        int roundNumber = room.getCurrentRound();
        
        // Create round ended broadcast
        RoundEndedBroadcast roundEnded = RoundEndedBroadcast.create(roomId, "drawer_disconnected", word, roundNumber);
        roundEnded.setLeaderboard(buildLeaderboard(room));
        roundEnded.setNextRoundInMs(5000);
        
        broadcastToRoom(roomId, toJson(roundEnded));
        
        // Reset round state
        room.resetRound();
        room.setStatus(RoomStatus.ROUND_OVER);
        
        logger.info("Round ended due to drawer disconnect: roomId={}, round={}", roomId, roundNumber);
    }

    /**
     * Handle reconnection window expiration.
     */
    private void handleReconnectionExpired(DisconnectedPlayerInfo info) {
        String playerId = info.getPlayerId();
        String roomId = info.getRoomId();
        String playerName = info.getPlayerName();
        
        logger.info("Reconnection window expired, removing player: playerId={}, roomId={}", playerId, roomId);
        
        // Remove from room
        roomManager.removePlayerFromRoom(playerId);
        
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            return;
        }
        
        RoomState room = roomOpt.get();
        
        // Broadcast player removal
        PlayerRemovedBroadcast removedBroadcast = PlayerRemovedBroadcast.create(
                roomId, playerId, playerName, "reconnect_timeout", room.getPlayerCount()
        );
        broadcastToRoom(roomId, toJson(removedBroadcast));
        
        // Clean up
        reconnectionManager.permanentlyRemovePlayer(playerId);
        
        // If drawer was removed and round is in progress, end round
        if (info.wasDrawer() && room.isGameInProgress() && room.isCurrentDrawer(playerId)) {
            endRoundDueToDrawerDisconnect(room);
        }
        
        // Check if game should end due to insufficient players
        if (room.isGameInProgress() && room.getPlayerCount() < room.getMinPlayers()) {
            logger.info("Insufficient players after removal, ending game: roomId={}, playerCount={}", 
                    roomId, room.getPlayerCount());
            endGame(room, "insufficient_players");
            return;
        }
        
        // Broadcast updated room status
        broadcastRoomStatusUpdate(room);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error: sessionId={}, error={}", 
                session.getId(), exception.getMessage(), exception);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        long startNanos = System.nanoTime();
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        performanceMonitor.messageReceived();
        
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            if (!jsonNode.has("type")) {
                sendError(session, "INVALID_MESSAGE", "Message must contain 'type' field");
                return;
            }
            
            String messageType = jsonNode.get("type").asText();
            
            // Route message based on type
            routeMessage(session, messageType, jsonNode);
            
        } catch (JsonProcessingException e) {
            logger.warn("Malformed JSON received: sessionId={}, error={}", sessionId, e.getMessage());
            sendError(session, "MALFORMED_JSON", "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing message: sessionId={}, error={}", sessionId, e.getMessage(), e);
            sendError(session, "INTERNAL_ERROR", "An unexpected error occurred");
        } finally {
            performanceMonitor.recordProcessingTime(System.nanoTime() - startNanos);
        }
    }

    /**
     * Route incoming messages to appropriate handlers based on message type.
     */
    private void routeMessage(WebSocketSession session, String messageType, JsonNode payload) {
        switch (messageType) {
            case "QUICK_PLAY":
                handleQuickPlay(session, payload);
                break;
            case "RECONNECT":
                handleReconnect(session, payload);
                break;
            case "JOIN_ROOM":
                handleJoinRoom(session, payload);
                break;
            case "CREATE_ROOM":
                handleCreateRoom(session, payload);
                break;
            case "LEAVE_ROOM":
                handleLeaveRoom(session, payload);
                break;
            case "START_GAME":
                handleStartGame(session, payload);
                break;
            case "DRAW":
                handleDraw(session, payload);
                break;
            case "DRAW_STROKE":
                handleDrawStroke(session, payload);
                break;
            case "GUESS":
                handleGuess(session, payload);
                break;
            case "SUBMIT_GUESS":
                handleSubmitGuess(session, payload);
                break;
            case "WORD_SELECTED":
                handleWordSelected(session, payload);
                break;
            case "CHAT":
                handleChat(session, payload);
                break;
            case "PING":
                handlePing(session);
                break;
            default:
                logger.warn("Unknown message type: sessionId={}, type={}", session.getId(), messageType);
                sendError(session, "UNKNOWN_TYPE", "Unknown message type: " + messageType);
        }
    }

    // ==================== Message Handlers ====================

    /**
     * Handle RECONNECT - Player attempting to reconnect after disconnect.
     */
    private void handleReconnect(WebSocketSession session, JsonNode payload) {
        String playerId = getStringField(payload, "playerId");
        String sessionToken = getStringField(payload, "sessionToken");
        
        if (playerId == null) {
            sendError(session, "INVALID_PAYLOAD", "RECONNECT requires 'playerId'");
            return;
        }
        
        logger.info("Reconnection attempt: playerId={}, newSessionId={}", playerId, session.getId());
        
        // Check for duplicate active sessions (security)
        if (reconnectionManager.isPlayerActive(playerId)) {
            logger.warn("Reconnection rejected - player already has active session: playerId={}", playerId);
            sendError(session, "DUPLICATE_SESSION", "Player already has an active session");
            return;
        }
        
        // Attempt reconnection
        Optional<DisconnectedPlayerInfo> infoOpt = reconnectionManager.attemptReconnect(playerId, sessionToken);
        
        if (infoOpt.isEmpty()) {
            logger.warn("Reconnection failed: playerId={}", playerId);
            sendError(session, "RECONNECT_FAILED", "Reconnection failed - window expired or invalid token");
            return;
        }
        
        DisconnectedPlayerInfo info = infoOpt.get();
        String roomId = info.getRoomId();
        
        // Verify room still exists
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            logger.warn("Reconnection failed - room no longer exists: roomId={}", roomId);
            sendError(session, "ROOM_NOT_FOUND", "Room no longer exists");
            return;
        }
        
        RoomState room = roomOpt.get();
        
        // Restore player to room
        restorePlayerToRoom(session, info, room);
        
        // Update session mappings
        reconnectionManager.updateSessionMapping(session.getId(), playerId);
        sessionManager.registerSession(session);
        
        // Store player session info
        PlayerSessionInfo sessionInfo = new PlayerSessionInfo(playerId, info.getPlayerName(), roomId);
        playerSessions.put(session.getId(), sessionInfo);
        
        // Broadcast reconnection to room
        PlayerReconnectedBroadcast reconnectBroadcast = PlayerReconnectedBroadcast.create(
                roomId, playerId, info.getPlayerName(), info.wasDrawer(), room.getPlayerCount()
        );
        broadcastToRoom(roomId, toJson(reconnectBroadcast));
        
        // Send full state sync to reconnected player
        sendStateSync(session, info, room);
        
        logger.info("Player reconnected successfully: playerId={}, roomId={}, wasDrawer={}",
                playerId, roomId, info.wasDrawer());
    }

    /**
     * Restore player state to room after reconnection.
     */
    private void restorePlayerToRoom(WebSocketSession session, DisconnectedPlayerInfo info, RoomState room) {
        String playerId = info.getPlayerId();
        
        // Re-add player to room if needed
        if (!room.hasPlayer(playerId)) {
            room.addPlayer(playerId, info.getPlayerName());
        }
        
        // Restore score
        room.getPlayerScores().setScore(playerId, info.getScore());
        
        // Restore guess status
        if (info.hadGuessedCorrectly()) {
            room.markPlayerGuessedCorrectly(playerId);
        }
    }

    /**
     * Send full state sync to a reconnected player.
     */
    private void sendStateSync(WebSocketSession session, DisconnectedPlayerInfo info, RoomState room) {
        String playerId = info.getPlayerId();
        String roomId = room.getRoomId();
        
        StateSyncEvent stateSync = StateSyncEvent.create(roomId, playerId, info.getPlayerName());
        
        // Room status
        stateSync.setStatus(room.getStatus().name());
        stateSync.setPlayerCount(room.getPlayerCount());
        stateSync.setMaxPlayers(room.getMaxPlayers());
        
        // Player state
        stateSync.setScore(info.getScore());
        stateSync.setHasGuessedCorrectly(info.hadGuessedCorrectly());
        
        // Determine role
        boolean isDrawer = info.wasDrawer() && room.isCurrentDrawer(playerId);
        stateSync.setRole(isDrawer ? "drawer" : "guesser");
        
        // Current drawer info
        String currentDrawerId = room.getCurrentDrawerId();
        stateSync.setCurrentDrawerId(currentDrawerId);
        if (currentDrawerId != null) {
            stateSync.setCurrentDrawerName(room.getPlayerName(currentDrawerId));
        }
        
        // Word hint (for guessers only)
        if (!isDrawer && room.getCurrentWord() != null) {
            stateSync.setWordHint(createWordHint(room.getCurrentWord()));
        }
        
        // Timer
        stateSync.setTimerRemainingMs(room.getRemainingTimeMs());
        
        // Round info
        stateSync.setRoundNumber(room.getCurrentRound());
        stateSync.setTotalRounds(room.getTotalRounds());
        
        // Leaderboard
        stateSync.setLeaderboard(buildLeaderboard(room));
        
        sendMessage(session, toJson(stateSync));
    }

    /**
     * Create a masked word hint (e.g., "apple" -> "_ _ _ _ _").
     */
    private String createWordHint(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        StringBuilder hint = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == ' ') {
                hint.append("  ");
            } else {
                hint.append("_ ");
            }
        }
        return hint.toString().trim();
    }

    /**
     * Build leaderboard for state sync.
     */
    private List<StateSyncEvent.LeaderboardEntry> buildLeaderboard(RoomState room) {
        List<LeaderboardEntry> entries = room.getLeaderboard();
        List<StateSyncEvent.LeaderboardEntry> result = new ArrayList<>();
        
        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            StateSyncEvent.LeaderboardEntry syncEntry = new StateSyncEvent.LeaderboardEntry(
                    entry.getPlayerId(),
                    entry.getPlayerName(),
                    entry.getScore(),
                    rank++,
                    entry.isHasGuessedThisRound()
            );
            result.add(syncEntry);
        }
        
        return result;
    }

    /**
     * Handle QUICK_PLAY - Auto-join or create room for matchmaking.
     */
    private void handleQuickPlay(WebSocketSession session, JsonNode payload) {
        String playerName = getStringField(payload, "playerName");
        
        if (playerName == null || playerName.trim().isEmpty()) {
            sendError(session, "INVALID_PAYLOAD", "QUICK_PLAY requires 'playerName'");
            return;
        }
        
        String playerId = session.getId();
        
        // Check if a new room will be created - enforce room limit
        boolean willCreateNew = roomManager.getJoinableRooms().isEmpty();
        if (willCreateNew) {
            ConnectionLimiter.RoomCreationResult roomResult = connectionLimiter.tryAcquireRoomSlot();
            if (roomResult != ConnectionLimiter.RoomCreationResult.ALLOWED) {
                logger.warn("Room creation rejected - server limit: playerId={}", playerId);
                sendError(session, "SERVER_FULL", "No rooms available, please try again later");
                return;
            }
        }
        
        // Find or create a room
        RoomManager.MatchmakingResult result = roomManager.findOrCreateRoom(playerId, playerName);
        RoomState room = result.getRoom();
        String roomId = room.getRoomId();
        
        // Track metrics
        performanceMonitor.playerJoined();
        if (result.isNewRoom()) {
            performanceMonitor.roomCreated(roomId);
            // Register room with stroke batcher
            strokeBatcher.registerRoom(roomId, msg -> broadcastToRoom(roomId, msg));
        } else if (willCreateNew) {
            // We acquired a slot but joined existing room - release it
            connectionLimiter.releaseRoomSlot();
        }
        
        // Store player session info
        PlayerSessionInfo sessionInfo = new PlayerSessionInfo(playerId, playerName, roomId);
        playerSessions.put(session.getId(), sessionInfo);
        
        // 1. Send ROOM_ASSIGNED to the joining player
        RoomAssignedEvent roomAssigned = RoomAssignedEvent.create(
                roomId,
                playerId,
                playerName,
                result.isNewRoom()
        );
        sendMessage(session, toJson(roomAssigned));
        
        // 2. Broadcast PLAYER_JOINED to all players in the room
        PlayerJoinedBroadcast playerJoined = PlayerJoinedBroadcast.create(
                roomId,
                playerId,
                playerName,
                room.getPlayerCount(),
                room.getMaxPlayers()
        );
        broadcastToRoom(roomId, toJson(playerJoined));
        
        // 3. Broadcast ROOM_STATUS_UPDATE with current player list
        logger.info("QUICK_PLAY: About to broadcast ROOM_STATUS_UPDATE for room {}", roomId);
        try {
            broadcastRoomStatusUpdate(room);
            logger.info("QUICK_PLAY: Successfully broadcast ROOM_STATUS_UPDATE for room {}", roomId);
        } catch (Exception e) {
            logger.error("QUICK_PLAY: Failed to broadcast ROOM_STATUS_UPDATE", e);
        }
        
        // 4. Check if we should start the game
        checkAndScheduleGameStart(room);
    }

    /**
     * Broadcast current room status to all players in the room.
     */
    private void broadcastRoomStatusUpdate(RoomState room) {
        logger.debug("Broadcasting ROOM_STATUS_UPDATE for room {} with {} players", 
                room.getRoomId(), room.getPlayerCount());
        List<RoomStatusUpdateBroadcast.PlayerInfo> players = new ArrayList<>();
        String currentDrawerId = room.getCurrentDrawerId();
        
        for (String pid : room.getPlayerIds()) {
            String name = room.getPlayerName(pid);
            int score = room.getPlayerScore(pid);
            boolean isDrawer = pid.equals(currentDrawerId);
            players.add(new RoomStatusUpdateBroadcast.PlayerInfo(pid, name != null ? name : pid, score, isDrawer));
        }
        
        RoomStatusUpdateBroadcast statusUpdate = RoomStatusUpdateBroadcast.create(
                room.getRoomId(),
                room.getStatus(),
                room.getPlayerCount(),
                room.getMaxPlayers(),
                room.getMinPlayers(),
                players
        );
        statusUpdate.setCurrentDrawerId(currentDrawerId);
        statusUpdate.setRoundNumber(room.getCurrentRound());
        statusUpdate.setTotalRounds(room.getTotalRounds());
        
        broadcastToRoom(room.getRoomId(), toJson(statusUpdate));
    }

    /**
     * Check if room has min players and schedule game start.
     */
    private void checkAndScheduleGameStart(RoomState room) {
        String roomId = room.getRoomId();
        
        // Only start if room is WAITING and has min players
        if (room.getStatus() != RoomStatus.WAITING) {
            return;
        }
        
        if (!room.hasMinPlayers()) {
            logger.debug("Room {} waiting for more players: {}/{}", 
                    roomId, room.getPlayerCount(), room.getMinPlayers());
            return;
        }
        
        // Avoid duplicate start scheduling
        if (pendingGameStarts.putIfAbsent(roomId, true) != null) {
            logger.debug("Game start already scheduled for room: {}", roomId);
            return;
        }
        
        // Update room status to STARTING
        room.setStatus(RoomStatus.STARTING);
        
        long startDelayMs = RoomState.GAME_START_DELAY_MS;
        
        // Broadcast GAME_STARTING countdown
        GameStartingBroadcast gameStarting = GameStartingBroadcast.create(
                roomId,
                startDelayMs,
                room.getPlayerCount()
        );
        broadcastToRoom(roomId, toJson(gameStarting));
        logger.info("Game starting in {}ms: roomId={}, playerCount={}", 
                startDelayMs, roomId, room.getPlayerCount());
        
        // Schedule actual game start
        scheduler.schedule(() -> {
            try {
                startGame(room);
            } finally {
                pendingGameStarts.remove(roomId);
            }
        }, startDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Start the game for a room.
     */
    private void startGame(RoomState room) {
        String roomId = room.getRoomId();
        
        // Double-check room still has enough players
        if (!room.hasMinPlayers()) {
            logger.warn("Game start cancelled - not enough players: roomId={}, count={}", 
                    roomId, room.getPlayerCount());
            room.setStatus(RoomStatus.WAITING);
            broadcastRoomStatusUpdate(room);
            return;
        }
        
        room.setGameInProgress(true);
        room.setCurrentRound(1);
        room.setGameStartTime(System.currentTimeMillis());
        
        // Initialize correct guesses tracking for this room
        playerCorrectGuesses.put(roomId, new ConcurrentHashMap<>());
        
        // Select first drawer and start word selection
        selectNextDrawerAndStartWordSelection(room);
        
        logger.info("Game started: roomId={}, playerCount={}", roomId, room.getPlayerCount());
    }

    /**
     * Select the next drawer and start word selection phase.
     */
    private void selectNextDrawerAndStartWordSelection(RoomState room) {
        String roomId = room.getRoomId();
        
        // Get next drawer from player list (round-robin)
        List<String> players = new ArrayList<>(room.getPlayerIds());
        if (players.isEmpty()) {
            logger.warn("No players available to draw: roomId={}", roomId);
            return;
        }
        
        // Simple round-robin: use (round - 1) % playerCount
        int drawerIndex = (room.getCurrentRound() - 1) % players.size();
        String drawerId = players.get(drawerIndex);
        String drawerName = room.getPlayerName(drawerId);
        
        room.setCurrentDrawerId(drawerId);
        room.setStatus(RoomStatus.WORD_SELECTION);
        
        logger.info("Drawer selected: roomId={}, round={}, drawerId={}, drawerName={}", 
                roomId, room.getCurrentRound(), drawerId, drawerName);
        
        // Start word selection phase
        WordSelectionSession session = wordSelectionManager.startWordSelection(
                roomId, 
                drawerId,
                this::handleWordSelectionTimeout
        );
        
        // Send WORD_SELECTION_STARTED to all guessers
        WordSelectionStartedBroadcast selectionStarted = WordSelectionStartedBroadcast.create(
                roomId, 
                drawerId, 
                drawerName,
                WordSelectionManager.SELECTION_TIMEOUT_SECONDS,
                room.getCurrentRound()
        );
        broadcastToRoomExcept(roomId, toJson(selectionStarted), drawerId);
        
        // Send SEND_WORD_OPTIONS only to the drawer (SECURITY CRITICAL)
        SendWordOptionsEvent wordOptions = SendWordOptionsEvent.create(
                roomId,
                session.getWordOptions(),
                WordSelectionManager.SELECTION_TIMEOUT_SECONDS
        );
        sendToPlayer(drawerId, toJson(wordOptions));
        
        logger.info("Word selection started: roomId={}, drawerId={}, optionsCount={}", 
                roomId, drawerId, session.getWordOptions().size());
        
        // Broadcast room status update
        broadcastRoomStatusUpdate(room);
    }

    /**
     * Handle word selection timeout - auto-select word and start drawing phase.
     */
    private void handleWordSelectionTimeout(String roomId, String drawerId, String selectedWord, boolean autoSelected) {
        logger.info("Word selection timeout handler: roomId={}, drawerId={}, autoSelected={}", 
                roomId, drawerId, autoSelected);
        
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            logger.warn("Room not found for word selection timeout: roomId={}", roomId);
            return;
        }
        
        RoomState room = roomOpt.get();
        
        // Verify we're still in word selection phase
        if (room.getStatus() != RoomStatus.WORD_SELECTION) {
            logger.debug("Room not in word selection phase: roomId={}, status={}", roomId, room.getStatus());
            return;
        }
        
        // Start drawing phase with auto-selected word
        startDrawingPhase(room, selectedWord, autoSelected);
    }

    /**
     * Handle WORD_SELECTED message from drawer.
     */
    private void handleWordSelected(WebSocketSession session, JsonNode payload) {
        String sessionId = session.getId();
        
        // Parse the message
        WordSelectedMessage message;
        try {
            message = objectMapper.treeToValue(payload, WordSelectedMessage.class);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse WORD_SELECTED message: sessionId={}, error={}", sessionId, e.getMessage());
            sendError(session, "INVALID_PAYLOAD", "Invalid WORD_SELECTED format");
            return;
        }
        
        // Validate required fields
        if (!message.isValid()) {
            sendError(session, "INVALID_PAYLOAD", "WORD_SELECTED requires: roomId, playerId, selectedWord");
            return;
        }
        
        String roomId = message.getRoomId();
        String playerId = message.getPlayerId();
        String selectedWord = message.getSelectedWord();
        
        // Get room state
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            sendError(session, "ROOM_NOT_FOUND", "Room does not exist");
            return;
        }
        RoomState room = roomOpt.get();
        
        // Verify room is in word selection phase
        if (room.getStatus() != RoomStatus.WORD_SELECTION) {
            logger.warn("Word selection attempted outside word selection phase: roomId={}, status={}", 
                    roomId, room.getStatus());
            sendError(session, "INVALID_PHASE", "Word selection is not active");
            return;
        }
        
        // Process word selection through manager (validates drawer, word option, etc.)
        WordSelectionResult result = wordSelectionManager.selectWord(roomId, playerId, selectedWord);
        
        if (!result.isSuccess()) {
            logger.warn("Word selection rejected: roomId={}, playerId={}, error={}", 
                    roomId, playerId, result.getErrorCode());
            
            // Log suspicious attempts
            if ("NOT_DRAWER".equals(result.getErrorCode()) || "INVALID_WORD".equals(result.getErrorCode())) {
                abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.INVALID_ACTION);
                logger.warn("SECURITY: Suspicious word selection attempt: roomId={}, playerId={}, error={}", 
                        roomId, playerId, result.getErrorCode());
            }
            
            sendError(session, result.getErrorCode(), result.getErrorMessage());
            return;
        }
        
        logger.info("Word selected by drawer: roomId={}, drawerId={}", roomId, playerId);
        
        // Start drawing phase
        startDrawingPhase(room, result.getSelectedWord(), false);
    }

    /**
     * Start the drawing phase after word is selected.
     */
    private void startDrawingPhase(RoomState room, String selectedWord, boolean autoSelected) {
        String roomId = room.getRoomId();
        String drawerId = room.getCurrentDrawerId();
        String drawerName = room.getPlayerName(drawerId);
        
        // Store selected word in room state (secure - not exposed to clients)
        room.setCurrentWord(selectedWord);
        room.setStatus(RoomStatus.DRAWING);
        room.startRound(room.getRoundDurationMs());
        
        // Create word hint for guessers (e.g., "_ _ _ _ _")
        String wordHint = createWordHint(selectedWord);
        int wordLength = selectedWord.length();
        
        // Send WORD_CONFIRMED to drawer (with actual word)
        WordConfirmedBroadcast drawerConfirmation = WordConfirmedBroadcast.createForDrawer(
                roomId, drawerId, drawerName, selectedWord
        );
        sendToPlayer(drawerId, toJson(drawerConfirmation));
        
        // Send WORD_CONFIRMED to guessers (with hint only)
        WordConfirmedBroadcast guesserConfirmation = WordConfirmedBroadcast.createForGuessers(
                roomId, drawerId, drawerName, wordHint, wordLength
        );
        broadcastToRoomExcept(roomId, toJson(guesserConfirmation), drawerId);
        
        // Broadcast room status update
        broadcastRoomStatusUpdate(room);
        
        logger.info("Drawing phase started: roomId={}, round={}, drawerId={}, wordLength={}, autoSelected={}", 
                roomId, room.getCurrentRound(), drawerId, wordLength, autoSelected);
        
        // Schedule round timeout
        scheduleRoundTimeout(room);
    }

    /**
     * Schedule round timeout to end round when time expires.
     */
    private void scheduleRoundTimeout(RoomState room) {
        String roomId = room.getRoomId();
        long roundDurationMs = room.getRoundDurationMs();
        
        scheduler.schedule(() -> {
            Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
            if (roomOpt.isEmpty()) {
                return;
            }
            
            RoomState currentRoom = roomOpt.get();
            
            // Check if round is still in progress
            if (currentRoom.getStatus() == RoomStatus.DRAWING && currentRoom.isRoundTimeExpired()) {
                logger.info("Round time expired: roomId={}, round={}", roomId, currentRoom.getCurrentRound());
                endRound(currentRoom, "time_expired");
            }
        }, roundDurationMs + 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Send message to a specific player by ID.
     */
    private void sendToPlayer(String playerId, String message) {
        Optional<String> sessionIdOpt = sessionManager.findSessionByPlayerId(playerId);
        if (sessionIdOpt.isPresent()) {
            sessionManager.sendToSession(sessionIdOpt.get(), message);
        } else {
            logger.warn("Cannot send to player - session not found: playerId={}", playerId);
        }
    }

    /**
     * Broadcast to room except one player.
     */
    private void broadcastToRoomExcept(String roomId, String message, String excludePlayerId) {
        Collection<String> playerIds = roomManager.getPlayersInRoom(roomId);
        int sentCount = 0;
        
        for (String playerId : playerIds) {
            if (playerId.equals(excludePlayerId)) {
                continue;
            }
            Optional<String> targetSessionId = sessionManager.findSessionByPlayerId(playerId);
            if (targetSessionId.isPresent()) {
                if (sessionManager.sendToSession(targetSessionId.get(), message)) {
                    sentCount++;
                }
            }
        }
        
        logger.debug("Broadcast to room (except {}): roomId={}, sentCount={}", excludePlayerId, roomId, sentCount);
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode payload) {
        String roomCode = getStringField(payload, "roomCode");
        String playerName = getStringField(payload, "playerName");
        String playerId = getStringField(payload, "playerId");
        
        if (roomCode == null || playerName == null) {
            sendError(session, "INVALID_PAYLOAD", "JOIN_ROOM requires 'roomCode' and 'playerName'");
            return;
        }
        
        logger.info("Player joining room: sessionId={}, roomCode={}, playerName={}, playerId={}", 
                session.getId(), roomCode, playerName, playerId);
        
        // Send ACK immediately
        sendMessage(session, createAckMessage("JOIN_ROOM", "Room join request received"));
        
        // Check if room exists
        if (!roomManager.roomExists(roomCode)) {
            sendError(session, "ROOM_NOT_FOUND", "Room '" + roomCode + "' does not exist");
            return;
        }
        
        // Get the room
        RoomState room = roomManager.getRoomById(roomCode).orElse(null);
        if (room == null) {
            sendError(session, "ROOM_NOT_FOUND", "Room '" + roomCode + "' not found");
            return;
        }
        
        // Check if room is joinable
        if (!room.isJoinable()) {
            sendError(session, "ROOM_FULL", "Room is full or not accepting players");
            return;
        }
        
        // Use provided playerId or generate one
        if (playerId == null || playerId.isEmpty()) {
            playerId = "player_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
        }
        
        // Add player to room
        room.addPlayer(playerId, playerName);
        
        // Store player session info
        PlayerSessionInfo playerInfo = new PlayerSessionInfo(playerId, playerName, roomCode);
        playerSessions.put(session.getId(), playerInfo);
        sessionManager.registerSession(session);
        
        // Send ROOM_ASSIGNED to joining player
        RoomAssignedEvent roomAssigned = RoomAssignedEvent.create(roomCode, playerId, playerName, false);
        sendMessage(session, toJson(roomAssigned));
        
        // Broadcast PLAYER_JOINED to other players in room
        PlayerJoinedBroadcast joinedMsg = PlayerJoinedBroadcast.create(
            roomCode, playerId, playerName,
            room.getPlayerCount(), room.getMaxPlayers()
        );
        broadcastToRoomExcept(roomCode, toJson(joinedMsg), playerId);
        
        // Broadcast ROOM_STATUS_UPDATE with current player list to all players
        broadcastRoomStatusUpdate(room);
        
        // Send room state to joining player
        sendRoomInfoToPlayer(session, room);
        
        logger.info("Player successfully joined room: playerId={}, roomId={}, playerCount={}", 
                playerId, roomCode, room.getPlayerCount());
    }

    private void handleCreateRoom(WebSocketSession session, JsonNode payload) {
        String playerName = getStringField(payload, "playerName");
        Integer maxPlayers = getIntField(payload, "maxPlayers");
        Integer roundTime = getIntField(payload, "roundTime");
        Integer totalRounds = getIntField(payload, "totalRounds");
        
        if (playerName == null) {
            sendError(session, "INVALID_PAYLOAD", "CREATE_ROOM requires 'playerName'");
            return;
        }
        
        logger.info("Player creating room: sessionId={}, playerName={}, maxPlayers={}", 
                session.getId(), playerName, maxPlayers);
        
        // Generate unique IDs
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String playerId = UUID.randomUUID().toString();
        
        // Create room via RoomManager
        RoomState room = roomManager.createRoom(roomCode);
        
        // Configure room settings
        if (maxPlayers != null) {
            room.setMaxPlayers(maxPlayers);
        }
        if (totalRounds != null) {
            room.setTotalRounds(totalRounds);
        }
        
        // Set creator as host
        room.setHostId(playerId);
        room.addPlayer(playerId, playerName);
        
        // Store player session info
        PlayerSessionInfo playerInfo = new PlayerSessionInfo(playerId, playerName, roomCode);
        playerSessions.put(session.getId(), playerInfo);
        sessionManager.registerSession(session);
        
        // Send ROOM_ASSIGNED to creator (with isHost=true)
        RoomAssignedEvent roomAssigned = RoomAssignedEvent.create(roomCode, playerId, playerName, true);
        sendMessage(session, toJson(roomAssigned));
        
        // Broadcast ROOM_STATUS_UPDATE with current player list
        broadcastRoomStatusUpdate(room);
        
        // Send ROOM_INFO to creator
        sendRoomInfoToPlayer(session, room);
        
        logger.info("Room created successfully: roomId={}, hostId={}, hostName={}", 
                roomCode, playerId, playerName);
    }

    private void handleLeaveRoom(WebSocketSession session, JsonNode payload) {
        logger.info("Player leaving room: sessionId={}", session.getId());
        
        PlayerSessionInfo playerInfo = playerSessions.get(session.getId());
        if (playerInfo == null) {
            sendError(session, "NOT_IN_ROOM", "You are not in any room");
            return;
        }
        
        // TODO: Implement room leaving logic
        sendMessage(session, createAckMessage("LEAVE_ROOM", "Left room successfully"));
    }

    private void handleStartGame(WebSocketSession session, JsonNode payload) {
        logger.info("Start game requested: sessionId={}", session.getId());
        
        // Get player info
        PlayerSessionInfo playerInfo = playerSessions.get(session.getId());
        if (playerInfo == null) {
            sendError(session, "NOT_IN_ROOM", "You are not in any room");
            return;
        }
        
        String playerId = playerInfo.getPlayerId();
        String roomCode = playerInfo.getRoomCode();
        Optional<RoomState> roomOpt = roomManager.getRoom(roomCode);
        if (roomOpt.isEmpty()) {
            sendError(session, "ROOM_NOT_FOUND", "Room does not exist");
            return;
        }
        
        RoomState room = roomOpt.get();
        
        // Check if player is the host
        if (!room.isHost(playerId)) {
            sendError(session, "NOT_HOST", "Only the host can start the game");
            return;
        }
        
        // Check if there are at least 2 players
        if (room.getPlayerCount() < 2) {
            sendError(session, "NOT_ENOUGH_PLAYERS", "At least 2 players are required to start the game");
            return;
        }
        
        // Check if game is already in progress
        if (room.getStatus() == RoomStatus.DRAWING || room.getStatus() == RoomStatus.IN_PROGRESS) {
            sendError(session, "GAME_ALREADY_STARTED", "Game is already in progress");
            return;
        }
        
        // Start the game
        room.setStatus(RoomStatus.WORD_SELECTION);
        room.setCurrentRound(1);
        room.setGameInProgress(true);
        
        // Broadcast game start to all players in room
        GameStartedBroadcast gameStartMsg = GameStartedBroadcast.create(
            roomCode,
            room.getTotalRounds(),
            room.getCurrentRound(),
            room.getCurrentDrawerId()
        );
        broadcastToRoom(roomCode, toJson(gameStartMsg));
        
        logger.info("Game started: roomId={}, hostId={}, playerCount={}", 
                roomCode, playerId, room.getPlayerCount());
    }

    private void handleDraw(WebSocketSession session, JsonNode payload) {
        // Extract drawing data
        JsonNode pathData = payload.get("pathData");
        if (pathData == null) {
            sendError(session, "INVALID_PAYLOAD", "DRAW requires 'pathData'");
            return;
        }
        
        logger.debug("Draw data received: sessionId={}", session.getId());
        
        // TODO: Validate player is current drawer
        // TODO: Broadcast to other players in room
        sendMessage(session, createAckMessage("DRAW", "Draw data received"));
    }

    /**
     * Handle DRAW_STROKE messages for real-time drawing broadcast.
     * Validates sender is current drawer and broadcasts to room.
     * Uses stroke batching for performance optimization.
     */
    private void handleDrawStroke(WebSocketSession session, JsonNode payload) {
        String sessionId = session.getId();
        
        // Parse the stroke message
        DrawStrokeMessage strokeMessage;
        try {
            strokeMessage = objectMapper.treeToValue(payload, DrawStrokeMessage.class);
        } catch (JsonProcessingException e) {
            sendError(session, "INVALID_PAYLOAD", "Invalid DRAW_STROKE format");
            return;
        }

        // Validate required fields
        if (!strokeMessage.isValid()) {
            sendError(session, "INVALID_PAYLOAD", 
                    "DRAW_STROKE requires: roomId, playerId, points (non-empty), color, strokeWidth (>0)");
            return;
        }

        String roomId = strokeMessage.getRoomId();
        String playerId = strokeMessage.getPlayerId();

        // Validate room exists
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            sendError(session, "ROOM_NOT_FOUND", "Room does not exist");
            return;
        }
        RoomState room = roomOpt.get();
        
        // Update room activity timestamp
        room.updateActivity();

        // Security validation - checks drawer, game state, rate limit
        SecurityValidator.ValidationResult validation = securityValidator.validateDrawStroke(playerId, room);
        if (!validation.isValid()) {
            sendError(session, validation.getErrorCode(), validation.getErrorMessage());
            handleAbuseCheck(session, playerId);
            return;
        }

        // Track metrics
        performanceMonitor.drawStrokeProcessed();
        performanceMonitor.messageReceived(roomId);

        // Add to stroke batcher for optimized broadcast
        // Convert Point objects to Map<String,Object> for JSON serialization
        List<Map<String, Object>> pointMaps = new ArrayList<>();
        for (var point : strokeMessage.getPoints()) {
            pointMaps.add(Map.of("x", point.getX(), "y", point.getY()));
        }
        
        StrokeBatcher.StrokeData strokeData = new StrokeBatcher.StrokeData(
                playerId,
                pointMaps,
                strokeMessage.getColor(),
                strokeMessage.getStrokeWidth()
        );
        
        if (!strokeBatcher.addStroke(roomId, strokeData)) {
            // Stroke was dropped (stale or room not registered)
            performanceMonitor.messageDropped();
        }
    }

    /**
     * Legacy GUESS handler - redirects to handleSubmitGuess for full validation.
     * Kept for backwards compatibility with clients using "GUESS" type.
     */
    private void handleGuess(WebSocketSession session, JsonNode payload) {
        // Delegate to SUBMIT_GUESS handler for full security validation
        handleSubmitGuess(session, payload);
    }

    /**
     * Handle SUBMIT_GUESS messages with full validation.
     * Validates: player in room, not drawer, round active, not already guessed.
     * Broadcasts CORRECT_GUESS to room or sends GUESS_FEEDBACK to player only.
     */
    private void handleSubmitGuess(WebSocketSession session, JsonNode payload) {
        String sessionId = session.getId();
        
        // Parse the guess message
        SubmitGuessMessage guessMessage;
        try {
            guessMessage = objectMapper.treeToValue(payload, SubmitGuessMessage.class);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse SUBMIT_GUESS message: sessionId={}, error={}", sessionId, e.getMessage());
            sendError(session, "INVALID_PAYLOAD", "Invalid SUBMIT_GUESS format: " + e.getMessage());
            return;
        }

        // Validate required fields
        if (!guessMessage.isValid()) {
            logger.warn("Invalid SUBMIT_GUESS - missing required fields: sessionId={}", sessionId);
            sendError(session, "INVALID_PAYLOAD", "SUBMIT_GUESS requires: roomId, playerId, guess (non-empty)");
            return;
        }

        String roomId = guessMessage.getRoomId();
        String playerId = guessMessage.getPlayerId();
        String normalizedGuess = guessMessage.getNormalizedGuess();

        // Get room state
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            sendError(session, "ROOM_NOT_FOUND", "Room does not exist");
            return;
        }
        RoomState room = roomOpt.get();
        
        // Update room activity
        room.updateActivity();

        // Validate player is in room
        if (!room.hasPlayer(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.INVALID_ACTION);
            sendError(session, "NOT_IN_ROOM", "You are not in this room");
            return;
        }

        // Security validation - checks mute, drawer, already guessed, game state, rate limit
        SecurityValidator.ValidationResult validation = securityValidator.validateGuess(playerId, room);
        if (!validation.isValid()) {
            sendError(session, validation.getErrorCode(), validation.getErrorMessage());
            handleAbuseCheck(session, playerId);
            return;
        }

        // Track metrics
        performanceMonitor.guessProcessed();
        performanceMonitor.messageReceived(roomId);

        // Validate player has not already guessed correctly
        if (room.hasPlayerGuessedCorrectly(playerId)) {
            sendError(session, "ALREADY_GUESSED", "You have already guessed the word correctly");
            return;
        }

        // Compare guess with current word
        String normalizedWord = room.getNormalizedCurrentWord();
        boolean isCorrect = normalizedGuess.equals(normalizedWord);

        if (isCorrect) {
            handleCorrectGuess(session, room, playerId, roomId);
        } else {
            handleIncorrectGuess(session, roomId, playerId);
        }
    }

    /**
     * Handle a correct guess - mark player, calculate score, broadcast to room.
     */
    private void handleCorrectGuess(WebSocketSession session, RoomState room, String playerId, String roomId) {
        // Check if scores are frozen (game ended)
        if (room.areScoresFrozen()) {
            sendError(session, "GAME_ENDED", "Game has ended, no more guesses allowed");
            return;
        }
        
        // Mark player as guessed correctly (thread-safe, returns order)
        int guessOrder = room.markPlayerGuessedCorrectly(playerId);
        
        if (guessOrder == -1) {
            // Race condition - player already marked
            sendError(session, "ALREADY_GUESSED", "You have already guessed the word correctly");
            return;
        }

        // Calculate score based on remaining time
        long timeLeftMs = room.getRemainingTimeMs();
        int pointsAwarded = scoreCalculator.calculateGuessScore(timeLeftMs);
        
        // Award score (thread-safe, prevents double scoring)
        int newTotalScore = room.awardScore(playerId, pointsAwarded);
        if (newTotalScore == -1) {
            logger.warn("Duplicate score award prevented: roomId={}, playerId={}", roomId, playerId);
            sendError(session, "ALREADY_SCORED", "Score already awarded for this round");
            return;
        }
        
        // Track correct guesses for final results
        incrementCorrectGuesses(roomId, playerId);

        logger.info("Score awarded: roomId={}, playerId={}, guessOrder={}, pointsAwarded={}, totalScore={}, timeLeftMs={}",
                roomId, playerId, guessOrder, pointsAwarded, newTotalScore, timeLeftMs);

        // Get player name
        String playerName = room.getPlayerName(playerId);

        // Send feedback to the guesser
        try {
            GuessFeedback feedback = GuessFeedback.correct(roomId, 
                    String.format("Correct! +%d points!", pointsAwarded));
            String feedbackJson = objectMapper.writeValueAsString(feedback);
            sendMessage(session, feedbackJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize guess feedback: roomId={}, error={}", roomId, e.getMessage());
        }

        // Broadcast CORRECT_GUESS to all players in room
        try {
            CorrectGuessBroadcast broadcast = CorrectGuessBroadcast.create(roomId, playerId, playerName, guessOrder);
            String broadcastJson = objectMapper.writeValueAsString(broadcast);
            broadcastToRoomAll(roomId, broadcastJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize correct guess broadcast: roomId={}, error={}", roomId, e.getMessage());
        }

        // Broadcast SCORE_UPDATE to all players
        broadcastScoreUpdate(roomId, playerId, playerName, pointsAwarded, newTotalScore, guessOrder);

        // Award drawer bonus on first correct guess
        if (guessOrder == 1) {
            awardDrawerBonus(room, roomId);
        }

        // Broadcast updated leaderboard
        broadcastLeaderboard(room, roomId);

        // Check if all players have guessed
        if (room.haveAllPlayersGuessed()) {
            logger.info("All players guessed correctly: roomId={}", roomId);
            endRound(room, "all_guessed");
        }
    }

    /**
     * Increment correct guess count for a player (for final results).
     */
    private void incrementCorrectGuesses(String roomId, String playerId) {
        playerCorrectGuesses.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .merge(playerId, 1, Integer::sum);
    }

    /**
     * Get correct guess count for a player.
     */
    private int getCorrectGuessCount(String roomId, String playerId) {
        Map<String, Integer> roomGuesses = playerCorrectGuesses.get(roomId);
        if (roomGuesses == null) {
            return 0;
        }
        Integer count = roomGuesses.get(playerId);
        return count != null ? count : 0;
    }

    /**
     * End the current round and check if game should end.
     */
    private void endRound(RoomState room, String reason) {
        String roomId = room.getRoomId();
        String word = room.getCurrentWord();
        int roundNumber = room.getCurrentRound();
        
        logger.info("Round ended: roomId={}, round={}, reason={}", 
                roomId, roundNumber, reason);
        
        // Clean up word selection session
        wordSelectionManager.cleanupSession(roomId);
        
        room.setStatus(RoomStatus.ROUND_OVER);
        
        // Broadcast ROUND_ENDED with the word revealed
        RoundEndedBroadcast roundEnded = RoundEndedBroadcast.create(roomId, reason, word, roundNumber);
        roundEnded.setLeaderboard(buildLeaderboard(room));
        roundEnded.setNextRoundInMs(5000);
        broadcastToRoom(roomId, toJson(roundEnded));
        
        // Check if game should end
        if (room.areAllRoundsCompleted()) {
            endGame(room, "all_rounds_completed");
        } else if (room.getPlayerCount() < room.getMinPlayers()) {
            endGame(room, "insufficient_players");
        } else {
            // Prepare for next round after delay
            int nextRound = room.getCurrentRound() + 1;
            room.setCurrentRound(nextRound);
            room.resetRound();
            
            // Schedule next round word selection
            scheduler.schedule(() -> {
                Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
                if (roomOpt.isPresent() && roomOpt.get().isGameInProgress()) {
                    selectNextDrawerAndStartWordSelection(roomOpt.get());
                }
            }, 5000, TimeUnit.MILLISECONDS);
            
            logger.info("Next round scheduled: roomId={}, round={}", roomId, nextRound);
        }
    }

    /**
     * End the game and initiate results flow.
     */
    private void endGame(RoomState room, String reason) {
        String roomId = room.getRoomId();
        
        // Prevent duplicate game end processing
        if (pendingGameEnds.putIfAbsent(roomId, true) != null) {
            logger.debug("Game end already in progress: roomId={}", roomId);
            return;
        }
        
        logger.info("Game ending: roomId={}, reason={}, finalRound={}", 
                roomId, reason, room.getCurrentRound());
        
        // Freeze scores and transition state
        room.endGame();
        
        long gameDurationMs = room.getGameDurationMs();
        
        // Broadcast GAME_ENDED
        GameEndedBroadcast gameEnded = GameEndedBroadcast.create(
                roomId,
                reason,
                room.getTotalRounds(),
                room.getCurrentRound(),
                gameDurationMs,
                RESULTS_DELAY_MS
        );
        broadcastToRoom(roomId, toJson(gameEnded));
        
        logger.info("GAME_ENDED broadcast: roomId={}, reason={}, duration={}ms", 
                roomId, reason, gameDurationMs);
        
        // Schedule FINAL_RESULTS broadcast
        scheduler.schedule(() -> {
            sendFinalResults(room, gameDurationMs);
        }, RESULTS_DELAY_MS, TimeUnit.MILLISECONDS);
        
        // Schedule room cleanup
        scheduler.schedule(() -> {
            cleanupRoom(roomId);
        }, RESULTS_DELAY_MS + ROOM_CLOSE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Send final results to all players in the room.
     */
    private void sendFinalResults(RoomState room, long gameDurationMs) {
        String roomId = room.getRoomId();
        
        FinalResultsBroadcast finalResults = FinalResultsBroadcast.create(
                roomId,
                room.getTotalRounds(),
                gameDurationMs,
                ROOM_CLOSE_DELAY_MS
        );
        
        // Get sorted leaderboard
        List<LeaderboardEntry> leaderboard = room.getLeaderboard();
        
        if (leaderboard.isEmpty()) {
            logger.warn("No players in leaderboard for final results: roomId={}", roomId);
            broadcastToRoom(roomId, toJson(finalResults));
            return;
        }
        
        // Determine winners (handle ties)
        int highestScore = leaderboard.get(0).getScore();
        List<FinalResultsBroadcast.WinnerInfo> winners = new ArrayList<>();
        
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.getScore() == highestScore) {
                boolean isTie = winners.size() > 0;
                winners.add(new FinalResultsBroadcast.WinnerInfo(
                        entry.getPlayerId(),
                        entry.getPlayerName(),
                        entry.getScore(),
                        isTie
                ));
            } else {
                break;
            }
        }
        
        // Mark first winner's tie status if there are multiple winners
        if (winners.size() > 1) {
            winners.get(0).setTie(true);
        }
        
        finalResults.setWinners(winners);
        
        // Build rankings
        int rank = 1;
        for (LeaderboardEntry entry : leaderboard) {
            boolean isWinner = entry.getScore() == highestScore;
            int correctGuesses = getCorrectGuessCount(roomId, entry.getPlayerId());
            
            FinalResultsBroadcast.PlayerResult result = new FinalResultsBroadcast.PlayerResult(
                    entry.getPlayerId(),
                    entry.getPlayerName(),
                    entry.getScore(),
                    rank++,
                    correctGuesses,
                    isWinner
            );
            finalResults.addRanking(result);
        }
        
        // Broadcast final results
        broadcastToRoom(roomId, toJson(finalResults));
        
        // Log winners
        StringBuilder winnerLog = new StringBuilder();
        for (FinalResultsBroadcast.WinnerInfo winner : winners) {
            winnerLog.append(winner.getPlayerName()).append(" (").append(winner.getScore()).append("), ");
        }
        logger.info("FINAL_RESULTS broadcast: roomId={}, winners=[{}], playerCount={}", 
                roomId, winnerLog.toString(), leaderboard.size());
    }

    /**
     * Clean up room resources after game ends.
     */
    private void cleanupRoom(String roomId) {
        logger.info("Cleaning up room: roomId={}", roomId);
        
        // Clear tracking data
        playerCorrectGuesses.remove(roomId);
        pendingGameEnds.remove(roomId);
        pendingGameStarts.remove(roomId);
        
        // Clean up word selection session
        wordSelectionManager.cleanupSession(roomId);
        
        // Remove room from manager
        roomManager.removeRoom(roomId);
        
        logger.info("Room cleanup complete: roomId={}", roomId);
    }

    /**
     * Manually terminate a game (e.g., when all players leave).
     */
    public void terminateGame(String roomId, String reason) {
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            logger.warn("Cannot terminate - room not found: roomId={}", roomId);
            return;
        }
        
        RoomState room = roomOpt.get();
        if (room.getStatus() == RoomStatus.ENDED) {
            logger.debug("Game already ended: roomId={}", roomId);
            return;
        }
        
        endGame(room, reason);
    }

    /**
     * Award drawer bonus when first player guesses correctly.
     */
    private void awardDrawerBonus(RoomState room, String roomId) {
        String drawerId = room.getCurrentDrawerId();
        if (drawerId == null) {
            return;
        }

        int bonus = scoreCalculator.calculateDrawerBonus();
        int newDrawerScore = room.awardDrawerBonus(drawerId, bonus);
        
        if (newDrawerScore == -1) {
            logger.debug("Drawer bonus already awarded: roomId={}, drawerId={}", roomId, drawerId);
            return;
        }

        String drawerName = room.getPlayerName(drawerId);
        
        logger.info("Drawer bonus awarded: roomId={}, drawerId={}, bonus={}, newTotal={}",
                roomId, drawerId, bonus, newDrawerScore);

        // Broadcast drawer's score update
        broadcastScoreUpdate(roomId, drawerId, drawerName, bonus, newDrawerScore, 0);
    }

    /**
     * Broadcast SCORE_UPDATE event to all players in room.
     */
    private void broadcastScoreUpdate(String roomId, String playerId, String playerName,
                                       int pointsAwarded, int totalScore, int guessOrder) {
        try {
            ScoreUpdateBroadcast scoreUpdate = ScoreUpdateBroadcast.create(
                    roomId, playerId, playerName, pointsAwarded, totalScore, guessOrder);
            String scoreJson = objectMapper.writeValueAsString(scoreUpdate);
            broadcastToRoomAll(roomId, scoreJson);
            
            logger.debug("SCORE_UPDATE broadcast: roomId={}, playerId={}, points={}, total={}",
                    roomId, playerId, pointsAwarded, totalScore);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize score update: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    /**
     * Broadcast LEADERBOARD_UPDATE event to all players in room.
     */
    private void broadcastLeaderboard(RoomState room, String roomId) {
        try {
            List<LeaderboardEntry> leaderboard = room.getLeaderboard();
            LeaderboardUpdateBroadcast broadcast = LeaderboardUpdateBroadcast.create(roomId, leaderboard);
            String leaderboardJson = objectMapper.writeValueAsString(broadcast);
            broadcastToRoomAll(roomId, leaderboardJson);
            
            logger.info("LEADERBOARD_UPDATE broadcast: roomId={}, playerCount={}", roomId, leaderboard.size());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize leaderboard update: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    /**
     * Handle an incorrect guess - send feedback only to the guesser.
     */
    private void handleIncorrectGuess(WebSocketSession session, String roomId, String playerId) {
        logger.debug("Incorrect guess: roomId={}, playerId={}", roomId, playerId);

        try {
            GuessFeedback feedback = GuessFeedback.incorrect(roomId, "Incorrect guess. Try again!");
            String feedbackJson = objectMapper.writeValueAsString(feedback);
            sendMessage(session, feedbackJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize guess feedback: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    /**
     * Get player name from session metadata or return default.
     */
    private String getPlayerNameFromSession(String sessionId, String playerId) {
        Optional<SessionManager.SessionMetadata> metadata = sessionManager.getSessionMetadata(sessionId);
        if (metadata.isPresent() && metadata.get().getPlayerName() != null) {
            return metadata.get().getPlayerName();
        }
        // Fallback to player ID
        return playerId;
    }

    /**
     * Broadcast a message to ALL players in a room (including sender).
     */
    private void broadcastToRoomAll(String roomId, String message) {
        Collection<String> playerIds = roomManager.getPlayersInRoom(roomId);
        int sentCount = 0;
        
        for (String playerId : playerIds) {
            Optional<String> targetSessionId = sessionManager.findSessionByPlayerId(playerId);
            if (targetSessionId.isPresent()) {
                if (sessionManager.sendToSession(targetSessionId.get(), message)) {
                    sentCount++;
                }
            }
        }
        
        logger.debug("Broadcast to room (all): roomId={}, sentCount={}", roomId, sentCount);
    }

    private void handleChat(WebSocketSession session, JsonNode payload) {
        String message = getStringField(payload, "message");
        String roomId = getStringField(payload, "roomId");
        String playerId = getStringField(payload, "playerId");
        
        if (message == null || message.trim().isEmpty()) {
            sendError(session, "INVALID_PAYLOAD", "CHAT requires non-empty 'message'");
            return;
        }

        if (roomId == null || playerId == null) {
            sendError(session, "INVALID_PAYLOAD", "CHAT requires 'roomId' and 'playerId'");
            return;
        }

        // Get room state
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            sendError(session, "ROOM_NOT_FOUND", "Room does not exist: " + roomId);
            return;
        }
        RoomState room = roomOpt.get();

        // Validate player is in room
        if (!room.hasPlayer(playerId)) {
            abuseTracker.recordViolation(playerId, AbuseTracker.ViolationType.INVALID_ACTION);
            sendError(session, "NOT_IN_ROOM", "You are not in this room");
            return;
        }

        // Security validation - checks mute status and rate limit
        SecurityValidator.ValidationResult validation = securityValidator.validateChat(playerId, room);
        if (!validation.isValid()) {
            sendError(session, validation.getErrorCode(), validation.getErrorMessage());
            if (handleAbuseCheck(session, playerId)) {
                return;
            }
            return;
        }
        
        logger.debug("Chat message: roomId={}, playerId={}, message={}", roomId, playerId, message);
        
        // Build chat broadcast message
        String chatBroadcast = String.format(
            "{\"type\":\"CHAT_MESSAGE\",\"roomId\":\"%s\",\"playerId\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
            roomId, playerId, message.replace("\"", "\\\""), System.currentTimeMillis());
        
        // Broadcast to all players in room
        broadcastToRoom(roomId, chatBroadcast);
    }

    private void handlePing(WebSocketSession session) {
        sendMessage(session, "{\"type\":\"PONG\",\"timestamp\":" + System.currentTimeMillis() + "}");
    }

    /**
     * Send current room state to a player.
     */
    private void sendRoomInfoToPlayer(WebSocketSession session, RoomState room) {
        try {
            StringBuilder players = new StringBuilder("[");
            boolean first = true;
            
            for (String playerId : room.getPlayerIds()) {
                if (!first) {
                    players.append(",");
                }
                first = false;
                
                String playerName = room.getPlayerName(playerId);
                int score = room.getPlayerScore(playerId);
                
                players.append(String.format(
                    "{\"playerId\":\"%s\",\"playerName\":\"%s\",\"score\":%d}",
                    playerId, playerName, score
                ));
            }
            players.append("]");
            
            String roomInfo = String.format(
                "{\"type\":\"ROOM_INFO\",\"roomId\":\"%s\",\"players\":%s,\"maxPlayers\":%d,\"roundsTotal\":%d,\"status\":\"%s\",\"timestamp\":%d}",
                room.getRoomId(),
                players.toString(),
                room.getMaxPlayers(),
                room.getTotalRounds(),
                room.getStatus(),
                System.currentTimeMillis()
            );
            
            sendMessage(session, roomInfo);
            logger.debug("Sent ROOM_INFO to session: roomId={}", room.getRoomId());
            
        } catch (Exception e) {
            logger.error("Failed to send room info: {}", e.getMessage(), e);
        }
    }

    // ==================== Helper Methods ====================

    private String getStringField(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private Integer getIntField(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asInt() : null;
    }

    /**
     * Convert object to JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Broadcast to all players in a room (including sender).
     */
    private void broadcastToRoom(String roomId, String message) {
        broadcastToRoomAll(roomId, message);
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
                performanceMonitor.messageSent();
            }
        } catch (IOException e) {
            logger.warn("Failed to send message: sessionId={}", session.getId());
        }
    }

    /**
     * Send standardized ERROR event to client.
     * Format: {"type":"ERROR","code":"<CODE>","message":"<message>","timestamp":<ms>}
     */
    private void sendError(WebSocketSession session, String errorCode, String errorMessage) {
        String errorJson = String.format(
                "{\"type\":\"ERROR\",\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}", 
                errorCode, errorMessage.replace("\"", "\\\""), System.currentTimeMillis());
        sendMessage(session, errorJson);
    }

    private String createWelcomeMessage(String sessionId) {
        return String.format(
                "{\"type\":\"CONNECTED\",\"sessionId\":\"%s\",\"timestamp\":%d}",
                sessionId, System.currentTimeMillis());
    }

    private String createAckMessage(String originalType, String message) {
        return String.format(
                "{\"type\":\"ACK\",\"originalType\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
                originalType, message, System.currentTimeMillis());
    }

    /**
     * Broadcast a message to all sessions in a room.
     */
    public void broadcastToRoom(String roomCode, String message, String excludeSessionId) {
        // TODO: Get all sessions in room from RoomManager
        logger.debug("Broadcasting to room: roomCode={}, excludeSession={}", roomCode, excludeSessionId);
    }

    /**
     * Inner class to track player session information.
     */
    private static class PlayerSessionInfo {
        private final String playerId;
        private final String playerName;
        private final String roomCode;

        public PlayerSessionInfo(String playerId, String playerName, String roomCode) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.roomCode = roomCode;
        }

        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public String getRoomCode() { return roomCode; }
    }
}
