package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for ROOM_ASSIGNED event sent to the joining player.
 */
public class RoomAssignedEvent {

    @JsonProperty("type")
    private final String type = "ROOM_ASSIGNED";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("isNewRoom")
    private boolean isNewRoom;

    @JsonProperty("isHost")
    private boolean isHost;

    @JsonProperty("timestamp")
    private long timestamp;

    public RoomAssignedEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public static RoomAssignedEvent create(String roomId, String playerId, String playerName, boolean isHost) {
        RoomAssignedEvent event = new RoomAssignedEvent();
        event.roomId = roomId;
        event.playerId = playerId;
        event.playerName = playerName;
        event.isHost = isHost;
        event.isNewRoom = isHost; // Keep isNewRoom for backwards compatibility
        return event;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public boolean isNewRoom() { return isNewRoom; }
    public void setNewRoom(boolean newRoom) { isNewRoom = newRoom; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
