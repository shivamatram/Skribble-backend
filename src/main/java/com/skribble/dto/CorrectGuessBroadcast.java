package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for CORRECT_GUESS broadcast event sent to all players in room.
 */
public class CorrectGuessBroadcast {

    @JsonProperty("type")
    private final String type = "CORRECT_GUESS";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("guessOrder")
    private int guessOrder;

    @JsonProperty("timestamp")
    private long timestamp;

    public CorrectGuessBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static CorrectGuessBroadcast create(String roomId, String playerId, String playerName, int guessOrder) {
        CorrectGuessBroadcast broadcast = new CorrectGuessBroadcast();
        broadcast.roomId = roomId;
        broadcast.playerId = playerId;
        broadcast.playerName = playerName;
        broadcast.guessOrder = guessOrder;
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getGuessOrder() { return guessOrder; }
    public void setGuessOrder(int guessOrder) { this.guessOrder = guessOrder; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
