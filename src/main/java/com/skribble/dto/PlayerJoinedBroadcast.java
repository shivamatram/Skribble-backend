package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PLAYER_JOINED broadcast event sent to all players in room.
 */
public class PlayerJoinedBroadcast {

    @JsonProperty("type")
    private final String type = "PLAYER_JOINED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("maxPlayers")
    private int maxPlayers;

    @JsonProperty("timestamp")
    private long timestamp;

    public PlayerJoinedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static PlayerJoinedBroadcast create(String roomId, String playerId, String playerName,
                                                int playerCount, int maxPlayers) {
        PlayerJoinedBroadcast broadcast = new PlayerJoinedBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.playerCount = playerCount;
        broadcast.maxPlayers = maxPlayers;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
