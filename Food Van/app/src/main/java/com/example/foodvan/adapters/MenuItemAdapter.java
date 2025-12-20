package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.MenuItem;

import java.util.List;

/**
 * MenuItemAdapter - RecyclerView adapter for displaying menu items
 */
public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private List<MenuItem> menuItems;
    private OnMenuItemClickListener clickListener;

    public interface OnMenuItemClickListener {
        void onAddToCart(MenuItem item);
        void onRemoveFromCart(MenuItem item);
        void onItemClick(MenuItem item);
    }

    public MenuItemAdapter(List<MenuItem> menuItems, OnMenuItemClickListener clickListener) {
        this.menuItems = menuItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_item, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.bind(menuItem);
    }

    @Override
    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    public void updateMenuItems(List<MenuItem> newMenuItems) {
        this.menuItems = newMenuItems;
        notifyDataSetChanged();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivItemImage;
        private ImageView ivVegIndicator;
        private TextView tvItemName;
        private TextView tvItemDescription;
        private TextView tvItemPrice;
        private TextView tvOriginalPrice;
        private TextView tvRating;
        private TextView tvPreparationTime;
        private TextView tvQuantity;
        private Button btnAddToCart;
        private Button btnRemoveFromCart;
        private View quantityControls;
        private TextView tvBestSeller;
        private TextView tvNew;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            ivVegIndicator = itemView.findViewById(R.id.iv_veg_indicator);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPreparationTime = itemView.findViewById(R.id.tv_preparation_time);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            btnRemoveFromCart = itemView.findViewById(R.id.btn_remove_from_cart);
            quantityControls = itemView.findViewById(R.id.quantity_controls);
            tvBestSeller = itemView.findViewById(R.id.tv_best_seller);
            tvNew = itemView.findViewById(R.id.tv_new);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onItemClick(menuItems.get(position));
                    }
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onAddToCart(menuItems.get(position));
                    }
                }
            });

            btnRemoveFromCart.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onRemoveFromCart(menuItems.get(position));
                    }
                }
            });
        }

        public void bind(MenuItem menuItem) {
            tvItemName.setText(menuItem.getName());
            tvItemDescription.setText(menuItem.getDescription());
            
            // Set price
            if (menuItem.hasDiscount()) {
                tvItemPrice.setText(menuItem.getFormattedDiscountedPrice());
                tvOriginalPrice.setText(menuItem.getFormattedPrice());
                tvOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                tvItemPrice.setText(menuItem.getFormattedPrice());
                tvOriginalPrice.setVisibility(View.GONE);
            }

            // Set rating
            if (menuItem.getRating() > 0) {
                tvRating.setText(menuItem.getFormattedRating() + " ★");
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }

            // Set preparation time
            tvPreparationTime.setText(menuItem.getFormattedPreparationTime());

            // Set veg/non-veg indicator
            if (menuItem.isVegetarian()) {
                ivVegIndicator.setImageResource(R.drawable.ic_veg);
            } else {
                ivVegIndicator.setImageResource(R.drawable.ic_non_veg);
            }

            // Set badges
            tvBestSeller.setVisibility(menuItem.isBestSeller() ? View.VISIBLE : View.GONE);
            tvNew.setVisibility(menuItem.isNew() ? View.VISIBLE : View.GONE);

            // Set quantity controls
            int quantity = menuItem.getCartQuantity();
            if (quantity > 0) {
                quantityControls.setVisibility(View.VISIBLE);
                btnAddToCart.setVisibility(View.GONE);
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                quantityControls.setVisibility(View.GONE);
                btnAddToCart.setVisibility(View.VISIBLE);
            }

            // Set availability
            if (!menuItem.isAvailable()) {
                itemView.setAlpha(0.6f);
                btnAddToCart.setEnabled(false);
                btnAddToCart.setText("Not Available");
            } else {
                itemView.setAlpha(1.0f);
                btnAddToCart.setEnabled(true);
                btnAddToCart.setText("ADD");
            }

            // Load item image
            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(menuItem.getImageUrl())
                        .placeholder(R.drawable.placeholder_food_item)
                        .error(R.drawable.placeholder_food_item)
                        .into(ivItemImage);
            } else {
                ivItemImage.setImageResource(R.drawable.placeholder_food_item);
            }
        }
    }
}
