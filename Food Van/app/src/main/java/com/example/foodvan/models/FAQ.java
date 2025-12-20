package com.example.foodvan.models;

import java.io.Serializable;

/**
 * FAQ model class representing a Frequently Asked Question
 */
public class FAQ implements Serializable {
    private String id;
    private String question;
    private String answer;
    private String category;
    private int order;
    private boolean expanded; // For UI state (not persisted)

    public FAQ() {
        // Default constructor required for Firebase
    }

    public FAQ(String id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.order = 0;
        this.expanded = false;
    }

    public FAQ(String id, String question, String answer, String category, int order) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.order = order;
        this.expanded = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}
