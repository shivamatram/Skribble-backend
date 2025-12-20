package com.example.foodvan.fragments.vendor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Edit Profile Fragment with Material Design 3
 * Allows vendors to edit their profile information and photo
 */
public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Components
    private ShapeableImageView profileImage;
    private MaterialButton btnChangePhoto, btnRemovePhoto, btnSaveChanges;
    private TextInputLayout tilVendorName, tilBusinessName, tilEmail, tilPhone;
    private TextInputLayout tilDescription, tilAddress, tilCuisineType;
    private TextInputEditText etVendorName, etBusinessName, etEmail, etPhone;
    private TextInputEditText etDescription, etAddress;
    private MaterialAutoCompleteTextView etCuisineType;

    // Data & Services
    private SessionManager sessionManager;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef;
    private StorageReference storageRef;
    private Vendor currentVendor;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    // Cuisine types for dropdown
    private final String[] cuisineTypes = {
        "North Indian", "South Indian", "Chinese", "Italian", "Mexican", 
        "Continental", "Fast Food", "Street Food", "Beverages", "Desserts",
        "Punjabi", "Gujarati", "Bengali", "Maharashtrian", "Rajasthani",
        "Biryani", "Pizza", "Burger", "Sandwich", "Momos", "Other"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        initializeViews(view);
        setupClickListeners();
        loadVendorData();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        String vendorId = sessionManager.getUserId();
        Log.d("EditProfileFragment", "Retrieved vendorId: " + vendorId);
        
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
            Log.d("EditProfileFragment", "vendorRef initialized successfully");
        } else {
            Log.e("EditProfileFragment", "vendorId is null! Cannot initialize vendorRef");
        }
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_photo);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);

        // Text Input Layouts
        tilVendorName = view.findViewById(R.id.til_vendor_name);
        tilBusinessName = view.findViewById(R.id.til_business_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilPhone = view.findViewById(R.id.til_phone);
        tilDescription = view.findViewById(R.id.til_description);
        tilAddress = view.findViewById(R.id.til_address);
        tilCuisineType = view.findViewById(R.id.til_cuisine_type);

        // Edit Texts
        etVendorName = view.findViewById(R.id.et_vendor_name);
        etBusinessName = view.findViewById(R.id.et_business_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etDescription = view.findViewById(R.id.et_description);
        etAddress = view.findViewById(R.id.et_address);
        etCuisineType = view.findViewById(R.id.et_cuisine_type);
        
        // Setup cuisine type dropdown
        setupCuisineTypeDropdown();
    }

    private void setupClickListeners() {
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        btnRemovePhoto.setOnClickListener(v -> removeProfilePhoto());
        
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> {
                Log.d("EditProfileFragment", "Save button clicked!");
                // Add visual feedback to confirm button works
                btnSaveChanges.setText("Saving...");
                btnSaveChanges.setEnabled(false);
                saveProfile();
            });
        } else {
            Log.e("EditProfileFragment", "Save button is null!");
            showError("Save button not found! Please restart the app.");
        }
    }

    private void setupCuisineTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                android.R.layout.simple_dropdown_item_1line, cuisineTypes);
        etCuisineType.setAdapter(adapter);
    }

    private void loadVendorData() {
        if (vendorRef == null) return;

        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentVendor = snapshot.getValue(Vendor.class);
                if (currentVendor != null) {
                    populateFields();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load vendor data: " + error.getMessage());
            }
        });
    }

    private void populateFields() {
        if (currentVendor == null) return;

        etVendorName.setText(currentVendor.getName());
        etBusinessName.setText(currentVendor.getVanName());
        etEmail.setText(currentVendor.getEmail());
        etPhone.setText(currentVendor.getPhone());
        etDescription.setText(currentVendor.getDescription());
        etAddress.setText(currentVendor.getAddress());
        
        // Set cuisine type if available
        if (currentVendor.getCategory() != null) {
            etCuisineType.setText(currentVendor.getCategory(), false);
        }

        // Load profile image if available
        // TODO: Implement image loading with Glide or Picasso
        if (currentVendor.getAvatarUrl() != null && !currentVendor.getAvatarUrl().isEmpty()) {
            // Load image from URL using Glide (when implemented)
            // For now, just show placeholder
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    private void removeProfilePhoto() {
        profileImage.setImageResource(R.drawable.ic_person);
        selectedImageUri = null;
        showSuccess("Profile photo removed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
            showSuccess("Profile photo updated");
        }
    }

    private void saveProfile() {
        Log.d("EditProfileFragment", "saveProfile() called");
        
        // Check if vendorRef is null and try to reinitialize
        if (vendorRef == null) {
            Log.e("EditProfileFragment", "vendorRef is null! Attempting to reinitialize...");
            String vendorId = sessionManager.getUserId();
            if (vendorId != null) {
                vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
                Log.d("EditProfileFragment", "vendorRef reinitialized successfully");
            } else {
                Log.e("EditProfileFragment", "Still no vendorId available. Cannot save profile.");
                showError("Unable to save profile - user not authenticated. Please login again.");
                return;
            }
        }
        
        Log.d("EditProfileFragment", "Validating inputs...");
        if (!validateInputs()) {
            Log.d("EditProfileFragment", "Validation failed");
            return;
        }
        
        Log.d("EditProfileFragment", "Validation passed, showing progress dialog");
        progressDialog.setMessage("Saving profile changes...");
        progressDialog.show();

        // If image is selected, upload it first, then save profile data
        if (selectedImageUri != null) {
            Log.d("EditProfileFragment", "Image selected, uploading image first");
            uploadImageAndSaveProfile();
        } else {
            Log.d("EditProfileFragment", "No image selected, saving profile data directly");
            // No new image, just save profile data
            saveProfileData(null);
        }
    }

    private void uploadImageAndSaveProfile() {
        String fileName = "profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveProfileData(imageUrl);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        showError("Failed to get image URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Failed to upload image: " + e.getMessage());
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading image... " + (int) progress + "%");
                });
    }

    private void saveProfileData(String imageUrl) {
        Log.d("EditProfileFragment", "saveProfileData() called with imageUrl: " + imageUrl);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etVendorName.getText().toString().trim());
        updates.put("vanName", etBusinessName.getText().toString().trim());
        updates.put("email", etEmail.getText().toString().trim());
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("description", etDescription.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("category", etCuisineType.getText().toString().trim());
        updates.put("lastUpdated", System.currentTimeMillis());

        // Add image URL if uploaded
        if (imageUrl != null) {
            updates.put("avatarUrl", imageUrl);
        }

        Log.d("EditProfileFragment", "Updating Firebase with data: " + updates.toString());
        vendorRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EditProfileFragment", "Firebase update successful!");
                    progressDialog.dismiss();
                    
                    // Reset button state
                    resetSaveButton();
                    
                    showSuccess("Profile updated successfully!");
                    
                    // Update current vendor object
                    if (currentVendor != null) {
                        currentVendor.setName(etVendorName.getText().toString().trim());
                        currentVendor.setVanName(etBusinessName.getText().toString().trim());
                        currentVendor.setEmail(etEmail.getText().toString().trim());
                        currentVendor.setPhone(etPhone.getText().toString().trim());
                        currentVendor.setDescription(etDescription.getText().toString().trim());
                        currentVendor.setAddress(etAddress.getText().toString().trim());
                        currentVendor.setCategory(etCuisineType.getText().toString().trim());
                        if (imageUrl != null) {
                            currentVendor.setAvatarUrl(imageUrl);
                        }
                    }
                    
                    // Clear selected image
                    selectedImageUri = null;
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfileFragment", "Firebase update failed: " + e.getMessage());
                    progressDialog.dismiss();
                    
                    // Reset button state
                    resetSaveButton();
                    
                    showError("Failed to update profile: " + e.getMessage());
                });
    }

    private void resetSaveButton() {
        if (btnSaveChanges != null) {
            btnSaveChanges.setText("Save Changes");
            btnSaveChanges.setEnabled(true);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        tilVendorName.setError(null);
        tilBusinessName.setError(null);
        tilEmail.setError(null);

        // Validate vendor name
        if (TextUtils.isEmpty(etVendorName.getText())) {
            tilVendorName.setError("Vendor name is required");
            isValid = false;
        }

        // Validate business name
        if (TextUtils.isEmpty(etBusinessName.getText())) {
            tilBusinessName.setError("Business name is required");
            isValid = false;
        }

        // Validate email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        return isValid;
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.success_color))
                .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(requireContext().getColor(R.color.error_color))
                .show();
        }
    }
}
