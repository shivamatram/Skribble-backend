package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for STATE_SYNC event sent to reconnecting players.
 */
public class StateSyncEvent {

    @JsonProperty("type")
    private final String type = "STATE_SYNC";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("role")
    private String role; // "drawer" or "guesser"

    @JsonProperty("score")
    private int score;

    @JsonProperty("hasGuessedCorrectly")
    private boolean hasGuessedCorrectly;

    @JsonProperty("currentDrawerId")
    private String currentDrawerId;

    @JsonProperty("currentDrawerName")
    private String currentDrawerName;

    @JsonProperty("wordHint")
    private String wordHint; // Masked word for guessers (e.g., "_ _ _ _ _")

    @JsonProperty("timerRemainingMs")
    private long timerRemainingMs;

    @JsonProperty("roundNumber")
    private int roundNumber;

    @JsonProperty("totalRounds")
    private int totalRounds;

    @JsonProperty("leaderboard")
    private List<LeaderboardEntry> leaderboard;

    @JsonProperty("playerCount")
    private int playerCount;

    @JsonProperty("maxPlayers")
    private int maxPlayers;

    @JsonProperty("timestamp")
    private long timestamp;

    public StateSyncEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public static StateSyncEvent create(String roomId, String playerId, String playerName) {
        StateSyncEvent event = new StateSyncEvent();
        event.roomId = roomId;
        event.playerId = playerId;
        event.playerName = playerName;
        return event;
    }

    // Nested class for leaderboard entries
    public static class LeaderboardEntry {
        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("playerName")
        private String playerName;

        @JsonProperty("score")
        private int score;

        @JsonProperty("rank")
        private int rank;

        @JsonProperty("hasGuessedCorrectly")
        private boolean hasGuessedCorrectly;

        public LeaderboardEntry() {}

        public LeaderboardEntry(String playerId, String playerName, int score, int rank, boolean hasGuessedCorrectly) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
            this.rank = rank;
            this.hasGuessedCorrectly = hasGuessedCorrectly;
        }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public boolean isHasGuessedCorrectly() { return hasGuessedCorrectly; }
        public void setHasGuessedCorrectly(boolean hasGuessedCorrectly) { this.hasGuessedCorrectly = hasGuessedCorrectly; }
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isHasGuessedCorrectly() { return hasGuessedCorrectly; }
    public void setHasGuessedCorrectly(boolean hasGuessedCorrectly) { this.hasGuessedCorrectly = hasGuessedCorrectly; }

    public String getCurrentDrawerId() { return currentDrawerId; }
    public void setCurrentDrawerId(String currentDrawerId) { this.currentDrawerId = currentDrawerId; }

    public String getCurrentDrawerName() { return currentDrawerName; }
    public void setCurrentDrawerName(String currentDrawerName) { this.currentDrawerName = currentDrawerName; }

    public String getWordHint() { return wordHint; }
    public void setWordHint(String wordHint) { this.wordHint = wordHint; }

    public long getTimerRemainingMs() { return timerRemainingMs; }
    public void setTimerRemainingMs(long timerRemainingMs) { this.timerRemainingMs = timerRemainingMs; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }

    public List<LeaderboardEntry> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<LeaderboardEntry> leaderboard) { this.leaderboard = leaderboard; }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
