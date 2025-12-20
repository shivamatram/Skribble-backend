package com.skribble.word;

/**
 * Result of a word selection attempt.
 * Contains success/error status and relevant data.
 */
public class WordSelectionResult {

    private final boolean success;
    private final String selectedWord;
    private final String errorCode;
    private final String errorMessage;

    private WordSelectionResult(boolean success, String selectedWord, String errorCode, String errorMessage) {
        this.success = success;
        this.selectedWord = selectedWord;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Create successful result.
     * @param selectedWord The word that was selected
     * @return Success result
     */
    public static WordSelectionResult success(String selectedWord) {
        return new WordSelectionResult(true, selectedWord, null, null);
    }

    /**
     * Create error result.
     * @param errorCode Error code for client
     * @param errorMessage Human-readable error message
     * @return Error result
     */
    public static WordSelectionResult error(String errorCode, String errorMessage) {
        return new WordSelectionResult(false, null, errorCode, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSelectedWord() {
        return selectedWord;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
