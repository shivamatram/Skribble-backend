package com.example.foodvan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.foodvan.database.ReviewDao;
import com.example.foodvan.database.ReviewEntity;
import com.example.foodvan.database.ReviewMetaDao;
import com.example.foodvan.database.ReviewMetaEntity;
import com.example.foodvan.repositories.ReviewRepository;

import java.util.List;

/**
 * ViewModel for managing reviews and ratings data
 */
public class ReviewViewModel extends AndroidViewModel {
    private static final String TAG = "ReviewViewModel";

    private final ReviewRepository reviewRepository;
    private final MutableLiveData<String> currentVendorId = new MutableLiveData<>();
    private final MutableLiveData<ReviewFilter> currentFilter = new MutableLiveData<>(ReviewFilter.ALL);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    
    // Filter and search results
    private final MediatorLiveData<List<ReviewEntity>> filteredReviews = new MediatorLiveData<>();

    public ReviewViewModel(@NonNull Application application, ReviewRepository reviewRepository) {
        super(application);
        this.reviewRepository = reviewRepository;
        setupFilteredReviews();
    }

    // Filter enum
    public enum ReviewFilter {
        ALL, FIVE_STAR, FOUR_STAR, THREE_STAR, TWO_STAR, ONE_STAR, WITH_REPLIES, WITHOUT_REPLIES
    }

    // Setup filtered reviews based on current filter and search query
    private void setupFilteredReviews() {
        filteredReviews.addSource(currentVendorId, vendorId -> updateFilteredReviews());
        filteredReviews.addSource(currentFilter, filter -> updateFilteredReviews());
        filteredReviews.addSource(searchQuery, query -> updateFilteredReviews());
    }

    private void updateFilteredReviews() {
        String vendorId = currentVendorId.getValue();
        ReviewFilter filter = currentFilter.getValue();
        String query = searchQuery.getValue();

        if (vendorId == null) return;

        LiveData<List<ReviewEntity>> source;

        // First check if we have a search query
        if (query != null && !query.trim().isEmpty()) {
            source = reviewRepository.searchReviews(vendorId, query.trim());
        } else {
            // Apply filter
            switch (filter != null ? filter : ReviewFilter.ALL) {
                case FIVE_STAR:
                    source = reviewRepository.getReviewsByRating(vendorId, 5);
                    break;
                case FOUR_STAR:
                    source = reviewRepository.getReviewsByRating(vendorId, 4);
                    break;
                case THREE_STAR:
                    source = reviewRepository.getReviewsByRating(vendorId, 3);
                    break;
                case TWO_STAR:
                    source = reviewRepository.getReviewsByRating(vendorId, 2);
                    break;
                case ONE_STAR:
                    source = reviewRepository.getReviewsByRating(vendorId, 1);
                    break;
                case WITH_REPLIES:
                    source = reviewRepository.getReviewsWithReplies(vendorId);
                    break;
                case WITHOUT_REPLIES:
                    source = reviewRepository.getReviewsWithoutReplies(vendorId);
                    break;
                default:
                    source = reviewRepository.getReviewsForVendor(vendorId);
                    break;
            }
        }

        filteredReviews.removeSource(source);
        filteredReviews.addSource(source, filteredReviews::setValue);
    }

    // Public methods
    public void setVendorId(String vendorId) {
        currentVendorId.setValue(vendorId);
        // Sync data when vendor ID is set
        if (vendorId != null) {
            syncReviewsFromFirestore();
            syncReviewMetaFromFirestore();
        }
    }

    public void setFilter(ReviewFilter filter) {
        currentFilter.setValue(filter);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void clearSearch() {
        searchQuery.setValue("");
    }

    // LiveData getters
    public LiveData<List<ReviewEntity>> getFilteredReviews() {
        return filteredReviews;
    }

    public LiveData<ReviewMetaEntity> getReviewMeta() {
        return Transformations.switchMap(currentVendorId, vendorId -> {
            if (vendorId != null) {
                return reviewRepository.getReviewMetaForVendor(vendorId);
            }
            return new MutableLiveData<>(null);
        });
    }

    public LiveData<String> getErrorLiveData() {
        return reviewRepository.getErrorLiveData();
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return reviewRepository.getIsLoadingLiveData();
    }

    public LiveData<String> getCurrentVendorId() {
        return currentVendorId;
    }

    public LiveData<ReviewFilter> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    // Repository operations
    public void syncReviewsFromFirestore() {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.syncReviewsFromFirestore(vendorId);
        }
    }

    public void syncReviewMetaFromFirestore() {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.syncReviewMetaFromFirestore(vendorId);
        }
    }

    public void addVendorReply(String reviewId, String replyText, String vendorName) {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.addVendorReply(vendorId, reviewId, replyText, vendorName);
        }
    }

    public void editVendorReply(String reviewId, String newReplyText) {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.editVendorReply(vendorId, reviewId, newReplyText);
        }
    }

    public void deleteVendorReply(String reviewId) {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.deleteVendorReply(vendorId, reviewId);
        }
    }

    public void flagReview(String reviewId, String reason) {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.flagReview(vendorId, reviewId, reason);
        }
    }

    public void softDeleteReview(String reviewId, String reason) {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.softDeleteReview(vendorId, reviewId, reason);
        }
    }

    public void recalculateReviewStats() {
        String vendorId = currentVendorId.getValue();
        if (vendorId != null) {
            reviewRepository.recalculateReviewStats(vendorId);
        }
    }

    public void clearError() {
        reviewRepository.clearError();
    }

    // Utility methods
    public String getFilterDisplayName(ReviewFilter filter) {
        switch (filter) {
            case ALL:
                return "All Reviews";
            case FIVE_STAR:
                return "5 Stars";
            case FOUR_STAR:
                return "4 Stars";
            case THREE_STAR:
                return "3 Stars";
            case TWO_STAR:
                return "2 Stars";
            case ONE_STAR:
                return "1 Star";
            case WITH_REPLIES:
                return "With Replies";
            case WITHOUT_REPLIES:
                return "Without Replies";
            default:
                return "All Reviews";
        }
    }

    public boolean isSearchActive() {
        String query = searchQuery.getValue();
        return query != null && !query.trim().isEmpty();
    }

    public boolean hasActiveFilter() {
        ReviewFilter filter = currentFilter.getValue();
        return filter != null && filter != ReviewFilter.ALL;
    }

    public void resetFilters() {
        currentFilter.setValue(ReviewFilter.ALL);
        searchQuery.setValue("");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}
