package com.skribble.timer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skribble.dto.TimerTickBroadcast;
import com.skribble.dto.WordSelectionTickMessage;
import com.skribble.room.RoomManager;
import com.skribble.room.RoomState;
import com.skribble.room.RoomStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Service for managing and broadcasting game timers.
 * Handles both round timers and word selection timers.
 */
@Service
public class TimerService {
    
    private static final Logger logger = LoggerFactory.getLogger(TimerService.class);
    
    private static final int WORD_SELECTION_DURATION_SECONDS = 10;
    
    private final ObjectMapper objectMapper;
    private final RoomManager roomManager;
    private final ScheduledExecutorService scheduler;
    
    // Track active timers per room
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> wordSelectionTimers = new ConcurrentHashMap<>();
    
    // Callback for broadcasting messages
    private BiConsumer<String, String> broadcastCallback;
    
    public TimerService(ObjectMapper objectMapper, RoomManager roomManager) {
        this.objectMapper = objectMapper;
        this.roomManager = roomManager;
        this.scheduler = Executors.newScheduledThreadPool(4);
    }
    
    /**
     * Set the callback for broadcasting messages to rooms.
     */
    public void setBroadcastCallback(BiConsumer<String, String> callback) {
        this.broadcastCallback = callback;
    }
    
    /**
     * Start the round timer with periodic broadcasts.
     * @param roomId The room ID
     * @param durationSeconds Total round duration in seconds
     * @param onTimeout Callback when timer expires
     */
    public void startRoundTimer(String roomId, int durationSeconds, Runnable onTimeout) {
        // Cancel any existing timer
        stopRoundTimer(roomId);
        
        logger.info("Starting round timer: roomId={}, duration={}s", roomId, durationSeconds);
        
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            logger.warn("Cannot start timer - room not found: roomId={}", roomId);
            return;
        }
        
        RoomState room = roomOpt.get();
        final int totalTime = durationSeconds;
        
        // Schedule timer tick every second
        ScheduledFuture<?> timerFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                Optional<RoomState> currentRoomOpt = roomManager.getRoom(roomId);
                if (currentRoomOpt.isEmpty()) {
                    stopRoundTimer(roomId);
                    return;
                }
                
                RoomState currentRoom = currentRoomOpt.get();
                
                // Only broadcast during drawing phase
                if (currentRoom.getStatus() != RoomStatus.DRAWING) {
                    return;
                }
                
                int timeRemaining = (int) (currentRoom.getRemainingTimeMs() / 1000);
                
                // Broadcast timer tick
                broadcastRoundTick(roomId, timeRemaining, totalTime, 
                        currentRoom.getCurrentRound(), currentRoom.getTotalRounds());
                
                // Check if time expired
                if (timeRemaining <= 0) {
                    logger.info("Round timer expired: roomId={}", roomId);
                    stopRoundTimer(roomId);
                    
                    if (onTimeout != null) {
                        scheduler.execute(onTimeout);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in round timer tick: roomId={}, error={}", roomId, e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        activeTimers.put(roomId, timerFuture);
    }
    
    /**
     * Stop the round timer for a room.
     */
    public void stopRoundTimer(String roomId) {
        ScheduledFuture<?> timer = activeTimers.remove(roomId);
        if (timer != null) {
            timer.cancel(false);
            logger.debug("Round timer stopped: roomId={}", roomId);
        }
    }
    
    /**
     * Start word selection timer with periodic broadcasts.
     * @param roomId The room ID
     * @param onTimeout Callback when timer expires (receives roomId)
     */
    public void startWordSelectionTimer(String roomId, Runnable onTimeout) {
        // Cancel any existing word selection timer
        stopWordSelectionTimer(roomId);
        
        logger.info("Starting word selection timer: roomId={}", roomId);
        
        final int[] timeRemaining = { WORD_SELECTION_DURATION_SECONDS };
        
        ScheduledFuture<?> timerFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                // Broadcast tick
                broadcastWordSelectionTick(roomId, timeRemaining[0], WORD_SELECTION_DURATION_SECONDS);
                
                timeRemaining[0]--;
                
                // Check if time expired
                if (timeRemaining[0] < 0) {
                    logger.info("Word selection timer expired: roomId={}", roomId);
                    stopWordSelectionTimer(roomId);
                    
                    if (onTimeout != null) {
                        scheduler.execute(onTimeout);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in word selection timer tick: roomId={}, error={}", roomId, e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        wordSelectionTimers.put(roomId, timerFuture);
    }
    
    /**
     * Stop word selection timer for a room.
     */
    public void stopWordSelectionTimer(String roomId) {
        ScheduledFuture<?> timer = wordSelectionTimers.remove(roomId);
        if (timer != null) {
            timer.cancel(false);
            logger.debug("Word selection timer stopped: roomId={}", roomId);
        }
    }
    
    /**
     * Stop all timers for a room.
     */
    public void stopAllTimers(String roomId) {
        stopRoundTimer(roomId);
        stopWordSelectionTimer(roomId);
    }
    
    /**
     * Broadcast round timer tick to all players in room.
     */
    private void broadcastRoundTick(String roomId, int timeRemaining, int totalTime, int round, int totalRounds) {
        if (broadcastCallback == null) {
            return;
        }
        
        TimerTickBroadcast tick = TimerTickBroadcast.roundTimer(timeRemaining, totalTime, round, totalRounds);
        
        try {
            String json = objectMapper.writeValueAsString(tick);
            broadcastCallback.accept(roomId, json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize timer tick: roomId={}, error={}", roomId, e.getMessage());
        }
    }
    
    /**
     * Broadcast word selection timer tick to all players in room.
     */
    private void broadcastWordSelectionTick(String roomId, int timeRemaining, int totalTime) {
        if (broadcastCallback == null) {
            return;
        }
        
        WordSelectionTickMessage tick = new WordSelectionTickMessage(timeRemaining, totalTime);
        
        try {
            String json = objectMapper.writeValueAsString(tick);
            broadcastCallback.accept(roomId, json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize word selection tick: roomId={}, error={}", roomId, e.getMessage());
        }
    }
    
    /**
     * Get remaining time for a room's round timer.
     */
    public int getRemainingRoundTime(String roomId) {
        Optional<RoomState> roomOpt = roomManager.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            return 0;
        }
        return (int) (roomOpt.get().getRemainingTimeMs() / 1000);
    }
    
    /**
     * Check if round timer is active for a room.
     */
    public boolean isRoundTimerActive(String roomId) {
        ScheduledFuture<?> timer = activeTimers.get(roomId);
        return timer != null && !timer.isDone() && !timer.isCancelled();
    }
    
    /**
     * Check if word selection timer is active for a room.
     */
    public boolean isWordSelectionTimerActive(String roomId) {
        ScheduledFuture<?> timer = wordSelectionTimers.get(roomId);
        return timer != null && !timer.isDone() && !timer.isCancelled();
    }
    
    /**
     * Shutdown the timer service.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
