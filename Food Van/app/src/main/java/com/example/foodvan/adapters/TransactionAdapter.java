package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactions;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView transactionOrderId;
        private TextView transactionDate;
        private TextView transactionPaymentMethod;
        private TextView transactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            transactionOrderId = itemView.findViewById(R.id.transaction_order_id);
            transactionDate = itemView.findViewById(R.id.transaction_date);
            transactionPaymentMethod = itemView.findViewById(R.id.transaction_payment_method);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(transactions.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Transaction transaction) {
            // Order ID
            transactionOrderId.setText("Order #" + transaction.getOrderId());

            // Date
            transactionDate.setText(formatTimestamp(transaction.getTimestamp()));

            // Payment Method
            transactionPaymentMethod.setText(transaction.getPaymentMethod());

            // Amount
            transactionAmount.setText(String.format(Locale.getDefault(), "+ ₹%.0f", transaction.getAmount()));
        }

        private String formatTimestamp(Date timestamp) {
            if (timestamp == null) return "Unknown";
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            return sdf.format(timestamp);
        }
    }
}
