package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for outgoing stroke broadcast to other players.
 */
public class StrokeBroadcast {

    @JsonProperty("type")
    private final String type = "STROKE_BROADCAST";

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("drawerId")
    private String drawerId;

    @JsonProperty("points")
    private List<DrawStrokeMessage.Point> points;

    @JsonProperty("color")
    private String color;

    @JsonProperty("strokeWidth")
    private float strokeWidth;

    @JsonProperty("timestamp")
    private long timestamp;

    public StrokeBroadcast() {
        this.timestamp = System.currentTimeMillis();
    }

    public static StrokeBroadcast fromDrawStroke(DrawStrokeMessage stroke) {
        StrokeBroadcast broadcast = new StrokeBroadcast();
        broadcast.roomId = stroke.getRoomId();
        broadcast.drawerId = stroke.getPlayerId();
        broadcast.points = stroke.getPoints();
        broadcast.color = stroke.getColor();
        broadcast.strokeWidth = stroke.getStrokeWidth();
        return broadcast;
    }

    public String getType() { return type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getDrawerId() { return drawerId; }
    public void setDrawerId(String drawerId) { this.drawerId = drawerId; }

    public List<DrawStrokeMessage.Point> getPoints() { return points; }
    public void setPoints(List<DrawStrokeMessage.Point> points) { this.points = points; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
