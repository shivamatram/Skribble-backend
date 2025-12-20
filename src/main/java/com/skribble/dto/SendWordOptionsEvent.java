package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * WebSocket event: SEND_WORD_OPTIONS
 * Sent ONLY to the drawer at the start of word selection phase.
 * Contains 3 word options for the drawer to choose from.
 * 
 * SECURITY: This message must NEVER be broadcast to guessers.
 */
public class SendWordOptionsEvent {

    @JsonProperty("type")
    private final String type = "SEND_WORD_OPTIONS";

    @JsonProperty("payload")
    private Payload payload;

    public SendWordOptionsEvent() {
    }

    public static SendWordOptionsEvent create(String roomId, List<String> words, int selectionTimeout) {
        SendWordOptionsEvent event = new SendWordOptionsEvent();
        event.payload = new Payload(roomId, words, selectionTimeout);
        return event;
    }

    public String getType() {
        return type;
    }

    public Payload getPayload() {
        return payload;
    }

    public static class Payload {
        @JsonProperty("roomId")
        private String roomId;

        @JsonProperty("words")
        private List<String> words;

        @JsonProperty("selectionTimeout")
        private int selectionTimeout;

        public Payload() {
        }

        public Payload(String roomId, List<String> words, int selectionTimeout) {
            this.roomId = roomId;
            this.words = words;
            this.selectionTimeout = selectionTimeout;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }

        public int getSelectionTimeout() {
            return selectionTimeout;
        }

        public void setSelectionTimeout(int selectionTimeout) {
            this.selectionTimeout = selectionTimeout;
        }
    }
}
