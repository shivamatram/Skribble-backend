package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebSocket event: WORD_CONFIRMED
 * Broadcast to ALL players when the drawer has selected a word.
 * Does NOT reveal the actual word - only confirms selection is complete.
 * 
 * Guessers receive word hint (underscores), drawer receives the actual word.
 */
public class WordConfirmedBroadcast {

    @JsonProperty("type")
    private final String type = "WORD_CONFIRMED";

    @JsonProperty("payload")
    private Payload payload;

    public WordConfirmedBroadcast() {
    }

    /**
     * Create broadcast for guessers (with word hint).
     */
    public static WordConfirmedBroadcast createForGuessers(String roomId, String drawerId, 
                                                            String drawerName, String wordHint,
                                                            int wordLength) {
        WordConfirmedBroadcast broadcast = new WordConfirmedBroadcast();
        broadcast.payload = new Payload(roomId, drawerId, drawerName, wordHint, wordLength, false);
        return broadcast;
    }

    /**
     * Create broadcast for drawer (with actual word).
     */
    public static WordConfirmedBroadcast createForDrawer(String roomId, String drawerId, 
                                                          String drawerName, String actualWord) {
        WordConfirmedBroadcast broadcast = new WordConfirmedBroadcast();
        broadcast.payload = new Payload(roomId, drawerId, drawerName, actualWord, actualWord.length(), true);
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

        @JsonProperty("wordHint")
        private String wordHint;

        @JsonProperty("wordLength")
        private int wordLength;

        @JsonProperty("isDrawer")
        private boolean isDrawer;

        public Payload() {
        }

        public Payload(String roomId, String drawerId, String drawerName, 
                       String wordHint, int wordLength, boolean isDrawer) {
            this.roomId = roomId;
            this.drawerId = drawerId;
            this.drawerName = drawerName;
            this.wordHint = wordHint;
            this.wordLength = wordLength;
            this.isDrawer = isDrawer;
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

        public String getWordHint() {
            return wordHint;
        }

        public void setWordHint(String wordHint) {
            this.wordHint = wordHint;
        }

        public int getWordLength() {
            return wordLength;
        }

        public void setWordLength(int wordLength) {
            this.wordLength = wordLength;
        }

        public boolean isDrawer() {
            return isDrawer;
        }

        public void setDrawer(boolean drawer) {
            isDrawer = drawer;
        }
    }
}
