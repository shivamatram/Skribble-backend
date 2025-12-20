package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for incoming DRAW_STROKE messages from clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrawStrokeMessage {

    @JsonProperty("type")
    private String type;

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("playerId")
    private String playerId;

    @JsonProperty("points")
    private List<Point> points;

    @JsonProperty("color")
    private String color;

    @JsonProperty("strokeWidth")
    private float strokeWidth;

    public DrawStrokeMessage() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    /**
     * Validate required fields.
     */
    public boolean isValid() {
        return roomId != null && !roomId.isEmpty()
                && playerId != null && !playerId.isEmpty()
                && points != null && !points.isEmpty()
                && color != null && !color.isEmpty()
                && strokeWidth > 0;
    }

    /**
     * Inner class representing a 2D point.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        @JsonProperty("x")
        private float x;

        @JsonProperty("y")
        private float y;

        public Point() {}

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() { return x; }
        public void setX(float x) { this.x = x; }

        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
    }
}
