package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for FINAL_RESULTS broadcast with complete game results.
 */
public class FinalResultsBroadcast {

    @JsonProperty("type")
    private final String type = "FINAL_RESULTS";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("winners")
    private List<WinnerInfo> winners; // Can have multiple in case of tie

    @JsonProperty("rankings")
    private List<PlayerResult> rankings;

    @JsonProperty("totalRounds")
    private int totalRounds;

    @JsonProperty("gameDurationMs")
    private long gameDurationMs;

    @JsonProperty("roomCloseDelayMs")
    private long roomCloseDelayMs; // Time until room closes

    @JsonProperty("timestamp")
    private long timestamp;

    public FinalResultsBroadcast() {
        this.timestamp = System.currentTimeMillis();
        this.winners = new ArrayList<>();
        this.rankings = new ArrayList<>();
    }

    public static FinalResultsBroadcast create(String roomId, int totalRounds, long gameDurationMs, long roomCloseDelayMs) {
        FinalResultsBroadcast broadcast = new FinalResultsBroadcast();
        broadcast.roomId = roomId;
        broadcast.totalRounds = totalRounds;
        broadcast.gameDurationMs = gameDurationMs;
        broadcast.roomCloseDelayMs = roomCloseDelayMs;
        return broadcast;
    }

    /**
     * Nested class for winner information.
     */
    public static class WinnerInfo {
        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("playerName")
        private String playerName;

        @JsonProperty("score")
        private int score;

        @JsonProperty("isTie")
        private boolean isTie;

        public WinnerInfo() {}

        public WinnerInfo(String playerId, String playerName, int score, boolean isTie) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
            this.isTie = isTie;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public boolean isTie() { return isTie; }
        public void setTie(boolean tie) { isTie = tie; }
    }

    /**
     * Nested class for player result in rankings.
     */
    public static class PlayerResult {
        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("playerName")
        private String playerName;

        @JsonProperty("score")
        private int score;

        @JsonProperty("rank")
        private int rank;

        @JsonProperty("correctGuesses")
        private int correctGuesses;

        @JsonProperty("isWinner")
        private boolean isWinner;

        public PlayerResult() {}

        public PlayerResult(String playerId, String playerName, int score, int rank, 
                           int correctGuesses, boolean isWinner) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
            this.rank = rank;
            this.correctGuesses = correctGuesses;
            this.isWinner = isWinner;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public int getCorrectGuesses() { return correctGuesses; }
        public void setCorrectGuesses(int correctGuesses) { this.correctGuesses = correctGuesses; }

        public boolean isWinner() { return isWinner; }
        public void setWinner(boolean winner) { isWinner = winner; }
    }

    // Getters and setters
    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public List<WinnerInfo> getWinners() { return winners; }
    public void setWinners(List<WinnerInfo> winners) { this.winners = winners; }

    public void addWinner(WinnerInfo winner) { this.winners.add(winner); }

    public List<PlayerResult> getRankings() { return rankings; }
    public void setRankings(List<PlayerResult> rankings) { this.rankings = rankings; }

    public void addRanking(PlayerResult result) { this.rankings.add(result); }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public long getGameDurationMs() { return gameDurationMs; }
    public void setGameDurationMs(long gameDurationMs) { this.gameDurationMs = gameDurationMs; }

    public long getRoomCloseDelayMs() { return roomCloseDelayMs; }
    public void setRoomCloseDelayMs(long roomCloseDelayMs) { this.roomCloseDelayMs = roomCloseDelayMs; }

    public long getTimestamp() { return timestamp; }
}
