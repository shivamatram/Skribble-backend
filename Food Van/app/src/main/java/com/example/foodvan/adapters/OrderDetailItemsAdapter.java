package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;

import java.util.List;

/**
 * Adapter for displaying order items in order details
 */
public class OrderDetailItemsAdapter extends RecyclerView.Adapter<OrderDetailItemsAdapter.ItemViewHolder> {

    private Context context;
    private List<Order.OrderItem> items;

    public OrderDetailItemsAdapter(Context context, List<Order.OrderItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Order.OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivItemImage;
        private ImageView ivVegIndicator;
        private TextView tvItemName;
        private TextView tvItemDescription;
        private TextView tvItemPrice;
        private TextView tvQuantity;
        private TextView tvTotalPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            ivVegIndicator = itemView.findViewById(R.id.iv_veg_indicator);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }

        public void bind(Order.OrderItem item) {
            // Set item name
            tvItemName.setText(item.getItemName() != null ? item.getItemName() : "Unknown Item");
            
            // Set item description (if available)
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
                tvItemDescription.setVisibility(View.VISIBLE);
                tvItemDescription.setText(item.getSpecialInstructions());
            } else {
                tvItemDescription.setVisibility(View.GONE);
            }
            
            // Set item price
            tvItemPrice.setText(item.getFormattedPrice());
            
            // Set quantity
            tvQuantity.setText("Qty: " + item.getQuantity());
            
            // Set total price
            tvTotalPrice.setText(item.getFormattedTotalPrice());
            
            // Set veg/non-veg indicator (default to veg for now)
            ivVegIndicator.setImageResource(R.drawable.ic_veg);
            
            // Set item image (placeholder for now)
            ivItemImage.setImageResource(R.drawable.placeholder_food_item);
        }
    }

    // Helper method to update items
    public void updateItems(List<Order.OrderItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}
