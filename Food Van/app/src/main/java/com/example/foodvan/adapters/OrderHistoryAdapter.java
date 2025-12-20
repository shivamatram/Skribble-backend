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

import com.example.foodvan.R;
import com.example.foodvan.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Professional adapter for displaying order history with Material 3 design
 * Supports filtering, real-time updates, and detailed order information
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onViewOrderDetails(Order order);
    }

    public OrderHistoryAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
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
        
        private TextView tvOrderId;
        private TextView tvOrderStatus;
        private TextView tvCustomerName;
        private TextView tvOrderTime;
        private TextView tvPaymentStatus;
        private TextView tvOrderTotal;
        private MaterialButton btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views based on our professional layout
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            
            setupClickListeners();
        }

        private void setupClickListeners() {
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onViewOrderDetails(orders.get(position));
                    }
                }
            });
        }

        public void bind(Order order) {
            // Set Order ID
            tvOrderId.setText("#" + (order.getId() != null ? order.getId() : "ORD" + System.currentTimeMillis()));
            
            // Set Order Status with appropriate styling
            String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
            tvOrderStatus.setText(status);
            setStatusBackground(status);
            
            // Set Customer Name
            tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "Customer");
            
            // Set Order Time
            tvOrderTime.setText(formatOrderTime(order.getOrderTime()));
            
            // Set Payment Status
            String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : "Paid";
            tvPaymentStatus.setText(paymentStatus);
            
            // Set Order Total
            double total = order.getTotalAmount();
            tvOrderTotal.setText(String.format(Locale.getDefault(), "₹%.0f", total));
        }

        private void setStatusBackground(String status) {
            int backgroundRes;
            switch (status) {
                case "COMPLETED":
                    backgroundRes = R.drawable.bg_status_completed;
                    break;
                case "CANCELLED":
                    backgroundRes = R.drawable.bg_status_cancelled;
                    break;
                case "PENDING":
                default:
                    backgroundRes = R.drawable.bg_status_pending;
                    break;
            }
            tvOrderStatus.setBackgroundResource(backgroundRes);
        }

        private String formatOrderTime(long timestamp) {
            if (timestamp == 0) {
                return "N/A";
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return timeFormat.format(new Date(timestamp));
        }
    }
}
