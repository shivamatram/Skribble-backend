package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebSocket event: WORD_SELECTION_STARTED
 * Broadcast to ALL players (except drawer) when word selection phase begins.
 * Informs guessers that the drawer is choosing a word.
 */
public class WordSelectionStartedBroadcast {

    @JsonProperty("type")
    private final String type = "WORD_SELECTION_STARTED";

    @JsonProperty("payload")
    private Payload payload;

    public WordSelectionStartedBroadcast() {
    }

    public static WordSelectionStartedBroadcast create(String roomId, String drawerId, 
                                                        String drawerName, int selectionTimeout,
                                                        int roundNumber) {
        WordSelectionStartedBroadcast broadcast = new WordSelectionStartedBroadcast();
        broadcast.payload = new Payload(roomId, drawerId, drawerName, selectionTimeout, roundNumber);
        return broadcast;
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

        @JsonProperty("drawerId")
        private String drawerId;

        @JsonProperty("drawerName")
        private String drawerName;

        @JsonProperty("selectionTimeout")
        private int selectionTimeout;

        @JsonProperty("roundNumber")
        private int roundNumber;

        public Payload() {
        }

        public Payload(String roomId, String drawerId, String drawerName, 
                       int selectionTimeout, int roundNumber) {
            this.roomId = roomId;
            this.drawerId = drawerId;
            this.drawerName = drawerName;
            this.selectionTimeout = selectionTimeout;
            this.roundNumber = roundNumber;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
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

        public int getSelectionTimeout() {
            return selectionTimeout;
        }

        public void setSelectionTimeout(int selectionTimeout) {
            this.selectionTimeout = selectionTimeout;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
        }
    }
}
