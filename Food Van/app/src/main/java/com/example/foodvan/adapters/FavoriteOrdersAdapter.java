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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodvan.R;
import com.example.foodvan.models.FavoriteOrder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying favorite orders in RecyclerView with Material Design 3
 */
public class FavoriteOrdersAdapter extends RecyclerView.Adapter<FavoriteOrdersAdapter.FavoriteViewHolder> {

    private Context context;
    private List<FavoriteOrder> favoriteOrders;
    private List<FavoriteOrder> selectedItems;
    private OnFavoriteActionListener listener;
    private boolean isMultiSelectMode = false;

    public interface OnFavoriteActionListener {
        void onFavoriteClicked(FavoriteOrder favorite);
        void onFavoriteToggled(FavoriteOrder favorite, boolean isRemoved);
        void onReorderClicked(FavoriteOrder favorite);
        void onViewDetailsClicked(FavoriteOrder favorite);
        void onSelectionChanged(int selectedCount);
    }

    public FavoriteOrdersAdapter(Context context, List<FavoriteOrder> favoriteOrders, OnFavoriteActionListener listener) {
        this.context = context;
        this.favoriteOrders = favoriteOrders != null ? favoriteOrders : new ArrayList<>();
        this.selectedItems = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_order, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteOrder favorite = favoriteOrders.get(position);
        holder.bind(favorite, position);
    }

    @Override
    public int getItemCount() {
        return favoriteOrders.size();
    }

    public void updateFavorites(List<FavoriteOrder> newFavorites) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteDiffCallback(this.favoriteOrders, newFavorites));
        this.favoriteOrders.clear();
        this.favoriteOrders.addAll(newFavorites);
        diffResult.dispatchUpdatesTo(this);
    }

    public void toggleMultiSelectMode() {
        isMultiSelectMode = !isMultiSelectMode;
        selectedItems.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
    }

    public void selectAll() {
        selectedItems.clear();
        selectedItems.addAll(favoriteOrders);
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedItems.size());
        }
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
    }

    public List<FavoriteOrder> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public void exitMultiSelectMode() {
        isMultiSelectMode = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardFavoriteItem, cardImage, cardTypeBadge, cardAvailability;
        ImageView ivItemImage;
        TextView tvItemName, tvVendorName, tvRating, tvReviewsCount, tvPrice, tvAddedDate;
        TextView tvTypeBadge, tvAvailability;
        MaterialButton btnFavorite, btnViewDetails, btnReorder;
        MaterialCheckBox checkboxSelect;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardFavoriteItem = itemView.findViewById(R.id.card_favorite_item);
            cardImage = itemView.findViewById(R.id.card_image);
            cardTypeBadge = itemView.findViewById(R.id.card_type_badge);
            cardAvailability = itemView.findViewById(R.id.card_availability);
            
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvVendorName = itemView.findViewById(R.id.tv_vendor_name);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvReviewsCount = itemView.findViewById(R.id.tv_reviews_count);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddedDate = itemView.findViewById(R.id.tv_added_date);
            tvTypeBadge = itemView.findViewById(R.id.tv_type_badge);
            tvAvailability = itemView.findViewById(R.id.tv_availability);
            
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnReorder = itemView.findViewById(R.id.btn_reorder);
            checkboxSelect = itemView.findViewById(R.id.checkbox_select);
        }

        public void bind(FavoriteOrder favorite, int position) {
            // Set basic information
            tvItemName.setText(favorite.getItemName());
            tvVendorName.setText(favorite.getVendorName());
            tvPrice.setText(favorite.getFormattedPrice());
            tvRating.setText(favorite.getFormattedRating());
            tvReviewsCount.setText(favorite.getFormattedReviews());
            
            // Set type badge
            tvTypeBadge.setText(favorite.getType());
            if ("FOOD".equals(favorite.getType())) {
                cardTypeBadge.setCardBackgroundColor(context.getColor(R.color.primary));
            } else {
                cardTypeBadge.setCardBackgroundColor(context.getColor(R.color.secondary_color));
            }
            
            // Set added date
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                favorite.getAddedDate(),
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            ).toString();
            tvAddedDate.setText("Added " + timeAgo.toLowerCase());
            
            // Set availability
            if (favorite.isAvailable()) {
                cardAvailability.setVisibility(View.VISIBLE);
                tvAvailability.setText("Available Now");
                tvAvailability.setTextColor(context.getColor(R.color.success_color));
                cardAvailability.setCardBackgroundColor(context.getColor(R.color.success_light));
            } else {
                cardAvailability.setVisibility(View.VISIBLE);
                tvAvailability.setText("Currently Unavailable");
                tvAvailability.setTextColor(context.getColor(R.color.error_color));
                cardAvailability.setCardBackgroundColor(context.getColor(R.color.warning_light));
            }
            
            // Load image with Glide
            if (favorite.getImageUrl() != null && !favorite.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(favorite.getImageUrl())
                    .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_food_item)
                        .error(R.drawable.placeholder_food_item)
                        .transform(new RoundedCorners(24)))
                    .into(ivItemImage);
            } else {
                ivItemImage.setImageResource(R.drawable.placeholder_food_item);
            }
            
            // Handle multi-select mode
            if (isMultiSelectMode) {
                checkboxSelect.setVisibility(View.VISIBLE);
                checkboxSelect.setChecked(selectedItems.contains(favorite));
                
                checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedItems.contains(favorite)) {
                            selectedItems.add(favorite);
                        }
                    } else {
                        selectedItems.remove(favorite);
                    }
                    
                    if (listener != null) {
                        listener.onSelectionChanged(selectedItems.size());
                    }
                });
            } else {
                checkboxSelect.setVisibility(View.GONE);
            }
            
            // Set click listeners
            cardFavoriteItem.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    checkboxSelect.setChecked(!checkboxSelect.isChecked());
                } else {
                    animateCardPress(cardFavoriteItem);
                    if (listener != null) {
                        listener.onFavoriteClicked(favorite);
                    }
                }
            });
            
            cardFavoriteItem.setOnLongClickListener(v -> {
                if (!isMultiSelectMode) {
                    toggleMultiSelectMode();
                    checkboxSelect.setChecked(true);
                }
                return true;
            });
            
            btnFavorite.setOnClickListener(v -> {
                animateHeartRemove(btnFavorite);
                if (listener != null) {
                    listener.onFavoriteToggled(favorite, true);
                }
            });
            
            btnViewDetails.setOnClickListener(v -> {
                animateButtonPress(btnViewDetails);
                if (listener != null) {
                    listener.onViewDetailsClicked(favorite);
                }
            });
            
            btnReorder.setOnClickListener(v -> {
                animateButtonPress(btnReorder);
                if (listener != null) {
                    listener.onReorderClicked(favorite);
                }
            });
            
            // Animate item entrance
            animateItemEntrance(cardFavoriteItem, position);
        }
        
        private void animateCardPress(View view) {
            ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
            ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
            ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
            ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
            
            AnimatorSet pressDown = new AnimatorSet();
            pressDown.playTogether(scaleXDown, scaleYDown);
            pressDown.setDuration(100);
            
            AnimatorSet pressUp = new AnimatorSet();
            pressUp.playTogether(scaleXUp, scaleYUp);
            pressUp.setDuration(100);
            
            pressDown.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    pressUp.start();
                }
            });
            
            pressDown.start();
        }
        
        private void animateHeartRemove(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 0.8f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 0.8f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 15f, -15f, 0f);
            
            AnimatorSet heartAnimation = new AnimatorSet();
            heartAnimation.playTogether(scaleX, scaleY, rotation);
            heartAnimation.setDuration(400);
            heartAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            heartAnimation.start();
        }
        
        private void animateButtonPress(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f);
            
            AnimatorSet buttonPress = new AnimatorSet();
            buttonPress.playTogether(scaleX, scaleY);
            buttonPress.setDuration(150);
            buttonPress.start();
        }
        
        private void animateItemEntrance(View view, int position) {
            view.setAlpha(0f);
            view.setTranslationY(100f);
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator slideIn = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f);
            
            AnimatorSet entrance = new AnimatorSet();
            entrance.playTogether(fadeIn, slideIn);
            entrance.setDuration(300);
            entrance.setStartDelay(position * 50L); // Staggered animation
            entrance.setInterpolator(new AccelerateDecelerateInterpolator());
            entrance.start();
        }
    }

    // DiffUtil callback for efficient updates
    private static class FavoriteDiffCallback extends DiffUtil.Callback {
        private List<FavoriteOrder> oldList;
        private List<FavoriteOrder> newList;

        public FavoriteDiffCallback(List<FavoriteOrder> oldList, List<FavoriteOrder> newList) {
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
            return oldList.get(oldItemPosition).getFavoriteId()
                    .equals(newList.get(newItemPosition).getFavoriteId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            FavoriteOrder oldItem = oldList.get(oldItemPosition);
            FavoriteOrder newItem = newList.get(newItemPosition);
            
            return oldItem.getItemName().equals(newItem.getItemName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   oldItem.isAvailable() == newItem.isAvailable();
        }
    }
}
