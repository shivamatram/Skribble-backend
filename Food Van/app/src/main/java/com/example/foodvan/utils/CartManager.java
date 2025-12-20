package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodvan.models.MenuItem;
import com.example.foodvan.models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CartManager - Handles shopping cart operations and persistence
 */
public class CartManager {
    private static final String PREF_NAME = "FoodVanCart";
    private static final String KEY_CART_ITEMS = "cartItems";
    private static final String KEY_VAN_ID = "vanId";
    private static final String KEY_VAN_NAME = "vanName";
    
    private static CartManager instance;
    private SharedPreferences preferences;
    private Gson gson;
    private Map<String, CartItem> cartItems;
    private String currentVanId;
    private String currentVanName;

    private CartManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadCart() {
        String cartJson = preferences.getString(KEY_CART_ITEMS, "");
        currentVanId = preferences.getString(KEY_VAN_ID, "");
        currentVanName = preferences.getString(KEY_VAN_NAME, "");
        
        if (!cartJson.isEmpty()) {
            Type type = new TypeToken<Map<String, CartItem>>(){}.getType();
            cartItems = gson.fromJson(cartJson, type);
        } else {
            cartItems = new HashMap<>();
        }
    }

    private void saveCart() {
        String cartJson = gson.toJson(cartItems);
        preferences.edit()
                .putString(KEY_CART_ITEMS, cartJson)
                .putString(KEY_VAN_ID, currentVanId)
                .putString(KEY_VAN_NAME, currentVanName)
                .apply();
    }

    public void addItem(MenuItem menuItem, String vanId, String vanName) {
        // If adding from a different van, clear existing cart
        if (!currentVanId.isEmpty() && !currentVanId.equals(vanId)) {
            clearCart();
        }
        
        currentVanId = vanId;
        currentVanName = vanName;
        
        String itemId = menuItem.getItemId();
        if (cartItems.containsKey(itemId)) {
            CartItem cartItem = cartItems.get(itemId);
            cartItem.quantity++;
        } else {
            CartItem cartItem = new CartItem();
            cartItem.itemId = menuItem.getItemId();
            cartItem.name = menuItem.getName();
            cartItem.price = menuItem.getDiscountedPrice();
            cartItem.imageUrl = menuItem.getImageUrl();
            cartItem.isVegetarian = menuItem.isVegetarian();
            cartItem.quantity = 1;
            cartItems.put(itemId, cartItem);
        }
        
        saveCart();
    }

    public void removeItem(String itemId) {
        if (cartItems.containsKey(itemId)) {
            CartItem cartItem = cartItems.get(itemId);
            cartItem.quantity--;
            
            if (cartItem.quantity <= 0) {
                cartItems.remove(itemId);
            }
            
            saveCart();
        }
    }

    public void removeItemCompletely(String itemId) {
        cartItems.remove(itemId);
        saveCart();
    }

    public int getItemQuantity(String itemId) {
        CartItem cartItem = cartItems.get(itemId);
        return cartItem != null ? cartItem.quantity : 0;
    }

    public int getTotalItemCount() {
        int total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.quantity;
        }
        return total;
    }

    public double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : cartItems.values()) {
            subtotal += item.price * item.quantity;
        }
        return subtotal;
    }

    public double getTax() {
        return getSubtotal() * 0.05; // 5% GST
    }

    public double getDeliveryFee() {
        return getSubtotal() > 200 ? 0.0 : 30.0; // Free delivery above ₹200
    }

    public double getTotal() {
        return getSubtotal() + getTax() + getDeliveryFee();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public String getCurrentVanId() {
        return currentVanId;
    }

    public String getCurrentVanName() {
        return currentVanName;
    }

    public void clearCart() {
        cartItems.clear();
        currentVanId = "";
        currentVanName = "";
        saveCart();
    }

    public Order createOrder(String customerId, String customerName, String customerPhone, 
                           String deliveryAddress, double deliveryLat, double deliveryLng) {
        if (isEmpty()) {
            return null;
        }

        String orderId = "ORDER_" + System.currentTimeMillis();
        Order order = new Order(orderId, customerId, "", currentVanId);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setVanName(currentVanName);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryLatitude(deliveryLat);
        order.setDeliveryLongitude(deliveryLng);

        // Convert cart items to order items
        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            Order.OrderItem orderItem = new Order.OrderItem(
                cartItem.itemId,
                cartItem.name,
                cartItem.price,
                cartItem.quantity
            );
            orderItems.add(orderItem);
        }
        
        order.setItems(orderItems);
        order.setSubtotal(getSubtotal());
        order.setDeliveryFee(getDeliveryFee());
        order.setTax(getTax());
        order.setTotalAmount(getTotal());
        order.setEstimatedDeliveryTime(30); // Default 30 minutes

        return order;
    }

    // Inner class for cart items
    public static class CartItem {
        public String itemId;
        public String name;
        public double price;
        public String imageUrl;
        public boolean isVegetarian;
        public int quantity;

        public String getFormattedPrice() {
            return String.format("₹%.2f", price);
        }

        public String getFormattedTotalPrice() {
            return String.format("₹%.2f", price * quantity);
        }
    }
}
