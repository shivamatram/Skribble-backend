package com.example.foodvan.activities.customer;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.models.Review;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.ReviewsManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Complete Reviews & Ratings Activity with full functionality
 * Features: Submit reviews, edit reviews, view all reviews, Material Design 3
 */
public class ReviewsRatingsMainActivity extends AppCompatActivity {

    private static final String TAG = "ReviewsRatingsMain";
    
    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialCardView cardWriteReview;
    private AutoCompleteTextView actvRestaurant;
    private LinearLayout layoutStarRating;
    private ImageView[] ratingStars;
    private TextView tvRatingLabel;
    private TextInputLayout tilReviewText;
    private TextInputEditText etReviewText;
    private MaterialButton btnSubmitReview;
    private CircularProgressIndicator progressSubmitting;
    
    // Filter Section
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipRecent, chipHighRated;
    
    // Reviews List
    private RecyclerView rvReviews;
    private ReviewsAdapter reviewsAdapter;
    private LinearLayout layoutEmptyState;
    private ExtendedFloatingActionButton fabWriteReview;
    
    // Data
    private List<Review> allReviews;
    private List<Review> filteredReviews;
    private List<Vendor> availableVendors;
    private Vendor selectedVendor;
    private float selectedRating = 0f;
    private String currentFilter = "all";
    
    // Services
    private ReviewsManager reviewsManager;
    private SessionManager sessionManager;
    private DatabaseReference vendorsRef;
    private Handler mainHandler;
    
    // State
    private boolean isLoading = false;
    private Review editingReview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews_ratings_main);
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupRatingInput();
        setupClickListeners();
        setupRecyclerView();
        loadVendors();
        loadUserReviews();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        reviewsManager = ReviewsManager.getInstance(this);
        vendorsRef = FirebaseDatabase.getInstance().getReference("vendors");
        mainHandler = new Handler(Looper.getMainLooper());
        
        allReviews = new ArrayList<>();
        filteredReviews = new ArrayList<>();
        availableVendors = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        cardWriteReview = findViewById(R.id.card_write_review);
        actvRestaurant = findViewById(R.id.actv_restaurant);
        layoutStarRating = findViewById(R.id.layout_star_rating);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        tilReviewText = findViewById(R.id.til_review_text);
        etReviewText = findViewById(R.id.et_review_text);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        progressSubmitting = findViewById(R.id.progress_submitting);
        
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        chipAll = findViewById(R.id.chip_all);
        chipRecent = findViewById(R.id.chip_recent);
        chipHighRated = findViewById(R.id.chip_high_rated);
        
        rvReviews = findViewById(R.id.rv_reviews);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        fabWriteReview = findViewById(R.id.fab_write_review);
        
        // Initialize rating stars
        ratingStars = new ImageView[5];
        ratingStars[0] = findViewById(R.id.star_1);
        ratingStars[1] = findViewById(R.id.star_2);
        ratingStars[2] = findViewById(R.id.star_3);
        ratingStars[3] = findViewById(R.id.star_4);
        ratingStars[4] = findViewById(R.id.star_5);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Reviews & Ratings");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRatingInput() {
        for (int i = 0; i < ratingStars.length; i++) {
            final int starIndex = i;
            ratingStars[i].setOnClickListener(v -> {
                selectedRating = starIndex + 1;
                updateStarRating();
                updateRatingLabel();
                validateForm();
            });
        }
        
        // Add text watcher for review text
        etReviewText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        
        btnSubmitReview.setOnClickListener(v -> {
            if (editingReview != null) {
                updateReview();
            } else {
                submitReview();
            }
        });
        
        fabWriteReview.setOnClickListener(v -> scrollToWriteReview());
        
        // Filter chips
        chipAll.setOnClickListener(v -> applyFilter("all"));
        chipRecent.setOnClickListener(v -> applyFilter("recent"));
        chipHighRated.setOnClickListener(v -> applyFilter("high_rated"));
        
        // Restaurant selection
        actvRestaurant.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (Vendor vendor : availableVendors) {
                if (vendor.getName().equals(selectedName)) {
                    selectedVendor = vendor;
                    validateForm();
                    break;
                }
            }
        });
    }

    private void setupRecyclerView() {
        reviewsAdapter = new ReviewsAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewsAdapter);
        rvReviews.setNestedScrollingEnabled(false);
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
                    ReviewsRatingsMainActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    vendorNames
                );
                actvRestaurant.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load restaurants: " + error.getMessage());
            }
        });
    }

    private void loadUserReviews() {
        if (isLoading) return;
        
        isLoading = true;
        showLoading(true);
        
        String userId = sessionManager.getUserId();
        if (userId == null) {
            showError("User not logged in");
            showLoading(false);
            return;
        }
        
        // Load user's reviews from Firebase
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
            .getReference("reviews")
            .child("user_reviews")
            .child(userId);
            
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReviews.clear();
                
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        allReviews.add(review);
                    }
                }
                
                // Sort by timestamp (newest first)
                Collections.sort(allReviews, (r1, r2) -> 
                    Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                
                applyFilter(currentFilter);
                showLoading(false);
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load reviews: " + error.getMessage());
                showLoading(false);
            }
        });
    }

    private void submitReview() {
        if (!validateReviewData()) return;
        
        String reviewText = etReviewText.getText().toString().trim();
        
        showSubmitProgress(true);
        
        reviewsManager.submitReview(
            selectedVendor.getId(),
            null, // No specific item
            selectedVendor.getName(),
            selectedRating,
            reviewText,
            new ArrayList<>(), // No images for now
            false, // Not anonymous
            new ReviewsManager.ReviewCallback() {
                @Override
                public void onSuccess(String message) {
                    mainHandler.post(() -> {
                        showSubmitProgress(false);
                        showSuccessDialog("Review submitted successfully!");
                        clearReviewForm();
                        loadUserReviews(); // Refresh the list
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        showSubmitProgress(false);
                        showErrorDialog("Failed to submit review", error);
                    });
                }
            }
        );
    }

    private void updateReview() {
        if (!validateReviewData()) return;
        
        String reviewText = etReviewText.getText().toString().trim();
        
        showSubmitProgress(true);
        
        reviewsManager.updateReview(
            editingReview.getReviewId(),
            selectedRating,
            reviewText,
            new ArrayList<>(), // No images for now
            new ReviewsManager.ReviewCallback() {
                @Override
                public void onSuccess(String message) {
                    mainHandler.post(() -> {
                        showSubmitProgress(false);
                        showSuccessDialog("Review updated successfully!");
                        cancelEdit();
                        loadUserReviews(); // Refresh the list
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        showSubmitProgress(false);
                        showErrorDialog("Failed to update review", error);
                    });
                }
            }
        );
    }

    private void editReview(Review review) {
        editingReview = review;
        
        // Pre-fill form with existing data
        for (Vendor vendor : availableVendors) {
            if (vendor.getId().equals(review.getVendorId())) {
                selectedVendor = vendor;
                actvRestaurant.setText(vendor.getName());
                break;
            }
        }
        
        selectedRating = review.getRating();
        updateStarRating();
        updateRatingLabel();
        
        etReviewText.setText(review.getReviewText());
        
        // Update UI for edit mode
        btnSubmitReview.setText("Update Review");
        btnSubmitReview.setIcon(getDrawable(R.drawable.ic_edit));
        
        // Show cancel button (you can add this to layout)
        
        scrollToWriteReview();
        validateForm();
    }

    private void cancelEdit() {
        editingReview = null;
        btnSubmitReview.setText("Submit Review");
        btnSubmitReview.setIcon(getDrawable(R.drawable.ic_send));
        clearReviewForm();
    }

    private void deleteReview(Review review) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                reviewsManager.deleteReview(review.getReviewId(), new ReviewsManager.ReviewCallback() {
                    @Override
                    public void onSuccess(String message) {
                        mainHandler.post(() -> {
                            showSuccessSnackbar("Review deleted successfully");
                            loadUserReviews(); // Refresh the list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> showErrorDialog("Failed to delete review", error));
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private boolean validateReviewData() {
        if (selectedVendor == null) {
            showErrorDialog("Validation Error", "Please select a restaurant");
            return false;
        }
        
        if (selectedRating == 0) {
            showErrorDialog("Validation Error", "Please select a rating");
            return false;
        }
        
        String reviewText = etReviewText.getText().toString().trim();
        if (TextUtils.isEmpty(reviewText)) {
            showErrorDialog("Validation Error", "Please write a review");
            return false;
        }
        
        if (reviewText.length() < 10) {
            showErrorDialog("Validation Error", "Review must be at least 10 characters long");
            return false;
        }
        
        return true;
    }

    private void validateForm() {
        boolean isValid = selectedVendor != null && 
                         selectedRating > 0 && 
                         !TextUtils.isEmpty(etReviewText.getText().toString().trim());
        
        btnSubmitReview.setEnabled(isValid);
        btnSubmitReview.setAlpha(isValid ? 1.0f : 0.6f);
    }

    private void updateStarRating() {
        for (int i = 0; i < ratingStars.length; i++) {
            if (i < selectedRating) {
                ratingStars[i].setImageResource(R.drawable.ic_star_filled);
                ratingStars[i].setColorFilter(getColor(R.color.rating_star));
            } else {
                ratingStars[i].setImageResource(R.drawable.ic_star_outline);
                ratingStars[i].setColorFilter(getColor(R.color.gray_400));
            }
        }
        
        // Animate the selected star
        if (selectedRating > 0) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ratingStars[(int)selectedRating - 1], "scaleX", 1.0f, 1.2f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ratingStars[(int)selectedRating - 1], "scaleY", 1.0f, 1.2f, 1.0f);
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.start();
            scaleY.start();
        }
    }

    private void updateRatingLabel() {
        String[] ratingLabels = {"", "Poor", "Fair", "Good", "Very Good", "Excellent"};
        if (selectedRating > 0 && selectedRating <= 5) {
            tvRatingLabel.setText(ratingLabels[(int)selectedRating]);
            tvRatingLabel.setVisibility(View.VISIBLE);
        } else {
            tvRatingLabel.setVisibility(View.GONE);
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredReviews.clear();
        
        // Reset chip states
        chipAll.setChecked(false);
        chipRecent.setChecked(false);
        chipHighRated.setChecked(false);
        
        switch (filter) {
            case "all":
                filteredReviews.addAll(allReviews);
                chipAll.setChecked(true);
                break;
            case "recent":
                // Reviews from last 30 days
                long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
                for (Review review : allReviews) {
                    if (review.getTimestamp() > thirtyDaysAgo) {
                        filteredReviews.add(review);
                    }
                }
                chipRecent.setChecked(true);
                break;
            case "high_rated":
                // Reviews with 4+ stars
                for (Review review : allReviews) {
                    if (review.getRating() >= 4.0f) {
                        filteredReviews.add(review);
                    }
                }
                chipHighRated.setChecked(true);
                break;
        }
        
        reviewsAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void clearReviewForm() {
        actvRestaurant.setText("");
        selectedVendor = null;
        selectedRating = 0f;
        updateStarRating();
        updateRatingLabel();
        etReviewText.setText("");
        validateForm();
    }

    private void scrollToWriteReview() {
        cardWriteReview.requestFocus();
        // Use NestedScrollView for smooth scrolling
        NestedScrollView nestedScrollView = swipeRefresh.findViewById(R.id.nested_scroll_view);
        if (nestedScrollView != null) {
            nestedScrollView.smoothScrollTo(0, cardWriteReview.getTop());
        }
    }

    private void refreshData() {
        loadUserReviews();
        loadVendors();
    }

    private void showLoading(boolean show) {
        isLoading = show;
        swipeRefresh.setRefreshing(show);
    }

    private void showSubmitProgress(boolean show) {
        progressSubmitting.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmitReview.setEnabled(!show);
        btnSubmitReview.setText(show ? "Submitting..." : 
            (editingReview != null ? "Update Review" : "Submit Review"));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredReviews.isEmpty();
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Log.e(TAG, message);
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showErrorDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showSuccessDialog(String message) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showSuccessSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.success_color))
            .show();
    }

    // RecyclerView Adapter
    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_review_main, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            holder.bind(filteredReviews.get(position));
        }

        @Override
        public int getItemCount() {
            return filteredReviews.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            private MaterialCardView cardReview;
            private TextView tvRestaurantName;
            private TextView tvReviewDate;
            private TextView tvRatingValue;
            private ImageView[] stars;
            private TextView tvReviewText;
            private MaterialButton btnEdit;
            private MaterialButton btnDelete;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                
                cardReview = (MaterialCardView) itemView;
                tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
                tvReviewDate = itemView.findViewById(R.id.tv_review_date);
                tvRatingValue = itemView.findViewById(R.id.tv_rating_value);
                tvReviewText = itemView.findViewById(R.id.tv_review_text);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                
                // Initialize star rating views
                stars = new ImageView[5];
                stars[0] = itemView.findViewById(R.id.star_1);
                stars[1] = itemView.findViewById(R.id.star_2);
                stars[2] = itemView.findViewById(R.id.star_3);
                stars[3] = itemView.findViewById(R.id.star_4);
                stars[4] = itemView.findViewById(R.id.star_5);
            }

            public void bind(Review review) {
                // Set restaurant name
                String restaurantName = "Restaurant #" + review.getVendorId();
                for (Vendor vendor : availableVendors) {
                    if (vendor.getId().equals(review.getVendorId())) {
                        restaurantName = vendor.getName();
                        break;
                    }
                }
                tvRestaurantName.setText(restaurantName);

                // Set review date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(review.getTimestamp()));
                tvReviewDate.setText(formattedDate);

                // Set rating value
                tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));

                // Set star rating
                int fullStars = (int) review.getRating();
                for (int i = 0; i < stars.length; i++) {
                    if (i < fullStars) {
                        stars[i].setImageResource(R.drawable.ic_star_filled);
                        stars[i].setColorFilter(getColor(R.color.rating_star));
                    } else {
                        stars[i].setImageResource(R.drawable.ic_star_outline);
                        stars[i].setColorFilter(getColor(R.color.gray_400));
                    }
                }

                // Set review text
                tvReviewText.setText(review.getReviewText());

                // Set click listeners
                btnEdit.setOnClickListener(v -> editReview(review));
                btnDelete.setOnClickListener(v -> deleteReview(review));
                
                // Card click for expand/collapse (if needed)
                cardReview.setOnClickListener(v -> {
                    // Toggle review text expansion if needed
                });
            }
        }
    }
}
