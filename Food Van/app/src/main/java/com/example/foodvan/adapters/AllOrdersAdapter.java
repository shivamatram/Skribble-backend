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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllOrdersAdapter extends RecyclerView.Adapter<AllOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onCallCustomer(Order order);
        void onViewOrderDetails(Order order);
    }

    public AllOrdersAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
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
        private TextView orderIdText;
        private Chip statusChip;
        private TextView customerNameText;
        private ImageView callCustomerIcon;
        private TextView timestampText;
        private TextView totalAmountText;
        private ImageView paymentMethodIcon;
        private TextView paymentMethodText;
        private MaterialButton viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            orderIdText = itemView.findViewById(R.id.order_id_text);
            statusChip = itemView.findViewById(R.id.status_chip);
            customerNameText = itemView.findViewById(R.id.customer_name_text);
            callCustomerIcon = itemView.findViewById(R.id.call_customer_icon);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            totalAmountText = itemView.findViewById(R.id.total_amount_text);
            paymentMethodIcon = itemView.findViewById(R.id.payment_method_icon);
            paymentMethodText = itemView.findViewById(R.id.payment_method_text);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);

            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(orders.get(getAdapterPosition()));
                }
            });

            callCustomerIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallCustomer(orders.get(getAdapterPosition()));
                }
            });

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOrderDetails(orders.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Order order) {
            // Order ID
            orderIdText.setText("#" + order.getOrderId());

            // Status chip with color
            statusChip.setText(order.getStatus());
            setStatusChipColor(order.getStatus());

            // Customer name
            customerNameText.setText(order.getCustomerName());

            // Timestamp
            timestampText.setText(formatTimestamp(order.getTimestamp()));

            // Total amount
            totalAmountText.setText("₹" + String.format(Locale.getDefault(), "%.0f", order.getTotalAmount()));

            // Payment method
            paymentMethodText.setText(order.getPaymentMethod());
            setPaymentMethodIcon(order.getPaymentMethod());
        }

        private void setStatusChipColor(String status) {
            int colorResId;
            switch (status.toLowerCase()) {
                case "pending":
                    colorResId = R.color.status_pending;
                    break;
                case "accepted":
                    colorResId = R.color.status_accepted;
                    break;
                case "preparing":
                    colorResId = R.color.status_preparing;
                    break;
                case "ready":
                    colorResId = R.color.status_ready;
                    break;
                case "delivered":
                    colorResId = R.color.status_delivered;
                    break;
                case "cancelled":
                    colorResId = R.color.status_cancelled;
                    break;
                default:
                    colorResId = R.color.status_pending;
                    break;
            }
            statusChip.setChipBackgroundColorResource(colorResId);
        }

        private void setPaymentMethodIcon(String paymentMethod) {
            int iconResId;
            switch (paymentMethod.toLowerCase()) {
                case "upi":
                    iconResId = R.drawable.ic_payment;
                    break;
                case "card":
                    iconResId = R.drawable.ic_credit_card;
                    break;
                case "cod":
                case "cash":
                    iconResId = R.drawable.ic_money;
                    break;
                default:
                    iconResId = R.drawable.ic_payment;
                    break;
            }
            paymentMethodIcon.setImageResource(iconResId);
        }

        private String formatTimestamp(Date timestamp) {
            if (timestamp == null) return "Unknown";
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            return sdf.format(timestamp);
        }
    }
}
