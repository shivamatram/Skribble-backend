package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying pending orders in the vendor dashboard
 */
public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> ordersList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onAcceptOrder(Order order);
        void onRejectOrder(Order order);
        void onViewOrderDetails(Order order);
    }

    public PendingOrdersAdapter(Context context, List<Order> ordersList, OnOrderActionListener listener) {
        this.context = context;
        this.ordersList = ordersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = ordersList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView orderCard;
        private TextView orderIdText;
        private TextView customerNameText;
        private TextView orderTimeText;
        private TextView totalAmountText;
        private TextView itemsCountText;
        private Chip statusChip;
        private MaterialButton acceptButton;
        private MaterialButton rejectButton;
        private MaterialButton viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            orderCard = itemView.findViewById(R.id.orderCard);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            orderTimeText = itemView.findViewById(R.id.orderTimeText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            itemsCountText = itemView.findViewById(R.id.itemsCountText);
            statusChip = itemView.findViewById(R.id.statusChip);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }

        public void bind(Order order) {
            // Set order basic info
            orderIdText.setText("Order #" + order.getOrderId());
            customerNameText.setText(order.getCustomerName() != null ? order.getCustomerName() : "Customer");
            
            // Format and set order time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            orderTimeText.setText(timeFormat.format(new Date(order.getOrderTime())));
            
            // Set total amount
            totalAmountText.setText(order.getFormattedTotalAmount());
            
            // Set items count
            int itemsCount = order.getTotalItems();
            itemsCountText.setText(itemsCount + " item" + (itemsCount > 1 ? "s" : ""));
            
            // Set status chip
            updateStatusChip(order.getStatus());
            
            // Set click listeners
            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptOrder(order);
                }
            });
            
            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectOrder(order);
                }
            });
            
            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(order);
                }
            });
            
            // Set card click listener for order details
            orderCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(order);
                }
            });
        }

        private void updateStatusChip(String status) {
            if (statusChip != null && status != null) {
                statusChip.setText(status.toUpperCase());
                
                // Set chip colors based on status
                switch (status.toLowerCase()) {
                    case "pending":
                        statusChip.setChipBackgroundColorResource(R.color.warning_color);
                        statusChip.setTextColor(context.getResources().getColor(R.color.white, null));
                        break;
                    case "confirmed":
                        statusChip.setChipBackgroundColorResource(R.color.info_color);
                        statusChip.setTextColor(context.getResources().getColor(R.color.white, null));
                        break;
                    case "preparing":
                        statusChip.setChipBackgroundColorResource(R.color.primary_color);
                        statusChip.setTextColor(context.getResources().getColor(R.color.white, null));
                        break;
                    case "ready":
                        statusChip.setChipBackgroundColorResource(R.color.success_color);
                        statusChip.setTextColor(context.getResources().getColor(R.color.white, null));
                        break;
                    default:
                        statusChip.setChipBackgroundColorResource(R.color.text_secondary);
                        statusChip.setTextColor(context.getResources().getColor(R.color.white, null));
                        break;
                }
            }
        }
    }

    // Method to update the orders list
    public void updateOrders(List<Order> newOrders) {
        this.ordersList = newOrders;
        notifyDataSetChanged();
    }

    // Method to add a new order
    public void addOrder(Order order) {
        if (ordersList != null) {
            ordersList.add(0, order); // Add to beginning
            notifyItemInserted(0);
        }
    }

    // Method to remove an order
    public void removeOrder(int position) {
        if (ordersList != null && position >= 0 && position < ordersList.size()) {
            ordersList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to update order status
    public void updateOrderStatus(String orderId, String newStatus) {
        if (ordersList != null) {
            for (int i = 0; i < ordersList.size(); i++) {
                Order order = ordersList.get(i);
                if (order.getOrderId().equals(orderId)) {
                    order.setStatus(newStatus);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }
}
