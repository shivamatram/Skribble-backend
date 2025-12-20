package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VendorOrderHistoryAdapter extends RecyclerView.Adapter<VendorOrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onViewOrderDetails(Order order);
        void onReorderClicked(Order order);
        void onRateOrder(Order order);
        void onTrackOrder(Order order);
    }

    public VendorOrderHistoryAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
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
        private ImageView vendorIcon;
        private TextView vendorName, orderId, orderDate, totalAmount, status;
        private TextView itemsSummary, itemCount;
        private MaterialButton reorderButton, viewDetailsButton;
        private MaterialButton rateOrderButton, trackOrderButton;
        private LinearLayout ratingLayout, deliveryInfoLayout;
        private TextView ratingText, deliveryInfo;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // vendorIcon = itemView.findViewById(R.id.iv_vendor_icon);
            vendorName = itemView.findViewById(R.id.tv_vendor_name);
            orderId = itemView.findViewById(R.id.tv_order_id);
            orderDate = itemView.findViewById(R.id.tv_order_date);
            totalAmount = itemView.findViewById(R.id.tv_total_amount);
            status = itemView.findViewById(R.id.tv_status);
            
            // itemsSummary = itemView.findViewById(R.id.tv_items_summary);
            // itemCount = itemView.findViewById(R.id.tv_item_count);
            
            reorderButton = itemView.findViewById(R.id.btn_reorder);
            viewDetailsButton = itemView.findViewById(R.id.btn_view_details);
            // rateOrderButton = itemView.findViewById(R.id.btn_rate_order);
            // trackOrderButton = itemView.findViewById(R.id.btn_track_order);
            
            ratingLayout = itemView.findViewById(R.id.layout_rating);
            // deliveryInfoLayout = itemView.findViewById(R.id.layout_delivery_info);
            // ratingText = itemView.findViewById(R.id.tv_rating_text);
            // deliveryInfo = itemView.findViewById(R.id.tv_delivery_info);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(orders.get(getAdapterPosition()));
                }
            });

            reorderButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReorderClicked(orders.get(getAdapterPosition()));
                }
            });

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(orders.get(getAdapterPosition()));
                }
            });

            rateOrderButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRateOrder(orders.get(getAdapterPosition()));
                }
            });

            trackOrderButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackOrder(orders.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Order order) {
            // Basic order info
            vendorName.setText(order.getVendorName() != null ? order.getVendorName() : "Food Van");
            orderId.setText("Order #" + order.getOrderId());
            orderDate.setText(formatDate(order.getTimestamp()));
            totalAmount.setText(String.format(Locale.getDefault(), "₹%.0f", order.getTotalAmount()));

            // Status with color coding
            status.setText(order.getStatus().toUpperCase());
            setStatusColor(order.getStatus());

            // Items summary
            int itemsCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
            itemCount.setText(String.valueOf(itemsCount));
            
            if (itemsCount > 0) {
                StringBuilder itemsText = new StringBuilder();
                itemsText.append(itemsCount).append(" item").append(itemsCount > 1 ? "s" : "");
                
                // Add first few item names
                if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                    itemsText.append(" • ");
                    for (int i = 0; i < Math.min(3, order.getOrderItems().size()); i++) {
                        if (i > 0) itemsText.append(", ");
                        itemsText.append(order.getOrderItems().get(i).getItemName());
                    }
                    if (order.getOrderItems().size() > 3) {
                        itemsText.append("...");
                    }
                }
                itemsSummary.setText(itemsText.toString());
            } else {
                itemsSummary.setText("No items");
            }

            // Show/hide rating and delivery info based on status
            String orderStatus = order.getStatus().toLowerCase();
            
            if (orderStatus.equals("delivered")) {
                ratingLayout.setVisibility(View.VISIBLE);
                deliveryInfoLayout.setVisibility(View.GONE);
                ratingText.setText("Rate your experience");
            } else if (isOngoingOrder(orderStatus)) {
                ratingLayout.setVisibility(View.GONE);
                deliveryInfoLayout.setVisibility(View.VISIBLE);
                deliveryInfo.setText("Order in progress");
            } else {
                ratingLayout.setVisibility(View.GONE);
                deliveryInfoLayout.setVisibility(View.GONE);
            }
        }

        private void setStatusColor(String orderStatus) {
            int colorResId;
            int backgroundResId;
            
            switch (orderStatus.toLowerCase()) {
                case "delivered":
                    colorResId = R.color.white;
                    backgroundResId = R.drawable.bg_status_delivered;
                    break;
                case "cancelled":
                    colorResId = R.color.white;
                    backgroundResId = R.drawable.bg_status_cancelled;
                    break;
                case "pending":
                    colorResId = R.color.white;
                    backgroundResId = R.drawable.bg_status_pending;
                    break;
                case "accepted":
                case "preparing":
                case "ready":
                case "out_for_delivery":
                    colorResId = R.color.white;
                    backgroundResId = R.drawable.bg_status_ongoing;
                    break;
                default:
                    colorResId = R.color.text_secondary;
                    backgroundResId = R.drawable.bg_status_default;
                    break;
            }
            
            status.setTextColor(ContextCompat.getColor(context, colorResId));
            status.setBackgroundResource(backgroundResId);
        }

        private boolean isOngoingOrder(String orderStatus) {
            return orderStatus.equals("pending") ||
                   orderStatus.equals("accepted") ||
                   orderStatus.equals("preparing") ||
                   orderStatus.equals("ready") ||
                   orderStatus.equals("out_for_delivery");
        }

        private String formatDate(Date timestamp) {
            if (timestamp == null) return "Unknown";
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
            return sdf.format(timestamp);
        }
    }
}
