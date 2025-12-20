package com.skribble.room;

/**
 * Enum representing the various states a game room can be in.
 */
public enum RoomStatus {
    /**
     * Room is waiting for players to join. Game has not started.
     */
    WAITING,
    
    /**
     * Minimum players reached, game is about to start.
     */
    STARTING,
    
    /**
     * Drawer is selecting a word. Guessers wait.
     * Flow: WAITING → WORD_SELECTION → DRAWING → ROUND_OVER
     */
    WORD_SELECTION,
    
    /**
     * Game is in progress, drawer is drawing.
     * Previously called IN_PROGRESS.
     */
    DRAWING,
    
    /**
     * Game is in progress, rounds are being played.
     * @deprecated Use DRAWING for clarity. Kept for backwards compatibility.
     */
    IN_PROGRESS,
    
    /**
     * Current round has ended, preparing for next round.
     */
    ROUND_OVER,
    
    /**
     * Game has ended. Room will be cleaned up.
     */
    ENDED;
    
    /**
     * Check if the room is joinable.
     */
    public boolean isJoinable() {
        return this == WAITING || this == IN_PROGRESS;
    }
}
