package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for SCORE_UPDATE broadcast event sent to all players in room.
 */
public class ScoreUpdateBroadcast {

    @JsonProperty("type")
    private final String type = "SCORE_UPDATE";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("pointsAwarded")
    private int pointsAwarded;

    @JsonProperty("totalScore")
    private int totalScore;

    @JsonProperty("guessOrder")
    private int guessOrder;

    @JsonProperty("timestamp")
    private long timestamp;

    public ScoreUpdateBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static ScoreUpdateBroadcast create(String roomId, String playerId, String playerName,
                                               int pointsAwarded, int totalScore, int guessOrder) {
        ScoreUpdateBroadcast broadcast = new ScoreUpdateBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.pointsAwarded = pointsAwarded;
        broadcast.totalScore = totalScore;
        broadcast.guessOrder = guessOrder;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(int pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getGuessOrder() { return guessOrder; }
    public void setGuessOrder(int guessOrder) { this.guessOrder = guessOrder; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
