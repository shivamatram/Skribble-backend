package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.utils.CartManager;

/**
 * CartActivity - Shopping cart and checkout
 */
public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTax, tvTotal;
    private Button btnCheckout;
    
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set window flags for status bar handling
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_cart);
        
        initializeViews();
        initializeServices();
        loadCartItems();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Shopping Cart");
        }
        
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        btnCheckout = findViewById(R.id.btn_checkout);
        
        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void initializeServices() {
        cartManager = CartManager.getInstance(this);
    }

    private void loadCartItems() {
        if (cartManager.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems();
            updateTotals();
        }
    }

    private void showEmptyCart() {
        // Show empty cart message
        Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
    }

    private void showCartItems() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Set up cart adapter
    }

    private void updateTotals() {
        double subtotal = cartManager.getSubtotal();
        double deliveryFee = cartManager.getDeliveryFee();
        double tax = cartManager.getTax();
        double total = cartManager.getTotal();
        
        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDeliveryFee.setText(String.format("₹%.2f", deliveryFee));
        tvTax.setText(String.format("₹%.2f", tax));
        tvTotal.setText(String.format("₹%.2f", total));
    }

    private void proceedToCheckout() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement checkout process
        Toast.makeText(this, "Checkout coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
