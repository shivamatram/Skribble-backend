package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PLAYER_REMOVED broadcast when reconnection window expires.
 */
public class PlayerRemovedBroadcast {

    @JsonProperty("type")
    private final String type = "PLAYER_REMOVED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("timestamp")
    private long timestamp;

    public PlayerRemovedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static PlayerRemovedBroadcast create(String roomId, String playerId, String playerName,
                                                 String reason, int playerCount) {
        PlayerRemovedBroadcast broadcast = new PlayerRemovedBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.reason = reason;
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

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
