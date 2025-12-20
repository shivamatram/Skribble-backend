package com.skribble.reconnect;

/**
 * Stores state of a disconnected player for reconnection.
 */
public class DisconnectedPlayerInfo {

    private final String playerId;
    private final String playerName;
    private final String roomId;
    private final int score;
    private final boolean wasDrawer;
    private final boolean hadGuessedCorrectly;
    private final long disconnectTime;
    private final String sessionToken;

    public DisconnectedPlayerInfo(String playerId, String playerName, String roomId,
                                   int score, boolean wasDrawer, boolean hadGuessedCorrectly,
                                   String sessionToken) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.roomId = roomId;
        this.score = score;
        this.wasDrawer = wasDrawer;
        this.hadGuessedCorrectly = hadGuessedCorrectly;
        this.disconnectTime = System.currentTimeMillis();
        this.sessionToken = sessionToken;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getScore() {
        return score;
    }

    public boolean wasDrawer() {
        return wasDrawer;
    }

    public boolean hadGuessedCorrectly() {
        return hadGuessedCorrectly;
    }

    public long getDisconnectTime() {
        return disconnectTime;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public boolean isExpired(long reconnectWindowMs) {
        return System.currentTimeMillis() - disconnectTime > reconnectWindowMs;
    }

    public long getRemainingTimeMs(long reconnectWindowMs) {
        long elapsed = System.currentTimeMillis() - disconnectTime;
        return Math.max(0, reconnectWindowMs - elapsed);
    }
}
