package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for broadcasting chat messages to all players.
 */
public class PublicChatBroadcast {
    
    @JsonProperty("type")
    private String type = "PUBLIC_CHAT";
    
    @JsonProperty("playerId")
    private String playerId;
    
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    public PublicChatBroadcast() {}
    
    public PublicChatBroadcast(String playerId, String playerName, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
