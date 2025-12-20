package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.adapters.MenuItemAdapter;
import com.example.foodvan.models.FoodVan;
import com.example.foodvan.utils.CartManager;
import com.example.foodvan.utils.FirebaseManager;
import com.example.foodvan.utils.FavoritesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuActivity - Displays food van menu items
 * Features: Menu categories, item details, add to cart, cart management
 */
public class MenuActivity extends AppCompatActivity {

    private ImageView ivVanImage;
    private TextView tvVanName, tvVanDescription, tvVanRating, tvVanDistance;
    private RecyclerView rvMenuItems;
    private FloatingActionButton fabCart;
    
    private MenuItemAdapter menuItemAdapter;
    private List<com.example.foodvan.models.MenuItem> menuItems;
    
    private String foodVanId;
    private String foodVanName;
    private FoodVan currentVan;
    
    private FirebaseManager firebaseManager;
    private CartManager cartManager;
    private FavoritesManager favoritesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        
        initializeViews();
        initializeServices();
        getIntentData();
        setupRecyclerView();
        loadFoodVanDetails();
        loadMenuItems();
        updateCartBadge();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        ivVanImage = findViewById(R.id.iv_van_image);
        tvVanName = findViewById(R.id.tv_van_name);
        tvVanDescription = findViewById(R.id.tv_van_description);
        tvVanRating = findViewById(R.id.tv_van_rating);
        tvVanDistance = findViewById(R.id.tv_van_distance);
        rvMenuItems = findViewById(R.id.rv_menu_items);
        fabCart = findViewById(R.id.fab_cart);
        
        fabCart.setOnClickListener(v -> openCart());
    }

    private void initializeServices() {
        firebaseManager = new FirebaseManager();
        cartManager = CartManager.getInstance(this);
        favoritesManager = FavoritesManager.getInstance(this);
        menuItems = new ArrayList<>();
    }

    private void getIntentData() {
        foodVanId = getIntent().getStringExtra("food_van_id");
        foodVanName = getIntent().getStringExtra("food_van_name");
        
        if (foodVanName != null) {
            setTitle(foodVanName);
        }
    }

    private void setupRecyclerView() {
        menuItemAdapter = new MenuItemAdapter(menuItems, new MenuItemAdapter.OnMenuItemClickListener() {
            @Override
            public void onAddToCart(com.example.foodvan.models.MenuItem item) {
                addItemToCart(item);
            }

            @Override
            public void onRemoveFromCart(com.example.foodvan.models.MenuItem item) {
                removeItemFromCart(item);
            }

            @Override
            public void onItemClick(com.example.foodvan.models.MenuItem item) {
                showItemDetails(item);
            }
        });
        
        rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        rvMenuItems.setAdapter(menuItemAdapter);
    }

    private void loadFoodVanDetails() {
        if (foodVanId == null) return;
        
        firebaseManager.getFoodVanById(foodVanId, new FirebaseManager.OnFoodVanLoadListener() {
            @Override
            public void onSuccess(FoodVan foodVan) {
                currentVan = foodVan;
                displayVanDetails(foodVan);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MenuActivity.this, 
                    "Error loading van details: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVanDetails(FoodVan van) {
        tvVanName.setText(van.getName());
        tvVanDescription.setText(van.getDescription());
        tvVanRating.setText(String.format("%.1f ★", van.getRating()));
        tvVanDistance.setText(String.format("%.1f km away", van.getDistance()));
        
        // Load van image
        if (van.getImageUrl() != null && !van.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(van.getImageUrl())
                .placeholder(R.drawable.placeholder_food_van)
                .error(R.drawable.placeholder_food_van)
                .into(ivVanImage);
        }
    }

    private void loadMenuItems() {
        if (foodVanId == null) return;
        
        firebaseManager.getMenuItems(foodVanId, new FirebaseManager.OnMenuItemsLoadListener() {
            @Override
            public void onSuccess(List<com.example.foodvan.models.MenuItem> items) {
                menuItems.clear();
                menuItems.addAll(items);
                menuItemAdapter.notifyDataSetChanged();
                
                // Update cart quantities for existing items
                updateCartQuantities();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MenuActivity.this, 
                    "Error loading menu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartQuantities() {
        for (com.example.foodvan.models.MenuItem item : menuItems) {
            int quantity = cartManager.getItemQuantity(item.getItemId());
            item.setCartQuantity(quantity);
        }
        menuItemAdapter.notifyDataSetChanged();
    }

    private void addItemToCart(com.example.foodvan.models.MenuItem item) {
        cartManager.addItem(item, foodVanId, foodVanName);
        item.setCartQuantity(item.getCartQuantity() + 1);
        menuItemAdapter.notifyDataSetChanged();
        updateCartBadge();
        
        Toast.makeText(this, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

    private void removeItemFromCart(com.example.foodvan.models.MenuItem item) {
        if (item.getCartQuantity() > 0) {
            cartManager.removeItem(item.getItemId());
            item.setCartQuantity(item.getCartQuantity() - 1);
            menuItemAdapter.notifyDataSetChanged();
            updateCartBadge();
            
            Toast.makeText(this, item.getName() + " removed from cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void showItemDetails(com.example.foodvan.models.MenuItem item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("menu_item", item);
        intent.putExtra("food_van_id", foodVanId);
        intent.putExtra("food_van_name", foodVanName);
        startActivity(intent);
    }

    private void updateCartBadge() {
        int totalItems = cartManager.getTotalItemCount();
        
        if (totalItems > 0) {
            fabCart.setVisibility(View.VISIBLE);
            // TODO: Update badge count on FAB
        } else {
            fabCart.setVisibility(View.GONE);
        }
    }

    private void openCart() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_search) {
            // TODO: Implement search
            Toast.makeText(this, "Search coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        if (currentVan != null) {
            favoritesManager.toggleFavorite(
                currentVan.getId(),
                currentVan.getName(),
                currentVan.getDescription(),
                currentVan.getId(), // vendorId same as itemId for vendors
                currentVan.getName(), // vendorName same as itemName
                currentVan.getImageUrl(),
                0.0, // price not applicable for vendors
                (float) currentVan.getRating(),
                currentVan.getTotalRatings(),
                "VENDOR", // type
                currentVan.getCuisineType(), // category
                currentVan.getCuisineType(), // cuisine
                currentVan.isOpen(), // isAvailable
                new FavoritesManager.FavoriteCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onError(String error) {
                        Toast.makeText(MenuActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartQuantities();
        updateCartBadge();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
