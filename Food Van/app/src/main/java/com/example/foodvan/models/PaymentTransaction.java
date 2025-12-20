package com.example.foodvan.models;

/**
 * Model class for Payment Transaction History
 */
public class PaymentTransaction {
    private String transactionId;
    private String orderId;
    private double amount;
    private String paymentMethod; // "UPI", "Card", "Bank Transfer", "Wallet"
    private String status; // "Success", "Pending", "Failed"
    private String description;
    private long timestamp;
    private String customerId;
    private String customerName;

    public PaymentTransaction() {
        // Default constructor for Firebase
    }

    public PaymentTransaction(String transactionId, String orderId, double amount, 
                            String paymentMethod, String status, String description) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    // Setters
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}
