package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing preview of filtered menu items
 */
public class FilterPreviewAdapter extends RecyclerView.Adapter<FilterPreviewAdapter.PreviewViewHolder> {
    
    private List<MenuItem> items = new ArrayList<>();
    
    public void setItems(List<MenuItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_preview, parent, false);
        return new PreviewViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        MenuItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView ivPreviewItem;
        private final TextView tvPreviewName;
        
        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreviewItem = itemView.findViewById(R.id.iv_preview_item);
            tvPreviewName = itemView.findViewById(R.id.tv_preview_name);
        }
        
        public void bind(MenuItem item) {
            // Set item name
            tvPreviewName.setText(item.getName());
            
            // Load item image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_fastfood)
                        .error(R.drawable.ic_fastfood)
                        .centerCrop()
                        .into(ivPreviewItem);
            } else {
                ivPreviewItem.setImageResource(R.drawable.ic_fastfood);
            }
        }
    }
}
