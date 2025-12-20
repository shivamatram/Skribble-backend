package com.example.foodvan.fragments.vendor.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.activities.vendor.VendorSavedAddressesActivity;
import com.example.foodvan.adapters.DeleteAddressAdapter;
import com.example.foodvan.models.Address;
import com.example.foodvan.repositories.AddressRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to delete vendor addresses with confirmation
 */
public class DeleteAddressFragment extends Fragment {

    private RecyclerView recyclerDeleteAddresses;
    private LinearLayout layoutEmptyState;
    
    private DeleteAddressAdapter deleteAdapter;
    private List<Address> deletableAddressList;
    private VendorSavedAddressesActivity parentActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (VendorSavedAddressesActivity) getActivity();
        deletableAddressList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delete_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        loadDeletableAddresses();
    }

    private void initializeViews(View view) {
        recyclerDeleteAddresses = view.findViewById(R.id.recycler_delete_addresses);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        deleteAdapter = new DeleteAddressAdapter(deletableAddressList, new DeleteAddressAdapter.OnDeleteActionListener() {
            @Override
            public void onDeleteAddress(Address address) {
                showDeleteConfirmationDialog(address);
            }
        });

        recyclerDeleteAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerDeleteAddresses.setAdapter(deleteAdapter);
    }

    private void loadDeletableAddresses() {
        if (parentActivity != null && parentActivity.getAddressRepository() != null) {
            parentActivity.getAddressRepository().getDeletableAddresses(new AddressRepository.AddressListCallback() {
                @Override
                public void onSuccess(List<Address> addresses) {
                    deletableAddressList.clear();
                    deletableAddressList.addAll(addresses);
                    updateUI();
                }

                @Override
                public void onError(String error) {
                    if (parentActivity != null) {
                        parentActivity.showError("Failed to load addresses: " + error);
                    }
                    // Show empty state on error
                    deletableAddressList.clear();
                    updateUI();
                }
            });
        } else {
            // Fallback to sample data
            loadSampleDeletableAddresses();
        }
    }

    private void loadSampleDeletableAddresses() {
        deletableAddressList.clear();
        
        // Sample secondary address
        Address secondaryAddress = new Address();
        secondaryAddress.setAddressId("secondary_001");
        secondaryAddress.setBusinessName("Branch Kitchen");
        secondaryAddress.setLabel("Secondary Location");
        secondaryAddress.setAddressLine1("456 Market Road");
        secondaryAddress.setAddressLine2("Opposite Mall");
        secondaryAddress.setCity("Mumbai");
        secondaryAddress.setState("Maharashtra");
        secondaryAddress.setPostalCode("400002");
        secondaryAddress.setPrimary(false);
        deletableAddressList.add(secondaryAddress);

        // Sample tertiary address
        Address tertiaryAddress = new Address();
        tertiaryAddress.setAddressId("tertiary_001");
        tertiaryAddress.setBusinessName("Special Events Kitchen");
        tertiaryAddress.setLabel("Special Events");
        tertiaryAddress.setAddressLine1("789 Event Plaza");
        tertiaryAddress.setAddressLine2("Exhibition Center");
        tertiaryAddress.setCity("Mumbai");
        tertiaryAddress.setState("Maharashtra");
        tertiaryAddress.setPostalCode("400003");
        tertiaryAddress.setPrimary(false);
        deletableAddressList.add(tertiaryAddress);

        updateUI();
    }

    private void updateUI() {
        if (deletableAddressList.isEmpty()) {
            showEmptyState();
        } else {
            showAddressList();
        }
    }

    private void showEmptyState() {
        recyclerDeleteAddresses.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showAddressList() {
        recyclerDeleteAddresses.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        
        if (deleteAdapter != null) {
            deleteAdapter.notifyDataSetChanged();
        }
    }

    private void showDeleteConfirmationDialog(Address address) {
        String addressName = address.getBusinessName() != null && !address.getBusinessName().isEmpty() 
            ? address.getBusinessName() 
            : address.getLabel();

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Address")
            .setMessage("Are you sure you want to delete \"" + addressName + "\"?\n\nThis action cannot be undone.")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteAddress(address);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteAddress(Address address) {
        String addressName = address.getBusinessName() != null && !address.getBusinessName().isEmpty() 
            ? address.getBusinessName() 
            : address.getLabel();
            
        if (parentActivity != null && parentActivity.getAddressRepository() != null) {
            parentActivity.getAddressRepository().deleteAddress(address.getAddressId(), new AddressRepository.AddressCallback() {
                @Override
                public void onSuccess() {
                    // Remove from local list
                    deletableAddressList.remove(address);
                    updateUI();
                    
                    if (parentActivity != null) {
                        parentActivity.showSuccess("\"" + addressName + "\" deleted successfully");
                        // Switch back to View Addresses tab to see updated list
                        parentActivity.switchToTab(0);
                    }
                }

                @Override
                public void onError(String error) {
                    if (parentActivity != null) {
                        parentActivity.showError("Failed to delete address: " + error);
                    }
                }
            });
        } else {
            // Fallback to old method
            deletableAddressList.remove(address);
            deleteAddressFromDatabase(address);
            updateUI();
            
            if (parentActivity != null) {
                parentActivity.showSuccess("\"" + addressName + "\" deleted successfully");
                parentActivity.switchToTab(0);
            }
        }
    }

    private void deleteAddressFromDatabase(Address address) {
        // In real implementation, delete from Firebase
        // For now, just simulate deletion
        if (parentActivity != null && parentActivity.getAddressesRef() != null) {
            // parentActivity.getAddressesRef().child(address.getAddressId()).removeValue();
        }
    }

    public void refreshAddresses() {
        loadDeletableAddresses();
    }
}
