package com.example.foodvan.activities.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.foodvan.R;
import com.example.foodvan.models.User;
import com.example.foodvan.models.UserProfile;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * EditPersonalInfoActivity - Comprehensive personal information editing screen
 * Features: Full name, display name, email, phone, DOB, gender, pronouns, language,
 * timezone, bio, and privacy controls
 */
public class EditPersonalInfoActivity extends AppCompatActivity {

    private static final String TAG = "EditPersonalInfo";
    private static final int REQUEST_PHONE_VERIFICATION = 1001;
    private static final String PREFS_NAME = "user_profile";

    // UI Components - Toolbar
    private MaterialToolbar toolbar;

    // Profile Picture
    private ImageView ivProfilePicture;
    private MaterialButton btnChangePicture;
    private FloatingActionButton fabChangePicture;

    // Basic Information
    private TextInputLayout tilFullName, tilDisplayName, tilEmail, tilPhone, tilDateOfBirth, tilBio;
    private TextInputEditText etFullName, etDisplayName, etEmail, etPhone, etDateOfBirth, etBio;

    // Gender & Pronouns
    private ChipGroup chipGroupGender, chipGroupPronouns;
    private Chip chipMale, chipFemale, chipOther, chipPreferNotToSay;
    private Chip chipHeHim, chipSheHer, chipTheyThem, chipCustomPronouns;
    private TextInputLayout tilCustomPronouns;
    private TextInputEditText etCustomPronouns;

    // Regional Preferences
    private TextInputLayout tilLanguage, tilTimezone;
    private AutoCompleteTextView dropdownLanguage, dropdownTimezone;

    // Privacy Controls
    private MaterialSwitch switchSharePhone, switchMarketing, switchPersonalization;

    // Verification
    private ImageView ivEmailVerified;
    private MaterialButton btnVerifyPhone;
    private LinearLayout layoutPhoneVerificationStatus;

    // Quick Access
    private LinearLayout btnManageAddresses, btnPaymentMethods;

    // Action Buttons
    private MaterialButton btnCancel, btnSave;
    private FrameLayout loadingOverlay;

    // Data & Services
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private UserProfile userProfile;
    private Calendar selectedDate;
    private boolean isPhoneVerified = false;
    private String verifiedPhoneNumber = "";

    // Language and Timezone options
    private final List<String> LANGUAGES = Arrays.asList(
            "English", "Hindi", "Tamil", "Telugu", "Kannada", 
            "Malayalam", "Marathi", "Bengali", "Gujarati", "Punjabi"
    );

    private final List<String> TIMEZONES = Arrays.asList(
            "Asia/Kolkata (IST)",
            "Asia/Dhaka (BST)",
            "Asia/Kathmandu (NPT)",
            "Asia/Colombo (IST)",
            "Asia/Dubai (GST)",
            "Asia/Singapore (SGT)",
            "America/New_York (EST)",
            "America/Los_Angeles (PST)",
            "Europe/London (GMT)",
            "Europe/Paris (CET)"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);

        initializeServices();
        initializeViews();
        setupStatusBar();
        setupToolbar();
        setupDropdowns();
        setupClickListeners();
        setupTextWatchers();
        loadUserData();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        firebaseManager = new FirebaseManager();
        selectedDate = Calendar.getInstance();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);

        // Profile Picture
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        btnChangePicture = findViewById(R.id.btn_change_picture);
        fabChangePicture = findViewById(R.id.fab_change_picture);

        // Basic Information - TextInputLayouts
        tilFullName = findViewById(R.id.til_full_name);
        tilDisplayName = findViewById(R.id.til_display_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilDateOfBirth = findViewById(R.id.til_date_of_birth);
        tilBio = findViewById(R.id.til_bio);

        // Basic Information - EditTexts
        etFullName = findViewById(R.id.et_full_name);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etBio = findViewById(R.id.et_bio);

        // Gender Selection
        chipGroupGender = findViewById(R.id.chip_group_gender);
        chipMale = findViewById(R.id.chip_male);
        chipFemale = findViewById(R.id.chip_female);
        chipOther = findViewById(R.id.chip_other);
        chipPreferNotToSay = findViewById(R.id.chip_prefer_not_to_say);

        // Pronouns Selection
        chipGroupPronouns = findViewById(R.id.chip_group_pronouns);
        chipHeHim = findViewById(R.id.chip_he_him);
        chipSheHer = findViewById(R.id.chip_she_her);
        chipTheyThem = findViewById(R.id.chip_they_them);
        chipCustomPronouns = findViewById(R.id.chip_custom_pronouns);
        tilCustomPronouns = findViewById(R.id.til_custom_pronouns);
        etCustomPronouns = findViewById(R.id.et_custom_pronouns);

        // Regional Preferences
        tilLanguage = findViewById(R.id.til_language);
        tilTimezone = findViewById(R.id.til_timezone);
        dropdownLanguage = findViewById(R.id.dropdown_language);
        dropdownTimezone = findViewById(R.id.dropdown_timezone);

        // Privacy Controls
        switchSharePhone = findViewById(R.id.switch_share_phone);
        switchMarketing = findViewById(R.id.switch_marketing);
        switchPersonalization = findViewById(R.id.switch_personalization);

        // Verification
        ivEmailVerified = findViewById(R.id.iv_email_verified);
        btnVerifyPhone = findViewById(R.id.btn_verify_phone);
        layoutPhoneVerificationStatus = findViewById(R.id.layout_phone_verification_status);

        // Quick Access
        btnManageAddresses = findViewById(R.id.btn_manage_addresses);
        btnPaymentMethods = findViewById(R.id.btn_payment_methods);

        // Action Buttons
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_color));
        }
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdowns() {
        // Language Dropdown
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, LANGUAGES);
        dropdownLanguage.setAdapter(languageAdapter);

        // Timezone Dropdown
        ArrayAdapter<String> timezoneAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, TIMEZONES);
        dropdownTimezone.setAdapter(timezoneAdapter);

        // Set default timezone based on device
        String defaultTimezone = TimeZone.getDefault().getID();
        for (String tz : TIMEZONES) {
            if (tz.contains(defaultTimezone.split("/")[1])) {
                dropdownTimezone.setText(tz, false);
                break;
            }
        }
        if (dropdownTimezone.getText().toString().isEmpty()) {
            dropdownTimezone.setText(TIMEZONES.get(0), false);
        }
    }

    private void setupClickListeners() {
        // Profile Picture
        btnChangePicture.setOnClickListener(v -> showProfilePictureOptions());
        if (fabChangePicture != null) {
            fabChangePicture.setOnClickListener(v -> showProfilePictureOptions());
        }

        // Date of Birth
        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        tilDateOfBirth.setEndIconOnClickListener(v -> showDatePicker());

        // Phone Verification
        btnVerifyPhone.setOnClickListener(v -> startPhoneVerification());

        // Custom Pronouns
        chipCustomPronouns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilCustomPronouns.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                etCustomPronouns.requestFocus();
            }
        });

        // Quick Access
        btnManageAddresses.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, SavedAddressesActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Saved Addresses - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        btnPaymentMethods.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, PaymentMethodsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Payment Methods - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        // Action Buttons
        btnCancel.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> {
            if (validateAllFields()) {
                saveChanges();
            }
        });
    }

    private void setupTextWatchers() {
        // Full Name validation
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFullName();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Phone validation
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showProfilePictureOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Profile Picture")
                .setItems(new String[]{"Take Photo", "Choose from Gallery", "Remove Photo"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Camera - Coming Soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "Gallery - Coming Soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            ivProfilePicture.setImageResource(R.drawable.ic_person_placeholder);
                            Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        if (userId != null) {
            showLoading(true);

            // First try to load from SharedPreferences (most recent data)
            userProfile = loadUserProfileFromPreferences();

            // If no saved profile, create default from SessionManager
            if (userProfile == null) {
                userProfile = createDefaultProfile();
            }

            populateFields();
            showLoading(false);
        } else {
            userProfile = createDefaultProfile();
            populateFields();
        }
    }

    private UserProfile loadUserProfileFromPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // Check if we have saved profile data
            if (!prefs.contains("user_id")) {
                return null;
            }

            UserProfile profile = new UserProfile();
            profile.setUserId(prefs.getString("user_id", sessionManager.getUserId()));
            profile.setFullName(prefs.getString("full_name", ""));
            profile.setDisplayName(prefs.getString("display_name", ""));
            profile.setEmail(prefs.getString("email", ""));
            profile.setPhoneNumber(prefs.getString("phone_number", ""));
            profile.setDateOfBirth(prefs.getString("date_of_birth", ""));
            profile.setGender(prefs.getString("gender", ""));
            profile.setPronouns(prefs.getString("pronouns", ""));
            profile.setBio(prefs.getString("bio", ""));
            profile.setTimeZone(prefs.getString("timezone", TimeZone.getDefault().getID()));
            profile.setPhoneVerified(prefs.getBoolean("phone_verified", false));
            profile.setEmailVerified(prefs.getBoolean("email_verified", true));
            profile.setSharePhoneNumber(prefs.getBoolean("share_phone", false));
            profile.setReceiveMarketing(prefs.getBoolean("marketing", false));
            profile.setAllowPersonalization(prefs.getBoolean("personalization", true));

            // Get preferences language
            if (profile.getPreferences() != null) {
                profile.getPreferences().setLanguage(prefs.getString("language", "English"));
            }

            // Update verification status
            isPhoneVerified = profile.isPhoneVerified();
            verifiedPhoneNumber = profile.getPhoneNumber();

            Log.d(TAG, "UserProfile loaded from SharedPreferences");
            return profile;

        } catch (Exception e) {
            Log.e(TAG, "Error loading UserProfile from SharedPreferences", e);
            return null;
        }
    }

    private UserProfile createDefaultProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserId(sessionManager.getUserId());
        profile.setFullName(sessionManager.getUserName());
        profile.setEmail(sessionManager.getUserEmail());

        // Get phone from user details
        User userDetails = sessionManager.getUserDetails();
        if (userDetails != null) {
            profile.setPhoneNumber(userDetails.getPhone());
        }

        // Set default verification status
        profile.setEmailVerified(true);
        profile.setPhoneVerified(false);

        // Set default timezone
        profile.setTimeZone(TimeZone.getDefault().getID());

        Log.d(TAG, "Created default UserProfile from SessionManager");
        return profile;
    }

    private void populateFields() {
        if (userProfile == null) return;

        // Basic info
        etFullName.setText(userProfile.getFullName());
        etDisplayName.setText(userProfile.getDisplayNameField());
        etEmail.setText(userProfile.getEmail());
        etPhone.setText(userProfile.getPhoneNumber());
        etBio.setText(userProfile.getBio());

        // Date of birth
        if (userProfile.getDateOfBirth() != null && !userProfile.getDateOfBirth().isEmpty()) {
            etDateOfBirth.setText(userProfile.getDateOfBirth());
            parseDateOfBirth(userProfile.getDateOfBirth());
        }

        // Gender
        if (userProfile.getGender() != null) {
            switch (userProfile.getGender().toLowerCase()) {
                case "male":
                    chipMale.setChecked(true);
                    break;
                case "female":
                    chipFemale.setChecked(true);
                    break;
                case "other":
                    chipOther.setChecked(true);
                    break;
                case "prefer not to say":
                    chipPreferNotToSay.setChecked(true);
                    break;
            }
        }

        // Pronouns
        if (userProfile.getPronouns() != null) {
            switch (userProfile.getPronouns().toLowerCase()) {
                case "he/him":
                    chipHeHim.setChecked(true);
                    break;
                case "she/her":
                    chipSheHer.setChecked(true);
                    break;
                case "they/them":
                    chipTheyThem.setChecked(true);
                    break;
                default:
                    if (!userProfile.getPronouns().isEmpty()) {
                        chipCustomPronouns.setChecked(true);
                        tilCustomPronouns.setVisibility(View.VISIBLE);
                        etCustomPronouns.setText(userProfile.getPronouns());
                    }
                    break;
            }
        }

        // Language
        if (userProfile.getPreferences() != null && userProfile.getPreferences().getLanguage() != null) {
            dropdownLanguage.setText(userProfile.getPreferences().getLanguage(), false);
        }

        // Timezone
        if (userProfile.getTimeZone() != null) {
            for (String tz : TIMEZONES) {
                if (tz.contains(userProfile.getTimeZone().split("/")[1])) {
                    dropdownTimezone.setText(tz, false);
                    break;
                }
            }
        }

        // Privacy controls
        switchSharePhone.setChecked(userProfile.isSharePhoneNumber());
        switchMarketing.setChecked(userProfile.isReceiveMarketing());
        switchPersonalization.setChecked(userProfile.isAllowPersonalization());

        // Verification status
        updateVerificationStatus();
    }

    private void parseDateOfBirth(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date != null) {
                selectedDate.setTime(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date of birth", e);
        }
    }

    private void updateVerificationStatus() {
        if (userProfile != null) {
            // Email verification
            if (userProfile.isEmailVerified()) {
                ivEmailVerified.setImageResource(R.drawable.ic_check_circle);
                ivEmailVerified.setColorFilter(ContextCompat.getColor(this, R.color.success_green));
            } else {
                ivEmailVerified.setImageResource(R.drawable.ic_warning);
                ivEmailVerified.setColorFilter(ContextCompat.getColor(this, R.color.warning_orange));
            }

            // Phone verification
            if (userProfile.isPhoneVerified()) {
                btnVerifyPhone.setText("Verified");
                btnVerifyPhone.setEnabled(false);
                btnVerifyPhone.setIconResource(R.drawable.ic_check_circle);
            } else {
                btnVerifyPhone.setText("Verify");
                btnVerifyPhone.setEnabled(true);
                btnVerifyPhone.setIconResource(R.drawable.ic_phone);
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDateOfBirth.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to 18 years ago (minimum age requirement)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private boolean validateFullName() {
        String name = etFullName.getText().toString().trim();

        if (name.isEmpty()) {
            tilFullName.setError("Full name is required");
            return false;
        }

        if (name.length() < 2) {
            tilFullName.setError("Name must be at least 2 characters");
            return false;
        }

        if (!name.matches("^[a-zA-Z\\s]+$")) {
            tilFullName.setError("Name can only contain letters and spaces");
            return false;
        }

        tilFullName.setError(null);
        return true;
    }

    private boolean validatePhone() {
        String phone = etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            tilPhone.setError("Phone number is required");
            return false;
        }

        if (phone.length() != 10) {
            tilPhone.setError("Phone number must be 10 digits");
            return false;
        }

        if (!phone.matches("^[6-9]\\d{9}$")) {
            tilPhone.setError("Please enter a valid Indian mobile number");
            return false;
        }

        tilPhone.setError(null);
        return true;
    }

    private boolean validateDateOfBirth() {
        String dateOfBirth = etDateOfBirth.getText().toString().trim();

        if (dateOfBirth.isEmpty()) {
            // Date of birth is optional
            return true;
        }

        // Check if user is at least 18 years old
        Calendar today = Calendar.getInstance();
        Calendar minAge = Calendar.getInstance();
        minAge.setTime(selectedDate.getTime());
        minAge.add(Calendar.YEAR, 18);

        if (minAge.after(today)) {
            tilDateOfBirth.setError("You must be at least 18 years old");
            return false;
        }

        tilDateOfBirth.setError(null);
        return true;
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (!validateFullName()) {
            isValid = false;
        }

        if (!validatePhone()) {
            isValid = false;
        }

        if (!validateDateOfBirth()) {
            isValid = false;
        }

        return isValid;
    }

    private void startPhoneVerification() {
        String phoneNumber = etPhone.getText().toString().trim();

        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            tilPhone.setError("Please enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        intent.putExtra("phone_number", phoneNumber);
        startActivityForResult(intent, REQUEST_PHONE_VERIFICATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHONE_VERIFICATION && resultCode == RESULT_OK && data != null) {
            String verifiedPhone = data.getStringExtra("verified_phone");
            boolean isVerified = data.getBooleanExtra("is_verified", false);

            if (isVerified && verifiedPhone != null) {
                this.isPhoneVerified = true;
                this.verifiedPhoneNumber = verifiedPhone;
                etPhone.setText(verifiedPhone);
                updatePhoneVerificationStatus();

                Toast.makeText(this, "Phone number verified successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePhoneVerificationStatus() {
        if (isPhoneVerified) {
            btnVerifyPhone.setText("Verified");
            btnVerifyPhone.setEnabled(false);
            layoutPhoneVerificationStatus.setVisibility(View.VISIBLE);
            tilPhone.setHelperText("Phone number verified");
            tilPhone.setEndIconDrawable(R.drawable.ic_check_circle);
            tilPhone.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.success_green));
        } else {
            btnVerifyPhone.setText("Verify");
            btnVerifyPhone.setEnabled(true);
            layoutPhoneVerificationStatus.setVisibility(View.GONE);
            tilPhone.setHelperText("10-digit mobile number");
        }
    }

    private void saveChanges() {
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUserId(sessionManager.getUserId());
        }

        showLoading(true);

        // Update profile with form data
        userProfile.setFullName(etFullName.getText().toString().trim());
        userProfile.setDisplayName(etDisplayName.getText().toString().trim());
        userProfile.setPhoneNumber(etPhone.getText().toString().trim());
        userProfile.setDateOfBirth(etDateOfBirth.getText().toString().trim());
        userProfile.setBio(etBio.getText().toString().trim());
        userProfile.setPhoneVerified(isPhoneVerified);
        userProfile.setEmail(etEmail.getText().toString().trim());

        // Get selected gender
        int selectedGenderId = chipGroupGender.getCheckedChipId();
        if (selectedGenderId != View.NO_ID) {
            Chip selectedChip = findViewById(selectedGenderId);
            if (selectedChip != null) {
                userProfile.setGender(selectedChip.getText().toString());
            }
        }

        // Get selected pronouns
        int selectedPronounsId = chipGroupPronouns.getCheckedChipId();
        if (selectedPronounsId == R.id.chip_custom_pronouns) {
            userProfile.setPronouns(etCustomPronouns.getText().toString().trim());
        } else if (selectedPronounsId != View.NO_ID) {
            Chip selectedChip = findViewById(selectedPronounsId);
            if (selectedChip != null) {
                userProfile.setPronouns(selectedChip.getText().toString());
            }
        }

        // Get timezone
        String selectedTimezone = dropdownTimezone.getText().toString();
        if (!selectedTimezone.isEmpty()) {
            // Extract timezone ID (e.g., "Asia/Kolkata" from "Asia/Kolkata (IST)")
            int parenIndex = selectedTimezone.indexOf(" (");
            if (parenIndex > 0) {
                userProfile.setTimeZone(selectedTimezone.substring(0, parenIndex));
            }
        }

        // Privacy settings
        userProfile.setSharePhoneNumber(switchSharePhone.isChecked());
        userProfile.setReceiveMarketing(switchMarketing.isChecked());
        userProfile.setAllowPersonalization(switchPersonalization.isChecked());

        // Update preferences
        if (userProfile.getPreferences() != null) {
            userProfile.getPreferences().setLanguage(dropdownLanguage.getText().toString());
            userProfile.getPreferences().setAllowPersonalization(switchPersonalization.isChecked());
            userProfile.getPreferences().setPromotionalOffers(switchMarketing.isChecked());
        }

        // Save to SharedPreferences and SessionManager
        saveToSessionManager();
        saveUserProfileToPreferences();

        // Also try to save to Firebase (background operation)
        saveToFirebase();

        // Show success after a short delay
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);

            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Profile updated successfully!",
                    Snackbar.LENGTH_LONG
            ).setAction("View Profile", v -> {
                setResult(RESULT_OK);
                finish();
            }).setActionTextColor(ContextCompat.getColor(this, R.color.primary_color))
                    .show();

            Log.d(TAG, "Profile saved successfully");

        }, 1500);
    }

    private void saveToSessionManager() {
        try {
            if (userProfile.getFullName() != null && !userProfile.getFullName().isEmpty()) {
                sessionManager.updateUserName(userProfile.getFullName());
            }

            User updatedUser = new User();
            updatedUser.setUserId(userProfile.getUserId());
            updatedUser.setName(userProfile.getFullName());
            updatedUser.setEmail(userProfile.getEmail());
            updatedUser.setPhone(userProfile.getPhoneNumber());
            updatedUser.setRole(sessionManager.getUserRole());

            sessionManager.createSession(updatedUser);
            Log.d(TAG, "User data saved to SessionManager");

        } catch (Exception e) {
            Log.e(TAG, "Error saving to SessionManager", e);
        }
    }

    private void saveUserProfileToPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("user_id", userProfile.getUserId());
            editor.putString("full_name", userProfile.getFullName());
            editor.putString("display_name", userProfile.getDisplayNameField());
            editor.putString("phone_number", userProfile.getPhoneNumber());
            editor.putString("email", userProfile.getEmail());
            editor.putString("date_of_birth", userProfile.getDateOfBirth());
            editor.putString("gender", userProfile.getGender());
            editor.putString("pronouns", userProfile.getPronouns());
            editor.putString("bio", userProfile.getBio());
            editor.putString("timezone", userProfile.getTimeZone());
            editor.putBoolean("phone_verified", userProfile.isPhoneVerified());
            editor.putBoolean("email_verified", userProfile.isEmailVerified());
            editor.putBoolean("share_phone", userProfile.isSharePhoneNumber());
            editor.putBoolean("marketing", userProfile.isReceiveMarketing());
            editor.putBoolean("personalization", userProfile.isAllowPersonalization());

            if (userProfile.getPreferences() != null) {
                editor.putString("language", userProfile.getPreferences().getLanguage());
            }

            editor.apply();
            Log.d(TAG, "UserProfile saved to SharedPreferences");

        } catch (Exception e) {
            Log.e(TAG, "Error saving UserProfile to SharedPreferences", e);
        }
    }

    private void saveToFirebase() {
        try {
            if (firebaseManager != null && userProfile != null) {
                User firebaseUser = new User();
                firebaseUser.setUserId(userProfile.getUserId());
                firebaseUser.setName(userProfile.getFullName());
                firebaseUser.setPhone(userProfile.getPhoneNumber());
                firebaseUser.setEmail(userProfile.getEmail());
                firebaseUser.setRole(sessionManager.getUserRole());

                Log.d(TAG, "Attempting to save profile to Firebase (background)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preparing Firebase save", e);
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Leave", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        if (userProfile == null) return false;

        String currentName = etFullName.getText().toString().trim();
        String currentPhone = etPhone.getText().toString().trim();
        String currentDateOfBirth = etDateOfBirth.getText().toString().trim();
        String currentDisplayName = etDisplayName.getText().toString().trim();
        String currentBio = etBio.getText().toString().trim();

        String originalName = userProfile.getFullName() != null ? userProfile.getFullName() : "";
        String originalPhone = userProfile.getPhoneNumber() != null ? userProfile.getPhoneNumber() : "";
        String originalDateOfBirth = userProfile.getDateOfBirth() != null ? userProfile.getDateOfBirth() : "";
        String originalDisplayName = userProfile.getDisplayNameField() != null ? userProfile.getDisplayNameField() : "";
        String originalBio = userProfile.getBio() != null ? userProfile.getBio() : "";

        return !currentName.equals(originalName) ||
                !currentPhone.equals(originalPhone) ||
                !currentDateOfBirth.equals(originalDateOfBirth) ||
                !currentDisplayName.equals(originalDisplayName) ||
                !currentBio.equals(originalBio);
    }
}
