package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.SupportTicket;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying support ticket items
 */
public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder> {

    private List<SupportTicket> tickets = new ArrayList<>();
    private OnTicketClickListener listener;
    private Context context;

    public interface OnTicketClickListener {
        void onTicketClick(SupportTicket ticket);
    }

    public SupportTicketAdapter() {
    }

    public SupportTicketAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void setTickets(List<SupportTicket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_support_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        SupportTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTicketId;
        private final TextView tvCategory;
        private final TextView tvDescription;
        private final TextView tvCreatedAt;
        private final Chip chipStatus;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        void bind(SupportTicket ticket) {
            tvTicketId.setText(ticket.getShortTicketId());
            tvCategory.setText(ticket.getCategory());
            tvDescription.setText(ticket.getDescription());
            tvCreatedAt.setText(ticket.getFormattedCreatedDate());

            // Set status chip appearance
            chipStatus.setText(ticket.getStatus());
            updateStatusChipStyle(ticket.getStatus());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTicketClick(ticket);
                }
            });
        }

        private void updateStatusChipStyle(String status) {
            int backgroundColor;
            int textColor = ContextCompat.getColor(context, R.color.white);

            switch (status) {
                case SupportTicket.STATUS_OPEN:
                    backgroundColor = ContextCompat.getColor(context, R.color.info_color);
                    break;
                case SupportTicket.STATUS_IN_PROGRESS:
                    backgroundColor = ContextCompat.getColor(context, R.color.warning_color);
                    break;
                case SupportTicket.STATUS_RESOLVED:
                    backgroundColor = ContextCompat.getColor(context, R.color.success_color);
                    break;
                case SupportTicket.STATUS_CLOSED:
                    backgroundColor = ContextCompat.getColor(context, R.color.gray_500);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.gray_400);
                    break;
            }

            chipStatus.setChipBackgroundColorResource(android.R.color.transparent);
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
            chipStatus.setTextColor(textColor);
        }
    }
}
