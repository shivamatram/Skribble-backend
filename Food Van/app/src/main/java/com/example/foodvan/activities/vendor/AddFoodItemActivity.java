package com.example.foodvan.activities.vendor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.MenuItem;
import com.example.foodvan.viewmodels.AddFoodItemViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddFoodItemActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private static final int STORAGE_PERMISSION_REQUEST = 1002;

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView foodImagePreview;
    private MaterialButton btnCamera, btnGallery, btnAddItem;
    private TextInputLayout foodNameLayout, foodPriceLayout, foodCategoryLayout, foodDescriptionLayout;
    private TextInputEditText foodNameEditText, foodPriceEditText, foodDescriptionEditText;
    private AutoCompleteTextView foodCategoryDropdown;
    private SwitchMaterial availabilitySwitch;

    // ViewModel
    private AddFoodItemViewModel viewModel;

    // Image handling
    private Uri currentPhotoUri;
    private String currentPhotoPath;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    // Food Categories
    private final String[] foodCategories = {
            "Chinese", "Indian", "Fast Food", "Drinks", "Desserts", 
            "South Indian", "North Indian", "Continental", "Italian", 
            "Mexican", "Thai", "Snacks", "Beverages", "Ice Cream"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar - BEFORE setContentView
        setupStatusBar();
        
        setContentView(R.layout.activity_add_food_item);

        initializeViewModel();
        initializeViews();
        setupToolbar();
        setupCategoryDropdown();
        setupClickListeners();
        setupActivityResultLaunchers();
        observeViewModel();
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(AddFoodItemViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        foodImagePreview = findViewById(R.id.food_image_preview);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnAddItem = findViewById(R.id.btn_add_item);
        
        foodNameLayout = findViewById(R.id.food_name_layout);
        foodPriceLayout = findViewById(R.id.food_price_layout);
        foodCategoryLayout = findViewById(R.id.food_category_layout);
        foodDescriptionLayout = findViewById(R.id.food_description_layout);
        
        foodNameEditText = findViewById(R.id.food_name_edit_text);
        foodPriceEditText = findViewById(R.id.food_price_edit_text);
        foodDescriptionEditText = findViewById(R.id.food_description_edit_text);
        foodCategoryDropdown = findViewById(R.id.food_category_dropdown);
        
        availabilitySwitch = findViewById(R.id.availability_switch);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, foodCategories);
        foodCategoryDropdown.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnAddItem.setOnClickListener(v -> validateAndAddFoodItem());
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadImageFromUri(currentPhotoUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            loadImageFromUri(selectedImageUri);
                            currentPhotoUri = selectedImageUri;
                        }
                    }
                });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnAddItem.setEnabled(!isLoading);
            btnAddItem.setText(isLoading ? "Adding..." : "Add Food Item");
        });

        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null) {
                showSuccessSnackbar(message);
                // Navigate back to dashboard after 2 seconds
                btnAddItem.postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 2000);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showErrorSnackbar(error);
            }
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                showErrorSnackbar("Error creating image file");
                return;
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.foodvan.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(cameraIntent);
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FOOD_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadImageFromUri(Uri uri) {
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.ic_add_photo_placeholder)
                .error(R.drawable.ic_add_photo_placeholder)
                .into(foodImagePreview);
    }

    private void validateAndAddFoodItem() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String name = foodNameEditText.getText() != null ? foodNameEditText.getText().toString().trim() : "";
        String priceStr = foodPriceEditText.getText() != null ? foodPriceEditText.getText().toString().trim() : "";
        String category = foodCategoryDropdown.getText().toString().trim();
        String description = foodDescriptionEditText.getText() != null ? foodDescriptionEditText.getText().toString().trim() : "";
        boolean isAvailable = availabilitySwitch.isChecked();

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            foodNameLayout.setError("Food name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(priceStr)) {
            foodPriceLayout.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    foodPriceLayout.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                foodPriceLayout.setError("Please enter a valid price");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(category)) {
            foodCategoryLayout.setError("Please select a category");
            isValid = false;
        }

        if (TextUtils.isEmpty(description)) {
            foodDescriptionLayout.setError("Description is required");
            isValid = false;
        }

        if (currentPhotoUri == null) {
            showErrorSnackbar("Please add a food image");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create MenuItem object
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setPrice(Double.parseDouble(priceStr));
        menuItem.setCategory(category);
        menuItem.setDescription(description);
        menuItem.setAvailable(isAvailable);
        menuItem.setImageUri(currentPhotoUri.toString());

        // Add menu item via ViewModel
        viewModel.addMenuItem(menuItem);
    }

    private void clearErrors() {
        foodNameLayout.setError(null);
        foodPriceLayout.setError(null);
        foodCategoryLayout.setError(null);
        foodDescriptionLayout.setError(null);
    }

    private void showSuccessSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show();
    }

    private void showErrorSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    showErrorSnackbar("Camera permission is required to take photos");
                }
                break;
            case STORAGE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    showErrorSnackbar("Storage permission is required to select photos");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure orange status bar is applied when activity resumes
        setupStatusBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Setup orange status bar to match the navbar theme (same as Order History)
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            
            // Clear any existing flags
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            
            // Enable drawing system bar backgrounds
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // Set orange status bar color - using exact color value
            int orangeColor = 0xFFFF6B35; // #FF6B35
            window.setStatusBarColor(orangeColor);
            
            // Ensure white status bar icons on orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                // Remove light status bar flag to get white icons
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(flags);
            }
        }
    }
}
