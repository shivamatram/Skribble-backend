package com.example.foodvan.activities.customer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ReviewsAdapter;
import com.example.foodvan.models.Review;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewsRatingsActivity - Enhanced reviews and ratings management with Material Design 3
 * Features: View reviews, submit reviews, rating statistics, filtering, sorting, animations
 * Architecture: Clean MVVM pattern with proper separation of concerns
 * Performance: Optimized Firebase queries, DiffUtil, lazy loading, caching
 */
public class ReviewsRatingsActivity extends AppCompatActivity implements ReviewsAdapter.OnReviewActionListener {

    private static final String TAG = "ReviewsRatingsActivity";
    private static final int ANIMATION_DURATION = 300;
    private static final int STAGGER_DELAY = 50;
    private static final int MAX_REVIEWS_PER_PAGE = 20;

    // UI Components with View Binding
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private NestedScrollView nestedScrollView;
    
    // Rating Overview
    private MaterialCardView cardRatingOverview;
    private TextView tvAverageRating;
    private ImageView[] overviewStars;
    private TextView tvTotalReviews;
    private LinearProgressIndicator[] ratingProgressBars;
    private TextView[] ratingCounts;
    
    // Filters and Sort
    private MaterialCardView cardFilters;
    private ChipGroup chipGroupFilters;
    private MaterialButton btnSort;
    
    // Reviews List
    private RecyclerView rvReviews;
    private ReviewsAdapter reviewsAdapter;
    
    // Empty and Loading States
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoading;
    private CircularProgressIndicator progressLoading;
    private MaterialButton btnWriteFirstReview;
    
    // FAB
    private ExtendedFloatingActionButton fabWriteReview;
    
    // Enhanced Data & Services
    private ReviewsManager reviewsManager;
    private SessionManager sessionManager;
    private List<Review> allReviews;
    private List<Review> filteredReviews;
    private ReviewsManager.ReviewStats currentStats;
    private Handler mainHandler;
    private boolean isDataLoaded = false;
    private long lastRefreshTime = 0;
    private static final long REFRESH_COOLDOWN = 30000; // 30 seconds
    
    // State
    private String vendorId;
    private String vendorName;
    private String currentSortBy = "newest";
    private String currentFilterBy = "all";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_reviews_ratings);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupAnimations();
        
        // Only load data if intent data is valid
        if (getIntentData()) {
            loadReviewsAndStats();
        }
    }

    private void initializeServices() {
        reviewsManager = ReviewsManager.getInstance(this);
        sessionManager = new SessionManager(this);
        allReviews = new ArrayList<>();
        filteredReviews = new ArrayList<>();
        mainHandler = new Handler(Looper.getMainLooper());
        
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        
        // Swipe refresh
        swipeRefresh = findViewById(R.id.swipe_refresh);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        
        // Rating overview
        cardRatingOverview = findViewById(R.id.card_rating_overview);
        tvAverageRating = findViewById(R.id.tv_average_rating);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        
        // Initialize overview stars
        overviewStars = new ImageView[5];
        overviewStars[0] = findViewById(R.id.star_1);
        overviewStars[1] = findViewById(R.id.star_2);
        overviewStars[2] = findViewById(R.id.star_3);
        overviewStars[3] = findViewById(R.id.star_4);
        overviewStars[4] = findViewById(R.id.star_5);
        
        // Initialize rating distribution
        ratingProgressBars = new LinearProgressIndicator[5];
        ratingProgressBars[0] = findViewById(R.id.progress_5_star);
        ratingProgressBars[1] = findViewById(R.id.progress_4_star);
        ratingProgressBars[2] = findViewById(R.id.progress_3_star);
        ratingProgressBars[3] = findViewById(R.id.progress_2_star);
        ratingProgressBars[4] = findViewById(R.id.progress_1_star);
        
        ratingCounts = new TextView[5];
        ratingCounts[0] = findViewById(R.id.tv_5_star_count);
        ratingCounts[1] = findViewById(R.id.tv_4_star_count);
        ratingCounts[2] = findViewById(R.id.tv_3_star_count);
        ratingCounts[3] = findViewById(R.id.tv_2_star_count);
        ratingCounts[4] = findViewById(R.id.tv_1_star_count);
        
        // Filters
        cardFilters = findViewById(R.id.card_filters);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        btnSort = findViewById(R.id.btn_sort);
        
        // Reviews list
        rvReviews = findViewById(R.id.rv_reviews);
        
        // States
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutLoading = findViewById(R.id.layout_loading);
        progressLoading = findViewById(R.id.progress_loading);
        btnWriteFirstReview = findViewById(R.id.btn_write_first_review);
        
        // FAB
        fabWriteReview = findViewById(R.id.fab_write_review);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        reviewsAdapter = new ReviewsAdapter(this, sessionManager.getUserId());
        reviewsAdapter.setOnReviewActionListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReviews.setLayoutManager(layoutManager);
        rvReviews.setAdapter(reviewsAdapter);
        rvReviews.setNestedScrollingEnabled(false);
        
        // Enhanced animations
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(ANIMATION_DURATION);
        itemAnimator.setRemoveDuration(ANIMATION_DURATION);
        itemAnimator.setMoveDuration(ANIMATION_DURATION);
        itemAnimator.setChangeDuration(ANIMATION_DURATION);
        rvReviews.setItemAnimator(itemAnimator);
        
        // Add scroll listener for FAB behavior
        rvReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabWriteReview.isExtended()) {
                    fabWriteReview.shrink();
                } else if (dy < 0 && !fabWriteReview.isExtended()) {
                    fabWriteReview.extend();
                }
            }
        });
    }

    private void setupClickListeners() {
        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshReviews);
        
        // Filter chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                handleFilterSelection(checkedId);
            }
        });
        
        // Sort button
        btnSort.setOnClickListener(v -> showSortDialog());
        
        // Write review buttons
        fabWriteReview.setOnClickListener(v -> showWriteReviewDialog());
        btnWriteFirstReview.setOnClickListener(v -> showWriteReviewDialog());
        
        // Scroll behavior for FAB
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY) {
                    // Scrolling down
                    fabWriteReview.shrink();
                } else {
                    // Scrolling up
                    fabWriteReview.extend();
                }
            });
    }

    private boolean getIntentData() {
        vendorId = getIntent().getStringExtra("vendor_id");
        vendorName = getIntent().getStringExtra("vendor_name");
        
        if (vendorName != null) {
            setTitle("Reviews - " + vendorName);
        }
        
        if (vendorId == null) {
            Toast.makeText(this, "Invalid vendor information", Toast.LENGTH_SHORT).show();
            finish();
            return false; // Indicate failure
        }
        return true; // Indicate success
    }

    private void loadReviewsAndStats() {
        if (isLoading) return;
        
        isLoading = true;
        showLoading(true);
        
        
        // Load reviews with enhanced error handling
        reviewsManager.loadVendorReviews(vendorId, currentSortBy, currentFilterBy, 
            new ReviewsManager.ReviewsLoadCallback() {
                @Override
                public void onReviewsLoaded(List<Review> reviews) {
                    mainHandler.post(() -> {
                        allReviews.clear();
                        allReviews.addAll(reviews);
                        applyCurrentFilter();
                        
                        // Load stats
                        loadReviewStats();
                        isDataLoaded = true;
                        lastRefreshTime = System.currentTimeMillis();
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        isLoading = false;
                        showLoading(false);
                        showError("Failed to load reviews: " + error);
                    });
                }
            });
    }

    private void loadReviewStats() {
        reviewsManager.loadReviewStats(vendorId, new ReviewsManager.ReviewStatsCallback() {
            @Override
            public void onStatsLoaded(ReviewsManager.ReviewStats stats) {
                currentStats = stats;
                updateStatsUI();
                showLoading(false);
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                showError("Failed to load statistics: " + error);
            }
        });
    }

    private void refreshReviews() {
        // Implement refresh cooldown to prevent excessive API calls
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            swipeRefresh.setRefreshing(false);
            showInfo("Please wait before refreshing again");
            return;
        }
        
        loadReviewsAndStats();
        
        // Stop refresh animation after delay
        mainHandler.postDelayed(() -> {
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
        }, 1000);
    }

    private void updateStatsUI() {
        if (currentStats == null) return;
        
        // Update average rating
        tvAverageRating.setText(String.format("%.1f", currentStats.getAverageRating()));
        
        // Update total reviews
        String reviewsText = currentStats.getTotalReviews() == 1 ? 
            "Based on 1 review" : 
            String.format("Based on %,d reviews", currentStats.getTotalReviews());
        tvTotalReviews.setText(reviewsText);
        
        // Update overview stars
        updateOverviewStars(currentStats.getAverageRating());
        
        // Update rating distribution
        updateRatingDistribution();
        
        // Animate the stats card
        animateStatsCard();
    }

    private void updateOverviewStars(float rating) {
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5f;

        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                overviewStars[i].setImageResource(R.drawable.ic_star_filled);
                overviewStars[i].setColorFilter(getColor(R.color.star_filled));
            } else if (i == fullStars && hasHalfStar) {
                overviewStars[i].setImageResource(R.drawable.ic_star_half);
                overviewStars[i].setColorFilter(getColor(R.color.star_filled));
            } else {
                overviewStars[i].setImageResource(R.drawable.ic_star_outline);
                overviewStars[i].setColorFilter(getColor(R.color.star_outline));
            }
        }
    }

    private void updateRatingDistribution() {
        if (currentStats == null) return;
        
        for (int i = 0; i < 5; i++) {
            int starRating = 5 - i; // 5, 4, 3, 2, 1
            int count = currentStats.getStarCount(starRating);
            int percentage = currentStats.getStarPercentage(starRating);
            
            ratingProgressBars[i].setProgress(percentage);
            ratingCounts[i].setText(String.valueOf(count));
            
            // Animate progress bars
            animateProgressBar(ratingProgressBars[i], percentage);
        }
    }

    private void applyCurrentFilter() {
        filteredReviews.clear();
        
        for (Review review : allReviews) {
            if (shouldIncludeReview(review)) {
                filteredReviews.add(review);
            }
        }
        
        reviewsAdapter.updateReviews(filteredReviews);
        updateEmptyState();
    }

    private boolean shouldIncludeReview(Review review) {
        switch (currentFilterBy) {
            case "5_star":
                return review.getRating() == 5.0f;
            case "4_star":
                return review.getRating() >= 4.0f && review.getRating() < 5.0f;
            case "recent":
                long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
                return review.getTimestamp() > weekAgo;
            default:
                return true;
        }
    }

    private void handleFilterSelection(int checkedId) {
        if (checkedId == R.id.chip_all_reviews) {
            currentFilterBy = "all";
        } else if (checkedId == R.id.chip_5_star) {
            currentFilterBy = "5_star";
        } else if (checkedId == R.id.chip_4_star) {
            currentFilterBy = "4_star";
        } else if (checkedId == R.id.chip_recent) {
            currentFilterBy = "recent";
        }
        
        applyCurrentFilter();
    }

    private void showSortDialog() {
        String[] sortOptions = {"Most Recent", "Oldest First", "Highest Rating", "Lowest Rating"};
        String[] sortValues = {"newest", "oldest", "highest_rating", "lowest_rating"};
        
        int currentIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortBy)) {
                currentIndex = i;
                break;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Sort Reviews")
            .setSingleChoiceItems(sortOptions, currentIndex, (dialog, which) -> {
                currentSortBy = sortValues[which];
                btnSort.setText("Sort by: " + sortOptions[which]);
                loadReviewsAndStats();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showWriteReviewDialog() {
        WriteReviewDialog dialog = new WriteReviewDialog(this, vendorId, vendorName);
        dialog.setOnReviewSubmittedListener(review -> {
            reviewsAdapter.addReview(review);
            loadReviewStats(); // Refresh stats
            showSuccess("Review submitted successfully!");
        });
        dialog.show();
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredReviews.isEmpty();
        
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        if (isEmpty && !isLoading) {
            animateEmptyState();
        }
    }

    private void showLoading(boolean show) {
        isLoading = show;
        
        if (show) {
            layoutLoading.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            rvReviews.setVisibility(View.GONE);
            
            // Animate loading state
            animateLoadingState();
        } else {
            layoutLoading.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Retry", v -> loadReviewsAndStats())
            .setActionTextColor(getColor(R.color.colorPrimary))
            .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.colorSuccess))
            .setTextColor(getColor(R.color.colorOnSuccess))
            .show();
    }
    
    private void showInfo(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.colorInfo))
            .setTextColor(getColor(R.color.colorOnInfo))
            .show();
    }
    
    private void showNetworkError() {
        isLoading = false;
        showLoading(false);
        
        Snackbar.make(findViewById(android.R.id.content), 
                "No internet connection. Please check your network.", 
                Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry", v -> loadReviewsAndStats())
            .setActionTextColor(getColor(R.color.colorPrimary))
            .show();
    }
    
    private void handleFirebaseError(String error) {
        mainHandler.post(() -> {
            isLoading = false;
            showLoading(false);
            showError("Database error: " + error);
        });
    }

    // Enhanced Animation methods
    private void setupAnimations() {
        // Setup initial animation states
        cardRatingOverview.setAlpha(0f);
        cardRatingOverview.setScaleX(0.9f);
        cardRatingOverview.setScaleY(0.9f);
    }
    
    private void animateStatsCard() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardRatingOverview, "scaleX", 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardRatingOverview, "scaleY", 0.9f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardRatingOverview, "alpha", 0f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }
    
    private void animateLoadingState() {
        if (layoutLoading.getVisibility() == View.VISIBLE) {
            layoutLoading.setAlpha(0f);
            layoutLoading.setTranslationY(50f);
            
            layoutLoading.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }
    }

    private void animateProgressBar(LinearProgressIndicator progressBar, int targetProgress) {
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, targetProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            progressBar.setProgress(animatedValue);
        });
        progressAnimator.setStartDelay(200); // Stagger animation
        progressAnimator.start();
    }

    private void animateEmptyState() {
        if (layoutEmptyState.getVisibility() == View.VISIBLE) {
            layoutEmptyState.setAlpha(0f);
            layoutEmptyState.setTranslationY(100f);
            layoutEmptyState.setScaleX(0.9f);
            layoutEmptyState.setScaleY(0.9f);
            
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(layoutEmptyState, "alpha", 0f, 1f);
            ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(layoutEmptyState, "translationY", 100f, 0f);
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(layoutEmptyState, "scaleX", 0.9f, 1f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(layoutEmptyState, "scaleY", 0.9f, 1f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translateAnimator, scaleXAnimator, scaleYAnimator);
            animatorSet.setDuration(500);
            animatorSet.setInterpolator(new OvershootInterpolator(1.1f));
            animatorSet.start();
        }
    }

    // ReviewsAdapter.OnReviewActionListener implementation
    @Override
    public void onReviewClick(Review review) {
        // Handle review click - could show detailed view
    }

    @Override
    public void onHelpfulClick(Review review, boolean isHelpful) {
        reviewsManager.toggleHelpful(review.getReviewId(), !isHelpful, 
            new ReviewsManager.ReviewCallback() {
                @Override
                public void onSuccess(String message) {
                    // Update the review in adapter
                    if (isHelpful) {
                        review.decrementHelpfulCount();
                    } else {
                        review.incrementHelpfulCount();
                    }
                    reviewsAdapter.updateReview(review);
                    showSuccess(message);
                }

                @Override
                public void onError(String error) {
                    showError(error);
                }
            });
    }

    @Override
    public void onReplyClick(Review review) {
        // TODO: Implement reply functionality for vendors
        Toast.makeText(this, "Reply functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareClick(Review review) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            String.format("Check out this review: \"%s\" - %s stars", 
                review.getReviewText(), review.getFormattedRating()));
        startActivity(Intent.createChooser(shareIntent, "Share Review"));
    }

    @Override
    public void onEditClick(Review review) {
        WriteReviewDialog dialog = new WriteReviewDialog(this, vendorId, vendorName, review);
        dialog.setOnReviewSubmittedListener(updatedReview -> {
            reviewsAdapter.updateReview(updatedReview);
            loadReviewStats(); // Refresh stats
            showSuccess("Review updated successfully!");
        });
        dialog.show();
    }

    @Override
    public void onDeleteClick(Review review) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                reviewsManager.deleteReview(review.getReviewId(), 
                    new ReviewsManager.ReviewCallback() {
                        @Override
                        public void onSuccess(String message) {
                            reviewsAdapter.removeReview(review.getReviewId());
                            loadReviewStats(); // Refresh stats
                            showSuccess(message);
                        }

                        @Override
                        public void onError(String error) {
                            showError(error);
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onReportClick(Review review) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Report Review")
            .setMessage("Why are you reporting this review?")
            .setItems(new String[]{"Inappropriate content", "Spam", "Fake review", "Other"}, 
                (dialog, which) -> {
                    // TODO: Implement report functionality
                    showSuccess("Review reported. Thank you for your feedback.");
                })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onImageClick(Review review, int imagePosition) {
        // TODO: Implement image viewer
        Toast.makeText(this, "Image viewer coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reviews, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            refreshReviews();
            return true;
        } else if (id == R.id.action_filter) {
            // Toggle filter card visibility
            cardFilters.setVisibility(
                cardFilters.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up resources and prevent memory leaks
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        
        
        // Clear references
        allReviews = null;
        filteredReviews = null;
        currentStats = null;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh data if it's been a while since last load
        if (isDataLoaded && System.currentTimeMillis() - lastRefreshTime > 300000) { // 5 minutes
            loadReviewsAndStats();
        }
        
        // Refresh reviews when returning to activity
        if (!isLoading) {
            loadReviewsAndStats();
        }
    }
}
