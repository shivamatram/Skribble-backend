package com.example.foodvan.utils;

import android.content.Context;
import android.util.Log;

import com.example.foodvan.models.FavoriteOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoritesManager - Utility class for managing user favorites
 * Provides methods to add, remove, and check favorite status
 */
public class FavoritesManager {
    
    private static final String TAG = "FavoritesManager";
    private static FavoritesManager instance;
    
    private Context context;
    private SessionManager sessionManager;
    private DatabaseReference favoritesRef;
    private List<String> favoriteItemIds;
    
    public interface FavoriteCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorite);
    }
    
    public interface FavoritesListCallback {
        void onFavoritesLoaded(List<FavoriteOrder> favorites);
        void onError(String error);
    }
    
    private FavoritesManager(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.favoriteItemIds = new ArrayList<>();
        
        String userId = sessionManager.getUserId();
        if (userId != null) {
            this.favoritesRef = FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(userId);
            loadFavoriteIds();
        }
    }
    
    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Add item to favorites
     */
    public void addToFavorites(String itemId, String itemName, String itemDescription,
                              String vendorId, String vendorName, String imageUrl,
                              double price, float rating, int reviewsCount,
                              String type, String category, String cuisine,
                              boolean isAvailable, FavoriteCallback callback) {
        
        if (favoritesRef == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }
        
        String favoriteId = favoritesRef.push().getKey();
        if (favoriteId == null) {
            if (callback != null) {
                callback.onError("Failed to generate favorite ID");
            }
            return;
        }
        
        FavoriteOrder favorite = new FavoriteOrder(
            favoriteId,
            sessionManager.getUserId(),
            itemId,
            itemName,
            itemDescription,
            vendorId,
            vendorName,
            imageUrl,
            price,
            rating,
            reviewsCount,
            type,
            System.currentTimeMillis(),
            isAvailable,
            category,
            cuisine
        );
        
        favoritesRef.child(favoriteId).setValue(favorite)
            .addOnSuccessListener(aVoid -> {
                favoriteItemIds.add(itemId);
                if (callback != null) {
                    callback.onSuccess("Added to favorites");
                }
                Log.d(TAG, "Added to favorites: " + itemName);
            })
            .addOnFailureListener(e -> {
                if (callback != null) {
                    callback.onError("Failed to add to favorites: " + e.getMessage());
                }
                Log.e(TAG, "Failed to add to favorites", e);
            });
    }
    
    /**
     * Remove item from favorites
     */
    public void removeFromFavorites(String itemId, FavoriteCallback callback) {
        if (favoritesRef == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }
        
        // Find and remove the favorite by itemId
        favoritesRef.orderByChild("itemId").equalTo(itemId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                        favoriteSnapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                favoriteItemIds.remove(itemId);
                                if (callback != null) {
                                    callback.onSuccess("Removed from favorites");
                                }
                                Log.d(TAG, "Removed from favorites: " + itemId);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onError("Failed to remove from favorites: " + e.getMessage());
                                }
                                Log.e(TAG, "Failed to remove from favorites", e);
                            });
                        break; // Remove only the first match
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    if (callback != null) {
                        callback.onError("Failed to remove from favorites: " + error.getMessage());
                    }
                    Log.e(TAG, "Failed to remove from favorites", error.toException());
                }
            });
    }
    
    /**
     * Remove favorite by favorite ID
     */
    public void removeFavoriteById(String favoriteId, FavoriteCallback callback) {
        if (favoritesRef == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }
        
        favoritesRef.child(favoriteId).removeValue()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) {
                    callback.onSuccess("Removed from favorites");
                }
                Log.d(TAG, "Removed favorite: " + favoriteId);
            })
            .addOnFailureListener(e -> {
                if (callback != null) {
                    callback.onError("Failed to remove favorite: " + e.getMessage());
                }
                Log.e(TAG, "Failed to remove favorite", e);
            });
    }
    
    /**
     * Check if item is in favorites
     */
    public void isFavorite(String itemId, FavoriteCheckCallback callback) {
        if (favoritesRef == null) {
            if (callback != null) {
                callback.onResult(false);
            }
            return;
        }
        
        // Check from cached list first
        if (favoriteItemIds.contains(itemId)) {
            if (callback != null) {
                callback.onResult(true);
            }
            return;
        }
        
        // Check from Firebase
        favoritesRef.orderByChild("itemId").equalTo(itemId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean isFavorite = snapshot.exists();
                    if (isFavorite && !favoriteItemIds.contains(itemId)) {
                        favoriteItemIds.add(itemId);
                    }
                    if (callback != null) {
                        callback.onResult(isFavorite);
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Failed to check favorite status", error.toException());
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
            });
    }
    
    /**
     * Get all favorites for current user
     */
    public void getAllFavorites(FavoritesListCallback callback) {
        if (favoritesRef == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }
        
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<FavoriteOrder> favorites = new ArrayList<>();
                favoriteItemIds.clear();
                
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    FavoriteOrder favorite = favoriteSnapshot.getValue(FavoriteOrder.class);
                    if (favorite != null) {
                        favorites.add(favorite);
                        favoriteItemIds.add(favorite.getItemId());
                    }
                }
                
                if (callback != null) {
                    callback.onFavoritesLoaded(favorites);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load favorites", error.toException());
                if (callback != null) {
                    callback.onError("Failed to load favorites: " + error.getMessage());
                }
            }
        });
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String itemId, String itemName, String itemDescription,
                              String vendorId, String vendorName, String imageUrl,
                              double price, float rating, int reviewsCount,
                              String type, String category, String cuisine,
                              boolean isAvailable, FavoriteCallback callback) {
        
        isFavorite(itemId, isFavorite -> {
            if (isFavorite) {
                removeFromFavorites(itemId, callback);
            } else {
                addToFavorites(itemId, itemName, itemDescription, vendorId, vendorName,
                              imageUrl, price, rating, reviewsCount, type, category,
                              cuisine, isAvailable, callback);
            }
        });
    }
    
    /**
     * Get favorites count
     */
    public int getFavoritesCount() {
        return favoriteItemIds.size();
    }
    
    /**
     * Load favorite item IDs for quick checking
     */
    private void loadFavoriteIds() {
        if (favoritesRef == null) return;
        
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                favoriteItemIds.clear();
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    FavoriteOrder favorite = favoriteSnapshot.getValue(FavoriteOrder.class);
                    if (favorite != null && favorite.getItemId() != null) {
                        favoriteItemIds.add(favorite.getItemId());
                    }
                }
                Log.d(TAG, "Loaded " + favoriteItemIds.size() + " favorite item IDs");
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load favorite IDs", error.toException());
            }
        });
    }
    
    /**
     * Clear all favorites (for logout)
     */
    public void clearFavorites() {
        favoriteItemIds.clear();
        favoritesRef = null;
    }
    
    /**
     * Reinitialize for new user
     */
    public void reinitialize(String userId) {
        favoriteItemIds.clear();
        if (userId != null) {
            this.favoritesRef = FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(userId);
            loadFavoriteIds();
        } else {
            this.favoritesRef = null;
        }
    }
}
