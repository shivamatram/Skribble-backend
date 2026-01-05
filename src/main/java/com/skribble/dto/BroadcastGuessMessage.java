package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for broadcasting a player's guess to the room.
 */
public class BroadcastGuessMessage {

    @JsonProperty("type")
    private String type = "GUESS_RESULT"; // Reuse client expectation

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("guess")
    private String guess;

    @JsonProperty("correct")
    private boolean correct;

    @JsonProperty("close")
    private boolean close;

    public BroadcastGuessMessage() {}

    public BroadcastGuessMessage(String playerId, String guess, boolean correct, boolean close) {
        this.playerId = playerId;
        this.guess = guess;
        this.correct = correct;
        this.close = close;
    }

    public String getType() { return type; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public boolean isClose() { return close; }
    public void setClose(boolean close) { this.close = close; }
}