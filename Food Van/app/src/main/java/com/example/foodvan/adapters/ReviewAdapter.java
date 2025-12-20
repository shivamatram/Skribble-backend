package com.example.foodvan.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.database.ReviewEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying reviews in RecyclerView
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final int MAX_REVIEW_LINES = 3;
    private static final int MAX_REPLY_LINES = 3;
    
    private final Context context;
    private List<ReviewEntity> reviews;
    private OnReviewActionListener actionListener;
    private final SimpleDateFormat dateFormat;

    public interface OnReviewActionListener {
        void onReplyClick(ReviewEntity review);
        void onEditReplyClick(ReviewEntity review);
        void onDeleteReplyClick(ReviewEntity review);
        void onFlagReviewClick(ReviewEntity review);
        void onSoftDeleteReviewClick(ReviewEntity review);
        void onSendReply(ReviewEntity review, String replyText);
        void onCancelReply(ReviewEntity review);
        void onOrderIdClick(ReviewEntity review);
    }

    public ReviewAdapter(Context context) {
        this.context = context;
        this.reviews = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setOnReviewActionListener(OnReviewActionListener listener) {
        this.actionListener = listener;
    }

    public void setReviews(List<ReviewEntity> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateReview(ReviewEntity updatedReview) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId().equals(updatedReview.getReviewId())) {
                reviews.set(i, updatedReview);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewEntity review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCustomerName;
        private final RatingBar ratingBarReview;
        private final TextView tvReviewDate;
        private final TextView tvOrderId;
        private final TextView tvReviewText;
        private final TextView tvExpandCollapse;
        private final RecyclerView recyclerReviewImages;
        private final View layoutVendorReply;
        private final TextView tvReplyText;
        private final TextView tvReplyDate;
        private final TextView tvReplyEdited;
        private final View layoutReplyComposer;
        private final TextInputLayout textInputReply;
        private final TextInputEditText etReply;
        private final View layoutReviewActions;
        private final ImageButton btnReviewMenu;
        private final ImageButton btnEditReply;
        private final ImageButton btnDeleteReply;
        private final MaterialButton btnReply;
        private final MaterialButton btnFlag;
        private final MaterialButton btnCancelReply;
        private final MaterialButton btnSendReply;

        private boolean isReviewExpanded = false;
        private boolean isReplyExpanded = false;
        private boolean isComposerVisible = false;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            ratingBarReview = itemView.findViewById(R.id.rating_bar_review);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            tvExpandCollapse = itemView.findViewById(R.id.tv_expand_collapse);
            recyclerReviewImages = itemView.findViewById(R.id.recycler_review_images);
            layoutVendorReply = itemView.findViewById(R.id.layout_vendor_reply);
            tvReplyText = itemView.findViewById(R.id.tv_reply_text);
            tvReplyDate = itemView.findViewById(R.id.tv_reply_date);
            tvReplyEdited = itemView.findViewById(R.id.tv_reply_edited);
            layoutReplyComposer = itemView.findViewById(R.id.layout_reply_composer);
            textInputReply = itemView.findViewById(R.id.text_input_reply);
            etReply = itemView.findViewById(R.id.et_reply);
            layoutReviewActions = itemView.findViewById(R.id.layout_review_actions);
            btnReviewMenu = itemView.findViewById(R.id.btn_review_menu);
            btnEditReply = itemView.findViewById(R.id.btn_edit_reply);
            btnDeleteReply = itemView.findViewById(R.id.btn_delete_reply);
            btnReply = itemView.findViewById(R.id.btn_reply);
            btnFlag = itemView.findViewById(R.id.btn_flag);
            btnCancelReply = itemView.findViewById(R.id.btn_cancel_reply);
            btnSendReply = itemView.findViewById(R.id.btn_send_reply);
        }

        public void bind(ReviewEntity review) {
            // Basic review info
            tvCustomerName.setText(review.isAnonymous() ? "Anonymous User" : review.getCustomerName());
            ratingBarReview.setRating(review.getRating());
            tvReviewDate.setText(getRelativeTimeString(review.getCreatedAt()));

            // Order ID
            if (!TextUtils.isEmpty(review.getOrderId())) {
                tvOrderId.setText("Order #" + review.getOrderId());
                tvOrderId.setVisibility(View.VISIBLE);
                tvOrderId.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onOrderIdClick(review);
                    }
                });
            } else {
                tvOrderId.setVisibility(View.GONE);
            }

            // Review text with expand/collapse
            setupReviewText(review);

            // Review images
            setupReviewImages(review);

            // Vendor reply
            setupVendorReply(review);

            // Action buttons
            setupActionButtons(review);

            // Menu button
            setupMenuButton(review);
        }

        private void setupReviewText(ReviewEntity review) {
            String reviewText = review.getText();
            if (TextUtils.isEmpty(reviewText)) {
                tvReviewText.setVisibility(View.GONE);
                tvExpandCollapse.setVisibility(View.GONE);
                return;
            }

            tvReviewText.setVisibility(View.VISIBLE);
            
            // Check if text needs truncation
            tvReviewText.post(() -> {
                if (tvReviewText.getLineCount() > MAX_REVIEW_LINES && !isReviewExpanded) {
                    tvReviewText.setMaxLines(MAX_REVIEW_LINES);
                    tvReviewText.setEllipsize(TextUtils.TruncateAt.END);
                    tvExpandCollapse.setVisibility(View.VISIBLE);
                    tvExpandCollapse.setText("Show more");
                } else if (isReviewExpanded) {
                    tvReviewText.setMaxLines(Integer.MAX_VALUE);
                    tvReviewText.setEllipsize(null);
                    tvExpandCollapse.setVisibility(View.VISIBLE);
                    tvExpandCollapse.setText("Show less");
                } else {
                    tvExpandCollapse.setVisibility(View.GONE);
                }
            });

            tvReviewText.setText(reviewText);
            
            tvExpandCollapse.setOnClickListener(v -> {
                isReviewExpanded = !isReviewExpanded;
                setupReviewText(review);
            });
        }

        private void setupReviewImages(ReviewEntity review) {
            if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
                recyclerReviewImages.setVisibility(View.VISIBLE);
                recyclerReviewImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                // TODO: Set up image adapter
                // ReviewImageAdapter imageAdapter = new ReviewImageAdapter(context, review.getImageUrls());
                // recyclerReviewImages.setAdapter(imageAdapter);
            } else {
                recyclerReviewImages.setVisibility(View.GONE);
            }
        }

        private void setupVendorReply(ReviewEntity review) {
            boolean hasReply = !TextUtils.isEmpty(review.getVendorReplyText());
            
            if (hasReply) {
                layoutVendorReply.setVisibility(View.VISIBLE);
                tvReplyText.setText(review.getVendorReplyText());
                tvReplyDate.setText(getRelativeTimeString(review.getVendorReplyCreatedAt()));
                tvReplyEdited.setVisibility(review.isVendorReplyIsEdited() ? View.VISIBLE : View.GONE);

                // Reply action buttons
                btnEditReply.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditReplyClick(review);
                    }
                });

                btnDeleteReply.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteReplyClick(review);
                    }
                });
            } else {
                layoutVendorReply.setVisibility(View.GONE);
            }
        }

        private void setupActionButtons(ReviewEntity review) {
            boolean hasReply = !TextUtils.isEmpty(review.getVendorReplyText());
            
            if (!hasReply && !isComposerVisible) {
                layoutReviewActions.setVisibility(View.VISIBLE);
                layoutReplyComposer.setVisibility(View.GONE);

                btnReply.setOnClickListener(v -> showReplyComposer(review));
                btnFlag.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onFlagReviewClick(review);
                    }
                });
            } else if (isComposerVisible) {
                layoutReviewActions.setVisibility(View.GONE);
                layoutReplyComposer.setVisibility(View.VISIBLE);
                setupReplyComposer(review);
            } else {
                layoutReviewActions.setVisibility(View.GONE);
                layoutReplyComposer.setVisibility(View.GONE);
            }
        }

        private void showReplyComposer(ReviewEntity review) {
            isComposerVisible = true;
            setupActionButtons(review);
            etReply.requestFocus();
        }

        private void hideReplyComposer(ReviewEntity review) {
            isComposerVisible = false;
            etReply.setText("");
            setupActionButtons(review);
        }

        private void setupReplyComposer(ReviewEntity review) {
            btnCancelReply.setOnClickListener(v -> {
                hideReplyComposer(review);
                if (actionListener != null) {
                    actionListener.onCancelReply(review);
                }
            });

            btnSendReply.setOnClickListener(v -> {
                String replyText = etReply.getText() != null ? etReply.getText().toString().trim() : "";
                if (!replyText.isEmpty()) {
                    hideReplyComposer(review);
                    if (actionListener != null) {
                        actionListener.onSendReply(review, replyText);
                    }
                } else {
                    textInputReply.setError("Reply cannot be empty");
                }
            });
        }

        private void setupMenuButton(ReviewEntity review) {
            btnReviewMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, btnReviewMenu);
                popup.getMenuInflater().inflate(R.menu.review_item_menu, popup.getMenu());
                
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_flag_review) {
                        if (actionListener != null) {
                            actionListener.onFlagReviewClick(review);
                        }
                        return true;
                    } else if (itemId == R.id.action_hide_review) {
                        if (actionListener != null) {
                            actionListener.onSoftDeleteReviewClick(review);
                        }
                        return true;
                    }
                    return false;
                });
                
                popup.show();
            });
        }

        private String getRelativeTimeString(long timestamp) {
            if (timestamp == 0) return "";
            
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) {
                if (days == 1) return "1 day ago";
                if (days < 7) return days + " days ago";
                return dateFormat.format(new Date(timestamp));
            } else if (hours > 0) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            } else if (minutes > 0) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else {
                return "Just now";
            }
        }
    }
}
