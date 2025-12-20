package com.example.foodvan.activities.customer;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Review;
import com.example.foodvan.utils.ReviewsManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for writing and editing reviews with Material Design 3 styling
 */
public class WriteReviewDialog extends Dialog {

    private Context context;
    private String vendorId;
    private String vendorName;
    private Review existingReview; // For editing
    private boolean isEditMode;

    // UI Components
    private TextView tvDialogTitle;
    private TextView tvDialogSubtitle;
    private LinearLayout layoutStarRating;
    private ImageView[] ratingStars;
    private TextView tvRatingLabel;
    private TextInputLayout tilReviewText;
    private TextInputEditText etReviewText;
    private MaterialButton btnAddPhotos;
    private RecyclerView rvSelectedPhotos;
    private MaterialCheckBox cbAnonymous;
    private MaterialButton btnCancel;
    private MaterialButton btnSubmitReview;
    private CircularProgressIndicator progressSubmitting;

    // Data
    private float selectedRating = 0f;
    private List<String> selectedPhotoUrls;
    private ReviewsManager reviewsManager;
    private SessionManager sessionManager;

    // Callback
    private OnReviewSubmittedListener listener;

    public interface OnReviewSubmittedListener {
        void onReviewSubmitted(Review review);
    }

    public WriteReviewDialog(@NonNull Context context, String vendorId, String vendorName) {
        super(context);
        this.context = context;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.isEditMode = false;
        initialize();
    }

    public WriteReviewDialog(@NonNull Context context, String vendorId, String vendorName, Review existingReview) {
        super(context);
        this.context = context;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.existingReview = existingReview;
        this.isEditMode = true;
        initialize();
    }

    private void initialize() {
        reviewsManager = ReviewsManager.getInstance(context);
        sessionManager = new SessionManager(context);
        selectedPhotoUrls = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Remove default dialog styling
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.dialog_write_review);
        
        // Set dialog properties
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
        
        initializeViews();
        setupClickListeners();
        setupTextWatcher();
        
        if (isEditMode && existingReview != null) {
            populateExistingReview();
        }
        
        updateSubmitButtonState();
    }

    private void initializeViews() {
        tvDialogTitle = findViewById(R.id.tv_dialog_title);
        tvDialogSubtitle = findViewById(R.id.tv_dialog_subtitle);
        layoutStarRating = findViewById(R.id.layout_star_rating);
        tvRatingLabel = findViewById(R.id.tv_rating_label);
        tilReviewText = findViewById(R.id.til_review_text);
        etReviewText = findViewById(R.id.et_review_text);
        btnAddPhotos = findViewById(R.id.btn_add_photos);
        rvSelectedPhotos = findViewById(R.id.rv_selected_photos);
        cbAnonymous = findViewById(R.id.cb_anonymous);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSubmitReview = findViewById(R.id.btn_submit_review);

        // Initialize rating stars
        ratingStars = new ImageView[5];
        ratingStars[0] = findViewById(R.id.star_rating_1);
        ratingStars[1] = findViewById(R.id.star_rating_2);
        ratingStars[2] = findViewById(R.id.star_rating_3);
        ratingStars[3] = findViewById(R.id.star_rating_4);
        ratingStars[4] = findViewById(R.id.star_rating_5);

        // Set dialog title based on mode
        if (isEditMode) {
            tvDialogTitle.setText("Edit Review");
            tvDialogSubtitle.setText("Update your experience to help others");
            btnSubmitReview.setText("Update Review");
        } else {
            tvDialogTitle.setText("Write a Review");
            tvDialogSubtitle.setText("Share your experience to help others make better choices");
            btnSubmitReview.setText("Submit Review");
        }
    }

    private void setupClickListeners() {
        // Star rating clicks
        for (int i = 0; i < ratingStars.length; i++) {
            final int starIndex = i;
            ratingStars[i].setOnClickListener(v -> {
                setRating(starIndex + 1);
                animateStarSelection(starIndex);
            });
        }

        // Add photos button
        btnAddPhotos.setOnClickListener(v -> {
            // TODO: Implement photo selection
            showPhotoSelectionDialog();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());

        // Submit button
        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void setupTextWatcher() {
        etReviewText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void populateExistingReview() {
        if (existingReview == null) return;

        // Set rating
        setRating((int) existingReview.getRating());

        // Set review text
        etReviewText.setText(existingReview.getReviewText());

        // Set anonymous checkbox
        cbAnonymous.setChecked(existingReview.isAnonymous());

        // Set photos if any
        if (existingReview.hasImages()) {
            selectedPhotoUrls.addAll(existingReview.getImageUrls());
            updatePhotosDisplay();
        }
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStarDisplay();
        updateRatingLabel();
        updateSubmitButtonState();
    }

    private void updateStarDisplay() {
        for (int i = 0; i < ratingStars.length; i++) {
            if (i < selectedRating) {
                ratingStars[i].setImageResource(R.drawable.ic_star_filled);
                ratingStars[i].setColorFilter(context.getColor(R.color.star_filled));
            } else {
                ratingStars[i].setImageResource(R.drawable.ic_star_outline);
                ratingStars[i].setColorFilter(context.getColor(R.color.star_outline));
            }
        }
    }

    private void updateRatingLabel() {
        String[] ratingLabels = {
            "Tap to rate",
            "Poor",
            "Fair", 
            "Good",
            "Very Good",
            "Excellent"
        };
        
        int labelIndex = (int) selectedRating;
        if (labelIndex >= 0 && labelIndex < ratingLabels.length) {
            tvRatingLabel.setText(ratingLabels[labelIndex]);
        }
    }

    private void updateSubmitButtonState() {
        boolean hasRating = selectedRating > 0;
        boolean hasText = etReviewText.getText() != null && 
                         !etReviewText.getText().toString().trim().isEmpty();
        
        // Enable submit if has rating (text is optional)
        btnSubmitReview.setEnabled(hasRating);
    }

    private void animateStarSelection(int selectedIndex) {
        // Animate the selected star
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ratingStars[selectedIndex], "scaleX", 1.0f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ratingStars[selectedIndex], "scaleY", 1.0f, 1.3f, 1.0f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        
        scaleX.start();
        scaleY.start();
    }

    private void showPhotoSelectionDialog() {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Add Photos")
            .setMessage("Photo upload functionality will be available soon.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void updatePhotosDisplay() {
        if (selectedPhotoUrls.isEmpty()) {
            rvSelectedPhotos.setVisibility(View.GONE);
        } else {
            rvSelectedPhotos.setVisibility(View.VISIBLE);
            // TODO: Set up photo adapter
        }
    }

    private void submitReview() {
        if (selectedRating == 0) {
            tvRatingLabel.setText("Please select a rating");
            return;
        }

        String reviewText = etReviewText.getText() != null ? 
                           etReviewText.getText().toString().trim() : "";
        boolean isAnonymous = cbAnonymous.isChecked();

        // Show loading
        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Submitting...");

        if (isEditMode && existingReview != null) {
            // Update existing review
            reviewsManager.updateReview(
                existingReview.getReviewId(),
                selectedRating,
                reviewText,
                selectedPhotoUrls,
                new ReviewsManager.ReviewCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Update the existing review object
                        existingReview.setRating(selectedRating);
                        existingReview.setReviewText(reviewText);
                        existingReview.setImageUrls(selectedPhotoUrls);
                        existingReview.setAnonymous(isAnonymous);
                        
                        if (listener != null) {
                            listener.onReviewSubmitted(existingReview);
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        showError(error);
                        resetSubmitButton();
                    }
                }
            );
        } else {
            // Submit new review
            reviewsManager.submitReview(
                vendorId,
                vendorId, // Using vendorId as itemId for vendor reviews
                vendorName,
                selectedRating,
                reviewText,
                selectedPhotoUrls,
                isAnonymous,
                new ReviewsManager.ReviewCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Create review object for callback
                        Review newReview = new Review();
                        newReview.setReviewId("temp_" + System.currentTimeMillis()); // Temporary ID
                        newReview.setUserId(sessionManager.getUserId());
                        newReview.setUserName(sessionManager.getUserName());
                        newReview.setVendorId(vendorId);
                        newReview.setItemId(vendorId);
                        newReview.setItemName(vendorName);
                        newReview.setRating(selectedRating);
                        newReview.setReviewText(reviewText);
                        newReview.setImageUrls(selectedPhotoUrls);
                        newReview.setAnonymous(isAnonymous);
                        newReview.setTimestamp(System.currentTimeMillis());
                        
                        if (listener != null) {
                            listener.onReviewSubmitted(newReview);
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        showError(error);
                        resetSubmitButton();
                    }
                }
            );
        }
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void resetSubmitButton() {
        btnSubmitReview.setEnabled(true);
        btnSubmitReview.setText(isEditMode ? "Update Review" : "Submit Review");
    }

    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }
}
