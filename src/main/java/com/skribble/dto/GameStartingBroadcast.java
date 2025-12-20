package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for GAME_STARTING broadcast when minimum players reached.
 */
public class GameStartingBroadcast {

    @JsonProperty("type")
    private final String type = "GAME_STARTING";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("startsInMs")
    private long startsInMs;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("timestamp")
    private long timestamp;

    public GameStartingBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static GameStartingBroadcast create(String roomId, long startsInMs, int playerCount) {
        GameStartingBroadcast broadcast = new GameStartingBroadcast();
        broadcast.roomId = roomId;
        broadcast.startsInMs = startsInMs;
        broadcast.playerCount = playerCount;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public long getStartsInMs() { return startsInMs; }
    public void setStartsInMs(long startsInMs) { this.startsInMs = startsInMs; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
