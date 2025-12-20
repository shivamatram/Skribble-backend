package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebSocket message: WORD_SELECTED
 * Sent from drawer to server when they select a word.
 * Server validates this selection before confirming.
 */
public class WordSelectedMessage {

    @JsonProperty("type")
    private String type = "WORD_SELECTED";

    @JsonProperty("payload")
    private Payload payload;

    public WordSelectedMessage() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    /**
     * Check if message has all required fields.
     */
    public boolean isValid() {
        return payload != null 
                && payload.roomId != null && !payload.roomId.isEmpty()
                && payload.playerId != null && !payload.playerId.isEmpty()
                && payload.selectedWord != null && !payload.selectedWord.isEmpty();
    }

    /**
     * Get room ID from payload.
     */
    public String getRoomId() {
        return payload != null ? payload.roomId : null;
    }

    /**
     * Get player ID from payload.
     */
    public String getPlayerId() {
        return payload != null ? payload.playerId : null;
    }

    /**
     * Get selected word from payload (normalized to lowercase).
     */
    public String getSelectedWord() {
        if (payload == null || payload.selectedWord == null) {
            return null;
        }
        return payload.selectedWord.toLowerCase().trim();
    }

    public static class Payload {
        @JsonProperty("roomId")
        private String roomId;

        @JsonProperty("playerId")
        private String playerId;

        @JsonProperty("selectedWord")
        private String selectedWord;

        public Payload() {
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public String getSelectedWord() {
            return selectedWord;
        }

        public void setSelectedWord(String selectedWord) {
            this.selectedWord = selectedWord;
        }
    }
}
