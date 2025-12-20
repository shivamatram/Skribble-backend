package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.Address;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Adapter for displaying saved vendor addresses in RecyclerView
 */
public class SavedAddressAdapter extends RecyclerView.Adapter<SavedAddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetPrimary(Address address);
    }

    public SavedAddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvAddressLabel;
        private TextView tvFullAddress;
        private Chip chipPrimary;
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
            if (fullAddress.isEmpty()) {
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
            btnSetPrimary.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetPrimary(address);
                }
            });

            btnEditAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });

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
}
