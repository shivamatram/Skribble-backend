package com.example.foodvan.activities.customer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.activities.customer.AboutActivity;
import com.example.foodvan.models.User;
import com.example.foodvan.models.UserProfile;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.utils.FirebaseManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;

/**
 * ProfileActivity - Comprehensive user profile management
 * Features: Profile picture, personal info, addresses, payment methods, order history, settings
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_GALLERY = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 1003;
    private static final int REQUEST_STORAGE_PERMISSION = 1004;

    // UI Components
    private Toolbar toolbar;
    private ImageView ivProfilePicture;
    private FloatingActionButton fabEditPicture;
    private TextView tvUserName, tvUserEmail;
    private TextView tvTotalOrders, tvLoyaltyPoints, tvMembershipLevel;
    private TextView tvCompletionPercentage, tvAddressesCount, tvPaymentMethodsCount;
    private LinearProgressIndicator progressCompletion;
    
    // Cards
    private MaterialCardView cardPersonalInfo, cardAddresses, cardPaymentMethods;
    private MaterialCardView cardOrderHistory, cardFavorites, cardReviews;
    private MaterialCardView cardSettings, cardHelpSupport, cardAbout, cardLogout;

    // Data & Services
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadUserProfile();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        firebaseManager = new FirebaseManager();
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        
        // Profile header
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        fabEditPicture = findViewById(R.id.fab_edit_picture);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        
        // Stats
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvLoyaltyPoints = findViewById(R.id.tv_loyalty_points);
        tvMembershipLevel = findViewById(R.id.tv_membership_level);
        
        // Profile completion
        tvCompletionPercentage = findViewById(R.id.tv_completion_percentage);
        progressCompletion = findViewById(R.id.progress_completion);
        
        // Counts
        tvAddressesCount = findViewById(R.id.tv_addresses_count);
        tvPaymentMethodsCount = findViewById(R.id.tv_payment_methods_count);
        
        // Cards
        cardPersonalInfo = findViewById(R.id.card_personal_info);
        cardAddresses = findViewById(R.id.card_addresses);
        cardPaymentMethods = findViewById(R.id.card_payment_methods);
        // Comment out missing views for simple layout
        // cardOrderHistory = findViewById(R.id.card_order_history);
        // cardFavorites = findViewById(R.id.card_favorites);
        // cardReviews = findViewById(R.id.card_reviews);
        // cardSettings = findViewById(R.id.card_settings);
        // cardHelpSupport = findViewById(R.id.card_help_support);
        // cardAbout = findViewById(R.id.card_about);
        // cardLogout = findViewById(R.id.card_logout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Profile picture edit - add null check
        if (fabEditPicture != null) {
            fabEditPicture.setOnClickListener(v -> showImagePickerDialog());
        }
        
        // Account section - add null checks
        if (cardPersonalInfo != null) {
            cardPersonalInfo.setOnClickListener(v -> openPersonalInfoEditor());
        }
        if (cardAddresses != null) {
            cardAddresses.setOnClickListener(v -> openAddressManager());
        }
        if (cardPaymentMethods != null) {
            cardPaymentMethods.setOnClickListener(v -> openPaymentMethodsManager());
        }
        
        // Food section - commented out since views don't exist in simple layout
        // if (cardOrderHistory != null) {
        //     cardOrderHistory.setOnClickListener(v -> openOrderHistory());
        // }
        // if (cardFavorites != null) {
        //     cardFavorites.setOnClickListener(v -> openFavorites());
        // }
        // if (cardReviews != null) {
        //     cardReviews.setOnClickListener(v -> openReviewsAndRatings());
        // }
        
        // More section - commented out since views don't exist in simple layout
        // if (cardSettings != null) {
        //     cardSettings.setOnClickListener(v -> openSettings());
        // }
        // if (cardHelpSupport != null) {
        //     cardHelpSupport.setOnClickListener(v -> openHelpAndSupport());
        // }
        // if (cardAbout != null) {
        //     cardAbout.setOnClickListener(v -> openAbout());
        // }
        // if (cardLogout != null) {
        //     cardLogout.setOnClickListener(v -> showLogoutDialog());
        // }
    }

    private void loadUserProfile() {
        String userId = sessionManager.getUserId();
        if (userId != null) {
            // Note: Using User instead of UserProfile for now since FirebaseManager doesn't support UserProfile yet
            // TODO: Add UserProfile support to FirebaseManager
            userProfile = createDefaultProfile();
            updateUI();
        } else {
            userProfile = createDefaultProfile();
            updateUI();
        }
    }

    private UserProfile createDefaultProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserId(sessionManager.getUserId());
        profile.setFullName(sessionManager.getUserName());
        profile.setEmail(sessionManager.getUserEmail());
        // Get phone from user details since getUserPhone() doesn't exist
        User userDetails = sessionManager.getUserDetails();
        if (userDetails != null) {
            profile.setPhoneNumber(userDetails.getPhone());
        }
        return profile;
    }

    private void updateUI() {
        if (userProfile == null) return;

        // Update header info
        tvUserName.setText(userProfile.getDisplayName());
        tvUserEmail.setText(userProfile.getEmail());
        
        // Update stats
        tvTotalOrders.setText(String.valueOf(userProfile.getTotalOrders()));
        tvLoyaltyPoints.setText(String.format("%,d", userProfile.getLoyaltyPoints()));
        tvMembershipLevel.setText(userProfile.getMembershipLevel());
        
        // Update profile completion
        int completionPercentage = userProfile.getProfileCompletionPercentage();
        tvCompletionPercentage.setText(completionPercentage + "%");
        progressCompletion.setProgress(completionPercentage);
        
        // Update counts
        int addressCount = userProfile.getSavedAddresses() != null ? userProfile.getSavedAddresses().size() : 0;
        tvAddressesCount.setText(addressCount + " saved addresses");
        
        // For payment methods, we'll show a default count for now
        tvPaymentMethodsCount.setText("2 payment methods saved");
        
        // Load profile picture if available
        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            // TODO: Load image using Glide or Picasso
            // Glide.with(this).load(userProfile.getProfileImageUrl()).into(ivProfilePicture);
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setItems(new String[]{"Take Photo", "Choose from Gallery", "Remove Photo"}, 
            (dialog, which) -> {
                switch (which) {
                    case 0:
                        checkCameraPermissionAndTakePhoto();
                        break;
                    case 1:
                        checkStoragePermissionAndOpenGallery();
                        break;
                    case 2:
                        removeProfilePicture();
                        break;
                }
            });
        builder.show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void removeProfilePicture() {
        ivProfilePicture.setImageResource(R.drawable.ic_person_placeholder);
        if (userProfile != null) {
            userProfile.setProfileImageUrl(null);
            saveUserProfile();
        }
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        if (imageBitmap != null) {
                            ivProfilePicture.setImageBitmap(imageBitmap);
                            uploadProfilePicture(imageBitmap);
                        }
                    }
                    break;
                    
                case REQUEST_IMAGE_GALLERY:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            ivProfilePicture.setImageBitmap(bitmap);
                            uploadProfilePicture(bitmap);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image from gallery", e);
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }

    private void uploadProfilePicture(Bitmap bitmap) {
        // TODO: Implement image upload to Firebase Storage
        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
        
        // For now, just save the profile
        if (userProfile != null) {
            // userProfile.setProfileImageUrl(downloadUrl); // Set after upload
            saveUserProfile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
                break;
                
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void openPersonalInfoEditor() {
        Intent intent = new Intent(this, EditPersonalInfoActivity.class);
        startActivityForResult(intent, 1001);
    }

    private void openAddressManager() {
        Intent intent = new Intent(this, SavedAddressesActivity.class);
        startActivity(intent);
    }

    private void openPaymentMethodsManager() {
        Intent intent = new Intent(this, PaymentMethodsActivity.class);
        startActivity(intent);
    }

    private void openOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
    }

    private void openFavorites() {
        Intent intent = new Intent(this, FavoriteOrdersActivity.class);
        startActivity(intent);
    }

    private void openReviewsAndRatings() {
        Intent intent = new Intent(this, ReviewsRatingsActivity.class);
        intent.putExtra("vendor_id", "all_vendors"); // For viewing all reviews
        intent.putExtra("vendor_name", "All Reviews");
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openHelpAndSupport() {
        // Help & Support feature removed
        Toast.makeText(this, "Help & Support - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        // Add smooth transition animation
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> performLogout());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performLogout() {
        // Clear session
        sessionManager.logout();
        
        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveUserProfile() {
        if (userProfile != null && userProfile.getUserId() != null) {
            // Note: Using User instead of UserProfile for now since FirebaseManager doesn't support UserProfile yet
            // TODO: Add UserProfile support to FirebaseManager
            Log.d(TAG, "User profile save - TODO: implement UserProfile support in FirebaseManager");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile data when returning to activity
        loadUserProfile();
    }
}
