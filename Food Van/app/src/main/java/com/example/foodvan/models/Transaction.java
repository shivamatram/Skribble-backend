package com.example.foodvan.models;

import java.util.Date;

public class Transaction {
    private String orderId;
    private double amount;
    private Date timestamp;
    private String paymentMethod;

    public Transaction() {
        // Default constructor required for Firebase
    }

    public Transaction(String orderId, double amount, Date timestamp, String paymentMethod) {
        this.orderId = orderId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
