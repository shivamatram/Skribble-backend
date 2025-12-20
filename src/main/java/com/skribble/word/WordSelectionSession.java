package com.skribble.word;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * Represents an active word selection session for a room.
 * Contains word options and tracks selection state.
 * Immutable word options for security.
 */
public class WordSelectionSession {

    private final String roomId;
    private final String drawerId;
    private final List<String> wordOptions;
    private final Set<String> wordOptionsSet;
    private final int timeoutSeconds;
    private final long createdAt;
    
    private volatile String selectedWord;
    private volatile boolean autoSelected;
    private volatile ScheduledFuture<?> timeoutFuture;

    public WordSelectionSession(String roomId, String drawerId, List<String> wordOptions, int timeoutSeconds) {
        this.roomId = roomId;
        this.drawerId = drawerId;
        this.wordOptions = Collections.unmodifiableList(wordOptions);
        this.wordOptionsSet = Collections.unmodifiableSet(new HashSet<>(wordOptions));
        this.timeoutSeconds = timeoutSeconds;
        this.createdAt = System.currentTimeMillis();
        this.autoSelected = false;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getDrawerId() {
        return drawerId;
    }

    /**
     * Get word options (immutable).
     * @return Unmodifiable list of word options
     */
    public List<String> getWordOptions() {
        return wordOptions;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Check if a word is a valid option.
     * @param word Word to check (should be normalized)
     * @return true if word is in options
     */
    public boolean isValidOption(String word) {
        return word != null && wordOptionsSet.contains(word.toLowerCase().trim());
    }

    /**
     * Check if word has been selected.
     * @return true if selection is complete
     */
    public boolean isWordSelected() {
        return selectedWord != null;
    }

    /**
     * Get selected word.
     * @return Selected word or null if not yet selected
     */
    public String getSelectedWord() {
        return selectedWord;
    }

    /**
     * Set selected word (internal use).
     * @param word The selected word
     */
    void setSelectedWord(String word) {
        this.selectedWord = word;
    }

    /**
     * Check if word was auto-selected due to timeout.
     * @return true if auto-selected
     */
    public boolean isAutoSelected() {
        return autoSelected;
    }

    /**
     * Mark selection as auto-selected (internal use).
     * @param autoSelected true if auto-selected
     */
    void setAutoSelected(boolean autoSelected) {
        this.autoSelected = autoSelected;
    }

    /**
     * Set timeout future (internal use).
     * @param future Scheduled timeout future
     */
    void setTimeoutFuture(ScheduledFuture<?> future) {
        this.timeoutFuture = future;
    }

    /**
     * Cancel the timeout timer.
     */
    void cancelTimeout() {
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(false);
        }
    }

    /**
     * Get remaining time for selection in milliseconds.
     * @return Remaining time or 0 if expired
     */
    public long getRemainingTimeMs() {
        long elapsed = System.currentTimeMillis() - createdAt;
        long timeoutMs = timeoutSeconds * 1000L;
        return Math.max(0, timeoutMs - elapsed);
    }
}
