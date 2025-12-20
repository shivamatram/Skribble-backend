package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying recent orders for help/report issue
 */
public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnOrderClickListener listener;
    private Context context;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public RecentOrderAdapter() {
    }

    public RecentOrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivOrderIcon;
        private final TextView tvOrderId;
        private final TextView tvVanName;
        private final TextView tvOrderDate;
        private final Chip chipOrderStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrderIcon = itemView.findViewById(R.id.ivOrderIcon);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvVanName = itemView.findViewById(R.id.tvVanName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            chipOrderStatus = itemView.findViewById(R.id.chipOrderStatus);
        }

        void bind(Order order) {
            // Order ID
            String orderId = order.getOrderId();
            if (orderId != null && orderId.length() > 8) {
                orderId = orderId.substring(0, 8);
            }
            tvOrderId.setText("Order #" + (orderId != null ? orderId.toUpperCase() : "N/A"));

            // Van/Vendor name
            String vanName = order.getVanName();
            if (vanName == null || vanName.isEmpty()) {
                vanName = order.getVendorName();
            }
            tvVanName.setText(vanName != null ? vanName : "Food Van");

            // Order date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvOrderDate.setText(sdf.format(new Date(order.getOrderTime())));

            // Status chip
            String status = order.getStatus();
            chipOrderStatus.setText(formatStatus(status));
            updateStatusChipStyle(status);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private String formatStatus(String status) {
            if (status == null) return "Unknown";
            switch (status.toUpperCase()) {
                case "PLACED":
                    return "Placed";
                case "CONFIRMED":
                    return "Confirmed";
                case "PREPARING":
                    return "Preparing";
                case "READY":
                    return "Ready";
                case "DELIVERED":
                    return "Delivered";
                case "CANCELLED":
                    return "Cancelled";
                default:
                    return status;
            }
        }

        private void updateStatusChipStyle(String status) {
            int backgroundColor;
            int textColor = ContextCompat.getColor(context, R.color.white);

            if (status == null) {
                backgroundColor = ContextCompat.getColor(context, R.color.gray_400);
            } else {
                switch (status.toUpperCase()) {
                    case "DELIVERED":
                        backgroundColor = ContextCompat.getColor(context, R.color.success_color);
                        break;
                    case "CANCELLED":
                        backgroundColor = ContextCompat.getColor(context, R.color.error_color);
                        break;
                    case "PREPARING":
                    case "CONFIRMED":
                        backgroundColor = ContextCompat.getColor(context, R.color.info_color);
                        break;
                    case "PLACED":
                    case "READY":
                        backgroundColor = ContextCompat.getColor(context, R.color.warning_color);
                        break;
                    default:
                        backgroundColor = ContextCompat.getColor(context, R.color.gray_400);
                        break;
                }
            }

            chipOrderStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
            chipOrderStatus.setTextColor(textColor);
        }
    }
}
