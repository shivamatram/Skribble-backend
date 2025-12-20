package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for incoming SUBMIT_GUESS messages from clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitGuessMessage {

    @JsonProperty("type")
    private String type;

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("guess")
    private String guess;

    @JsonProperty("timestamp")
    private long timestamp;

    public SubmitGuessMessage() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Get trimmed and normalized guess for comparison.
     */
    public String getNormalizedGuess() {
        return guess != null ? guess.trim().toLowerCase() : "";
    }

    /**
     * Validate required fields.
     */
    public boolean isValid() {
        return roomId != null && !roomId.isEmpty()
                && playerId != null && !playerId.isEmpty()
                && guess != null && !guess.trim().isEmpty();
    }
}
