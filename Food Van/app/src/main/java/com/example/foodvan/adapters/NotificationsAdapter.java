package com.example.foodvan.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying notifications
 * Handles different notification types with appropriate icons and styling
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener clickListener;
    private OnNotificationLongClickListener longClickListener;

    // Interface for handling notification clicks
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    // Interface for handling notification long clicks (for actions like delete)
    public interface OnNotificationLongClickListener {
        void onNotificationLongClick(Notification notification, int position);
    }

    public NotificationsAdapter(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Update the notifications list
     */
    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications.clear();
        if (newNotifications != null) {
            this.notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    /**
     * Add a single notification to the list
     */
    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Add to top
        notifyItemInserted(0);
    }

    /**
     * Remove notification at position
     */
    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.get(position).setRead(true);
            notifyItemChanged(position);
        }
    }

    /**
     * Clear all notifications
     */
    public void clearAll() {
        int size = notifications.size();
        notifications.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Get notification at position
     */
    public Notification getNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            return notifications.get(position);
        }
        return null;
    }

    /**
     * Set click listener
     */
    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Set long click listener
     */
    public void setOnNotificationLongClickListener(OnNotificationLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * ViewHolder class for notification items
     */
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;
        private TextView tvOrderId;
        private View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onNotificationClick(notifications.get(position), position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onNotificationLongClick(notifications.get(position), position);
                    return true;
                }
                return false;
            });
        }

        public void bind(Notification notification, int position) {
            // Set notification title
            tvTitle.setText(notification.getTitle());
            
            // Set notification message
            tvMessage.setText(notification.getMessage());
            
            // Set timestamp
            tvTime.setText(notification.getFormattedTime());
            
            // Set notification icon based on type
            setNotificationIcon(notification.getType());
            
            // Show/hide order ID for order-related notifications
            if (!TextUtils.isEmpty(notification.getOrderId()) && 
                Notification.TYPE_ORDER.equals(notification.getType())) {
                tvOrderId.setText("Order #" + notification.getOrderId());
                tvOrderId.setVisibility(View.VISIBLE);
            } else {
                tvOrderId.setVisibility(View.GONE);
            }
            
            // Show/hide unread indicator
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
            
            // Apply read/unread styling
            applyReadUnreadStyling(notification.isRead());
        }

        /**
         * Set appropriate icon based on notification type
         */
        private void setNotificationIcon(String type) {
            int iconRes;
            int backgroundRes;
            
            switch (type) {
                case Notification.TYPE_ORDER:
                    iconRes = R.drawable.ic_shopping_bag;
                    backgroundRes = R.drawable.circle_background_success;
                    break;
                case Notification.TYPE_PAYMENT:
                    iconRes = R.drawable.ic_payment;
                    backgroundRes = R.drawable.circle_background_primary;
                    break;
                case Notification.TYPE_SYSTEM:
                    iconRes = R.drawable.ic_info;
                    backgroundRes = R.drawable.circle_background_warning;
                    break;
                case Notification.TYPE_MENU:
                    iconRes = R.drawable.ic_restaurant_menu;
                    backgroundRes = R.drawable.circle_background_accent;
                    break;
                case Notification.TYPE_PROFILE:
                    iconRes = R.drawable.ic_person;
                    backgroundRes = R.drawable.circle_background_secondary;
                    break;
                default:
                    iconRes = R.drawable.ic_notifications;
                    backgroundRes = R.drawable.circle_background_primary;
                    break;
            }
            
            ivIcon.setImageResource(iconRes);
            ivIcon.setBackgroundResource(backgroundRes);
        }

        /**
         * Apply styling based on read/unread status
         */
        private void applyReadUnreadStyling(boolean isRead) {
            if (isRead) {
                // Read notification - normal styling
                tvTitle.setAlpha(0.7f);
                tvMessage.setAlpha(0.7f);
                itemView.setAlpha(0.8f);
            } else {
                // Unread notification - emphasized styling
                tvTitle.setAlpha(1.0f);
                tvMessage.setAlpha(1.0f);
                itemView.setAlpha(1.0f);
            }
        }
    }
}
