package com.example.foodvan.models;

import java.io.Serializable;
import java.util.List;

/**
 * Order model class representing customer orders
 */
public class Order implements Serializable {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String vendorId;
    private String vanId;
    private String vanName;
    private List<OrderItem> items;
    private double subtotal;
    private double deliveryFee;
    private double tax;
    private double discount;
    private double totalAmount;
    private String status; // PLACED, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED
    private String paymentMethod; // CASH, ONLINE, UPI
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED
    private String deliveryAddress;
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String specialInstructions;
    private int estimatedDeliveryTime; // in minutes
    private long orderTime;
    private long confirmedTime;
    private long readyTime;
    private long deliveredTime;
    private double customerRating;
    private String customerReview;

    public Order() {
        // Default constructor required for Firebase
    }

    public Order(String orderId, String customerId, String vendorId, String vanId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.vendorId = vendorId;
        this.vanId = vanId;
        this.status = "PLACED";
        this.paymentStatus = "PENDING";
        this.orderTime = System.currentTimeMillis();
        this.customerRating = 0.0;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVanId() {
        return vanId;
    }

    public void setVanId(String vanId) {
        this.vanId = vanId;
    }

    public String getVanName() {
        return vanName;
    }

    public void setVanName(String vanName) {
        this.vanName = vanName;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public int getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(int estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(long orderTime) {
        this.orderTime = orderTime;
    }

    public long getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(long confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public long getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }

    public long getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(long deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(double customerRating) {
        this.customerRating = customerRating;
    }

    public String getCustomerReview() {
        return customerReview;
    }

    public void setCustomerReview(String customerReview) {
        this.customerReview = customerReview;
    }

    // Utility methods
    public String getFormattedTotalAmount() {
        return String.format("₹%.2f", totalAmount);
    }

    public String getFormattedSubtotal() {
        return String.format("₹%.2f", subtotal);
    }

    public String getFormattedDeliveryFee() {
        return String.format("₹%.2f", deliveryFee);
    }

    public String getFormattedTax() {
        return String.format("₹%.2f", tax);
    }

    public String getFormattedDiscount() {
        return String.format("₹%.2f", discount);
    }

    public int getTotalItems() {
        if (items == null) return 0;
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public boolean canBeCancelled() {
        return "PLACED".equals(status) || "CONFIRMED".equals(status);
    }

    public boolean isCompleted() {
        return "DELIVERED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

    // Additional methods for VendorDashboardActivity
    public String getId() {
        return orderId;
    }

    public void setId(String id) {
        this.orderId = id;
    }

    public String getOrderDate() {
        // Return formatted date string for filtering
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(orderTime));
    }

    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            subtotal = 0.0;
            totalAmount = 0.0;
            return;
        }

        subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        // Calculate tax (assuming 5% GST)
        tax = subtotal * 0.05;

        // Calculate total
        totalAmount = subtotal + deliveryFee + tax - discount;
    }

    // Missing methods that are referenced in the code
    public java.util.Date getTimestamp() {
        return new java.util.Date(orderTime);
    }
    
    public void setTimestamp(java.util.Date timestamp) {
        this.orderTime = timestamp != null ? timestamp.getTime() : System.currentTimeMillis();
    }
    
    public String getVendorName() {
        return vanName; // Using vanName as vendor name
    }
    
    public void setVendorName(String vendorName) {
        this.vanName = vendorName;
    }
    
    public List<OrderItem> getOrderItems() {
        return items;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.items = orderItems;
    }

    // Inner class for order items
    public static class OrderItem implements Serializable {
        private String itemId;
        private String itemName;
        private double price;
        private int quantity;
        private String specialInstructions;

        public OrderItem() {
            // Default constructor
        }

        public OrderItem(String itemId, String itemName, double price, int quantity) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getSpecialInstructions() {
            return specialInstructions;
        }

        public void setSpecialInstructions(String specialInstructions) {
            this.specialInstructions = specialInstructions;
        }

        public double getTotalPrice() {
            return price * quantity;
        }

        public String getFormattedPrice() {
            return String.format("₹%.2f", price);
        }

        public String getFormattedTotalPrice() {
            return String.format("₹%.2f", getTotalPrice());
        }
    }
}
