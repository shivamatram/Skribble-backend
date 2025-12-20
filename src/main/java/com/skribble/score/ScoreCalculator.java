package com.skribble.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Server-authoritative score calculator for Skribble.
 * All score calculations happen exclusively on the server.
 */
@Component
public class ScoreCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ScoreCalculator.class);

    // Scoring constants
    private static final int MIN_SCORE = 10;
    private static final int TIME_MULTIPLIER = 10;
    private static final int DRAWER_BONUS = 50;

    /**
     * Calculate score for a correct guess.
     * Formula: score = max(10, timeLeftSeconds × 10)
     *
     * @param timeLeftMs Time remaining in milliseconds
     * @return Calculated score
     */
    public int calculateGuessScore(long timeLeftMs) {
        int timeLeftSeconds = (int) (timeLeftMs / 1000);
        int score = Math.max(MIN_SCORE, timeLeftSeconds * TIME_MULTIPLIER);
        
        logger.debug("Calculated guess score: timeLeftMs={}, timeLeftSeconds={}, score={}",
                timeLeftMs, timeLeftSeconds, score);
        
        return score;
    }

    /**
     * Calculate drawer bonus when at least one player guesses correctly.
     *
     * @return Drawer bonus points
     */
    public int calculateDrawerBonus() {
        return DRAWER_BONUS;
    }

    /**
     * Get minimum score.
     */
    public int getMinScore() {
        return MIN_SCORE;
    }

    /**
     * Get time multiplier.
     */
    public int getTimeMultiplier() {
        return TIME_MULTIPLIER;
    }

    /**
     * Get drawer bonus amount.
     */
    public int getDrawerBonusAmount() {
        return DRAWER_BONUS;
    }
}
