package com.example.foodvan.activities.vendor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import com.example.foodvan.R;

public class ImportExportActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_FILE_PICKER = 1002;

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    
    // Import Tab Components
    private MaterialCardView cardUploadArea;
    private MaterialButton btnSelectFile, btnDownloadTemplate;
    private TextInputEditText etFileName;
    private ChipGroup chipGroupFormat;
    private LinearProgressIndicator progressImport;
    private MaterialButton btnImport;
    
    // Export Tab Components
    private ChipGroup chipGroupExportType, chipGroupExportFormat;
    private MaterialButton btnExport;
    private LinearProgressIndicator progressExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
        
        initializeViews();
        setupToolbar();
        setupTabs();
        setupListeners();
        checkPermissions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        
        // Import components
        cardUploadArea = findViewById(R.id.card_upload_area);
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnDownloadTemplate = findViewById(R.id.btn_download_template);
        etFileName = findViewById(R.id.et_file_name);
        chipGroupFormat = findViewById(R.id.chip_group_format);
        progressImport = findViewById(R.id.progress_import);
        btnImport = findViewById(R.id.btn_import);
        
        // Export components
        chipGroupExportType = findViewById(R.id.chip_group_export_type);
        chipGroupExportFormat = findViewById(R.id.chip_group_export_format);
        btnExport = findViewById(R.id.btn_export);
        progressExport = findViewById(R.id.progress_export);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Import / Export");
        }
    }

    private void setupTabs() {
        // TODO: Setup ViewPager adapter for Import/Export tabs
        // For now, we'll handle both in the same activity
        tabLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Import listeners
        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnDownloadTemplate.setOnClickListener(v -> downloadTemplate());
        btnImport.setOnClickListener(v -> startImport());
        
        // Export listeners
        btnExport.setOnClickListener(v -> startExport());
        
        // Upload area click
        cardUploadArea.setOnClickListener(v -> openFilePicker());
        
        // Format chip listeners
        chipGroupFormat.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateImportUI();
        });
        
        chipGroupExportType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateExportUI();
        });
        
        chipGroupExportFormat.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateExportUI();
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_STORAGE_PERMISSION);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        String[] mimeTypes = {"text/csv", "application/vnd.ms-excel", 
                             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILE_PICKER);
    }

    private void downloadTemplate() {
        // TODO: Generate and download CSV template
        showSnackbar("Template download started...", Snackbar.LENGTH_SHORT);
        
        // Simulate download
        new Handler().postDelayed(() -> {
            showSnackbar("Template downloaded to Downloads folder", Snackbar.LENGTH_LONG);
        }, 2000);
    }

    private void startImport() {
        if (etFileName.getText() == null || etFileName.getText().toString().trim().isEmpty()) {
            showSnackbar("Please select a file first", Snackbar.LENGTH_SHORT);
            return;
        }
        
        // Show progress
        progressImport.setVisibility(View.VISIBLE);
        btnImport.setEnabled(false);
        
        // TODO: Implement actual import logic
        simulateImportProcess();
    }

    private void simulateImportProcess() {
        new Handler().postDelayed(() -> {
            progressImport.setProgress(25);
            showSnackbar("Validating file format...", Snackbar.LENGTH_SHORT);
            
            new Handler().postDelayed(() -> {
                progressImport.setProgress(50);
                showSnackbar("Processing items...", Snackbar.LENGTH_SHORT);
                
                new Handler().postDelayed(() -> {
                    progressImport.setProgress(75);
                    showSnackbar("Uploading to database...", Snackbar.LENGTH_SHORT);
                    
                    new Handler().postDelayed(() -> {
                        progressImport.setProgress(100);
                        progressImport.setVisibility(View.GONE);
                        btnImport.setEnabled(true);
                        showSnackbar("Import completed successfully! 25 items imported.", Snackbar.LENGTH_LONG);
                    }, 1000);
                }, 1000);
            }, 1000);
        }, 1000);
    }

    private void startExport() {
        // Show progress
        progressExport.setVisibility(View.VISIBLE);
        btnExport.setEnabled(false);
        
        // TODO: Implement actual export logic
        simulateExportProcess();
    }

    private void simulateExportProcess() {
        new Handler().postDelayed(() -> {
            progressExport.setProgress(33);
            showSnackbar("Fetching menu items...", Snackbar.LENGTH_SHORT);
            
            new Handler().postDelayed(() -> {
                progressExport.setProgress(66);
                showSnackbar("Generating file...", Snackbar.LENGTH_SHORT);
                
                new Handler().postDelayed(() -> {
                    progressExport.setProgress(100);
                    progressExport.setVisibility(View.GONE);
                    btnExport.setEnabled(true);
                    showSnackbar("Export completed! File saved to Downloads folder.", Snackbar.LENGTH_LONG);
                }, 1000);
            }, 1000);
        }, 1000);
    }

    private void updateImportUI() {
        // Update UI based on selected format
        int checkedId = chipGroupFormat.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                String format = selectedChip.getText().toString();
                // Update UI based on format
            }
        }
    }

    private void updateExportUI() {
        // Update export button state based on selections
        boolean hasType = chipGroupExportType.getCheckedChipId() != View.NO_ID;
        boolean hasFormat = chipGroupExportFormat.getCheckedChipId() != View.NO_ID;
        btnExport.setEnabled(hasType && hasFormat);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String fileName = getFileName(fileUri);
                etFileName.setText(fileName);
                showSnackbar("File selected: " + fileName, Snackbar.LENGTH_SHORT);
            }
        }
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return "Unknown file";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar("Storage permission granted", Snackbar.LENGTH_SHORT);
            } else {
                showSnackbar("Storage permission required for import/export", Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showSnackbar(String message, int duration) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
