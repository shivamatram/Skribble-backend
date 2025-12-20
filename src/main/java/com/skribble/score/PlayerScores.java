package com.skribble.score;

import com.skribble.dto.LeaderboardUpdateBroadcast.LeaderboardEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe player score tracking for a room.
 */
public class PlayerScores {

    // Player ID -> Total Score
    private final Map<String, AtomicInteger> scores = new ConcurrentHashMap<>();
    
    // Player ID -> Player Name
    private final Map<String, String> playerNames = new ConcurrentHashMap<>();
    
    // Players who have scored this round (to prevent double scoring)
    private final Set<String> scoredThisRound = ConcurrentHashMap.newKeySet();
    
    // Track if drawer has received bonus this round
    private volatile boolean drawerBonusAwarded = false;

    /**
     * Add a player to tracking.
     */
    public void addPlayer(String playerId, String playerName) {
        scores.putIfAbsent(playerId, new AtomicInteger(0));
        playerNames.put(playerId, playerName);
    }

    /**
     * Remove a player from tracking.
     */
    public void removePlayer(String playerId) {
        scores.remove(playerId);
        playerNames.remove(playerId);
        scoredThisRound.remove(playerId);
    }

    /**
     * Check if player has already scored this round.
     */
    public boolean hasPlayerScoredThisRound(String playerId) {
        return scoredThisRound.contains(playerId);
    }

    /**
     * Award score to a player. Thread-safe.
     * Returns the new total score, or -1 if player already scored this round.
     */
    public int awardScore(String playerId, int points) {
        // Prevent double scoring
        if (!scoredThisRound.add(playerId)) {
            return -1; // Already scored
        }
        
        AtomicInteger score = scores.get(playerId);
        if (score == null) {
            scores.putIfAbsent(playerId, new AtomicInteger(0));
            score = scores.get(playerId);
        }
        
        return score.addAndGet(points);
    }

    /**
     * Award drawer bonus. Returns new total or -1 if already awarded.
     */
    public int awardDrawerBonus(String drawerId, int bonus) {
        if (drawerBonusAwarded) {
            return -1;
        }
        drawerBonusAwarded = true;
        
        AtomicInteger score = scores.get(drawerId);
        if (score == null) {
            scores.putIfAbsent(drawerId, new AtomicInteger(0));
            score = scores.get(drawerId);
        }
        
        return score.addAndGet(bonus);
    }

    /**
     * Check if drawer bonus has been awarded this round.
     */
    public boolean isDrawerBonusAwarded() {
        return drawerBonusAwarded;
    }

    /**
     * Get player's current total score.
     */
    public int getScore(String playerId) {
        AtomicInteger score = scores.get(playerId);
        return score != null ? score.get() : 0;
    }

    /**
     * Set player's total score (used for reconnection state restore).
     */
    public void setScore(String playerId, int newScore) {
        AtomicInteger score = scores.get(playerId);
        if (score == null) {
            scores.putIfAbsent(playerId, new AtomicInteger(newScore));
        } else {
            score.set(newScore);
        }
    }

    /**
     * Get player name.
     */
    public String getPlayerName(String playerId) {
        return playerNames.getOrDefault(playerId, playerId);
    }

    /**
     * Update player name.
     */
    public void setPlayerName(String playerId, String playerName) {
        playerNames.put(playerId, playerName);
    }

    /**
     * Get the leaderboard sorted by score (descending).
     * 
     * @param playersWhoGuessedCorrectly Set of player IDs who guessed correctly this round
     * @return Sorted list of leaderboard entries
     */
    public List<LeaderboardEntry> getLeaderboard(Set<String> playersWhoGuessedCorrectly) {
        List<LeaderboardEntry> entries = scores.entrySet().stream()
                .map(entry -> new LeaderboardEntry(
                        0, // rank will be set after sorting
                        entry.getKey(),
                        playerNames.getOrDefault(entry.getKey(), entry.getKey()),
                        entry.getValue().get(),
                        playersWhoGuessedCorrectly.contains(entry.getKey())
                ))
                .sorted(Comparator.comparingInt(LeaderboardEntry::getScore).reversed())
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }

    /**
     * Get all player IDs.
     */
    public Set<String> getAllPlayerIds() {
        return Collections.unmodifiableSet(scores.keySet());
    }

    /**
     * Reset round-specific state (for new round).
     */
    public void resetRound() {
        scoredThisRound.clear();
        drawerBonusAwarded = false;
    }

    /**
     * Reset all scores (for new game).
     */
    public void resetAllScores() {
        for (AtomicInteger score : scores.values()) {
            score.set(0);
        }
        scoredThisRound.clear();
        drawerBonusAwarded = false;
    }

    /**
     * Get number of players who scored this round.
     */
    public int getPlayersWhoScoredCount() {
        return scoredThisRound.size();
    }
}
