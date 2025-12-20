package com.example.foodvan.activities.customer;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.UserReviewAdapter;
import com.example.foodvan.models.Review;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.ReviewsManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive Settings Reviews & Ratings Activity
 * Allows users to view, create, edit and manage their reviews
 */
public class SettingsReviewsRatingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsReviewsRatings";
    private static final int MAX_REVIEW_LENGTH = 500;

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialCardView cardWriteReview;
    private MaterialCardView cardFilters;
    private MaterialCardView cardEmptyState;
    private MaterialCardView cardLoadingState;
    private RecyclerView rvMyReviews;
    private ChipGroup chipGroupFilters;

    // Write Review Components
    private TextInputLayout tilVendorSelection;
    private AutoCompleteTextView actvVendorSelection;
    private TextInputLayout tilReviewText;
    private TextInputEditText etReviewText;
    private MaterialButton btnSubmitReview;
    private TextView tvRatingDescription;

    // Star Rating Components
    private ImageView[] starInputs = new ImageView[5];
    private int selectedRating = 0;

    // Statistics Components
    private TextView tvTotalReviewsCount;
    private TextView tvUserAverageRating;
    private TextView tvHelpfulVotes;

    // Data & Managers
    private SessionManager sessionManager;
    private ReviewsManager reviewsManager;
    private UserReviewAdapter reviewAdapter;
    private List<Review> userReviews = new ArrayList<>();
    private List<Vendor> availableVendors = new ArrayList<>();
    private String currentFilter = "all";
    private boolean isLoading = false;

    // Firebase
    private DatabaseReference vendorsRef;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_reviews_ratings);

        initializeComponents();
        initializeViews();
        setupToolbar();
        setupRatingInput();
        setupClickListeners();
        setupRecyclerView();
        loadVendors();
        loadUserReviews();
        updateUserStats();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        reviewsManager = ReviewsManager.getInstance(this);
        vendorsRef = FirebaseDatabase.getInstance().getReference("vendors");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        cardWriteReview = findViewById(R.id.card_write_review);
        cardFilters = findViewById(R.id.card_filters);
        cardEmptyState = findViewById(R.id.card_empty_state);
        cardLoadingState = findViewById(R.id.card_loading_state);
        rvMyReviews = findViewById(R.id.rv_my_reviews);
        chipGroupFilters = findViewById(R.id.chip_group_filters);

        // Write Review Components
        tilVendorSelection = findViewById(R.id.til_vendor_selection);
        actvVendorSelection = findViewById(R.id.actv_vendor_selection);
        tilReviewText = findViewById(R.id.til_review_text);
        etReviewText = findViewById(R.id.et_review_text);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        tvRatingDescription = findViewById(R.id.tv_rating_description);

        // Statistics Components
        tvTotalReviewsCount = findViewById(R.id.tv_total_reviews_count);
        tvUserAverageRating = findViewById(R.id.tv_user_average_rating);
        tvHelpfulVotes = findViewById(R.id.tv_helpful_votes);

        // Star Rating Components
        starInputs[0] = findViewById(R.id.star_input_1);
        starInputs[1] = findViewById(R.id.star_input_2);
        starInputs[2] = findViewById(R.id.star_input_3);
        starInputs[3] = findViewById(R.id.star_input_4);
        starInputs[4] = findViewById(R.id.star_input_5);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRatingInput() {
        for (int i = 0; i < starInputs.length; i++) {
            final int rating = i + 1;
            starInputs[i].setOnClickListener(v -> {
                setRating(rating);
                animateRatingSelection(rating);
            });
        }

        // Add text watcher for review text
        etReviewText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateReviewForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // Filter chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                handleFilterSelection(checkedId);
            }
        });

        // Submit review button
        btnSubmitReview.setOnClickListener(v -> submitReview());

        // Vendor selection
        actvVendorSelection.setOnItemClickListener((parent, view, position, id) -> {
            validateReviewForm();
        });
    }

    private void setupRecyclerView() {
        reviewAdapter = new UserReviewAdapter(this, userReviews);
        reviewAdapter.setOnReviewActionListener(new UserReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEditReview(Review review) {
                showEditReviewDialog(review);
            }

            @Override
            public void onDeleteReview(Review review) {
                showDeleteConfirmationDialog(review);
            }
        });

        rvMyReviews.setLayoutManager(new LinearLayoutManager(this));
        rvMyReviews.setAdapter(reviewAdapter);
        rvMyReviews.setNestedScrollingEnabled(false);
    }

    private void setRating(int rating) {
        selectedRating = rating;
        
        // Update star visuals
        for (int i = 0; i < starInputs.length; i++) {
            if (i < rating) {
                starInputs[i].setImageResource(R.drawable.ic_star_filled);
                starInputs[i].setImageTintList(getColorStateList(R.color.colorPrimary));
            } else {
                starInputs[i].setImageResource(R.drawable.ic_star_outline);
                starInputs[i].setImageTintList(getColorStateList(R.color.colorOutline));
            }
        }

        // Update description
        String[] descriptions = {
            "Tap stars to rate",
            "Poor - Not recommended",
            "Fair - Below expectations", 
            "Good - Meets expectations",
            "Very Good - Exceeds expectations",
            "Excellent - Outstanding!"
        };
        
        tvRatingDescription.setText(descriptions[rating]);
        validateReviewForm();
    }

    private void animateRatingSelection(int rating) {
        for (int i = 0; i < rating; i++) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(starInputs[i], "scaleX", 1.0f, 1.3f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(starInputs[i], "scaleY", 1.0f, 1.3f, 1.0f);
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.setStartDelay(i * 50);
            scaleY.setStartDelay(i * 50);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleX.start();
            scaleY.start();
        }
    }

    private void validateReviewForm() {
        String vendorText = actvVendorSelection.getText().toString().trim();
        String reviewText = etReviewText.getText().toString().trim();
        
        boolean isValid = selectedRating > 0 && 
                         !vendorText.isEmpty() && 
                         !vendorText.equals("Select a restaurant to review...") &&
                         !reviewText.isEmpty() && 
                         reviewText.length() >= 10;
        
        btnSubmitReview.setEnabled(isValid);
        
        // Update character count color
        if (tilReviewText != null) {
            int currentLength = reviewText.length();
            if (currentLength > MAX_REVIEW_LENGTH * 0.9) {
                tilReviewText.setCounterTextColor(getColorStateList(R.color.colorError));
            } else {
                tilReviewText.setCounterTextColor(getColorStateList(R.color.colorOnSurfaceVariant));
            }
        }
    }

    private void loadVendors() {
        vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableVendors.clear();
                List<String> vendorNames = new ArrayList<>();
                
                for (DataSnapshot vendorSnapshot : snapshot.getChildren()) {
                    Vendor vendor = vendorSnapshot.getValue(Vendor.class);
                    if (vendor != null && vendor.isVerified()) {
                        availableVendors.add(vendor);
                        vendorNames.add(vendor.getName());
                    }
                }
                
                // Setup autocomplete adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    SettingsReviewsRatingsActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    vendorNames
                );
                actvVendorSelection.setAdapter(adapter);
                actvVendorSelection.setText(""); // Clear placeholder
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load restaurants: " + error.getMessage());
            }
        });
    }

    private void loadUserReviews() {
        if (isLoading) return;
        
        String userId = sessionManager.getUserId();
        if (userId == null) {
            showError("Please login to view your reviews");
            return;
        }

        showLoading(true);
        isLoading = true;

        reviewsManager.loadUserReviews(userId, new ReviewsManager.ReviewsLoadCallback() {
            @Override
            public void onReviewsLoaded(List<Review> reviews) {
                mainHandler.post(() -> {
                    userReviews.clear();
                    userReviews.addAll(reviews);
                    applyCurrentFilter();
                    updateUserStats();
                    showLoading(false);
                    isLoading = false;
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    showError("Failed to load reviews: " + error);
                    showLoading(false);
                    isLoading = false;
                });
            }
        });
    }

    private void submitReview() {
        if (!validateReviewInput()) return;

        String vendorName = actvVendorSelection.getText().toString().trim();
        String reviewText = etReviewText.getText().toString().trim();
        
        // Find selected vendor
        Vendor selectedVendor = null;
        for (Vendor vendor : availableVendors) {
            if (vendor.getName().equals(vendorName)) {
                selectedVendor = vendor;
                break;
            }
        }

        if (selectedVendor == null) {
            showError("Please select a valid restaurant");
            return;
        }

        // Show progress
        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Submitting...");

        // Create review
        reviewsManager.submitReview(
            selectedVendor.getId(),
            null, // No specific item
            null, // No item name
            selectedRating,
            reviewText,
            new ArrayList<>(), // No images for now
            false, // Not anonymous
            new ReviewsManager.ReviewCallback() {
                @Override
                public void onSuccess(String message) {
                    mainHandler.post(() -> {
                        showSuccess("Review submitted successfully!");
                        clearReviewForm();
                        refreshData();
                        btnSubmitReview.setEnabled(true);
                        btnSubmitReview.setText("Submit Review");
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        showError("Failed to submit review: " + error);
                        btnSubmitReview.setEnabled(true);
                        btnSubmitReview.setText("Submit Review");
                    });
                }
            }
        );
    }

    private boolean validateReviewInput() {
        if (selectedRating == 0) {
            showError("Please select a rating");
            return false;
        }

        String vendorText = actvVendorSelection.getText().toString().trim();
        if (vendorText.isEmpty() || vendorText.equals("Select a restaurant to review...")) {
            showError("Please select a restaurant");
            tilVendorSelection.setError("Restaurant selection required");
            return false;
        } else {
            tilVendorSelection.setError(null);
        }

        String reviewText = etReviewText.getText().toString().trim();
        if (reviewText.isEmpty()) {
            showError("Please write a review");
            tilReviewText.setError("Review text required");
            return false;
        } else if (reviewText.length() < 10) {
            showError("Review must be at least 10 characters");
            tilReviewText.setError("Minimum 10 characters required");
            return false;
        } else {
            tilReviewText.setError(null);
        }

        return true;
    }

    private void clearReviewForm() {
        actvVendorSelection.setText("");
        etReviewText.setText("");
        setRating(0);
        tilVendorSelection.setError(null);
        tilReviewText.setError(null);
    }

    private void handleFilterSelection(int checkedId) {
        if (checkedId == R.id.chip_all_reviews) {
            currentFilter = "all";
        } else if (checkedId == R.id.chip_recent) {
            currentFilter = "recent";
        } else if (checkedId == R.id.chip_high_rated) {
            currentFilter = "high_rated";
        }
        
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        List<Review> filteredReviews = new ArrayList<>();
        
        for (Review review : userReviews) {
            boolean include = false;
            
            switch (currentFilter) {
                case "all":
                    include = true;
                    break;
                case "recent":
                    // Show reviews from last 30 days
                    long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
                    include = review.getTimestamp() > thirtyDaysAgo;
                    break;
                case "high_rated":
                    include = review.getRating() >= 4.0f;
                    break;
            }
            
            if (include) {
                filteredReviews.add(review);
            }
        }
        
        // Sort by most recent
        Collections.sort(filteredReviews, (r1, r2) -> 
            Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        
        reviewAdapter.updateReviews(filteredReviews);
        updateEmptyState(filteredReviews.isEmpty());
    }

    private void updateUserStats() {
        if (userReviews.isEmpty()) {
            tvTotalReviewsCount.setText("0");
            tvUserAverageRating.setText("0.0");
            tvHelpfulVotes.setText("0");
            return;
        }

        // Calculate statistics
        int totalReviews = userReviews.size();
        float totalRating = 0;
        int totalHelpful = 0;

        for (Review review : userReviews) {
            totalRating += review.getRating();
            totalHelpful += review.getHelpfulCount();
        }

        float averageRating = totalRating / totalReviews;

        // Update UI
        tvTotalReviewsCount.setText(String.valueOf(totalReviews));
        tvUserAverageRating.setText(String.format("%.1f", averageRating));
        tvHelpfulVotes.setText(String.valueOf(totalHelpful));
    }

    private void showEditReviewDialog(Review review) {
        // Implementation for edit review dialog
        Toast.makeText(this, "Edit review feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(Review review) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteReview(review))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteReview(Review review) {
        reviewsManager.deleteReview(review.getReviewId(), new ReviewsManager.ReviewCallback() {
            @Override
            public void onSuccess(String message) {
                mainHandler.post(() -> {
                    showSuccess("Review deleted successfully");
                    refreshData();
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    showError("Failed to delete review: " + error);
                });
            }
        });
    }

    private void refreshData() {
        loadUserReviews();
        swipeRefresh.setRefreshing(false);
    }

    private void showLoading(boolean show) {
        cardLoadingState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMyReviews.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        cardEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMyReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorSuccess))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorError))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        if (!isLoading) {
            loadUserReviews();
        }
    }
}
