package com.example.foodvan.adapters;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.FAQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying FAQ items with expandable answers
 */
public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQ> faqs = new ArrayList<>();
    private OnFAQClickListener listener;

    public interface OnFAQClickListener {
        void onFAQClick(int position);
    }

    public FAQAdapter() {
    }

    public FAQAdapter(OnFAQClickListener listener) {
        this.listener = listener;
    }

    public void setFaqs(List<FAQ> faqs) {
        this.faqs = faqs != null ? faqs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQ faq = faqs.get(position);
        holder.bind(faq, position);
    }

    @Override
    public int getItemCount() {
        return faqs.size();
    }

    class FAQViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvQuestion;
        private final TextView tvAnswer;
        private final ImageView ivExpand;

        FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }

        void bind(FAQ faq, int position) {
            tvQuestion.setText(faq.getQuestion());
            tvAnswer.setText(faq.getAnswer());

            // Set expanded state
            updateExpandedState(faq.isExpanded(), false);

            // Click listener
            itemView.setOnClickListener(v -> {
                faq.toggleExpanded();
                updateExpandedState(faq.isExpanded(), true);
                
                if (listener != null) {
                    listener.onFAQClick(position);
                }
            });
        }

        private void updateExpandedState(boolean expanded, boolean animate) {
            if (expanded) {
                tvAnswer.setVisibility(View.VISIBLE);
                if (animate) {
                    // Animate arrow rotation
                    ObjectAnimator.ofFloat(ivExpand, "rotation", 0f, 180f)
                            .setDuration(200)
                            .start();
                } else {
                    ivExpand.setRotation(180f);
                }
            } else {
                tvAnswer.setVisibility(View.GONE);
                if (animate) {
                    ObjectAnimator.ofFloat(ivExpand, "rotation", 180f, 0f)
                            .setDuration(200)
                            .start();
                } else {
                    ivExpand.setRotation(0f);
                }
            }
        }
    }
}
