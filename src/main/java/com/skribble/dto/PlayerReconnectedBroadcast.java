package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PLAYER_RECONNECTED broadcast event.
 */
public class PlayerReconnectedBroadcast {

    @JsonProperty("type")
    private final String type = "PLAYER_RECONNECTED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("wasDrawer")
    private boolean wasDrawer;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("timestamp")
    private long timestamp;

    public PlayerReconnectedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static PlayerReconnectedBroadcast create(String roomId, String playerId, String playerName,
                                                     boolean wasDrawer, int playerCount) {
        PlayerReconnectedBroadcast broadcast = new PlayerReconnectedBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.wasDrawer = wasDrawer;
        broadcast.playerCount = playerCount;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public boolean isWasDrawer() { return wasDrawer; }
    public void setWasDrawer(boolean wasDrawer) { this.wasDrawer = wasDrawer; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
