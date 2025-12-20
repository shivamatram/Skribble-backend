package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for broadcasting timer ticks to all players.
 * Used for round timer and word selection timer.
 */
public class TimerTickBroadcast {
    
    @JsonProperty("type")
    private String type = "TIMER_TICK";
    
    @JsonProperty("timeRemaining")
    private int timeRemaining;
    
    @JsonProperty("totalTime")
    private int totalTime;
    
    @JsonProperty("timerType")
    private String timerType; // "ROUND" or "WORD_SELECTION"
    
    @JsonProperty("round")
    private int round;
    
    @JsonProperty("totalRounds")
    private int totalRounds;
    
    public TimerTickBroadcast() {}
    
    public TimerTickBroadcast(int timeRemaining, int totalTime, String timerType, int round, int totalRounds) {
        this.timeRemaining = timeRemaining;
        this.totalTime = totalTime;
        this.timerType = timerType;
        this.round = round;
        this.totalRounds = totalRounds;
    }
    
    // Static factory methods for convenience
    public static TimerTickBroadcast roundTimer(int timeRemaining, int totalTime, int round, int totalRounds) {
        return new TimerTickBroadcast(timeRemaining, totalTime, "ROUND", round, totalRounds);
    }
    
    public static TimerTickBroadcast wordSelectionTimer(int timeRemaining, int totalTime, int round, int totalRounds) {
        return new TimerTickBroadcast(timeRemaining, totalTime, "WORD_SELECTION", round, totalRounds);
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getTimeRemaining() {
        return timeRemaining;
    }
    
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    
    public int getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
    
    public String getTimerType() {
        return timerType;
    }
    
    public void setTimerType(String timerType) {
        this.timerType = timerType;
    }
    
    public int getRound() {
        return round;
    }
    
    public void setRound(int round) {
        this.round = round;
    }
    
    public int getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }
}
