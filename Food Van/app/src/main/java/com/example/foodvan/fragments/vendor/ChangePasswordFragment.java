package com.example.foodvan.fragments.vendor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodvan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Change Password Fragment with Material Design 3
 * Allows vendors to change their password with validation and strength indicator
 */
public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    // UI Components
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private LinearProgressIndicator passwordStrengthProgress;
    private TextView tvPasswordStrength;
    private ImageView icLengthCheck, icUppercaseCheck, icNumberCheck;
    private MaterialButton btnUpdatePassword;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    // Password strength tracking
    private boolean hasMinLength = false;
    private boolean hasUppercase = false;
    private boolean hasNumberOrSymbol = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        initializeViews(view);
        setupClickListeners();
        setupPasswordWatcher();
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    private void initializeViews(View view) {
        // Text Input Layouts
        tilCurrentPassword = view.findViewById(R.id.til_current_password);
        tilNewPassword = view.findViewById(R.id.til_new_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);

        // Edit Texts
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        // Password Strength Components
        passwordStrengthProgress = view.findViewById(R.id.password_strength_progress);
        tvPasswordStrength = view.findViewById(R.id.tv_password_strength);
        icLengthCheck = view.findViewById(R.id.ic_length_check);
        icUppercaseCheck = view.findViewById(R.id.ic_uppercase_check);
        icNumberCheck = view.findViewById(R.id.ic_number_check);

        // Button
        btnUpdatePassword = view.findViewById(R.id.btn_update_password);
    }

    private void setupClickListeners() {
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void setupPasswordWatcher() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkPasswordStrength(String password) {
        // Check minimum length
        hasMinLength = password.length() >= 8;
        updateCheckIcon(icLengthCheck, hasMinLength);

        // Check uppercase letter
        hasUppercase = password.matches(".*[A-Z].*");
        updateCheckIcon(icUppercaseCheck, hasUppercase);

        // Check number or symbol
        hasNumberOrSymbol = password.matches(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        updateCheckIcon(icNumberCheck, hasNumberOrSymbol);

        // Calculate overall strength
        int strength = 0;
        if (hasMinLength) strength += 33;
        if (hasUppercase) strength += 33;
        if (hasNumberOrSymbol) strength += 34;

        // Update progress bar
        passwordStrengthProgress.setProgress(strength);

        // Update strength text and color
        if (strength < 33) {
            tvPasswordStrength.setText("Weak");
            tvPasswordStrength.setTextColor(requireContext().getColor(R.color.error_color));
            passwordStrengthProgress.setIndicatorColor(requireContext().getColor(R.color.error_color));
        } else if (strength < 67) {
            tvPasswordStrength.setText("Medium");
            tvPasswordStrength.setTextColor(requireContext().getColor(R.color.warning_color));
            passwordStrengthProgress.setIndicatorColor(requireContext().getColor(R.color.warning_color));
        } else {
            tvPasswordStrength.setText("Strong");
            tvPasswordStrength.setTextColor(requireContext().getColor(R.color.success_color));
            passwordStrengthProgress.setIndicatorColor(requireContext().getColor(R.color.success_color));
        }
    }

    private void updateCheckIcon(ImageView icon, boolean isValid) {
        if (isValid) {
            icon.setImageResource(R.drawable.ic_check_circle);
            icon.setColorFilter(requireContext().getColor(R.color.success_color));
        } else {
            icon.setImageResource(R.drawable.ic_circle);
            icon.setColorFilter(requireContext().getColor(android.R.color.darker_gray));
        }
    }

    private void updatePassword() {
        if (!validateInputs()) return;

        if (currentUser == null) {
            showError("User not authenticated");
            return;
        }

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Re-authenticate user with current password
        String email = currentUser.getEmail();
        if (email == null) {
            showError("Unable to get user email");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        btnUpdatePassword.setEnabled(false);
        btnUpdatePassword.setText("Updating...");

        currentUser.reauthenticate(credential)
            .addOnSuccessListener(aVoid -> {
                // Update password
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid1 -> {
                        showSuccess("Password updated successfully");
                        clearFields();
                        btnUpdatePassword.setEnabled(true);
                        btnUpdatePassword.setText("Update Password");
                    })
                    .addOnFailureListener(e -> {
                        showError("Failed to update password: " + e.getMessage());
                        btnUpdatePassword.setEnabled(true);
                        btnUpdatePassword.setText("Update Password");
                    });
            })
            .addOnFailureListener(e -> {
                showError("Current password is incorrect");
                tilCurrentPassword.setError("Incorrect password");
                btnUpdatePassword.setEnabled(true);
                btnUpdatePassword.setText("Update Password");
            });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate current password
        if (TextUtils.isEmpty(etCurrentPassword.getText())) {
            tilCurrentPassword.setError("Current password is required");
            isValid = false;
        }

        // Validate new password
        String newPassword = etNewPassword.getText().toString().trim();
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("New password is required");
            isValid = false;
        } else if (!hasMinLength || !hasUppercase || !hasNumberOrSymbol) {
            tilNewPassword.setError("Password doesn't meet strength requirements");
            isValid = false;
        }

        // Validate confirm password
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your new password");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        
        // Reset password strength indicators
        passwordStrengthProgress.setProgress(0);
        tvPasswordStrength.setText("Enter a password to check strength");
        tvPasswordStrength.setTextColor(requireContext().getColor(android.R.color.darker_gray));
        
        updateCheckIcon(icLengthCheck, false);
        updateCheckIcon(icUppercaseCheck, false);
        updateCheckIcon(icNumberCheck, false);
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
