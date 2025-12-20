package com.example.foodvan.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.Review;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying reviews in RecyclerView with Material Design 3 styling
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private Context context;
    private OnReviewActionListener listener;
    private String currentUserId;

    public interface OnReviewActionListener {
        void onReviewClick(Review review);
        void onHelpfulClick(Review review, boolean isHelpful);
        void onReplyClick(Review review);
        void onShareClick(Review review);
        void onEditClick(Review review);
        void onDeleteClick(Review review);
        void onReportClick(Review review);
        void onImageClick(Review review, int imagePosition);
    }

    public ReviewsAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.reviews = new ArrayList<>();
    }

    public void setOnReviewActionListener(OnReviewActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
        
        // Add entrance animation for new items
        animateItemEntrance(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void updateReviews(List<Review> newReviews) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ReviewDiffCallback(this.reviews, newReviews));
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        diffResult.dispatchUpdatesTo(this);
    }

    public void addReview(Review review) {
        reviews.add(0, review);
        notifyItemInserted(0);
    }

    public void updateReview(Review updatedReview) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId().equals(updatedReview.getReviewId())) {
                reviews.set(i, updatedReview);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeReview(String reviewId) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId().equals(reviewId)) {
                reviews.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    private void animateItemEntrance(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(100f);
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translateAnimator);
        animatorSet.setDuration(300);
        animatorSet.setStartDelay(position * 50L); // Staggered animation
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardReview;
        private ShapeableImageView ivUserAvatar;
        private TextView tvUserName;
        private TextView tvReviewDate;
        private MaterialButton btnMoreOptions;
        private LinearLayout layoutRating;
        private ImageView[] ratingStars;
        private MaterialCardView cardVerifiedBadge;
        private TextView tvReviewText;
        private RecyclerView rvReviewImages;
        private MaterialButton btnHelpful;
        private MaterialButton btnReply;
        private MaterialButton btnShare;
        private MaterialCardView cardVendorReply;
        private TextView tvReplyDate;
        private TextView tvVendorReply;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
            setupClickListeners();
        }

        private void initializeViews() {
            cardReview = itemView.findViewById(R.id.card_review);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
            layoutRating = itemView.findViewById(R.id.layout_rating);
            cardVerifiedBadge = itemView.findViewById(R.id.card_verified_badge);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            rvReviewImages = itemView.findViewById(R.id.rv_review_images);
            btnHelpful = itemView.findViewById(R.id.btn_helpful);
            btnReply = itemView.findViewById(R.id.btn_reply);
            btnShare = itemView.findViewById(R.id.btn_share);
            cardVendorReply = itemView.findViewById(R.id.card_vendor_reply);
            tvReplyDate = itemView.findViewById(R.id.tv_reply_date);
            tvVendorReply = itemView.findViewById(R.id.tv_vendor_reply);

            // Initialize rating stars array
            ratingStars = new ImageView[5];
            ratingStars[0] = itemView.findViewById(R.id.review_star_1);
            ratingStars[1] = itemView.findViewById(R.id.review_star_2);
            ratingStars[2] = itemView.findViewById(R.id.review_star_3);
            ratingStars[3] = itemView.findViewById(R.id.review_star_4);
            ratingStars[4] = itemView.findViewById(R.id.review_star_5);
        }

        private void setupClickListeners() {
            cardReview.setOnClickListener(v -> {
                if (listener != null) {
                    animateCardPress(cardReview);
                    listener.onReviewClick(reviews.get(getAdapterPosition()));
                }
            });

            btnMoreOptions.setOnClickListener(v -> showMoreOptionsMenu(v));

            btnHelpful.setOnClickListener(v -> {
                if (listener != null) {
                    animateButtonPress(btnHelpful);
                    listener.onHelpfulClick(reviews.get(getAdapterPosition()), false);
                }
            });

            btnReply.setOnClickListener(v -> {
                if (listener != null) {
                    animateButtonPress(btnReply);
                    listener.onReplyClick(reviews.get(getAdapterPosition()));
                }
            });

            btnShare.setOnClickListener(v -> {
                if (listener != null) {
                    animateButtonPress(btnShare);
                    listener.onShareClick(reviews.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Review review) {
            // Set user information
            tvUserName.setText(review.getDisplayName());
            
            // Load user avatar
            if (review.getUserProfileImage() != null && !review.getUserProfileImage().isEmpty()) {
                Glide.with(context)
                    .load(review.getUserProfileImage())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivUserAvatar);
            } else {
                ivUserAvatar.setImageResource(R.drawable.ic_person_placeholder);
            }

            // Set review date
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                review.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString();
            tvReviewDate.setText(timeAgo);

            // Set rating stars
            updateRatingStars(review.getRating());

            // Show/hide verified purchase badge
            cardVerifiedBadge.setVisibility(review.isVerifiedPurchase() ? View.VISIBLE : View.GONE);

            // Set review text
            if (review.getReviewText() != null && !review.getReviewText().trim().isEmpty()) {
                tvReviewText.setText(review.getReviewText());
                tvReviewText.setVisibility(View.VISIBLE);
            } else {
                tvReviewText.setVisibility(View.GONE);
            }

            // Handle review images
            if (review.hasImages()) {
                rvReviewImages.setVisibility(View.VISIBLE);
                // TODO: Set up image adapter
            } else {
                rvReviewImages.setVisibility(View.GONE);
            }

            // Update helpful button
            btnHelpful.setText(String.format("Helpful (%d)", review.getHelpfulCount()));

            // Show/hide vendor reply
            if (review.hasVendorReply()) {
                cardVendorReply.setVisibility(View.VISIBLE);
                tvVendorReply.setText(review.getVendorReply());
                
                String replyTimeAgo = DateUtils.getRelativeTimeSpanString(
                    review.getVendorReplyTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString();
                tvReplyDate.setText(replyTimeAgo);
            } else {
                cardVendorReply.setVisibility(View.GONE);
            }

            // Show/hide more options based on ownership
            btnMoreOptions.setVisibility(
                review.isOwnedBy(currentUserId) ? View.VISIBLE : View.VISIBLE
            );
        }

        private void updateRatingStars(float rating) {
            int fullStars = (int) rating;
            boolean hasHalfStar = (rating - fullStars) >= 0.5f;

            for (int i = 0; i < 5; i++) {
                if (i < fullStars) {
                    ratingStars[i].setImageResource(R.drawable.ic_star_filled);
                    ratingStars[i].setColorFilter(context.getColor(R.color.star_filled));
                } else if (i == fullStars && hasHalfStar) {
                    ratingStars[i].setImageResource(R.drawable.ic_star_half);
                    ratingStars[i].setColorFilter(context.getColor(R.color.star_filled));
                } else {
                    ratingStars[i].setImageResource(R.drawable.ic_star_outline);
                    ratingStars[i].setColorFilter(context.getColor(R.color.star_outline));
                }
            }
        }

        private void showMoreOptionsMenu(View anchor) {
            // TODO: Implement popup menu with options like Edit, Delete, Report
            Review review = reviews.get(getAdapterPosition());
            
            if (review.isOwnedBy(currentUserId)) {
                // Show Edit/Delete options for own reviews
                if (listener != null) {
                    listener.onEditClick(review);
                }
            } else {
                // Show Report option for other users' reviews
                if (listener != null) {
                    listener.onReportClick(review);
                }
            }
        }

        private void animateCardPress(View view) {
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.98f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.98f);
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.98f, 1.0f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.98f, 1.0f);

            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.play(scaleDownX).with(scaleDownY);
            scaleDown.setDuration(100);

            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.play(scaleUpX).with(scaleUpY);
            scaleUp.setDuration(100);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(scaleDown).before(scaleUp);
            animatorSet.start();
        }

        private void animateButtonPress(View button) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.95f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.95f, 1.0f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(150);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.start();
        }
    }

    private static class ReviewDiffCallback extends DiffUtil.Callback {
        private final List<Review> oldList;
        private final List<Review> newList;

        public ReviewDiffCallback(List<Review> oldList, List<Review> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getReviewId()
                    .equals(newList.get(newItemPosition).getReviewId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Review oldReview = oldList.get(oldItemPosition);
            Review newReview = newList.get(newItemPosition);
            
            return oldReview.getRating() == newReview.getRating() &&
                   oldReview.getReviewText().equals(newReview.getReviewText()) &&
                   oldReview.getHelpfulCount() == newReview.getHelpfulCount() &&
                   oldReview.hasVendorReply() == newReview.hasVendorReply();
        }
    }
}
