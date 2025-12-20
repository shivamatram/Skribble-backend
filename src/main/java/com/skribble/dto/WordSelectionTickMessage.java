package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for word selection timer tick (10 second countdown for drawer to choose).
 */
public class WordSelectionTickMessage {
    
    @JsonProperty("type")
    private String type = "WORD_SELECTION_TICK";
    
    @JsonProperty("timeRemaining")
    private int timeRemaining;
    
    @JsonProperty("totalTime")
    private int totalTime;
    
    public WordSelectionTickMessage() {}
    
    public WordSelectionTickMessage(int timeRemaining, int totalTime) {
        this.timeRemaining = timeRemaining;
        this.totalTime = totalTime;
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
}
