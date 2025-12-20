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
import com.example.foodvan.models.Address;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * SavedAddressesAdapter - RecyclerView adapter for managing saved addresses
 * Features: Display addresses, edit, delete, set default with smooth animations
 */
public class SavedAddressesAdapter extends RecyclerView.Adapter<SavedAddressesAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addressesList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetDefaultAddress(Address address);
    }

    public SavedAddressesAdapter(Context context, List<Address> addressesList, OnAddressActionListener listener) {
        this.context = context;
        this.addressesList = addressesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressesList.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addressesList.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvAddressLabel;
        private TextView tvFullAddress;
        private com.google.android.material.chip.Chip chipPrimary;
        private MaterialButton btnSetPrimary;
        private MaterialButton btnEditAddress;
        private MaterialButton btnDeleteAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvAddressLabel = itemView.findViewById(R.id.tv_address_label);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            chipPrimary = itemView.findViewById(R.id.chip_primary);
            btnSetPrimary = itemView.findViewById(R.id.btn_set_primary);
            btnEditAddress = itemView.findViewById(R.id.btn_edit_address);
            btnDeleteAddress = itemView.findViewById(R.id.btn_delete_address);
        }

        public void bind(Address address) {
            // Set address label (business name or label)
            String displayLabel = address.getBusinessName() != null && !address.getBusinessName().isEmpty() 
                ? address.getBusinessName() 
                : address.getLabel();
            tvAddressLabel.setText(displayLabel);
            
            // Set full address
            String fullAddress = address.getFormattedAddress();
            if (fullAddress == null || fullAddress.trim().isEmpty()) {
                fullAddress = address.getFullAddress();
            }
            tvFullAddress.setText(fullAddress);
            
            // Handle primary status
            if (address.isPrimary()) {
                chipPrimary.setVisibility(View.VISIBLE);
                btnSetPrimary.setVisibility(View.GONE);
            } else {
                chipPrimary.setVisibility(View.GONE);
                btnSetPrimary.setVisibility(View.VISIBLE);
            }
            
            // Set click listeners
            setupClickListeners(address);
        }

        private void setupClickListeners(Address address) {
            // Set primary button
            btnSetPrimary.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetDefaultAddress(address);
                }
            });
            
            // Edit button
            btnEditAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });
            
            // Delete button
            btnDeleteAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });
            
            // Disable delete for primary address
            if (address.isPrimary()) {
                btnDeleteAddress.setEnabled(false);
                btnDeleteAddress.setAlpha(0.5f);
            } else {
                btnDeleteAddress.setEnabled(true);
                btnDeleteAddress.setAlpha(1.0f);
            }
        }

    }

    // Animation methods for smooth list updates
    public void addAddress(Address address) {
        addressesList.add(address);
        notifyItemInserted(addressesList.size() - 1);
    }

    public void updateAddress(Address address) {
        for (int i = 0; i < addressesList.size(); i++) {
            if (addressesList.get(i).getAddressId().equals(address.getAddressId())) {
                addressesList.set(i, address);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeAddress(Address address) {
        for (int i = 0; i < addressesList.size(); i++) {
            if (addressesList.get(i).getAddressId().equals(address.getAddressId())) {
                addressesList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateAddresses(List<Address> newAddresses) {
        addressesList.clear();
        addressesList.addAll(newAddresses);
        notifyDataSetChanged();
    }
}
