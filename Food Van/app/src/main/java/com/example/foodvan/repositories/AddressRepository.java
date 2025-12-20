package com.example.foodvan.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.foodvan.models.Address;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressRepository {
    private static final String TAG = "AddressRepository";
    private DatabaseReference addressesRef;
    private String vendorId;

    public AddressRepository(String vendorId) {
        this.vendorId = vendorId;
        this.addressesRef = FirebaseDatabase.getInstance().getReference("vendor_addresses").child(vendorId);
    }

    // Interface for callbacks
    public interface AddressCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface AddressListCallback {
        void onSuccess(List<Address> addresses);
        void onError(String error);
    }

    public interface SingleAddressCallback {
        void onSuccess(Address address);
        void onError(String error);
    }

    // Add new address
    public void addAddress(Address address, AddressCallback callback) {
        String addressId = addressesRef.push().getKey();
        if (addressId != null) {
            address.setAddressId(addressId);
            address.setCreatedAt(System.currentTimeMillis());
            address.setLastUsed(System.currentTimeMillis());
            
            // If this is set as primary, first remove primary from other addresses
            if (address.isPrimary()) {
                clearOtherPrimaryAddresses(() -> {
                    saveAddressToDatabase(address, callback);
                }, callback);
            } else {
                saveAddressToDatabase(address, callback);
            }
        } else {
            callback.onError("Failed to generate address ID");
        }
    }

    // Update existing address
    public void updateAddress(Address address, AddressCallback callback) {
        if (address.getAddressId() == null) {
            callback.onError("Address ID is required for update");
            return;
        }
        
        address.setLastUsed(System.currentTimeMillis());
        
        // If this is set as primary, first remove primary from other addresses
        if (address.isPrimary()) {
            clearOtherPrimaryAddresses(() -> {
                saveAddressToDatabase(address, callback);
            }, callback);
        } else {
            saveAddressToDatabase(address, callback);
        }
    }

    // Delete address
    public void deleteAddress(String addressId, AddressCallback callback) {
        if (addressId == null) {
            callback.onError("Address ID is required for deletion");
            return;
        }
        
        // Check if this is a primary address
        addressesRef.child(addressId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Address address = snapshot.getValue(Address.class);
                if (address != null && address.isPrimary()) {
                    callback.onError("Cannot delete primary address. Set another address as primary first.");
                } else {
                    // Safe to delete
                    addressesRef.child(addressId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Address deleted successfully");
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to delete address", e);
                            callback.onError("Failed to delete address: " + e.getMessage());
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check address", error.toException());
                callback.onError("Failed to check address: " + error.getMessage());
            }
        });
    }

    // Get all addresses
    public void getAllAddresses(AddressListCallback callback) {
        addressesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Address> addresses = new ArrayList<>();
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address address = addressSnapshot.getValue(Address.class);
                    if (address != null) {
                        addresses.add(address);
                    }
                }
                Log.d(TAG, "Retrieved " + addresses.size() + " addresses");
                callback.onSuccess(addresses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to retrieve addresses", error.toException());
                callback.onError("Failed to retrieve addresses: " + error.getMessage());
            }
        });
    }

    // Get primary address
    public void getPrimaryAddress(SingleAddressCallback callback) {
        addressesRef.orderByChild("primary").equalTo(true).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                            Address address = addressSnapshot.getValue(Address.class);
                            if (address != null) {
                                callback.onSuccess(address);
                                return;
                            }
                        }
                    }
                    callback.onError("No primary address found");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to retrieve primary address", error.toException());
                    callback.onError("Failed to retrieve primary address: " + error.getMessage());
                }
            });
    }

    // Set address as primary
    public void setAddressPrimary(String addressId, AddressCallback callback) {
        // First clear other primary addresses, then set this one as primary
        clearOtherPrimaryAddresses(() -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("primary", true);
            updates.put("lastUsed", System.currentTimeMillis());
            
            addressesRef.child(addressId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Address set as primary successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to set address as primary", e);
                    callback.onError("Failed to set address as primary: " + e.getMessage());
                });
        }, callback);
    }

    // Helper method to save address to database
    private void saveAddressToDatabase(Address address, AddressCallback callback) {
        addressesRef.child(address.getAddressId()).setValue(address)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Address saved successfully");
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save address", e);
                callback.onError("Failed to save address: " + e.getMessage());
            });
    }

    // Helper method to clear primary flag from other addresses
    private void clearOtherPrimaryAddresses(Runnable onSuccess, AddressCallback callback) {
        addressesRef.orderByChild("primary").equalTo(true)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String, Object> updates = new HashMap<>();
                    for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                        updates.put(addressSnapshot.getKey() + "/primary", false);
                    }
                    
                    if (!updates.isEmpty()) {
                        addressesRef.updateChildren(updates)
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to clear other primary addresses", e);
                                callback.onError("Failed to clear other primary addresses: " + e.getMessage());
                            });
                    } else {
                        onSuccess.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to clear other primary addresses", error.toException());
                    callback.onError("Failed to clear other primary addresses: " + error.getMessage());
                }
            });
    }

    // Get deletable addresses (non-primary addresses)
    public void getDeletableAddresses(AddressListCallback callback) {
        addressesRef.orderByChild("primary").equalTo(false)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Address> addresses = new ArrayList<>();
                    for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                        Address address = addressSnapshot.getValue(Address.class);
                        if (address != null) {
                            addresses.add(address);
                        }
                    }
                    Log.d(TAG, "Retrieved " + addresses.size() + " deletable addresses");
                    callback.onSuccess(addresses);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to retrieve deletable addresses", error.toException());
                    callback.onError("Failed to retrieve deletable addresses: " + error.getMessage());
                }
            });
    }

    // Clean up listeners
    public void cleanup() {
        if (addressesRef != null) {
            addressesRef.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
