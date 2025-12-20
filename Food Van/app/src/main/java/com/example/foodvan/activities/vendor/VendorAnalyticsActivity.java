package com.example.foodvan.activities.vendor;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.foodvan.R;
import com.example.foodvan.models.Order;
import com.example.foodvan.viewmodels.VendorAnalyticsViewModel;
import com.example.foodvan.utils.PdfExportUtil;
import com.example.foodvan.utils.ExcelExportUtil;

import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * VendorAnalyticsActivity - Professional Analytics Dashboard for Food Van Vendors
 * Features: Real-time analytics, earnings tracking, order insights, performance metrics
 */
public class VendorAnalyticsActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private ChipGroup chipGroupTimePeriod;
    
    // Analytics Data Views
    private TextView tvTotalRevenue, tvTotalEarningsWeek, tvTotalEarningsMonth;
    private TextView tvTotalOrders, tvCompletedOrders, tvPendingOrders, tvCancelledOrders;
    private TextView tvAvgOrderValue, tvCustomerRating, tvCompletionRate;
    private TextView tvHighestSellingItem, tvPeakHours, tvRepeatCustomers;
    
    // Action Buttons
    private MaterialButton btnExportPdf, btnExportExcel;
    
    // ViewModel
    private VendorAnalyticsViewModel viewModel;
    
    // Firebase Components
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef, ordersRef, earningsRef;
    private String vendorId;
    
    // Analytics Data
    private AnalyticsData todayData, weekData, monthData;
    private AnalyticsData currentData;
    
    // Permission constants
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_vendor_analytics);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(VendorAnalyticsViewModel.class);
        
        initializeFirebase();
        initializeViews();
        setupToolbar();
        setupSampleData();
        setupClickListeners();
        setupTimePeriodChips();
        setupObservers();
        
        // Load real-time data
        loadRealTimeAnalytics();
        
        // Load default data (This Week)
        loadAnalyticsData(weekData);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            vendorId = firebaseAuth.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            vendorRef = database.getReference("vendors").child(vendorId);
            ordersRef = database.getReference("orders");
            earningsRef = database.getReference("earnings").child(vendorId);
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupTimePeriod = findViewById(R.id.chip_group_time_period);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvAvgOrderValue = findViewById(R.id.tv_avg_order_value);
        tvCustomerRating = findViewById(R.id.tv_customer_rating);
        tvCompletionRate = findViewById(R.id.tv_completion_rate);
        
        // Performance insights views
        tvHighestSellingItem = findViewById(R.id.tv_highest_selling_item);
        tvPeakHours = findViewById(R.id.tv_peak_hours);
        
        btnExportPdf = findViewById(R.id.btn_export_pdf);
        btnExportExcel = findViewById(R.id.btn_export_excel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupSampleData() {
        // Today's data
        todayData = new AnalyticsData(5, 450, 90, 4.9, 100);
        
        // This week's data
        weekData = new AnalyticsData(24, 2450, 102, 4.8, 96);
        
        // This month's data
        monthData = new AnalyticsData(89, 9850, 111, 4.7, 94);
        
        currentData = weekData; // Default to week view
    }

    private void setupClickListeners() {
        btnExportPdf.setOnClickListener(v -> {
            Toast.makeText(this, "PDF Export clicked!", Toast.LENGTH_SHORT).show();
            try {
                if (checkStoragePermission()) {
                    // Use simple export method to avoid library issues
                    exportSimplePDF();
                } else {
                    requestStoragePermission();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
        
        btnExportExcel.setOnClickListener(v -> {
            Toast.makeText(this, "Excel Export clicked!", Toast.LENGTH_SHORT).show();
            try {
                if (checkStoragePermission()) {
                    // Use simple export method to avoid library issues
                    exportSimpleExcel();
                } else {
                    requestStoragePermission();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void setupTimePeriodChips() {
        chipGroupTimePeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                
                if (checkedId == R.id.chip_today) {
                    loadAnalyticsData(todayData);
                } else if (checkedId == R.id.chip_week) {
                    loadAnalyticsData(weekData);
                } else if (checkedId == R.id.chip_month) {
                    loadAnalyticsData(monthData);
                }
            }
        });
    }

    private void loadAnalyticsData(AnalyticsData data) {
        currentData = data;
        
        // Update UI with new data
        tvTotalRevenue.setText("₹" + formatNumber(data.totalRevenue));
        tvAvgOrderValue.setText("₹" + data.avgOrderValue);
        tvCustomerRating.setText(String.valueOf(data.customerRating) + " ★");
        tvCompletionRate.setText(data.completionRate + "%");
        
        // Update performance insights
        if (tvHighestSellingItem != null) {
            tvHighestSellingItem.setText("Butter Chicken"); // Sample data
        }
        if (tvPeakHours != null) {
            tvPeakHours.setText("Most orders received during this time");
        }
    }

    private void loadRealTimeAnalytics() {
        if (vendorId == null) return;
        
        // Load real-time data from Firebase
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Load today's earnings
        earningsRef.child(todayDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double earnings = snapshot.getValue(Double.class);
                    if (earnings != null) {
                        todayData.totalRevenue = earnings.intValue();
                        if (currentData == todayData) {
                            loadAnalyticsData(todayData);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently
            }
        });

        // Load orders data
        ordersRef.orderByChild("vendorId").equalTo(vendorId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int todayOrders = 0, weekOrders = 0, monthOrders = 0;
                
                Calendar cal = Calendar.getInstance();
                long todayStart = getStartOfDay(cal.getTime()).getTime();
                long weekStart = getStartOfWeek(cal.getTime()).getTime();
                long monthStart = getStartOfMonth(cal.getTime()).getTime();
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && order.getTimestamp() != null) {
                        long orderTime = order.getTimestamp().getTime();
                        
                        if (orderTime >= todayStart) todayOrders++;
                        if (orderTime >= weekStart) weekOrders++;
                        if (orderTime >= monthStart) monthOrders++;
                    }
                }
                
                todayData.totalOrders = todayOrders;
                weekData.totalOrders = weekOrders;
                monthData.totalOrders = monthOrders;
                
                if (currentData != null) {
                    loadAnalyticsData(currentData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently
            }
        });
    }

    private String formatNumber(int number) {
        if (number >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    /**
     * Setup observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe earnings data
        viewModel.getTodayEarnings().observe(this, earnings -> {
            if (earnings != null) {
                todayData.totalRevenue = earnings.intValue();
                if (currentData == todayData) {
                    loadAnalyticsData(todayData);
                }
            }
        });

        viewModel.getWeekEarnings().observe(this, earnings -> {
            if (earnings != null) {
                weekData.totalRevenue = earnings.intValue();
                if (currentData == weekData) {
                    loadAnalyticsData(weekData);
                }
            }
        });

        viewModel.getMonthEarnings().observe(this, earnings -> {
            if (earnings != null) {
                monthData.totalRevenue = earnings.intValue();
                if (currentData == monthData) {
                    loadAnalyticsData(monthData);
                }
            }
        });

        // Observe order data
        viewModel.getTotalOrders().observe(this, orders -> {
            if (orders != null && currentData != null) {
                currentData.totalOrders = orders;
                loadAnalyticsData(currentData);
            }
        });
    }

    /**
     * Generate sample daily data for Excel export
     */
    private java.util.List<ExcelExportUtil.DailyData> generateSampleDailyData() {
        java.util.List<ExcelExportUtil.DailyData> sampleData = new java.util.ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Generate data for last 30 days
        for (int i = 29; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            
            String date = dateFormat.format(cal.getTime());
            int orders = (int) (Math.random() * 20) + 5; // 5-25 orders
            double earnings = orders * (50 + Math.random() * 100); // ₹50-150 per order
            int completed = (int) (orders * 0.9); // 90% completion rate
            int cancelled = orders - completed;

            sampleData.add(new ExcelExportUtil.DailyData(date, orders, earnings, completed, cancelled));
        }

        return sampleData;
    }

    /**
     * Check storage permission
     */
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ - use scoped storage, no permission needed for app-specific directories
            return true;
        } else {
            // Android 10 and below - check traditional storage permissions
            return EasyPermissions.hasPermissions(this, 
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Request storage permission
     */
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ - no permission needed for app-specific directories
            Toast.makeText(this, "No permission needed for app storage", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EasyPermissions.requestPermissions(this,
            "Storage permission is required to export files",
            STORAGE_PERMISSION_CODE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Export analytics data to PDF
     */
    private void exportToPDF() {
        Toast.makeText(this, "Starting PDF export...", Toast.LENGTH_SHORT).show();
        
        try {
            // Create analytics data for PDF export
            PdfExportUtil.AnalyticsData pdfData = new PdfExportUtil.AnalyticsData(
                todayData.totalRevenue,
                weekData.totalRevenue,
                monthData.totalRevenue,
                weekData.totalOrders,
                weekData.totalOrders - 1, // completed (sample)
                1, // pending (sample)
                0, // cancelled (sample)
                weekData.avgOrderValue,
                weekData.customerRating,
                weekData.completionRate,
                "Butter Chicken", // highest selling item
                "12-2 PM" // peak hours
            );

            Toast.makeText(this, "PDF data prepared, calling export...", Toast.LENGTH_SHORT).show();

            // Export to PDF in background thread
            new Thread(() -> {
                try {
                    PdfExportUtil.exportToPdf(this, pdfData, new PdfExportUtil.ExportCallback() {
                        @Override
                        public void onSuccess(File file) {
                            runOnUiThread(() -> {
                                Toast.makeText(VendorAnalyticsActivity.this, 
                                    "PDF exported successfully!\nSaved to: " + file.getName(), 
                                    Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(VendorAnalyticsActivity.this, 
                                    "Export failed: " + error, 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(VendorAnalyticsActivity.this, 
                            "PDF Export Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Export analytics data to Excel
     */
    private void exportToExcel() {
        Toast.makeText(this, "Starting Excel export...", Toast.LENGTH_SHORT).show();
        
        try {
            // Create summary data for Excel export
            ExcelExportUtil.SummaryData summaryData = new ExcelExportUtil.SummaryData(
                todayData.totalRevenue,
                weekData.totalRevenue,
                monthData.totalRevenue,
                weekData.totalOrders,
                weekData.totalOrders - 1, // completed (sample)
                1, // pending (sample)
                0, // cancelled (sample)
                weekData.avgOrderValue,
                weekData.customerRating,
                weekData.completionRate,
                "Butter Chicken", // highest selling item
                "12-2 PM" // peak hours
            );

            // Generate sample daily data (will be replaced with real data)
            java.util.List<ExcelExportUtil.DailyData> dailyDataList = generateSampleDailyData();

            Toast.makeText(this, "Excel data prepared, calling export...", Toast.LENGTH_SHORT).show();

            // Export to Excel in background thread
            new Thread(() -> {
                try {
                    ExcelExportUtil.exportToExcel(this, summaryData, dailyDataList, new ExcelExportUtil.ExportCallback() {
                        @Override
                        public void onSuccess(File file) {
                            runOnUiThread(() -> {
                                Toast.makeText(VendorAnalyticsActivity.this, 
                                    "Excel exported successfully!\nSaved to: " + file.getName(), 
                                    Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(VendorAnalyticsActivity.this, 
                                    "Export failed: " + error, 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(VendorAnalyticsActivity.this, 
                            "Excel Export Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to export Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Simple PDF export fallback method
     */
    private void exportSimplePDF() {
        try {
            // Create a simple text file with analytics data
            File directory = new File(getExternalFilesDir(null), "FoodVan_Reports");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "analytics_report_" + timestamp + ".txt";
            File file = new File(directory, fileName);

            StringBuilder content = new StringBuilder();
            content.append("FOOD VAN ANALYTICS REPORT\n");
            content.append("Generated on: ").append(new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");
            content.append("EARNINGS SUMMARY:\n");
            content.append("Today: ₹").append(todayData.totalRevenue).append("\n");
            content.append("This Week: ₹").append(weekData.totalRevenue).append("\n");
            content.append("This Month: ₹").append(monthData.totalRevenue).append("\n\n");
            content.append("ORDER INSIGHTS:\n");
            content.append("Total Orders: ").append(weekData.totalOrders).append("\n");
            content.append("Average Order Value: ₹").append(weekData.avgOrderValue).append("\n");
            content.append("Customer Rating: ").append(weekData.customerRating).append(" ★\n");
            content.append("Completion Rate: ").append(weekData.completionRate).append("%\n\n");
            content.append("PERFORMANCE:\n");
            content.append("Highest Selling Item: Butter Chicken\n");
            content.append("Peak Hours: 12-2 PM\n");

            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content.toString());
            writer.close();

            Toast.makeText(this, "Simple report exported!\nSaved to: " + file.getName(), Toast.LENGTH_LONG).show();

            // Try to open the file
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, 
                    getPackageName() + ".fileprovider", file);
                intent.setDataAndType(uri, "text/plain");
                intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "File saved but couldn't open viewer", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Simple export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Simple Excel export fallback method (creates CSV)
     */
    private void exportSimpleExcel() {
        try {
            // Create a simple CSV file with analytics data
            File directory = new File(getExternalFilesDir(null), "FoodVan_Reports");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "analytics_data_" + timestamp + ".csv";
            File file = new File(directory, fileName);

            StringBuilder content = new StringBuilder();
            content.append("Food Van Analytics Data\n");
            content.append("Generated,").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");
            content.append("Metric,Value\n");
            content.append("Today Earnings,₹").append(todayData.totalRevenue).append("\n");
            content.append("Week Earnings,₹").append(weekData.totalRevenue).append("\n");
            content.append("Month Earnings,₹").append(monthData.totalRevenue).append("\n");
            content.append("Total Orders,").append(weekData.totalOrders).append("\n");
            content.append("Avg Order Value,₹").append(weekData.avgOrderValue).append("\n");
            content.append("Customer Rating,").append(weekData.customerRating).append("\n");
            content.append("Completion Rate,").append(weekData.completionRate).append("%\n");
            content.append("Highest Selling,Butter Chicken\n");
            content.append("Peak Hours,12-2 PM\n");

            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content.toString());
            writer.close();

            Toast.makeText(this, "CSV data exported!\nSaved to: " + file.getName(), Toast.LENGTH_LONG).show();

            // Try to open the file
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, 
                    getPackageName() + ".fileprovider", file);
                intent.setDataAndType(uri, "text/csv");
                intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "File saved but couldn't open viewer", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Simple CSV export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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

    /**
     * Setup orange status bar to match the navbar theme with reduced spacing
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_color, getTheme()));
            
            // Make status bar icons light (white) for dark orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0); // Clear light status bar flag
            }
            
            // Ensure proper spacing - don't fit system windows
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            }
        }
    }

    // Date utility methods
    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getStartOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getStartOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Analytics Data Model
    private static class AnalyticsData {
        int totalOrders;
        int totalRevenue;
        int avgOrderValue;
        double customerRating;
        int completionRate;

        public AnalyticsData(int totalOrders, int totalRevenue, int avgOrderValue, 
                           double customerRating, int completionRate) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.avgOrderValue = avgOrderValue;
            this.customerRating = customerRating;
            this.completionRate = completionRate;
        }
    }
}
