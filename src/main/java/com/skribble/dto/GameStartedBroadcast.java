package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for GAME_STARTED broadcast when host starts the game.
 */
public class GameStartedBroadcast {

    @JsonProperty("type")
    private final String type = "GAME_STARTED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("totalRounds")
    private int totalRounds;

    @JsonProperty("currentRound")
    private int currentRound;

    @JsonProperty("currentDrawerId")
    private String currentDrawerId;

    @JsonProperty("timestamp")
    private long timestamp;

    public GameStartedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static GameStartedBroadcast create(String roomId, int totalRounds, int currentRound, String currentDrawerId) {
        GameStartedBroadcast broadcast = new GameStartedBroadcast();
        broadcast.roomId = roomId;
        broadcast.totalRounds = totalRounds;
        broadcast.currentRound = currentRound;
        broadcast.currentDrawerId = currentDrawerId;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }

    public String getCurrentDrawerId() { return currentDrawerId; }
    public void setCurrentDrawerId(String currentDrawerId) { this.currentDrawerId = currentDrawerId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
