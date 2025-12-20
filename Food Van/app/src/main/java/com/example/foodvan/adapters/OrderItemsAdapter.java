package com.example.foodvan.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

    private Context context;
    private List<Order.OrderItem> orderItems;

    public OrderItemsAdapter(Context context) {
        this.context = context;
        this.orderItems = new ArrayList<>();
    }

    public void updateItems(List<Order.OrderItem> items) {
        this.orderItems.clear();
        if (items != null) {
            this.orderItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order.OrderItem item = orderItems.get(position);
        
        // Item name and quantity
        holder.tvItemName.setText(item.getItemName());
        holder.tvQuantity.setText(item.getQuantity() + "x");
        
        // Customizations (using specialInstructions)
        if (!TextUtils.isEmpty(item.getSpecialInstructions())) {
            holder.tvCustomizations.setVisibility(View.VISIBLE);
            holder.tvCustomizations.setText(item.getSpecialInstructions());
        } else {
            holder.tvCustomizations.setVisibility(View.GONE);
        }
        
        // Prices
        holder.tvUnitPrice.setText(String.format("₹%.0f each", item.getPrice()));
        holder.tvTotalPrice.setText(String.format("₹%.0f", item.getTotalPrice()));
        
        // Item image - nested OrderItem doesn't have image URL, use default
        holder.ivItemImage.setImageResource(R.drawable.ic_fastfood);
        
        // Dietary indicators - nested OrderItem doesn't have these fields, hide them
        holder.ivVegIndicator.setVisibility(View.GONE);
        holder.ivSpicyIndicator.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage, ivVegIndicator, ivSpicyIndicator;
        TextView tvItemName, tvQuantity, tvCustomizations, tvUnitPrice, tvTotalPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            ivVegIndicator = itemView.findViewById(R.id.iv_veg_indicator);
            ivSpicyIndicator = itemView.findViewById(R.id.iv_spicy_indicator);
            
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvCustomizations = itemView.findViewById(R.id.tv_customizations);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }
}
