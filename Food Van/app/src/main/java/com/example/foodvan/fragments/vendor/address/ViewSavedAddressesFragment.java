package com.example.foodvan.fragments.vendor.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.activities.vendor.VendorSavedAddressesActivity;
import com.example.foodvan.adapters.SavedAddressesAdapter;
import com.example.foodvan.models.Address;
import com.example.foodvan.repositories.AddressRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to view all saved vendor addresses
 */
public class ViewSavedAddressesFragment extends Fragment {

    private RecyclerView recyclerAddresses;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnAddFirstAddress;
    private FloatingActionButton fabAddAddress;
    
    private SavedAddressesAdapter addressAdapter;
    private List<Address> addressList;
    private VendorSavedAddressesActivity parentActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (VendorSavedAddressesActivity) getActivity();
        addressList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_saved_addresses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadAddresses();
    }

    private void initializeViews(View view) {
        recyclerAddresses = view.findViewById(R.id.recycler_addresses);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnAddFirstAddress = view.findViewById(R.id.btn_add_first_address);
        fabAddAddress = view.findViewById(R.id.fab_add_address);
    }

    private void setupRecyclerView() {
        addressAdapter = new SavedAddressesAdapter(getContext(), addressList, new SavedAddressesAdapter.OnAddressActionListener() {
            @Override
            public void onEditAddress(Address address) {
                // Switch to edit tab
                if (parentActivity != null) {
                    if (address.isPrimary()) {
                        parentActivity.switchToTab(2); // Switch to "Edit Primary" tab
                    } else {
                        parentActivity.showInfo("Edit address: " + address.getBusinessName());
                        // For non-primary addresses, could switch to a general edit tab or show dialog
                    }
                }
            }

            @Override
            public void onDeleteAddress(Address address) {
                // Switch to delete tab
                if (parentActivity != null) {
                    parentActivity.switchToTab(3); // Switch to "Delete" tab
                }
            }

            @Override
            public void onSetDefaultAddress(Address address) {
                // Set as primary address
                if (parentActivity != null && parentActivity.getAddressRepository() != null) {
                    parentActivity.getAddressRepository().setAddressPrimary(address.getAddressId(), new AddressRepository.AddressCallback() {
                        @Override
                        public void onSuccess() {
                            if (parentActivity != null) {
                                parentActivity.showSuccess("Set as primary: " + address.getBusinessName());
                                // Reload addresses to reflect changes
                                loadAddresses();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (parentActivity != null) {
                                parentActivity.showError("Failed to set as primary: " + error);
                            }
                        }
                    });
                }
            }
        });

        recyclerAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAddresses.setAdapter(addressAdapter);
    }

    private void setupClickListeners() {
        btnAddFirstAddress.setOnClickListener(v -> openAddAddressTab());
        fabAddAddress.setOnClickListener(v -> openAddAddressTab());
    }

    private void openAddAddressTab() {
        // Switch to Add New Address tab (index 1)
        if (parentActivity != null) {
            parentActivity.switchToTab(1); // Switch to "Add New" tab
        }
    }

    private void loadAddresses() {
        if (parentActivity != null && parentActivity.getAddressRepository() != null) {
            parentActivity.getAddressRepository().getAllAddresses(new AddressRepository.AddressListCallback() {
                @Override
                public void onSuccess(List<Address> addresses) {
                    addressList.clear();
                    addressList.addAll(addresses);
                    updateUI();
                }

                @Override
                public void onError(String error) {
                    if (parentActivity != null) {
                        parentActivity.showError("Failed to load addresses: " + error);
                    }
                    // Show empty state on error
                    addressList.clear();
                    updateUI();
                }
            });
        } else {
            // Fallback to sample data if repository is not available
            loadSampleAddresses();
        }
    }

    private void loadSampleAddresses() {
        addressList.clear();
        
        // Sample primary address
        Address primaryAddress = new Address();
        primaryAddress.setAddressId("primary_001");
        primaryAddress.setBusinessName("Main Kitchen");
        primaryAddress.setLabel("Primary Location");
        primaryAddress.setAddressLine1("123 Food Street");
        primaryAddress.setAddressLine2("Near Central Market");
        primaryAddress.setCity("Mumbai");
        primaryAddress.setState("Maharashtra");
        primaryAddress.setPostalCode("400001");
        primaryAddress.setPrimary(true);
        addressList.add(primaryAddress);

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
        addressList.add(secondaryAddress);
        updateUI();
    }

    private void updateUI() {
        if (addressList.isEmpty()) {
            showEmptyState();
        } else {
            showAddressList();
        }
    }

    private void showEmptyState() {
        recyclerAddresses.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        fabAddAddress.setVisibility(View.GONE);
    }

    private void showAddressList() {
        recyclerAddresses.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        fabAddAddress.setVisibility(View.VISIBLE);
        
        if (addressAdapter != null) {
            addressAdapter.notifyDataSetChanged();
        }
    }

    public void refreshAddresses() {
        loadAddresses();
    }
}
