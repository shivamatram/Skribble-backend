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
 * Adapter for displaying addresses that can be deleted
 */
public class DeleteAddressAdapter extends RecyclerView.Adapter<DeleteAddressAdapter.DeleteAddressViewHolder> {

    private List<Address> addressList;
    private OnDeleteActionListener listener;

    public interface OnDeleteActionListener {
        void onDeleteAddress(Address address);
    }

    public DeleteAddressAdapter(List<Address> addressList, OnDeleteActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeleteAddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delete_address, parent, false);
        return new DeleteAddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteAddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    class DeleteAddressViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvAddressLabel;
        private TextView tvFullAddress;
        private MaterialButton btnDelete;
        private Chip chipPrimaryProtected;

        public DeleteAddressViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvAddressLabel = itemView.findViewById(R.id.tv_address_label);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            chipPrimaryProtected = itemView.findViewById(R.id.chip_primary_protected);
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

            // Handle primary address protection
            if (address.isPrimary()) {
                // Primary addresses cannot be deleted
                btnDelete.setVisibility(View.GONE);
                chipPrimaryProtected.setVisibility(View.VISIBLE);
                itemView.setAlpha(0.6f); // Make it look disabled
            } else {
                // Non-primary addresses can be deleted
                btnDelete.setVisibility(View.VISIBLE);
                chipPrimaryProtected.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                
                // Set delete click listener
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteAddress(address);
                    }
                });
            }
        }
    }
}
