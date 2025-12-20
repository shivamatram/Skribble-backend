package com.skribble.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input messages.
 * Prevents XSS attacks, removes profanity, and enforces length limits.
 */
@Component
public class MessageSanitizer {
    
    // Maximum message length
    private static final int MAX_MESSAGE_LENGTH = 50;
    private static final int MAX_CHAT_LENGTH = 200;
    
    // Pattern for HTML tags
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    
    // Pattern for script injection attempts
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "(?i)(javascript:|data:|vbscript:|on\\w+=)", Pattern.CASE_INSENSITIVE);
    
    // Pattern for repeated characters (spam detection)
    private static final Pattern REPEATED_CHARS_PATTERN = Pattern.compile("(.)\\1{4,}");
    
    // Pattern for excessive whitespace
    private static final Pattern EXCESSIVE_WHITESPACE_PATTERN = Pattern.compile("\\s{3,}");
    
    // Basic profanity filter (extend as needed)
    private static final String[] BLOCKED_WORDS = {
        // Add profanity words here
    };
    
    /**
     * Sanitize a guess input.
     * @param guess The raw guess from the user
     * @return Sanitized guess, or null if invalid
     */
    public String sanitizeGuess(String guess) {
        if (guess == null) {
            return null;
        }
        
        String sanitized = guess.trim();
        
        // Empty check
        if (sanitized.isEmpty()) {
            return null;
        }
        
        // Length limit
        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            sanitized = sanitized.substring(0, MAX_MESSAGE_LENGTH);
        }
        
        // Remove HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove script injection attempts
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Normalize whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        // Convert to lowercase for comparison (guesses are case-insensitive)
        sanitized = sanitized.toLowerCase();
        
        // Final empty check after sanitization
        if (sanitized.isEmpty()) {
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * Sanitize a chat message.
     * @param message The raw chat message from the user
     * @return Sanitized message, or null if invalid
     */
    public String sanitizeChat(String message) {
        if (message == null) {
            return null;
        }
        
        String sanitized = message.trim();
        
        // Empty check
        if (sanitized.isEmpty()) {
            return null;
        }
        
        // Length limit
        if (sanitized.length() > MAX_CHAT_LENGTH) {
            sanitized = sanitized.substring(0, MAX_CHAT_LENGTH);
        }
        
        // Remove HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove script injection attempts
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Reduce repeated characters (spam prevention)
        sanitized = REPEATED_CHARS_PATTERN.matcher(sanitized).replaceAll("$1$1$1");
        
        // Normalize excessive whitespace
        sanitized = EXCESSIVE_WHITESPACE_PATTERN.matcher(sanitized).replaceAll("  ");
        sanitized = sanitized.trim();
        
        // Filter profanity
        sanitized = filterProfanity(sanitized);
        
        // Final empty check after sanitization
        if (sanitized.isEmpty()) {
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * Sanitize a player name.
     * @param name The raw player name
     * @return Sanitized name, or "Player" if invalid
     */
    public String sanitizeName(String name) {
        if (name == null) {
            return "Player";
        }
        
        String sanitized = name.trim();
        
        // Remove HTML
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove special characters except basic ones
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_\\- ]", "");
        
        // Limit length
        if (sanitized.length() > 20) {
            sanitized = sanitized.substring(0, 20);
        }
        
        // Default if empty
        if (sanitized.isEmpty()) {
            return "Player";
        }
        
        return sanitized;
    }
    
    /**
     * Check if a guess contains the actual word (prevent word leaking in chat).
     * @param message The message to check
     * @param secretWord The current secret word
     * @return true if message contains the word
     */
    public boolean containsSecretWord(String message, String secretWord) {
        if (message == null || secretWord == null) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        String lowerWord = secretWord.toLowerCase();
        
        return lowerMessage.contains(lowerWord);
    }
    
    /**
     * Calculate similarity between guess and actual word (for "close" detection).
     * Uses Levenshtein distance normalized by word length.
     * @param guess The player's guess
     * @param word The actual word
     * @return Similarity score from 0.0 to 1.0
     */
    public double calculateSimilarity(String guess, String word) {
        if (guess == null || word == null) {
            return 0.0;
        }
        
        String g = guess.toLowerCase().trim();
        String w = word.toLowerCase().trim();
        
        if (g.equals(w)) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(g, w);
        int maxLength = Math.max(g.length(), w.length());
        
        if (maxLength == 0) {
            return 0.0;
        }
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Check if a guess is "close" to the actual word.
     * @param guess The player's guess
     * @param word The actual word
     * @return true if the guess is close (similarity > 0.7)
     */
    public boolean isCloseGuess(String guess, String word) {
        double similarity = calculateSimilarity(guess, word);
        // Consider it close if similarity is between 70% and 99%
        return similarity >= 0.7 && similarity < 1.0;
    }
    
    /**
     * Check if a guess is correct.
     * @param guess The player's guess
     * @param word The actual word
     * @return true if correct
     */
    public boolean isCorrectGuess(String guess, String word) {
        if (guess == null || word == null) {
            return false;
        }
        return guess.toLowerCase().trim().equals(word.toLowerCase().trim());
    }
    
    /**
     * Filter profanity from message.
     */
    private String filterProfanity(String message) {
        String filtered = message;
        for (String word : BLOCKED_WORDS) {
            // Replace profanity with asterisks
            String replacement = "*".repeat(word.length());
            filtered = filtered.replaceAll("(?i)" + Pattern.quote(word), replacement);
        }
        return filtered;
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}
