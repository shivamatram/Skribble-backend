package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for LEADERBOARD_UPDATE broadcast event sent to all players in room.
 */
public class LeaderboardUpdateBroadcast {

    @JsonProperty("type")
    private final String type = "LEADERBOARD_UPDATE";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("leaderboard")
    private List<LeaderboardEntry> leaderboard;

    @JsonProperty("timestamp")
    private long timestamp;

    public LeaderboardUpdateBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static LeaderboardUpdateBroadcast create(String roomId, List<LeaderboardEntry> leaderboard) {
        LeaderboardUpdateBroadcast broadcast = new LeaderboardUpdateBroadcast();
        broadcast.roomId = roomId;
        broadcast.leaderboard = leaderboard;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public List<LeaderboardEntry> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<LeaderboardEntry> leaderboard) { this.leaderboard = leaderboard; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Entry in the leaderboard.
     */
    public static class LeaderboardEntry {
        @JsonProperty("rank")
        private int rank;

        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("playerName")
        private String playerName;

        @JsonProperty("score")
        private int score;

        @JsonProperty("hasGuessedThisRound")
        private boolean hasGuessedThisRound;

        public LeaderboardEntry() {}

        public LeaderboardEntry(int rank, String playerId, String playerName, int score, boolean hasGuessedThisRound) {
            this.rank = rank;
            this.playerId = playerId;
            this.playerName = playerName;
            this.score = score;
            this.hasGuessedThisRound = hasGuessedThisRound;
        }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }

        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public boolean isHasGuessedThisRound() { return hasGuessedThisRound; }
        public void setHasGuessedThisRound(boolean hasGuessedThisRound) { this.hasGuessedThisRound = hasGuessedThisRound; }
    }
}
