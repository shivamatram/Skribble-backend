package com.skribble.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for word hint updates (reveals letters progressively).
 */
public class WordHintMessage {
    
    @JsonProperty("type")
    private String type = "WORD_HINT";
    
    @JsonProperty("hint")
    private String hint; // e.g., "_ _ P P _ E"
    
    @JsonProperty("wordLength")
    private int wordLength;
    
    @JsonProperty("revealedCount")
    private int revealedCount;
    
    public WordHintMessage() {}
    
    public WordHintMessage(String hint, int wordLength, int revealedCount) {
        this.hint = hint;
        this.wordLength = wordLength;
        this.revealedCount = revealedCount;
    }
    
    /**
     * Create a hint from a word with specified positions revealed.
     */
    public static WordHintMessage createHint(String word, int[] revealedPositions) {
        StringBuilder hint = new StringBuilder();
        int revealed = 0;
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == ' ') {
                hint.append("  "); // Double space for word separator
            } else {
                boolean isRevealed = false;
                for (int pos : revealedPositions) {
                    if (pos == i) {
                        isRevealed = true;
                        revealed++;
                        break;
                    }
                }
                hint.append(isRevealed ? c : '_');
                hint.append(' '); // Space between letters
            }
        }
        
        return new WordHintMessage(hint.toString().trim(), word.length(), revealed);
    }
    
    /**
     * Create initial hint with only underscores.
     */
    public static WordHintMessage initialHint(String word) {
        StringBuilder hint = new StringBuilder();
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == ' ') {
                hint.append("  ");
            } else {
                hint.append("_ ");
            }
        }
        
        return new WordHintMessage(hint.toString().trim(), word.length(), 0);
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getHint() {
        return hint;
    }
    
    public void setHint(String hint) {
        this.hint = hint;
    }
    
    public int getWordLength() {
        return wordLength;
    }
    
    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }
    
    public int getRevealedCount() {
        return revealedCount;
    }
    
    public void setRevealedCount(int revealedCount) {
        this.revealedCount = revealedCount;
    }
}
