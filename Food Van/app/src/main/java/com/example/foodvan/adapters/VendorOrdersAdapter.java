package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;
// Using nested Order.OrderItem class

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VendorOrdersAdapter extends RecyclerView.Adapter<VendorOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onOrderClick(Order order);
        void onCallCustomer(Order order);
        void onAcceptOrder(Order order);
        void onRejectOrder(Order order);
        void onUpdateOrderStatus(Order order, String newStatus);
    }

    public VendorOrdersAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vendor_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        // Bind order data
        holder.tvOrderId.setText("#" + order.getOrderId());
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvOrderTime.setText(formatTime(order.getOrderTime()));
        holder.tvEta.setText("ETA: " + order.getEstimatedDeliveryTime() + " mins");
        holder.tvTotalAmount.setText(String.format("₹%.0f", order.getTotalAmount()));
        
        // Set order status
        setOrderStatus(holder.chipOrderStatus, order.getStatus());
        
        // Set payment method
        setPaymentMethod(holder.ivPaymentMethod, holder.tvPaymentMethod, order.getPaymentMethod());
        
        // Set item summary
        setItemSummary(holder, order);
        
        // Set action buttons based on order status
        setupActionButtons(holder, order);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
        
        holder.btnCallCustomer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallCustomer(order);
            }
        });
        
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptOrder(order);
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectOrder(order);
            }
        });
        
        holder.btnStartPreparing.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateOrderStatus(order, "PREPARING");
            }
        });
        
        holder.btnMarkReady.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateOrderStatus(order, "READY");
            }
        });
        
        holder.btnMarkDelivered.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateOrderStatus(order, "DELIVERED");
            }
        });
        
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private void setOrderStatus(Chip chip, String status) {
        chip.setText(getStatusDisplayText(status));
        
        switch (status) {
            case "PLACED":
                chip.setChipBackgroundColorResource(R.color.warning_color);
                break;
            case "CONFIRMED":
                chip.setChipBackgroundColorResource(R.color.info_color);
                break;
            case "PREPARING":
                chip.setChipBackgroundColorResource(R.color.primary_color);
                break;
            case "READY":
                chip.setChipBackgroundColorResource(R.color.accent);
                break;
            case "DELIVERED":
                chip.setChipBackgroundColorResource(R.color.success_color);
                break;
            case "CANCELLED":
                chip.setChipBackgroundColorResource(R.color.error_color);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.text_secondary);
                break;
        }
    }

    private String getStatusDisplayText(String status) {
        switch (status) {
            case "PLACED": return "Pending";
            case "CONFIRMED": return "Accepted";
            case "PREPARING": return "Preparing";
            case "READY": return "Ready";
            case "DELIVERED": return "Delivered";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    private void setPaymentMethod(ImageView icon, TextView text, String paymentMethod) {
        text.setText(paymentMethod);
        
        switch (paymentMethod.toUpperCase()) {
            case "UPI":
                icon.setImageResource(R.drawable.ic_account_balance);
                break;
            case "CARD":
            case "ONLINE":
                icon.setImageResource(R.drawable.ic_credit_card);
                break;
            case "CASH":
            case "COD":
                icon.setImageResource(R.drawable.ic_currency_rupee);
                break;
            default:
                icon.setImageResource(R.drawable.ic_credit_card);
                break;
        }
    }

    private void setItemSummary(OrderViewHolder holder, Order order) {
        List<Order.OrderItem> items = order.getItems();
        
        if (items != null && !items.isEmpty()) {
            // Set item count
            holder.tvItemSummary.setText(items.size() + " items");
            
            // Set item details (first few items)
            StringBuilder itemDetails = new StringBuilder();
            int maxItems = Math.min(items.size(), 3);
            
            for (int i = 0; i < maxItems; i++) {
                if (i > 0) itemDetails.append(", ");
                itemDetails.append(items.get(i).getItemName());
            }
            
            if (items.size() > 3) {
                itemDetails.append("...");
            }
            
            holder.tvItemDetails.setText(itemDetails.toString());
            
            // Load first item image - nested OrderItem doesn't have imageUrl, use default
            holder.ivItemThumbnail.setImageResource(R.drawable.ic_fastfood);
        } else {
            holder.tvItemSummary.setText("No items");
            holder.tvItemDetails.setText("");
            holder.ivItemThumbnail.setImageResource(R.drawable.ic_fastfood);
        }
    }

    private void setupActionButtons(OrderViewHolder holder, Order order) {
        // Hide all buttons first
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnStartPreparing.setVisibility(View.GONE);
        holder.btnMarkReady.setVisibility(View.GONE);
        holder.btnMarkDelivered.setVisibility(View.GONE);
        
        // Show appropriate buttons based on order status
        switch (order.getStatus()) {
            case "PLACED":
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                break;
            case "CONFIRMED":
                holder.btnStartPreparing.setVisibility(View.VISIBLE);
                break;
            case "PREPARING":
                holder.btnMarkReady.setVisibility(View.VISIBLE);
                break;
            case "READY":
                holder.btnMarkDelivered.setVisibility(View.VISIBLE);
                break;
            case "DELIVERED":
            case "CANCELLED":
                // No action buttons for completed orders
                break;
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvOrderTime, tvEta;
        TextView tvItemSummary, tvItemDetails, tvTotalAmount, tvPaymentMethod;
        Chip chipOrderStatus;
        ImageView ivItemThumbnail, ivPaymentMethod;
        MaterialButton btnCallCustomer, btnAccept, btnReject;
        MaterialButton btnStartPreparing, btnMarkReady, btnMarkDelivered, btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvEta = itemView.findViewById(R.id.tv_eta);
            tvItemSummary = itemView.findViewById(R.id.tv_item_summary);
            tvItemDetails = itemView.findViewById(R.id.tv_item_details);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            
            chipOrderStatus = itemView.findViewById(R.id.chip_order_status);
            ivItemThumbnail = itemView.findViewById(R.id.iv_item_thumbnail);
            ivPaymentMethod = itemView.findViewById(R.id.iv_payment_method);
            
            btnCallCustomer = itemView.findViewById(R.id.btn_call_customer);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnStartPreparing = itemView.findViewById(R.id.btn_start_preparing);
            btnMarkReady = itemView.findViewById(R.id.btn_mark_ready);
            btnMarkDelivered = itemView.findViewById(R.id.btn_mark_delivered);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
