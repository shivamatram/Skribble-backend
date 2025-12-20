package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PLAYER_DISCONNECTED broadcast event.
 */
public class PlayerDisconnectedBroadcast {

    @JsonProperty("type")
    private final String type = "PLAYER_DISCONNECTED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("wasDrawer")
    private boolean wasDrawer;

    @JsonProperty("reconnectWindowMs")
    private long reconnectWindowMs;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("timestamp")
    private long timestamp;

    public PlayerDisconnectedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static PlayerDisconnectedBroadcast create(String roomId, String playerId, String playerName,
                                                      boolean wasDrawer, long reconnectWindowMs, int playerCount) {
        PlayerDisconnectedBroadcast broadcast = new PlayerDisconnectedBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.wasDrawer = wasDrawer;
        broadcast.reconnectWindowMs = reconnectWindowMs;
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

    public long getReconnectWindowMs() { return reconnectWindowMs; }
    public void setReconnectWindowMs(long reconnectWindowMs) { this.reconnectWindowMs = reconnectWindowMs; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
