package com.skribble.word;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * In-memory word bank for the Skribble game.
 * Contains 200+ simple, safe English words for drawing.
 * Thread-safe for concurrent access.
 */
@Component
public class WordBank {

    private static final Logger logger = LoggerFactory.getLogger(WordBank.class);

    /** Number of word options to present to drawer */
    public static final int WORD_OPTIONS_COUNT = 3;

    /** Immutable list of words */
    private List<String> words;

    @PostConstruct
    public void init() {
        this.words = Collections.unmodifiableList(initializeWords());
        logger.info("WordBank initialized with {} words", words.size());
    }

    /**
     * Get random unique words for word selection.
     * @param count Number of words to return
     * @return List of unique random words
     */
    public List<String> getRandomWords(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        if (count > words.size()) {
            count = words.size();
        }

        Set<Integer> usedIndices = new HashSet<>();
        List<String> selectedWords = new ArrayList<>(count);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (selectedWords.size() < count) {
            int index = random.nextInt(words.size());
            if (usedIndices.add(index)) {
                selectedWords.add(words.get(index));
            }
        }

        return selectedWords;
    }

    /**
     * Get 3 random word options for drawer selection.
     * @return List of exactly 3 unique words
     */
    public List<String> getWordOptions() {
        return getRandomWords(WORD_OPTIONS_COUNT);
    }

    /**
     * Check if a word exists in the word bank.
     * @param word Word to check (case-insensitive)
     * @return true if word exists
     */
    public boolean containsWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return words.contains(word.toLowerCase().trim());
    }

    /**
     * Get total word count.
     * @return Number of words in bank
     */
    public int getWordCount() {
        return words.size();
    }

    /**
     * Initialize the word bank with 200+ simple English words.
     * Words are: lowercase, single-word, safe for all ages.
     */
    private List<String> initializeWords() {
        return Arrays.asList(
            // Animals (30)
            "cat", "dog", "bird", "fish", "lion", "tiger", "bear", "elephant", "monkey", "rabbit",
            "snake", "horse", "cow", "pig", "sheep", "chicken", "duck", "frog", "turtle", "butterfly",
            "bee", "ant", "spider", "whale", "shark", "dolphin", "penguin", "owl", "eagle", "wolf",

            // Food (30)
            "apple", "banana", "orange", "pizza", "burger", "cake", "cookie", "bread", "cheese", "egg",
            "milk", "water", "juice", "coffee", "tea", "rice", "pasta", "soup", "salad", "sandwich",
            "hotdog", "icecream", "chocolate", "candy", "popcorn", "donut", "pie", "grape", "lemon", "cherry",

            // Objects (40)
            "car", "bus", "train", "plane", "boat", "bicycle", "phone", "computer", "book", "pencil",
            "chair", "table", "bed", "lamp", "clock", "mirror", "window", "door", "key", "umbrella",
            "hat", "shoe", "shirt", "pants", "glasses", "watch", "ring", "bag", "wallet", "camera",
            "guitar", "piano", "drum", "ball", "balloon", "kite", "robot", "rocket", "sword", "crown",

            // Nature (25)
            "sun", "moon", "star", "cloud", "rain", "snow", "tree", "flower", "grass", "mountain",
            "river", "ocean", "beach", "island", "forest", "desert", "rainbow", "fire", "water", "wind",
            "leaf", "rock", "sand", "wave", "volcano",

            // Places (20)
            "house", "school", "hospital", "church", "castle", "bridge", "tower", "farm", "zoo", "park",
            "beach", "airport", "museum", "library", "restaurant", "store", "bank", "hotel", "theater", "stadium",

            // Body Parts (15)
            "head", "face", "eye", "nose", "mouth", "ear", "hand", "finger", "foot", "leg",
            "arm", "heart", "brain", "tooth", "hair",

            // Actions/Sports (20)
            "run", "jump", "swim", "dance", "sing", "sleep", "eat", "drink", "read", "write",
            "basketball", "football", "soccer", "tennis", "baseball", "golf", "hockey", "boxing", "skiing", "surfing",

            // Professions (15)
            "doctor", "teacher", "police", "firefighter", "chef", "farmer", "pilot", "astronaut", "artist", "singer",
            "dancer", "soldier", "nurse", "scientist", "magician",

            // Fantasy/Fun (15)
            "dragon", "unicorn", "wizard", "princess", "knight", "pirate", "ghost", "vampire", "zombie", "alien",
            "mermaid", "fairy", "giant", "ninja", "superhero",

            // Misc Common (10)
            "baby", "family", "friend", "love", "smile", "music", "movie", "game", "party", "gift"
        );
    }
}
