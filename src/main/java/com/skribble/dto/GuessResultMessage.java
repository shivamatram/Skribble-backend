package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for sending guess result back to the guesser.
 */
public class GuessResultMessage {
    
    @JsonProperty("type")
    private String type = "GUESS_RESULT";
    
    @JsonProperty("correct")
    private boolean correct;
    
    @JsonProperty("close")
    private boolean close;
    
    @JsonProperty("guess")
    private String guess;
    
    @JsonProperty("pointsAwarded")
    private int pointsAwarded;
    
    @JsonProperty("message")
    private String message;
    
    public GuessResultMessage() {}
    
    public GuessResultMessage(boolean correct, boolean close, String guess, int pointsAwarded, String message) {
        this.correct = correct;
        this.close = close;
        this.guess = guess;
        this.pointsAwarded = pointsAwarded;
        this.message = message;
    }
    
    // Static factory methods
    public static GuessResultMessage correct(String guess, int points) {
        return new GuessResultMessage(true, false, guess, points, "Correct! +" + points + " points");
    }
    
    public static GuessResultMessage close(String guess) {
        return new GuessResultMessage(false, true, guess, 0, "Close! Try again.");
    }
    
    public static GuessResultMessage wrong(String guess) {
        return new GuessResultMessage(false, false, guess, 0, null);
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isCorrect() {
        return correct;
    }
    
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
    
    public boolean isClose() {
        return close;
    }
    
    public void setClose(boolean close) {
        this.close = close;
    }
    
    public String getGuess() {
        return guess;
    }
    
    public void setGuess(String guess) {
        this.guess = guess;
    }
    
    public int getPointsAwarded() {
        return pointsAwarded;
    }
    
    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
