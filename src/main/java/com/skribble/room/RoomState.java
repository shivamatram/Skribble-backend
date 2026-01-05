package com.skribble.room;

import com.skribble.dto.LeaderboardUpdateBroadcast.LeaderboardEntry;
import com.skribble.score.PlayerScores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe state for a game room.
 */
public class RoomState {

    // Room configuration constants
    public static final int DEFAULT_MAX_PLAYERS = 8;
    public static final int DEFAULT_MIN_PLAYERS = 2;
    public static final int DEFAULT_TOTAL_ROUNDS = 3;
    public static final long DEFAULT_ROUND_DURATION_MS = 80000; // 80 seconds (was 60s)
    public static final long GAME_START_DELAY_MS = 5000;

    private final String roomId;
    private final Set<String> playerIds = ConcurrentHashMap.newKeySet();
    private final Set<String> playersWhoGuessedCorrectly = ConcurrentHashMap.newKeySet();
    private final AtomicInteger correctGuessOrder = new AtomicInteger(0);
    private final PlayerScores playerScores = new PlayerScores();
    private final List<String> drawerOrder = Collections.synchronizedList(new ArrayList<>());
    
    // Room configuration
    private volatile int maxPlayers = DEFAULT_MAX_PLAYERS;
    private volatile int minPlayers = DEFAULT_MIN_PLAYERS;
    private volatile int totalRounds = DEFAULT_TOTAL_ROUNDS;
    
    // Room state
    private volatile RoomStatus status = RoomStatus.WAITING;
    private volatile String hostId; // The player who created the room
    private volatile String currentDrawerId;
    private volatile String currentWord;
    private volatile boolean gameInProgress;
    private volatile long roundStartTime;
    private volatile long roundDurationMs = DEFAULT_ROUND_DURATION_MS;
    private volatile int currentRound = 0;
    private volatile int currentDrawerIndex = 0;
    
    // Game end state
    private volatile long gameStartTime;
    private volatile boolean scoresFrozen = false;
    
    // Lifecycle timestamps for cleanup
    private final long createdTime;
    private volatile long gameEndTime;
    private volatile long lastActivityTime;

    public RoomState(String roomId) {
        this.roomId = roomId;
        this.gameInProgress = false;
        this.createdTime = System.currentTimeMillis();
        this.lastActivityTime = this.createdTime;
    }

    public String getRoomId() {
        return roomId;
    }
    
    // ==================== Host Management ====================
    
    public String getHostId() {
        return hostId;
    }
    
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    public boolean isHost(String playerId) {
        return hostId != null && hostId.equals(playerId);
    }
    
    // ==================== Lifecycle Timestamps ====================
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public long getGameEndTime() {
        return gameEndTime;
    }
    
    public void setGameEndTime(long gameEndTime) {
        this.gameEndTime = gameEndTime;
    }
    
    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    // ==================== Room Status ====================

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public boolean isJoinable() {
        return status.isJoinable() && playerIds.size() < maxPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public boolean hasMinPlayers() {
        return playerIds.size() >= minPlayers;
    }

    public boolean isFull() {
        return playerIds.size() >= maxPlayers;
    }

    public int getAvailableSlots() {
        return maxPlayers - playerIds.size();
    }

    // ==================== Player Management ====================

    public void addPlayer(String playerId) {
        playerIds.add(playerId);
        playerScores.addPlayer(playerId, playerId);
        if (!drawerOrder.contains(playerId)) {
            drawerOrder.add(playerId);
        }
    }

    public void addPlayer(String playerId, String playerName) {
        playerIds.add(playerId);
        playerScores.addPlayer(playerId, playerName);
        if (!drawerOrder.contains(playerId)) {
            drawerOrder.add(playerId);
        }
    }

    public void removePlayer(String playerId) {
        playerIds.remove(playerId);
        playersWhoGuessedCorrectly.remove(playerId);
        playerScores.removePlayer(playerId);
        drawerOrder.remove(playerId);
    }

    public boolean hasPlayer(String playerId) {
        return playerIds.contains(playerId);
    }

    public Set<String> getPlayerIds() {
        return Collections.unmodifiableSet(playerIds);
    }

    public int getPlayerCount() {
        return playerIds.size();
    }

    public String getCurrentDrawerId() {
        return currentDrawerId;
    }

    public void setCurrentDrawerId(String drawerId) {
        this.currentDrawerId = drawerId;
    }

    public boolean isCurrentDrawer(String playerId) {
        return playerId != null && playerId.equals(currentDrawerId);
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String word) {
        this.currentWord = word;
    }

    /**
     * Get normalized current word for comparison (lowercase, trimmed).
     */
    public String getNormalizedCurrentWord() {
        return currentWord != null ? currentWord.trim().toLowerCase() : "";
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(boolean inProgress) {
        this.gameInProgress = inProgress;
    }

    // ==================== Round Timing ====================

    public void startRound(long durationMs) {
        this.roundStartTime = System.currentTimeMillis();
        this.roundDurationMs = durationMs;
        this.playersWhoGuessedCorrectly.clear();
        this.correctGuessOrder.set(0);
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public long getRoundDurationMs() {
        return roundDurationMs;
    }

    public boolean isRoundTimeExpired() {
        if (roundStartTime == 0) {
            return true; // No round started
        }
        return System.currentTimeMillis() > (roundStartTime + roundDurationMs);
    }

    public long getRemainingTimeMs() {
        if (roundStartTime == 0) {
            return 0;
        }
        long remaining = (roundStartTime + roundDurationMs) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    // ==================== Guess Tracking ====================

    /**
     * Check if a player has already guessed correctly this round.
     */
    public boolean hasPlayerGuessedCorrectly(String playerId) {
        return playersWhoGuessedCorrectly.contains(playerId);
    }

    /**
     * Mark a player as having guessed correctly.
     * Returns the order in which they guessed (1st, 2nd, etc.) or -1 if already guessed.
     */
    public int markPlayerGuessedCorrectly(String playerId) {
        if (playersWhoGuessedCorrectly.add(playerId)) {
            return correctGuessOrder.incrementAndGet();
        }
        return -1; // Already guessed
    }

    /**
     * Get the number of players who have guessed correctly.
     */
    public int getCorrectGuessCount() {
        return playersWhoGuessedCorrectly.size();
    }

    /**
     * Get all players who have guessed correctly this round.
     */
    public Set<String> getPlayersWhoGuessedCorrectly() {
        return Collections.unmodifiableSet(playersWhoGuessedCorrectly);
    }

    /**
     * Reset round state for a new round.
     */
    public void resetRound() {
        this.playersWhoGuessedCorrectly.clear();
        this.correctGuessOrder.set(0);
        this.currentWord = null;
        this.currentDrawerId = null;
        this.roundStartTime = 0;
        this.playerScores.resetRound();
    }

    /**
     * Check if all non-drawer players have guessed correctly.
     */
    public boolean haveAllPlayersGuessed() {
        int guessingPlayers = playerIds.size() - 1; // Exclude drawer
        return playersWhoGuessedCorrectly.size() >= guessingPlayers;
    }

    // ==================== Scoring ====================

    /**
     * Get the player scores tracker.
     */
    public PlayerScores getPlayerScores() {
        return playerScores;
    }

    /**
     * Award score to a player. Returns new total or -1 if already scored.
     */
    public int awardScore(String playerId, int points) {
        return playerScores.awardScore(playerId, points);
    }

    /**
     * Award drawer bonus. Returns new total or -1 if already awarded.
     */
    public int awardDrawerBonus(String drawerId, int bonus) {
        return playerScores.awardDrawerBonus(drawerId, bonus);
    }

    /**
     * Check if drawer bonus has been awarded this round.
     */
    public boolean isDrawerBonusAwarded() {
        return playerScores.isDrawerBonusAwarded();
    }

    /**
     * Get player's current total score.
     */
    public int getPlayerScore(String playerId) {
        return playerScores.getScore(playerId);
    }

    /**
     * Get player name.
     */
    public String getPlayerName(String playerId) {
        return playerScores.getPlayerName(playerId);
    }

    /**
     * Set player name.
     */
    public void setPlayerName(String playerId, String playerName) {
        playerScores.setPlayerName(playerId, playerName);
    }

    /**
     * Get leaderboard sorted by score (descending).
     */
    public List<LeaderboardEntry> getLeaderboard() {
        return playerScores.getLeaderboard(playersWhoGuessedCorrectly);
    }

    /**
     * Reset all scores for a new game.
     */
    public void resetAllScores() {
        playerScores.resetAllScores();
    }

    // ==================== Game End State ====================

    /**
     * Get game start time.
     */
    public long getGameStartTime() {
        return gameStartTime;
    }

    /**
     * Set game start time.
     */
    public void setGameStartTime(long gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    /**
     * Calculate total game duration in milliseconds.
     */
    public long getGameDurationMs() {
        if (gameStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - gameStartTime;
    }

    /**
     * Check if scores are frozen (game ended).
     */
    public boolean areScoresFrozen() {
        return scoresFrozen;
    }

    /**
     * Freeze scores (prevents further scoring).
     */
    public void freezeScores() {
        this.scoresFrozen = true;
    }

    /**
     * Check if all rounds have been completed.
     */
    public boolean areAllRoundsCompleted() {
        return currentRound >= totalRounds;
    }

    /**
     * Check if game should end (all rounds completed or insufficient players).
     */
    public boolean shouldGameEnd() {
        return areAllRoundsCompleted() || playerIds.size() < minPlayers;
    }

    /**
     * End the game - transition to ENDED state and freeze scores.
     */
    public void endGame() {
        this.status = RoomStatus.ENDED;
        this.gameInProgress = false;
        this.scoresFrozen = true;
        this.gameEndTime = System.currentTimeMillis();
    }
}
