package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skribble.room.RoomStatus;

import java.util.List;

/**
 * DTO for ROOM_STATUS_UPDATE broadcast event sent to all players in room.
 */
public class RoomStatusUpdateBroadcast {

    @JsonProperty("type")
    private final String type = "ROOM_STATUS_UPDATE";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("status")
    private RoomStatus status;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("maxPlayers")
    private int maxPlayers;

    @JsonProperty("minPlayersToStart")
    private int minPlayersToStart;

    @JsonProperty("players")
    private List<PlayerInfo> players;

    @JsonProperty("currentDrawerId")
    private String currentDrawerId;

    @JsonProperty("roundNumber")
    private int roundNumber;

    @JsonProperty("totalRounds")
    private int totalRounds;

    @JsonProperty("timestamp")
    private long timestamp;

    public RoomStatusUpdateBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static RoomStatusUpdateBroadcast create(String roomId, RoomStatus status, int playerCount,
                                                    int maxPlayers, int minPlayersToStart,
                                                    List<PlayerInfo> players) {
        RoomStatusUpdateBroadcast broadcast = new RoomStatusUpdateBroadcast();
        broadcast.roomId = roomId;
        broadcast.status = status;
        broadcast.playerCount = playerCount;
        broadcast.maxPlayers = maxPlayers;
        broadcast.minPlayersToStart = minPlayersToStart;
        broadcast.players = players;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getMinPlayersToStart() { return minPlayersToStart; }
    public void setMinPlayersToStart(int minPlayersToStart) { this.minPlayersToStart = minPlayersToStart; }

    public List<PlayerInfo> getPlayers() { return players; }
    public void setPlayers(List<PlayerInfo> players) { this.players = players; }

    public String getCurrentDrawerId() { return currentDrawerId; }
    public void setCurrentDrawerId(String currentDrawerId) { this.currentDrawerId = currentDrawerId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Player info for room status.
     */
    public static class PlayerInfo {
        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("playerName")
        private String playerName;

        @JsonProperty("score")
        private int score;

        @JsonProperty("isDrawer")
        private boolean isDrawer;

        public PlayerInfo() {}

        public PlayerInfo(String playerId, String playerName, int score, boolean isDrawer) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
            this.isDrawer = isDrawer;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public boolean isDrawer() { return isDrawer; }
        public void setDrawer(boolean drawer) { isDrawer = drawer; }
    }
}
