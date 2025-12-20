package com.example.foodvan.models;

import java.io.Serializable;

/**
 * Payment method model for user saved payment options
 */
public class PaymentMethod implements Serializable {
    
    public enum PaymentType {
        CASH("Cash"),
        UPI("UPI"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        NET_BANKING("Net Banking"),
        WALLET("Digital Wallet");
        
        private final String displayName;
        
        PaymentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private String paymentId;
    private PaymentType type;
    private String displayName;
    private String maskedDetails; // Last 4 digits for cards, UPI ID for UPI, etc.
    private String fullDetails; // Encrypted full details
    private String expiryDate; // For cards
    private String holderName;
    private boolean isDefault;
    private boolean isVerified;
    private long createdAt;
    private long lastUsed;
    
    // UPI specific
    private String upiId;
    private String upiProvider; // GPay, PhonePe, Paytm, etc.
    
    // Card specific
    private String cardNumber;
    private String cardType; // Visa, MasterCard, RuPay, etc.
    private String bankName;
    
    // Wallet specific
    private String walletProvider; // Paytm, PhonePe, Amazon Pay, etc.
    private String walletId;
    
    public PaymentMethod() {
        this.paymentId = generatePaymentId();
        this.createdAt = System.currentTimeMillis();
        this.lastUsed = System.currentTimeMillis();
        this.isDefault = false;
        this.isVerified = false;
    }
    
    public PaymentMethod(PaymentType type, String displayName) {
        this();
        this.type = type;
        this.displayName = displayName;
    }
    
    private String generatePaymentId() {
        return "pay_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public PaymentType getType() {
        return type;
    }
    
    public void setType(PaymentType type) {
        this.type = type;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getMaskedDetails() {
        return maskedDetails;
    }
    
    public void setMaskedDetails(String maskedDetails) {
        this.maskedDetails = maskedDetails;
    }
    
    public String getFullDetails() {
        return fullDetails;
    }
    
    public void setFullDetails(String fullDetails) {
        this.fullDetails = fullDetails;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getHolderName() {
        return holderName;
    }
    
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public String getUpiId() {
        return upiId;
    }
    
    public void setUpiId(String upiId) {
        this.upiId = upiId;
        if (upiId != null) {
            this.maskedDetails = maskUpiId(upiId);
        }
    }
    
    public String getUpiProvider() {
        return upiProvider;
    }
    
    public void setUpiProvider(String upiProvider) {
        this.upiProvider = upiProvider;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        if (cardNumber != null) {
            this.maskedDetails = maskCardNumber(cardNumber);
        }
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getWalletProvider() {
        return walletProvider;
    }
    
    public void setWalletProvider(String walletProvider) {
        this.walletProvider = walletProvider;
    }
    
    public String getWalletId() {
        return walletId;
    }
    
    public void setWalletId(String walletId) {
        this.walletId = walletId;
        if (walletId != null) {
            this.maskedDetails = maskWalletId(walletId);
        }
    }
    
    // Helper methods
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (cleaned.length() >= 4) {
            return "**** **** **** " + cleaned.substring(cleaned.length() - 4);
        }
        return "****";
    }
    
    private String maskUpiId(String upiId) {
        if (upiId == null || !upiId.contains("@")) {
            return "****@****";
        }
        String[] parts = upiId.split("@");
        if (parts.length >= 2) {
            String username = parts[0];
            String provider = parts[1];
            if (username.length() > 2) {
                return username.substring(0, 2) + "****@" + provider;
            }
        }
        return "****@****";
    }
    
    private String maskWalletId(String walletId) {
        if (walletId == null || walletId.length() < 4) {
            return "****";
        }
        if (walletId.length() <= 8) {
            return "****" + walletId.substring(walletId.length() - 2);
        }
        return walletId.substring(0, 2) + "****" + walletId.substring(walletId.length() - 2);
    }
    
    public String getFormattedDetails() {
        switch (type) {
            case CASH:
                return "Cash on Delivery";
            case UPI:
                return maskedDetails + (upiProvider != null ? " (" + upiProvider + ")" : "");
            case CREDIT_CARD:
            case DEBIT_CARD:
                return maskedDetails + (cardType != null ? " (" + cardType + ")" : "");
            case NET_BANKING:
                return "Net Banking" + (bankName != null ? " - " + bankName : "");
            case WALLET:
                return maskedDetails + (walletProvider != null ? " (" + walletProvider + ")" : "");
            default:
                return displayName != null ? displayName : type.getDisplayName();
        }
    }
    
    public String getIcon() {
        switch (type) {
            case CASH:
                return "💵";
            case UPI:
                return "📱";
            case CREDIT_CARD:
                return "💳";
            case DEBIT_CARD:
                return "💳";
            case NET_BANKING:
                return "🏦";
            case WALLET:
                return "👛";
            default:
                return "💰";
        }
    }
    
    public boolean isExpired() {
        if (expiryDate == null || type == PaymentType.CASH || type == PaymentType.UPI) {
            return false;
        }
        
        try {
            // Assuming expiryDate is in MM/YY format
            String[] parts = expiryDate.split("/");
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int year = 2000 + Integer.parseInt(parts[1]);
                
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int currentYear = cal.get(java.util.Calendar.YEAR);
                int currentMonth = cal.get(java.util.Calendar.MONTH) + 1;
                
                return year < currentYear || (year == currentYear && month < currentMonth);
            }
        } catch (Exception e) {
            // Invalid date format
        }
        
        return false;
    }
    
    public void updateLastUsed() {
        this.lastUsed = System.currentTimeMillis();
    }
    
    public boolean requiresVerification() {
        return !isVerified && (type == PaymentType.UPI || 
                              type == PaymentType.CREDIT_CARD || 
                              type == PaymentType.DEBIT_CARD ||
                              type == PaymentType.WALLET);
    }
    
    @Override
    public String toString() {
        return getFormattedDetails();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PaymentMethod that = (PaymentMethod) obj;
        return paymentId != null ? paymentId.equals(that.paymentId) : that.paymentId == null;
    }
    
    @Override
    public int hashCode() {
        return paymentId != null ? paymentId.hashCode() : 0;
    }
    
    // Static factory methods
    public static PaymentMethod createCashPayment() {
        PaymentMethod payment = new PaymentMethod(PaymentType.CASH, "Cash on Delivery");
        payment.setVerified(true);
        return payment;
    }
    
    public static PaymentMethod createUPIPayment(String upiId, String provider) {
        PaymentMethod payment = new PaymentMethod(PaymentType.UPI, "UPI Payment");
        payment.setUpiId(upiId);
        payment.setUpiProvider(provider);
        return payment;
    }
    
    public static PaymentMethod createCardPayment(PaymentType cardType, String cardNumber, 
                                                 String holderName, String expiryDate, String bankName) {
        PaymentMethod payment = new PaymentMethod(cardType, cardType.getDisplayName());
        payment.setCardNumber(cardNumber);
        payment.setHolderName(holderName);
        payment.setExpiryDate(expiryDate);
        payment.setBankName(bankName);
        return payment;
    }
    
    public static PaymentMethod createWalletPayment(String walletId, String provider) {
        PaymentMethod payment = new PaymentMethod(PaymentType.WALLET, "Digital Wallet");
        payment.setWalletId(walletId);
        payment.setWalletProvider(provider);
        return payment;
    }
}
