package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Review;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying user's reviews in Settings Reviews & Ratings activity
 */
public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder> {

    private Context context;
    private List<Review> reviews;
    private OnReviewActionListener actionListener;

    public interface OnReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(Review review);
    }

    public UserReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    public void setOnReviewActionListener(OnReviewActionListener listener) {
        this.actionListener = listener;
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_review, parent, false);
        return new UserReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class UserReviewViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivRestaurantImage;
        private TextView tvRestaurantName;
        private TextView tvReviewDate;
        private TextView tvRatingValue;
        private TextView tvReviewText;
        private TextView tvHelpfulCount;
        private Chip chipReviewStatus;
        private MaterialButton btnEditReview;
        private MaterialButton btnDeleteReview;
        
        // Star rating views
        private ImageView[] stars = new ImageView[5];

        public UserReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivRestaurantImage = itemView.findViewById(R.id.iv_restaurant_image);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvRatingValue = itemView.findViewById(R.id.tv_rating_value);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            tvHelpfulCount = itemView.findViewById(R.id.tv_helpful_count);
            chipReviewStatus = itemView.findViewById(R.id.chip_review_status);
            btnEditReview = itemView.findViewById(R.id.btn_edit_review);
            btnDeleteReview = itemView.findViewById(R.id.btn_delete_review);
            
            // Initialize star rating views
            stars[0] = itemView.findViewById(R.id.star_1);
            stars[1] = itemView.findViewById(R.id.star_2);
            stars[2] = itemView.findViewById(R.id.star_3);
            stars[3] = itemView.findViewById(R.id.star_4);
            stars[4] = itemView.findViewById(R.id.star_5);
        }

        public void bind(Review review) {
            // Set restaurant name (use vendor ID as fallback since getVendorName doesn't exist)
            String restaurantName = "Restaurant #" + review.getVendorId();
            tvRestaurantName.setText(restaurantName);

            // Set review date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(review.getTimestamp()));
            tvReviewDate.setText(formattedDate);

            // Set rating value
            tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f", review.getRating()));

            // Set star rating display
            int fullStars = (int) review.getRating();
            for (int i = 0; i < stars.length; i++) {
                if (i < fullStars) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                    stars[i].setImageTintList(context.getColorStateList(R.color.colorPrimary));
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_outline);
                    stars[i].setImageTintList(context.getColorStateList(R.color.colorOutline));
                }
            }

            // Set review text
            tvReviewText.setText(review.getReviewText());

            // Set helpful count
            int helpfulCount = review.getHelpfulCount();
            if (helpfulCount > 0) {
                tvHelpfulCount.setText(helpfulCount + " helpful");
            } else {
                tvHelpfulCount.setText("0 helpful");
            }

            // Set review status
            String status = review.getStatus();
            if (status == null) status = "ACTIVE";
            
            switch (status.toUpperCase()) {
                case "ACTIVE":
                case "PUBLISHED":
                    chipReviewStatus.setText("Published");
                    chipReviewStatus.setChipIconResource(R.drawable.ic_check_circle);
                    chipReviewStatus.setChipBackgroundColor(context.getColorStateList(R.color.colorPrimaryContainer));
                    chipReviewStatus.setTextColor(context.getColor(R.color.colorOnPrimaryContainer));
                    break;
                case "PENDING":
                    chipReviewStatus.setText("Pending");
                    chipReviewStatus.setChipIconResource(R.drawable.ic_schedule);
                    chipReviewStatus.setChipBackgroundColor(context.getColorStateList(R.color.colorSecondaryContainer));
                    chipReviewStatus.setTextColor(context.getColor(R.color.colorOnSecondaryContainer));
                    break;
                case "REJECTED":
                    chipReviewStatus.setText("Rejected");
                    chipReviewStatus.setChipIconResource(R.drawable.ic_error);
                    chipReviewStatus.setChipBackgroundColor(context.getColorStateList(R.color.colorErrorContainer));
                    chipReviewStatus.setTextColor(context.getColor(R.color.colorOnErrorContainer));
                    break;
                default:
                    chipReviewStatus.setText("Unknown");
                    chipReviewStatus.setChipIconResource(R.drawable.ic_help);
                    break;
            }

            // Set restaurant image (placeholder for now)
            ivRestaurantImage.setImageResource(R.drawable.placeholder_restaurant);

            // Set click listeners
            btnEditReview.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEditReview(review);
                }
            });

            btnDeleteReview.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDeleteReview(review);
                }
            });

            // Disable edit/delete for rejected reviews
            boolean canModify = !"REJECTED".equals(status.toUpperCase());
            btnEditReview.setEnabled(canModify);
            btnDeleteReview.setEnabled(canModify);
            
            if (!canModify) {
                btnEditReview.setAlpha(0.5f);
                btnDeleteReview.setAlpha(0.5f);
            } else {
                btnEditReview.setAlpha(1.0f);
                btnDeleteReview.setAlpha(1.0f);
            }
        }
    }
}
