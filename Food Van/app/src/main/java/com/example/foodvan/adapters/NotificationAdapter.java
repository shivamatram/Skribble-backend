package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.example.foodvan.R;
import com.example.foodvan.models.VendorNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<VendorNotification> notificationList;

    public NotificationAdapter(Context context, List<VendorNotification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        VendorNotification notification = notificationList.get(position);
        
        // Set notification content
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTimeAgo());
        holder.ivIcon.setImageResource(notification.getIconResource());
        
        // Set read/unread state
        holder.viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        
        // Set background color based on read state
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }
        
        // Set icon background based on notification type
        int backgroundResource = getIconBackground(notification.getType());
        holder.ivIcon.setBackgroundResource(backgroundResource);
        
        // Show/hide action buttons based on notification type
        if ("ORDER".equals(notification.getType()) && !notification.isRead()) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            holder.btnActionPrimary.setText("View Order");
            holder.btnActionSecondary.setText("Dismiss");
        } else {
            holder.layoutActionButtons.setVisibility(View.GONE);
        }
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            // Mark as read when clicked
            notification.setRead(true);
            notifyItemChanged(position);
            
            // Handle notification click based on type
            handleNotificationClick(notification);
        });
        
        holder.btnMoreOptions.setOnClickListener(v -> {
            showNotificationOptions(notification, position);
        });
        
        if (holder.btnActionPrimary.getVisibility() == View.VISIBLE) {
            holder.btnActionPrimary.setOnClickListener(v -> {
                handleNotificationAction(notification, "primary");
            });
            
            holder.btnActionSecondary.setOnClickListener(v -> {
                notification.setRead(true);
                notifyItemChanged(position);
                Toast.makeText(context, "Notification dismissed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private int getIconBackground(String type) {
        switch (type) {
            case "ORDER":
                return R.drawable.circle_background_primary;
            case "PAYMENT":
                return R.drawable.circle_background_success;
            case "SYSTEM":
                return R.drawable.circle_background_info;
            default:
                return R.drawable.circle_background_primary;
        }
    }

    private void handleNotificationClick(VendorNotification notification) {
        switch (notification.getType()) {
            case "ORDER":
                Toast.makeText(context, "Opening order details...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to order details
                break;
            case "PAYMENT":
                Toast.makeText(context, "Opening payment details...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to payment history
                break;
            case "SYSTEM":
                Toast.makeText(context, "Opening system info...", Toast.LENGTH_SHORT).show();
                // TODO: Handle system notification
                break;
        }
    }

    private void handleNotificationAction(VendorNotification notification, String action) {
        if ("primary".equals(action)) {
            switch (notification.getType()) {
                case "ORDER":
                    Toast.makeText(context, "Opening order #" + notification.getId(), Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to specific order
                    break;
            }
        }
    }

    private void showNotificationOptions(VendorNotification notification, int position) {
        // TODO: Show bottom sheet or popup menu with options:
        // - Mark as read/unread
        // - Delete notification
        // - Notification settings
        Toast.makeText(context, "Notification options - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        View viewUnreadIndicator;
        TextView tvTitle, tvMessage, tvTime;
        LinearLayout layoutActionButtons;
        MaterialButton btnActionPrimary, btnActionSecondary;
        ImageButton btnMoreOptions;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            layoutActionButtons = itemView.findViewById(R.id.layout_action_buttons);
            btnActionPrimary = itemView.findViewById(R.id.btn_action_primary);
            btnActionSecondary = itemView.findViewById(R.id.btn_action_secondary);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}
