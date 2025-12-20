package com.example.foodvan.models;

/**
 * Model class for Bank Account Details
 */
public class BankAccountDetails {
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String accountType; // "Savings" or "Current"
    private boolean isVerified;
    private long lastUpdated;

    public BankAccountDetails() {
        // Default constructor for Firebase
    }

    public BankAccountDetails(String accountHolderName, String accountNumber, String ifscCode, 
                            String bankName, String accountType) {
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.bankName = bankName;
        this.accountType = accountType;
        this.isVerified = false;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "BankAccountDetails{" +
                "accountHolderName='" + accountHolderName + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", ifscCode='" + ifscCode + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", isVerified=" + isVerified +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
