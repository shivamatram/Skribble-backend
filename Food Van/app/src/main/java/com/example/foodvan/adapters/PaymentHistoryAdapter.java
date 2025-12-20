package com.example.foodvan.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.PaymentTransaction;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for Payment History RecyclerView
 */
public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private List<PaymentTransaction> transactions;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(PaymentTransaction transaction);
    }

    public PaymentHistoryAdapter(List<PaymentTransaction> transactions, OnPaymentClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_transaction, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentTransaction transaction = transactions.get(position);
        holder.bind(transaction, listener);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView ivPaymentMethod;
        private TextView tvTransactionId, tvAmount, tvDescription, tvDateTime, tvCustomerName;
        private Chip chipStatus;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_transaction);
            ivPaymentMethod = itemView.findViewById(R.id.iv_payment_method);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }

        public void bind(PaymentTransaction transaction, OnPaymentClickListener listener) {
            // Set transaction ID
            tvTransactionId.setText(transaction.getTransactionId());

            // Set amount
            tvAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));

            // Set description
            tvDescription.setText(transaction.getDescription());

            // Set customer name if available
            if (transaction.getCustomerName() != null && !transaction.getCustomerName().isEmpty()) {
                tvCustomerName.setText(transaction.getCustomerName());
                tvCustomerName.setVisibility(View.VISIBLE);
            } else {
                tvCustomerName.setVisibility(View.GONE);
            }

            // Set date and time
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date(transaction.getTimestamp())));

            // Set payment method icon
            setPaymentMethodIcon(transaction.getPaymentMethod());

            // Set status chip
            setStatusChip(transaction.getStatus());

            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(transaction);
                }
            });
        }

        private void setPaymentMethodIcon(String paymentMethod) {
            int iconRes;
            switch (paymentMethod) {
                case "UPI":
                    iconRes = R.drawable.ic_payment;
                    break;
                case "Card":
                    iconRes = R.drawable.ic_credit_card;
                    break;
                case "Bank Transfer":
                    iconRes = R.drawable.ic_account_balance;
                    break;
                case "Wallet":
                    iconRes = R.drawable.ic_wallet;
                    break;
                default:
                    iconRes = R.drawable.ic_payment;
                    break;
            }
            ivPaymentMethod.setImageResource(iconRes);
            // Set orange tint for consistency
            ivPaymentMethod.setColorFilter(itemView.getContext().getColor(R.color.primary_color));
        }

        private void setStatusChip(String status) {
            chipStatus.setText(status);
            
            int backgroundColor;
            int textColor = Color.WHITE;
            
            switch (status) {
                case "Success":
                    backgroundColor = itemView.getContext().getColor(R.color.success_color);
                    break;
                case "Pending":
                    backgroundColor = itemView.getContext().getColor(R.color.warning_color);
                    break;
                case "Failed":
                    backgroundColor = itemView.getContext().getColor(R.color.error_color);
                    break;
                default:
                    backgroundColor = itemView.getContext().getColor(R.color.info_color);
                    break;
            }
            
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
            chipStatus.setTextColor(textColor);
        }
    }
}
