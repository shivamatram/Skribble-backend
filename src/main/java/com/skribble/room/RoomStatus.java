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
     * Game is in progress, rounds are being played.
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
