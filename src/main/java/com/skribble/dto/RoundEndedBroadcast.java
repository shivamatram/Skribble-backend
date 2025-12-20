package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for ROUND_ENDED broadcast.
 */
public class RoundEndedBroadcast {

    @JsonProperty("type")
    private final String type = "ROUND_ENDED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("reason")
    private String reason; // "time_up", "all_guessed", "drawer_disconnected"

    @JsonProperty("word")
    private String word;

    @JsonProperty("roundNumber")
    private int roundNumber;

    @JsonProperty("leaderboard")
    private List<StateSyncEvent.LeaderboardEntry> leaderboard;

    @JsonProperty("nextRoundIn")
    private long nextRoundInMs;

    @JsonProperty("timestamp")
    private long timestamp;

    public RoundEndedBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static RoundEndedBroadcast create(String roomId, String reason, String word, int roundNumber) {
        RoundEndedBroadcast broadcast = new RoundEndedBroadcast();
        broadcast.roomId = roomId;
        broadcast.reason = reason;
        broadcast.word = word;
        broadcast.roundNumber = roundNumber;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public List<StateSyncEvent.LeaderboardEntry> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<StateSyncEvent.LeaderboardEntry> leaderboard) { this.leaderboard = leaderboard; }

    public long getNextRoundInMs() { return nextRoundInMs; }
    public void setNextRoundInMs(long nextRoundInMs) { this.nextRoundInMs = nextRoundInMs; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
