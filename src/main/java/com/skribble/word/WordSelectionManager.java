package com.skribble.word;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages word selection phase for game rooms.
 * Handles word options, selection timeout, and secure word storage.
 * Thread-safe for concurrent room operations.
 */
@Component
public class WordSelectionManager {

    private static final Logger logger = LoggerFactory.getLogger(WordSelectionManager.class);

    /** Timeout for drawer to select a word (in seconds) */
    public static final int SELECTION_TIMEOUT_SECONDS = 10;

    /** Timeout in milliseconds */
    public static final long SELECTION_TIMEOUT_MS = SELECTION_TIMEOUT_SECONDS * 1000L;

    private final WordBank wordBank;
    
    /** Active word selection sessions per room */
    private final ConcurrentHashMap<String, WordSelectionSession> activeSessions = new ConcurrentHashMap<>();
    
    /** Scheduler for selection timeouts */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public WordSelectionManager(WordBank wordBank) {
        this.wordBank = wordBank;
    }

    /**
     * Start word selection phase for a room.
     * Generates 3 word options and stores them securely.
     * 
     * @param roomId Room identifier
     * @param drawerId Player ID of the drawer
     * @param onTimeout Callback when selection times out (receives auto-selected word)
     * @return WordSelectionSession containing word options
     */
    public WordSelectionSession startWordSelection(String roomId, String drawerId, 
                                                     WordSelectionCallback onTimeout) {
        // Cancel any existing session for this room
        cancelWordSelection(roomId);

        // Generate 3 unique word options
        List<String> wordOptions = wordBank.getWordOptions();
        
        // Create new session
        WordSelectionSession session = new WordSelectionSession(
                roomId, 
                drawerId, 
                wordOptions,
                SELECTION_TIMEOUT_SECONDS
        );
        
        // Store session
        activeSessions.put(roomId, session);
        
        // Schedule timeout
        ScheduledFuture<?> timeoutFuture = scheduler.schedule(() -> {
            handleSelectionTimeout(roomId, onTimeout);
        }, SELECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        
        session.setTimeoutFuture(timeoutFuture);
        
        logger.info("Word selection started: roomId={}, drawerId={}, options={}", 
                roomId, drawerId, wordOptions.size());
        
        return session;
    }

    /**
     * Process word selection from drawer.
     * Validates the selection and returns the result.
     * 
     * @param roomId Room identifier
     * @param playerId Player attempting to select
     * @param selectedWord The word selected
     * @return WordSelectionResult with success/failure info
     */
    public WordSelectionResult selectWord(String roomId, String playerId, String selectedWord) {
        WordSelectionSession session = activeSessions.get(roomId);
        
        // Validate session exists
        if (session == null) {
            logger.warn("Word selection attempted without active session: roomId={}, playerId={}", 
                    roomId, playerId);
            return WordSelectionResult.error("NO_ACTIVE_SESSION", 
                    "No word selection in progress for this room");
        }
        
        // Validate player is the drawer
        if (!session.getDrawerId().equals(playerId)) {
            logger.warn("Non-drawer attempted word selection: roomId={}, playerId={}, drawerId={}", 
                    roomId, playerId, session.getDrawerId());
            return WordSelectionResult.error("NOT_DRAWER", 
                    "Only the drawer can select a word");
        }
        
        // Check if already selected
        if (session.isWordSelected()) {
            logger.warn("Word already selected for session: roomId={}", roomId);
            return WordSelectionResult.error("ALREADY_SELECTED", 
                    "Word has already been selected");
        }
        
        // Validate selected word is one of the options
        String normalizedWord = selectedWord != null ? selectedWord.toLowerCase().trim() : "";
        if (!session.isValidOption(normalizedWord)) {
            logger.warn("Invalid word selection attempted: roomId={}, playerId={}, word={}, options={}", 
                    roomId, playerId, normalizedWord, session.getWordOptions());
            return WordSelectionResult.error("INVALID_WORD", 
                    "Selected word is not one of the available options");
        }
        
        // Mark word as selected
        session.setSelectedWord(normalizedWord);
        
        // Cancel timeout
        session.cancelTimeout();
        
        logger.info("Word selected: roomId={}, drawerId={}, word=***", roomId, playerId);
        
        return WordSelectionResult.success(normalizedWord);
    }

    /**
     * Handle selection timeout - auto-select a word.
     */
    private void handleSelectionTimeout(String roomId, WordSelectionCallback onTimeout) {
        WordSelectionSession session = activeSessions.get(roomId);
        
        if (session == null || session.isWordSelected()) {
            // Session already completed
            return;
        }
        
        // Auto-select first word option
        String autoSelectedWord = session.getWordOptions().get(0);
        session.setSelectedWord(autoSelectedWord);
        session.setAutoSelected(true);
        
        logger.info("Word selection timeout - auto-selected: roomId={}, word=***", roomId);
        
        // Invoke callback
        if (onTimeout != null) {
            try {
                onTimeout.onWordSelected(roomId, session.getDrawerId(), autoSelectedWord, true);
            } catch (Exception e) {
                logger.error("Error in word selection timeout callback: roomId={}, error={}", 
                        roomId, e.getMessage(), e);
            }
        }
    }

    /**
     * Get active session for a room.
     * @param roomId Room identifier
     * @return Optional containing session if exists
     */
    public Optional<WordSelectionSession> getSession(String roomId) {
        return Optional.ofNullable(activeSessions.get(roomId));
    }

    /**
     * Get selected word for a room (if selection is complete).
     * @param roomId Room identifier
     * @return Optional containing selected word
     */
    public Optional<String> getSelectedWord(String roomId) {
        WordSelectionSession session = activeSessions.get(roomId);
        if (session != null && session.isWordSelected()) {
            return Optional.of(session.getSelectedWord());
        }
        return Optional.empty();
    }

    /**
     * Check if word selection is in progress for a room.
     * @param roomId Room identifier
     * @return true if selection is active and word not yet selected
     */
    public boolean isSelectionInProgress(String roomId) {
        WordSelectionSession session = activeSessions.get(roomId);
        return session != null && !session.isWordSelected();
    }

    /**
     * Cancel word selection for a room.
     * @param roomId Room identifier
     */
    public void cancelWordSelection(String roomId) {
        WordSelectionSession session = activeSessions.remove(roomId);
        if (session != null) {
            session.cancelTimeout();
            logger.debug("Word selection cancelled: roomId={}", roomId);
        }
    }

    /**
     * Clean up session after round ends.
     * @param roomId Room identifier
     */
    public void cleanupSession(String roomId) {
        cancelWordSelection(roomId);
    }

    /**
     * Callback interface for word selection events.
     */
    @FunctionalInterface
    public interface WordSelectionCallback {
        void onWordSelected(String roomId, String drawerId, String selectedWord, boolean autoSelected);
    }
}
