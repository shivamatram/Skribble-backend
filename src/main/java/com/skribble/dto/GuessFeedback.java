package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for GUESS_FEEDBACK event sent only to the guessing player.
 */
public class GuessFeedback {

    @JsonProperty("type")
    private final String type = "GUESS_FEEDBACK";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("correct")
    private boolean correct;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private long timestamp;

    public GuessFeedback() {
        this.timestamp = System.currentTimeMillis();
    }

    public static GuessFeedback incorrect(String roomId, String message) {
        GuessFeedback feedback = new GuessFeedback();
        feedback.roomId = roomId;
        feedback.correct = false;
        feedback.message = message;
        return feedback;
    }

    public static GuessFeedback correct(String roomId, String message) {
        GuessFeedback feedback = new GuessFeedback();
        feedback.roomId = roomId;
        feedback.correct = true;
        feedback.message = message;
        return feedback;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
