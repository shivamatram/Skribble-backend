package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for GAME_ENDED broadcast when the game finishes.
 */
public class GameEndedBroadcast {

    @JsonProperty("type")
    private final String type = "GAME_ENDED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("reason")
    private String reason; // "all_rounds_completed", "manual_termination", "insufficient_players"

    @JsonProperty("totalRounds")
    private int totalRounds;

    @JsonProperty("finalRound")
    private int finalRound;

    @JsonProperty("gameDurationMs")
    private long gameDurationMs;

    @JsonProperty("resultsDelayMs")
    private long resultsDelayMs; // Time until FINAL_RESULTS is sent

    @JsonProperty("timestamp")
    private long timestamp;

    public GameEndedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static GameEndedBroadcast create(String roomId, String reason, int totalRounds, 
                                             int finalRound, long gameDurationMs, long resultsDelayMs) {
        GameEndedBroadcast broadcast = new GameEndedBroadcast();
        broadcast.roomId = roomId;
        broadcast.reason = reason;
        broadcast.totalRounds = totalRounds;
        broadcast.finalRound = finalRound;
        broadcast.gameDurationMs = gameDurationMs;
        broadcast.resultsDelayMs = resultsDelayMs;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public int getFinalRound() { return finalRound; }
    public void setFinalRound(int finalRound) { this.finalRound = finalRound; }

    public long getGameDurationMs() { return gameDurationMs; }
    public void setGameDurationMs(long gameDurationMs) { this.gameDurationMs = gameDurationMs; }

    public long getResultsDelayMs() { return resultsDelayMs; }
    public void setResultsDelayMs(long resultsDelayMs) { this.resultsDelayMs = resultsDelayMs; }

    public long getTimestamp() { return timestamp; }
}
