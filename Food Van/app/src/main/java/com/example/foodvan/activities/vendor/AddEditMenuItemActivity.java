package com.example.foodvan.activities.vendor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
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

import com.example.foodvan.R;
// import com.example.foodvan.models.MenuItem; // Commented to avoid conflict with android.view.MenuItem
import com.example.foodvan.utils.SessionManager;

import java.util.UUID;

public class AddEditMenuItemActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView ivItemPhoto;
    private MaterialButton btnCamera, btnGallery, btnSaveItem;
    private TextInputLayout tilItemName, tilItemDescription, tilItemPrice, tilItemCategory;
    private TextInputEditText etItemName, etItemDescription, etItemPrice;
    private MaterialAutoCompleteTextView etItemCategory;
    private SwitchMaterial switchItemAvailability;
    
    // Data
    private Uri selectedImageUri;
    private String menuItemId;
    private boolean isEditMode = false;
    private com.example.foodvan.models.MenuItem currentMenuItem;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference menuRef;
    private StorageReference storageRef;
    private String vendorId;
    
    // Utils
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;
    
    // Constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    
    // Categories
    private String[] categories = {
        "Snacks", "Beverages", "Main Course", "Desserts", "Appetizers",
        "Fast Food", "Street Food", "North Indian", "South Indian", 
        "Chinese", "Continental", "Italian", "Mexican"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Handle status bar properly with the 24dp spacer
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        setContentView(R.layout.activity_add_edit_menu_item);
        
        // Check if this is edit mode
        Intent intent = getIntent();
        if (intent != null) {
            menuItemId = intent.getStringExtra("menu_item_id");
            isEditMode = intent.getBooleanExtra("is_edit_mode", false);
        }
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupCategoryDropdown();
        setupClickListeners();
        
        if (isEditMode && menuItemId != null) {
            loadMenuItemData();
        }
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        vendorId = sessionManager.getUserId();
        
        if (vendorId != null) {
            menuRef = FirebaseDatabase.getInstance().getReference("vendors")
                    .child(vendorId).child("menuItems");
        }
        
        storageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivItemPhoto = findViewById(R.id.iv_item_photo);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnSaveItem = findViewById(R.id.btn_save_item);
        
        tilItemName = findViewById(R.id.til_item_name);
        tilItemDescription = findViewById(R.id.til_item_description);
        tilItemPrice = findViewById(R.id.til_item_price);
        tilItemCategory = findViewById(R.id.til_item_category);
        
        etItemName = findViewById(R.id.et_item_name);
        etItemDescription = findViewById(R.id.et_item_description);
        etItemPrice = findViewById(R.id.et_item_price);
        etItemCategory = findViewById(R.id.et_item_category);
        
        switchItemAvailability = findViewById(R.id.switch_item_availability);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            if (isEditMode) {
                getSupportActionBar().setTitle("Edit Menu Item");
            } else {
                getSupportActionBar().setTitle("Add Menu Item");
            }
        }
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, categories);
        etItemCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSaveItem.setOnClickListener(v -> saveMenuItem());
        
        ivItemPhoto.setOnClickListener(v -> openGallery());
    }

    private void loadMenuItemData() {
        if (menuRef == null || menuItemId == null) return;
        
        progressDialog.setMessage("Loading item details...");
        progressDialog.show();
        
        menuRef.child(menuItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                
                currentMenuItem = snapshot.getValue(com.example.foodvan.models.MenuItem.class);
                if (currentMenuItem != null) {
                    populateFields();
                } else {
                    showToast("Error loading menu item");
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                showToast("Error: " + error.getMessage());
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentMenuItem == null) return;
        
        etItemName.setText(currentMenuItem.getName());
        etItemDescription.setText(currentMenuItem.getDescription());
        etItemPrice.setText(String.valueOf(currentMenuItem.getPrice()));
        etItemCategory.setText(currentMenuItem.getCategory());
        switchItemAvailability.setChecked(currentMenuItem.isAvailable());
        
        // Load image if available
        if (currentMenuItem.getImageUrl() != null && !currentMenuItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentMenuItem.getImageUrl())
                    .placeholder(R.drawable.ic_add_photo_alternate)
                    .error(R.drawable.ic_add_photo_alternate)
                    .into(ivItemPhoto);
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    ivItemPhoto.setImageURI(selectedImageUri);
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                // Handle camera result - for simplicity, we'll use gallery approach
                // In a real app, you'd save the camera image and get its URI
                showToast("Camera functionality - Use gallery for now");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission required to take photos");
            }
        }
    }

    private void saveMenuItem() {
        if (!validateInputs()) {
            return;
        }
        
        progressDialog.setMessage("Saving menu item...");
        progressDialog.show();
        
        // If image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveItem();
        } else {
            // No new image, save item with existing image URL
            String existingImageUrl = (currentMenuItem != null) ? currentMenuItem.getImageUrl() : null;
            saveMenuItemToDatabase(existingImageUrl);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        tilItemName.setError(null);
        tilItemDescription.setError(null);
        tilItemPrice.setError(null);
        tilItemCategory.setError(null);
        
        // Validate name
        if (TextUtils.isEmpty(etItemName.getText())) {
            tilItemName.setError("Item name is required");
            isValid = false;
        }
        
        // Validate description
        if (TextUtils.isEmpty(etItemDescription.getText())) {
            tilItemDescription.setError("Description is required");
            isValid = false;
        }
        
        // Validate price
        if (TextUtils.isEmpty(etItemPrice.getText())) {
            tilItemPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(etItemPrice.getText().toString());
                if (price <= 0) {
                    tilItemPrice.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilItemPrice.setError("Please enter a valid price");
                isValid = false;
            }
        }
        
        // Validate category
        if (TextUtils.isEmpty(etItemCategory.getText())) {
            tilItemCategory.setError("Category is required");
            isValid = false;
        }
        
        return isValid;
    }

    private void uploadImageAndSaveItem() {
        String fileName = "menu_items/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);
        
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveMenuItemToDatabase(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast("Failed to upload image: " + e.getMessage());
                });
    }

    private void saveMenuItemToDatabase(String imageUrl) {
        String itemId = isEditMode ? menuItemId : menuRef.push().getKey();
        
        if (itemId == null) {
            progressDialog.dismiss();
            showToast("Error generating item ID");
            return;
        }
        
        // Create MenuItem object
        com.example.foodvan.models.MenuItem menuItem = new com.example.foodvan.models.MenuItem();
        menuItem.setId(itemId);
        menuItem.setVanId(vendorId);
        menuItem.setName(etItemName.getText().toString().trim());
        menuItem.setDescription(etItemDescription.getText().toString().trim());
        menuItem.setPrice(Double.parseDouble(etItemPrice.getText().toString()));
        menuItem.setCategory(etItemCategory.getText().toString().trim());
        menuItem.setAvailable(switchItemAvailability.isChecked());
        menuItem.setImageUrl(imageUrl);
        
        if (isEditMode && currentMenuItem != null) {
            // Preserve existing data
            menuItem.setOrderCount(currentMenuItem.getOrderCount());
            menuItem.setRating(currentMenuItem.getRating());
            menuItem.setTotalRatings(currentMenuItem.getTotalRatings());
            menuItem.setCreatedAt(currentMenuItem.getCreatedAt());
        } else {
            menuItem.setCreatedAt(System.currentTimeMillis());
        }
        
        menuItem.setLastUpdated(System.currentTimeMillis());
        
        // Save to Firebase
        menuRef.child(itemId).setValue(menuItem)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    String message = isEditMode ? "Menu item updated successfully" : "Menu item added successfully";
                    showToast(message);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast("Failed to save menu item: " + e.getMessage());
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
