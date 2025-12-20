package com.skribble.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe manager for game rooms with matchmaking support.
 */
@Component
public class RoomManager {

    private static final Logger logger = LoggerFactory.getLogger(RoomManager.class);

    // Thread-safe map of room ID to RoomState
    private final Map<String, RoomState> rooms = new ConcurrentHashMap<>();

    // Map player ID to room ID for quick lookup
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();

    // Counter for generating room IDs
    private final AtomicInteger roomCounter = new AtomicInteger(1);

    /**
     * Result of matchmaking operation.
     */
    public static class MatchmakingResult {
        private final RoomState room;
        private final boolean newRoom;

        public MatchmakingResult(RoomState room, boolean newRoom) {
            this.room = room;
            this.newRoom = newRoom;
        }

        public RoomState getRoom() {
            return room;
        }

        public boolean isNewRoom() {
            return newRoom;
        }
    }

    /**
     * Find an existing joinable room or create a new one.
     * Thread-safe matchmaking for quick play.
     */
    public synchronized MatchmakingResult findOrCreateRoom(String playerId, String playerName) {
        // First, check if player is already in a room
        String existingRoomId = playerRoomMap.get(playerId);
        if (existingRoomId != null) {
            RoomState existingRoom = rooms.get(existingRoomId);
            if (existingRoom != null) {
                logger.info("Player already in room: playerId={}, roomId={}", playerId, existingRoomId);
                return new MatchmakingResult(existingRoom, false);
            }
        }

        // Find best available room (WAITING rooms preferred, then IN_PROGRESS with slots)
        RoomState bestRoom = findBestAvailableRoom();

        if (bestRoom != null) {
            // Join existing room
            bestRoom.addPlayer(playerId, playerName);
            playerRoomMap.put(playerId, bestRoom.getRoomId());
            logger.info("Player matched to existing room: playerId={}, roomId={}, playerCount={}",
                    playerId, bestRoom.getRoomId(), bestRoom.getPlayerCount());
            return new MatchmakingResult(bestRoom, false);
        }

        // No available room, create a new one
        String newRoomId = generateRoomId();
        RoomState newRoom = new RoomState(newRoomId);
        newRoom.addPlayer(playerId, playerName);
        rooms.put(newRoomId, newRoom);
        playerRoomMap.put(playerId, newRoomId);
        
        logger.info("Created new room for player: playerId={}, roomId={}", playerId, newRoomId);
        return new MatchmakingResult(newRoom, true);
    }

    /**
     * Find the best available room to join.
     * Priority: WAITING rooms with most players > IN_PROGRESS rooms with most slots
     */
    private RoomState findBestAvailableRoom() {
        RoomState bestWaitingRoom = null;
        RoomState bestInProgressRoom = null;

        for (RoomState room : rooms.values()) {
            if (!room.isJoinable()) {
                continue;
            }

            if (room.getStatus() == RoomStatus.WAITING) {
                // Prefer WAITING room with most players (closer to starting)
                if (bestWaitingRoom == null || 
                    room.getPlayerCount() > bestWaitingRoom.getPlayerCount()) {
                    bestWaitingRoom = room;
                }
            } else if (room.getStatus() == RoomStatus.IN_PROGRESS) {
                // For IN_PROGRESS, prefer room with most slots (more room for late joiners)
                if (bestInProgressRoom == null ||
                    room.getAvailableSlots() > bestInProgressRoom.getAvailableSlots()) {
                    bestInProgressRoom = room;
                }
            }
        }

        // Prefer WAITING rooms over IN_PROGRESS
        return bestWaitingRoom != null ? bestWaitingRoom : bestInProgressRoom;
    }

    /**
     * Get all joinable rooms.
     */
    public List<RoomState> getJoinableRooms() {
        List<RoomState> joinable = new ArrayList<>();
        for (RoomState room : rooms.values()) {
            if (room.isJoinable()) {
                joinable.add(room);
            }
        }
        return joinable;
    }

    /**
     * Generate a unique room ID.
     */
    private String generateRoomId() {
        return "ROOM-" + roomCounter.getAndIncrement();
    }

    /**
     * Create a new room with specific ID.
     */
    public RoomState createRoom(String roomId) {
        RoomState room = new RoomState(roomId);
        rooms.put(roomId, room);
        logger.info("Room created: roomId={}", roomId);
        return room;
    }

    /**
     * Get a room by ID.
     */
    public Optional<RoomState> getRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * Remove a room.
     */
    public void removeRoom(String roomId) {
        RoomState room = rooms.remove(roomId);
        if (room != null) {
            // Clean up player-room mappings
            for (String playerId : room.getPlayerIds()) {
                playerRoomMap.remove(playerId);
            }
            logger.info("Room removed: roomId={}", roomId);
        }
    }

    /**
     * Add a player to a room.
     */
    public boolean addPlayerToRoom(String roomId, String playerId) {
        RoomState room = rooms.get(roomId);
        if (room == null) {
            logger.warn("Cannot add player - room not found: roomId={}, playerId={}", roomId, playerId);
            return false;
        }

        room.addPlayer(playerId);
        playerRoomMap.put(playerId, roomId);
        logger.info("Player added to room: roomId={}, playerId={}, playerCount={}", 
                roomId, playerId, room.getPlayerCount());
        return true;
    }

    /**
     * Remove a player from their room.
     */
    public void removePlayerFromRoom(String playerId) {
        String roomId = playerRoomMap.remove(playerId);
        if (roomId != null) {
            RoomState room = rooms.get(roomId);
            if (room != null) {
                room.removePlayer(playerId);
                logger.info("Player removed from room: roomId={}, playerId={}, remainingPlayers={}", 
                        roomId, playerId, room.getPlayerCount());
                
                // Clean up empty rooms
                if (room.getPlayerCount() == 0) {
                    removeRoom(roomId);
                }
            }
        }
    }

    /**
     * Get the room ID for a player.
     */
    public Optional<String> getRoomIdForPlayer(String playerId) {
        return Optional.ofNullable(playerRoomMap.get(playerId));
    }

    /**
     * Check if a player is the current drawer in their room.
     */
    public boolean isPlayerCurrentDrawer(String playerId) {
        String roomId = playerRoomMap.get(playerId);
        if (roomId == null) {
            return false;
        }
        RoomState room = rooms.get(roomId);
        return room != null && room.isCurrentDrawer(playerId);
    }

    /**
     * Set the current drawer for a room.
     */
    public void setCurrentDrawer(String roomId, String drawerId) {
        RoomState room = rooms.get(roomId);
        if (room != null) {
            room.setCurrentDrawerId(drawerId);
            logger.info("Current drawer set: roomId={}, drawerId={}", roomId, drawerId);
        }
    }

    /**
     * Get all player IDs in a room.
     */
    public Collection<String> getPlayersInRoom(String roomId) {
        RoomState room = rooms.get(roomId);
        return room != null ? room.getPlayerIds() : java.util.Collections.emptySet();
    }

    /**
     * Check if a room exists.
     */
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    /**
     * Get count of active rooms.
     */
    public int getRoomCount() {
        return rooms.size();
    }

    /**
     * Get all active room IDs.
     */
    public Set<String> getAllRoomIds() {
        return new HashSet<>(rooms.keySet());
    }

    /**
     * Get all rooms (for iteration/monitoring).
     */
    public Collection<RoomState> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Mark a room as ended (will be cleaned up later).
     */
    public void markRoomEnded(String roomId) {
        RoomState room = rooms.get(roomId);
        if (room != null) {
            room.endGame();
            logger.info("Room marked as ended: roomId={}", roomId);
        }
    }
}
