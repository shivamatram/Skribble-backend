package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for broadcasting word selection started event.
 * Sent to drawer with word options, sent to others without.
 */
public class WordSelectionStartedMessage {
    
    @JsonProperty("type")
    private String type = "WORD_SELECTION_STARTED";
    
    @JsonProperty("drawerId")
    private String drawerId;
    
    @JsonProperty("drawerName")
    private String drawerName;
    
    @JsonProperty("wordOptions")
    private List<String> wordOptions; // Only sent to the drawer
    
    @JsonProperty("timeLimit")
    private int timeLimit; // Time to select word (e.g., 10 seconds)
    
    @JsonProperty("round")
    private int round;
    
    @JsonProperty("totalRounds")
    private int totalRounds;
    
    public WordSelectionStartedMessage() {}
    
    // Constructor for drawer (with word options)
    public WordSelectionStartedMessage(String drawerId, String drawerName, List<String> wordOptions, 
                                       int timeLimit, int round, int totalRounds) {
        this.drawerId = drawerId;
        this.drawerName = drawerName;
        this.wordOptions = wordOptions;
        this.timeLimit = timeLimit;
        this.round = round;
        this.totalRounds = totalRounds;
    }
    
    // Factory for drawer message (with word options)
    public static WordSelectionStartedMessage forDrawer(String drawerId, String drawerName, 
                                                        List<String> wordOptions, int timeLimit,
                                                        int round, int totalRounds) {
        return new WordSelectionStartedMessage(drawerId, drawerName, wordOptions, timeLimit, round, totalRounds);
    }
    
    // Factory for guesser message (without word options)
    public static WordSelectionStartedMessage forGuesser(String drawerId, String drawerName,
                                                         int timeLimit, int round, int totalRounds) {
        return new WordSelectionStartedMessage(drawerId, drawerName, null, timeLimit, round, totalRounds);
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDrawerId() {
        return drawerId;
    }
    
    public void setDrawerId(String drawerId) {
        this.drawerId = drawerId;
    }
    
    public String getDrawerName() {
        return drawerName;
    }
    
    public void setDrawerName(String drawerName) {
        this.drawerName = drawerName;
    }
    
    public List<String> getWordOptions() {
        return wordOptions;
    }
    
    public void setWordOptions(List<String> wordOptions) {
        this.wordOptions = wordOptions;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
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
