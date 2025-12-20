package com.example.foodvan.fragments.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodvan.R;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.HashMap;
import java.util.Map;

/**
 * Vendor Profile Tab Fragment
 * Handles profile information editing
 */
public class VendorProfileTabFragment extends Fragment {

    private static final String TAG = "VendorProfileTab";
    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Components
    private ShapeableImageView profileImage;
    private MaterialButton btnChangePhoto;
    private TextInputLayout tilVendorName, tilBusinessName, tilEmail, tilPhone;
    private TextInputLayout tilDescription, tilCuisineType, tilAddress;
    private TextInputEditText etVendorName, etBusinessName, etEmail, etPhone;
    private TextInputEditText etDescription, etAddress;
    private AutoCompleteTextView etCuisineType;
    private MaterialButton btnCancel, btnSave;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef;
    private StorageReference storageRef;
    private SessionManager sessionManager;
    private Vendor currentVendor;
    
    // Image handling
    private Uri selectedImageUri;

    // Data
    private String[] cuisineTypes = {
            "North Indian", "South Indian", "Chinese", "Italian", "Mexican",
            "Thai", "Continental", "Fast Food", "Street Food", "Beverages",
            "Desserts", "Bakery", "Pizza", "Burger", "Sandwich"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vendor_profile_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupCuisineDropdown();
        setupClickListeners();
        loadVendorData();
    }

    private void initializeComponents(View view) {
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(requireContext());
        
        try {
            // Initialize Firebase Storage with proper configuration
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Use the specific storage bucket from google-services.json
            storage = FirebaseStorage.getInstance("gs://food-van-app.firebasestorage.app");
            storageRef = storage.getReference();
            
            // Test Firebase Storage connection
            if (firebaseAuth.getCurrentUser() == null) {
                showError("Please login again to upload images");
            }
        } catch (Exception e) {
            showError("Firebase Storage initialization failed: " + e.getMessage());
        }
        
        String vendorId = sessionManager.getUserId();
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
        }

        // Initialize UI components
        profileImage = view.findViewById(R.id.profile_image);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);
        
        tilVendorName = view.findViewById(R.id.til_vendor_name);
        tilBusinessName = view.findViewById(R.id.til_business_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilPhone = view.findViewById(R.id.til_phone);
        tilDescription = view.findViewById(R.id.til_description);
        tilCuisineType = view.findViewById(R.id.til_cuisine_type);
        tilAddress = view.findViewById(R.id.til_address);
        
        etVendorName = view.findViewById(R.id.et_vendor_name);
        etBusinessName = view.findViewById(R.id.et_business_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etDescription = view.findViewById(R.id.et_description);
        etCuisineType = view.findViewById(R.id.et_cuisine_type);
        etAddress = view.findViewById(R.id.et_address);
        
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);
    }

    private void setupCuisineDropdown() {
        if (etCuisineType != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    cuisineTypes
            );
            etCuisineType.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> openImagePicker());
        }
        
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }
        
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfileChanges());
        }
    }

    private void loadVendorData() {
        if (vendorRef == null) {
            showError("Unable to load vendor data - not authenticated");
            return;
        }

        // Show loading state
        setFieldsEnabled(false);
        if (btnSave != null) {
            btnSave.setText("Loading...");
            btnSave.setEnabled(false);
        }

        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentVendor = snapshot.getValue(Vendor.class);
                if (currentVendor != null) {
                    populateFields();
                } else {
                    // Create default vendor data if doesn't exist
                    createDefaultVendorData();
                }
                
                // Enable fields after loading
                setFieldsEnabled(true);
                if (btnSave != null) {
                    btnSave.setText("Save Changes");
                    btnSave.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load vendor data: " + error.getMessage());
                setFieldsEnabled(true);
                if (btnSave != null) {
                    btnSave.setText("Save Changes");
                    btnSave.setEnabled(true);
                }
            }
        });
    }

    private void populateFields() {
        if (currentVendor == null) return;

        if (etVendorName != null) {
            etVendorName.setText(currentVendor.getName());
        }
        if (etBusinessName != null) {
            etBusinessName.setText(currentVendor.getVanName()); // Use vanName instead of businessName
        }
        if (etEmail != null) {
            etEmail.setText(currentVendor.getEmail());
        }
        if (etPhone != null) {
            etPhone.setText(currentVendor.getPhone());
        }
        if (etDescription != null) {
            etDescription.setText(currentVendor.getDescription());
        }
        if (etCuisineType != null) {
            etCuisineType.setText(currentVendor.getCategory(), false); // Use category instead of cuisineType
        }
        if (etAddress != null) {
            etAddress.setText(currentVendor.getAddress());
        }
        
        // Load profile image if available
        if (profileImage != null && currentVendor.getAvatarUrl() != null && !currentVendor.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentVendor.getAvatarUrl())
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(profileImage);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK 
            && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (profileImage != null) {
                // Display selected image immediately with circular crop
                Glide.with(this)
                        .load(selectedImageUri)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(profileImage);
            }
        }
    }

    private void saveProfileChanges() {
        if (!validateInputs()) {
            return;
        }

        if (vendorRef == null) {
            showError("Unable to save changes");
            return;
        }

        // Disable save button during save
        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
        }

        // If image is selected, upload it first, then save profile data
        if (selectedImageUri != null) {
            // Try simple upload first, fallback to profile save if it fails
            tryImageUpload();
        } else {
            // No new image, just save profile data
            saveProfileDataToDatabase(null);
        }
    }

    private void tryImageUpload() {
        // First check if all required components are available
        String vendorId = sessionManager.getUserId();
        if (vendorId == null) {
            showError("User not authenticated. Saving profile without image...");
            saveProfileDataToDatabase(null);
            return;
        }

        if (selectedImageUri == null) {
            showError("No image selected. Saving profile data...");
            saveProfileDataToDatabase(null);
            return;
        }

        if (storageRef == null) {
            showError("Storage not initialized. Saving profile without image...");
            saveProfileDataToDatabase(null);
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            showError("Please login again. Saving profile without image...");
            saveProfileDataToDatabase(null);
            return;
        }

        // Try the upload
        uploadImageAndSaveProfile();
    }

    private void uploadImageAndSaveProfile() {
        String vendorId = sessionManager.getUserId();
        if (vendorId == null || storageRef == null) {
            showError("Unable to upload image - authentication required");
            resetSaveButton();
            return;
        }

        if (selectedImageUri == null) {
            showError("No image selected");
            resetSaveButton();
            return;
        }

        // Create a unique filename with timestamp to avoid conflicts
        String fileName = "profile_" + vendorId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child("vendor_profiles/" + fileName);

        // Show upload progress
        if (btnSave != null) {
            btnSave.setText("Uploading image...");
        }

        // Check authentication before upload
        if (firebaseAuth.getCurrentUser() == null) {
            showError("Please login again to upload images");
            resetSaveButton();
            return;
        }

        // Upload the image with metadata
        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        
        uploadTask.addOnProgressListener(taskSnapshot -> {
            // Show upload progress
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (btnSave != null) {
                btnSave.setText("Uploading " + (int) progress + "%");
            }
        }).addOnSuccessListener(taskSnapshot -> {
            if (btnSave != null) {
                btnSave.setText("Getting image URL...");
            }
            
            // Get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Save profile data with image URL
                saveProfileDataToDatabase(downloadUri.toString());
            }).addOnFailureListener(e -> {
                showError("Failed to get image URL: " + e.getMessage());
                // Try alternative approach - save without image
                showError("Saving profile without image...");
                saveProfileDataToDatabase(null);
            });
        }).addOnFailureListener(e -> {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("does not exist")) {
                showError("Firebase Storage not properly configured. Please contact support.");
            } else if (errorMsg != null && errorMsg.contains("permission")) {
                showError("Permission denied. Please check Firebase Storage rules.");
            } else {
                showError("Upload failed: " + errorMsg + "\nTrying to save profile without image...");
            }
            
            // Fallback: Save profile without image
            saveProfileDataToDatabase(null);
        });
    }

    private void saveProfileDataToDatabase(String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etVendorName.getText().toString().trim());
        updates.put("vanName", etBusinessName.getText().toString().trim());
        updates.put("email", etEmail.getText().toString().trim());
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("description", etDescription.getText().toString().trim());
        updates.put("category", etCuisineType.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("lastLocationUpdate", System.currentTimeMillis());

        // Add image URL if provided
        if (imageUrl != null) {
            updates.put("avatarUrl", imageUrl);
        }

        vendorRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showSuccess("Profile updated successfully! Your changes have been saved.");
                    
                    // Update current vendor object with new data
                    if (currentVendor != null) {
                        currentVendor.setName(etVendorName.getText().toString().trim());
                        currentVendor.setVanName(etBusinessName.getText().toString().trim());
                        currentVendor.setEmail(etEmail.getText().toString().trim());
                        currentVendor.setPhone(etPhone.getText().toString().trim());
                        currentVendor.setDescription(etDescription.getText().toString().trim());
                        currentVendor.setCategory(etCuisineType.getText().toString().trim());
                        currentVendor.setAddress(etAddress.getText().toString().trim());
                        
                        // Update avatar URL if image was uploaded
                        if (imageUrl != null) {
                            currentVendor.setAvatarUrl(imageUrl);
                        }
                    }
                    
                    // Clear selected image URI since it's now saved
                    selectedImageUri = null;
                    
                    resetSaveButton();
                    clearFieldErrors();
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update profile: " + e.getMessage());
                    resetSaveButton();
                });
    }

    private void resetSaveButton() {
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate vendor name
        if (TextUtils.isEmpty(etVendorName.getText())) {
            tilVendorName.setError("Vendor name is required");
            isValid = false;
        } else {
            tilVendorName.setError(null);
        }

        // Validate business name
        if (TextUtils.isEmpty(etBusinessName.getText())) {
            tilBusinessName.setError("Business name is required");
            isValid = false;
        } else {
            tilBusinessName.setError(null);
        }

        // Validate email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate phone
        if (TextUtils.isEmpty(etPhone.getText())) {
            tilPhone.setError("Phone number is required");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        return isValid;
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.green_500, null))
                    .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.red_500, null))
                    .show();
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        if (etVendorName != null) etVendorName.setEnabled(enabled);
        if (etBusinessName != null) etBusinessName.setEnabled(enabled);
        if (etEmail != null) etEmail.setEnabled(enabled);
        if (etPhone != null) etPhone.setEnabled(enabled);
        if (etDescription != null) etDescription.setEnabled(enabled);
        if (etCuisineType != null) etCuisineType.setEnabled(enabled);
        if (etAddress != null) etAddress.setEnabled(enabled);
        if (btnChangePhoto != null) btnChangePhoto.setEnabled(enabled);
    }

    private void createDefaultVendorData() {
        String userId = sessionManager.getUserId();
        String userEmail = firebaseAuth.getCurrentUser() != null ? 
                firebaseAuth.getCurrentUser().getEmail() : "";
        
        currentVendor = new Vendor(userId, "", userEmail, "");
        
        // Set some default values
        if (etEmail != null && userEmail != null) {
            etEmail.setText(userEmail);
        }
        
        showError("No profile data found. Please fill in your details.");
    }

    private void clearFieldErrors() {
        if (tilVendorName != null) tilVendorName.setError(null);
        if (tilBusinessName != null) tilBusinessName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilDescription != null) tilDescription.setError(null);
        if (tilCuisineType != null) tilCuisineType.setError(null);
        if (tilAddress != null) tilAddress.setError(null);
    }
}
