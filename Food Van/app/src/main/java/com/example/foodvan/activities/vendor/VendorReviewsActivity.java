package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ReviewAdapter;
import com.example.foodvan.database.ReviewEntity;
import com.example.foodvan.database.ReviewMetaEntity;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.viewmodels.ReviewViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// PDF imports
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.geom.PageSize;

/**
 * Activity for displaying and managing vendor reviews and ratings
 */
public class VendorReviewsActivity extends AppCompatActivity implements ReviewAdapter.OnReviewActionListener {
    private static final String TAG = "VendorReviewsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvOverallRating;
    private TextView tvTotalReviews;
    private TextView tvTrendValue;
    private LinearProgressIndicator[] ratingProgressBars;
    private TextView[] ratingCounts;
    private TextView[] ratingPercentages;
    private MaterialButton btnExportReviews;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerViewReviews;
    private View layoutEmptyState;
    private CircularProgressIndicator progressLoading;
    private View snackbarAnchor;

    // Data
    private ReviewViewModel reviewViewModel;
    private ReviewAdapter reviewAdapter;
    private SessionManager sessionManager;
    private String vendorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_reviews);

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupViewModel();
        setupObservers();
        
        loadVendorData();
    }

    private void initializeComponents() {
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        tvOverallRating = findViewById(R.id.tv_overall_rating);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        tvTrendValue = findViewById(R.id.tv_trend_value);
        btnExportReviews = findViewById(R.id.btn_export_reviews);
        etSearch = findViewById(R.id.et_search);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        recyclerViewReviews = findViewById(R.id.recycler_view_reviews);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        progressLoading = findViewById(R.id.progress_loading);
        snackbarAnchor = findViewById(R.id.snackbar_anchor);

        // Initialize rating breakdown views
        ratingProgressBars = new LinearProgressIndicator[5];
        ratingCounts = new TextView[5];
        ratingPercentages = new TextView[5];
        
        initializeRatingBreakdownViews();

        // Initialize session manager
        sessionManager = new SessionManager(this);
        vendorId = sessionManager.getUserId();
    }

    private void initializeRatingBreakdownViews() {
        int[] ratingIds = {R.id.rating_5_star, R.id.rating_4_star, R.id.rating_3_star, R.id.rating_2_star, R.id.rating_1_star};
        
        for (int i = 0; i < 5; i++) {
            View ratingView = findViewById(ratingIds[i]);
            ratingProgressBars[i] = ratingView.findViewById(R.id.progress_rating);
            ratingCounts[i] = ratingView.findViewById(R.id.tv_count);
            ratingPercentages[i] = ratingView.findViewById(R.id.tv_percentage);
            
            // Set star labels
            TextView tvStarLabel = ratingView.findViewById(R.id.tv_star_label);
            tvStarLabel.setText((5 - i) + "★");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(this);
        reviewAdapter.setOnReviewActionListener(this);
        
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (reviewViewModel != null) {
                    reviewViewModel.setSearchQuery(s.toString());
                }
            }
        });

        // Filter chips
        setupFilterChips();
        
        // Export button
        btnExportReviews.setOnClickListener(v -> exportReviews());
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (reviewViewModel == null) return;
            
            if (checkedIds.isEmpty()) {
                reviewViewModel.setFilter(ReviewViewModel.ReviewFilter.ALL);
                return;
            }
            
            int checkedId = checkedIds.get(0);
            ReviewViewModel.ReviewFilter filter = ReviewViewModel.ReviewFilter.ALL;
            
            if (checkedId == R.id.chip_5_star) {
                filter = ReviewViewModel.ReviewFilter.FIVE_STAR;
            } else if (checkedId == R.id.chip_4_star) {
                filter = ReviewViewModel.ReviewFilter.FOUR_STAR;
            } else if (checkedId == R.id.chip_3_star) {
                filter = ReviewViewModel.ReviewFilter.THREE_STAR;
            } else if (checkedId == R.id.chip_2_star) {
                filter = ReviewViewModel.ReviewFilter.TWO_STAR;
            } else if (checkedId == R.id.chip_1_star) {
                filter = ReviewViewModel.ReviewFilter.ONE_STAR;
            } else if (checkedId == R.id.chip_with_replies) {
                filter = ReviewViewModel.ReviewFilter.WITH_REPLIES;
            } else if (checkedId == R.id.chip_without_replies) {
                filter = ReviewViewModel.ReviewFilter.WITHOUT_REPLIES;
            }
            
            reviewViewModel.setFilter(filter);
        });
    }

    private void setupViewModel() {
        // TODO: Initialize ReviewViewModel with proper dependencies
        // This would require setting up the database and repository first
        // reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
    }

    private void setupObservers() {
        if (reviewViewModel == null) return;
        
        // Observe filtered reviews
        reviewViewModel.getFilteredReviews().observe(this, this::updateReviewsList);
        
        // Observe review metadata
        reviewViewModel.getReviewMeta().observe(this, this::updateReviewMeta);
        
        // Observe loading state
        reviewViewModel.getIsLoadingLiveData().observe(this, this::updateLoadingState);
        
        // Observe errors
        reviewViewModel.getErrorLiveData().observe(this, this::handleError);
    }

    private void loadVendorData() {
        if (reviewViewModel != null && vendorId != null) {
            reviewViewModel.setVendorId(vendorId);
        }
    }

    private void updateReviewsList(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            recyclerViewReviews.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewReviews.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            reviewAdapter.setReviews(reviews);
        }
    }

    private void updateReviewMeta(ReviewMetaEntity meta) {
        if (meta == null) return;
        
        // Update overall rating
        tvOverallRating.setText(String.format("%.1f", meta.getAverageRating()));
        tvTotalReviews.setText("Based on " + meta.getTotalReviews() + " reviews");
        
        // Update trend
        String trend = meta.getThirtyDayAverage() > meta.getAverageRating() ? "↗ Improving" : 
                      meta.getThirtyDayAverage() < meta.getAverageRating() ? "↘ Declining" : "→ Stable";
        tvTrendValue.setText(trend);
        
        // Update rating breakdown
        updateRatingBreakdown(meta);
    }

    private void updateRatingBreakdown(ReviewMetaEntity meta) {
        int totalReviews = meta.getTotalReviews();
        
        for (int i = 0; i < 5; i++) {
            int starRating = 5 - i;
            int count = getRatingCount(meta, starRating);
            double percentage = totalReviews > 0 ? (count * 100.0) / totalReviews : 0;
            
            ratingProgressBars[i].setProgress((int) percentage);
            ratingCounts[i].setText(String.valueOf(count));
            ratingPercentages[i].setText(String.format("%.1f%%", percentage));
        }
    }

    private int getRatingCount(ReviewMetaEntity meta, int rating) {
        switch (rating) {
            case 5: return meta.getRating5Count();
            case 4: return meta.getRating4Count();
            case 3: return meta.getRating3Count();
            case 2: return meta.getRating2Count();
            case 1: return meta.getRating1Count();
            default: return 0;
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void handleError(String error) {
        if (error != null) {
            Snackbar.make(snackbarAnchor, error, Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> {
                        if (reviewViewModel != null) {
                            reviewViewModel.syncReviewsFromFirestore();
                            reviewViewModel.clearError();
                        }
                    })
                    .show();
        }
    }

    private void exportReviews() {
        // Show export options dialog (works with sample data for now)
        showExportOptionsDialog();
    }

    private void showExportOptionsDialog() {
        String[] exportOptions = {"CSV Format", "PDF Report", "JSON Data"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Reviews")
                .setIcon(R.drawable.ic_file_download)
                .setItems(exportOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportToCSV();
                            break;
                        case 1:
                            exportToPDF();
                            break;
                        case 2:
                            exportToJSON();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportToCSV() {
        // Show progress
        progressLoading.setVisibility(View.VISIBLE);
        
        try {
            // Get current filtered reviews
            List<ReviewEntity> reviews = getCurrentReviews();
            if (reviews == null || reviews.isEmpty()) {
                showError("No reviews to export");
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Create CSV file
            File csvFile = createExportFile("reviews", "csv");
            if (csvFile == null) {
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Write CSV data
            writeCSVData(csvFile, reviews);
            
            // Show success and offer to share
            progressLoading.setVisibility(View.GONE);
            showExportSuccessDialog(csvFile, "CSV");
            
        } catch (Exception e) {
            progressLoading.setVisibility(View.GONE);
            showError("Export failed: " + e.getMessage());
        }
    }

    private void exportToPDF() {
        // Show progress
        progressLoading.setVisibility(View.VISIBLE);
        
        try {
            // Get current filtered reviews
            List<ReviewEntity> reviews = getCurrentReviews();
            if (reviews == null || reviews.isEmpty()) {
                showError("No reviews to export");
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Create PDF file
            File pdfFile = createExportFile("reviews_report", "pdf");
            if (pdfFile == null) {
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Generate PDF report
            generatePDFReport(pdfFile, reviews);
            
            // Show success and offer to share
            progressLoading.setVisibility(View.GONE);
            showExportSuccessDialog(pdfFile, "PDF");
            
        } catch (Exception e) {
            progressLoading.setVisibility(View.GONE);
            showError("PDF export failed: " + e.getMessage());
        }
    }

    private void generatePDFReport(File pdfFile, List<ReviewEntity> reviews) throws Exception {
        // Create PDF writer and document
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        
        // Set margins
        document.setMargins(50, 50, 50, 50);
        
        try {
            // Create fonts
            PdfFont titleFont = PdfFontFactory.createFont();
            PdfFont headerFont = PdfFontFactory.createFont();
            PdfFont normalFont = PdfFontFactory.createFont();
            
            // Add title
            Paragraph title = new Paragraph("Reviews & Ratings Report")
                    .setFont(titleFont)
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // Add report metadata
            addReportMetadata(document, reviews, headerFont, normalFont);
            
            // Add spacing
            document.add(new Paragraph("\n").setMarginTop(10).setMarginBottom(10));
            
            // Add summary statistics
            addSummaryStatistics(document, reviews, headerFont, normalFont);
            
            // Add spacing
            document.add(new Paragraph("\n").setMarginTop(10).setMarginBottom(10));
            
            // Add reviews table
            addReviewsTable(document, reviews, headerFont, normalFont);
            
            // Add footer
            addReportFooter(document, normalFont);
            
        } finally {
            document.close();
        }
    }

    private void addReportMetadata(Document document, List<ReviewEntity> reviews, PdfFont headerFont, PdfFont normalFont) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        
        Paragraph metadata = new Paragraph()
                .setFont(normalFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        
        metadata.add("Generated on: " + dateFormat.format(new Date()) + "\n");
        metadata.add("Vendor ID: " + vendorId + "\n");
        metadata.add("Total Reviews: " + reviews.size());
        
        document.add(metadata.setMarginBottom(15));
    }

    private void addSummaryStatistics(Document document, List<ReviewEntity> reviews, PdfFont headerFont, PdfFont normalFont) {
        // Calculate statistics
        float totalRating = 0;
        int[] ratingCounts = new int[6]; // Index 0 unused, 1-5 for star ratings
        int repliedCount = 0;
        int flaggedCount = 0;
        
        for (ReviewEntity review : reviews) {
            totalRating += review.getRating();
            ratingCounts[(int) review.getRating()]++;
            if (review.getVendorReplyText() != null && !review.getVendorReplyText().isEmpty()) {
                repliedCount++;
            }
            if (review.isFlagged()) {
                flaggedCount++;
            }
        }
        
        float averageRating = reviews.size() > 0 ? totalRating / reviews.size() : 0;
        
        // Add summary header
        Paragraph summaryHeader = new Paragraph("Summary Statistics")
                .setFont(headerFont)
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
        document.add(summaryHeader);
        
        // Create summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // Add summary rows
        addSummaryRow(summaryTable, "Average Rating", String.format("%.1f / 5.0", averageRating), normalFont);
        addSummaryRow(summaryTable, "Total Reviews", String.valueOf(reviews.size()), normalFont);
        addSummaryRow(summaryTable, "Replied Reviews", repliedCount + " (" + Math.round((float) repliedCount / reviews.size() * 100) + "%)", normalFont);
        addSummaryRow(summaryTable, "Flagged Reviews", String.valueOf(flaggedCount), normalFont);
        
        // Add rating breakdown
        for (int i = 5; i >= 1; i--) {
            int count = ratingCounts[i];
            int percentage = reviews.size() > 0 ? Math.round((float) count / reviews.size() * 100) : 0;
            addSummaryRow(summaryTable, i + " Star Reviews", count + " (" + percentage + "%)", normalFont);
        }
        
        document.add(summaryTable.setMarginBottom(15));
    }

    private void addSummaryRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(10))
                .setBorder(Border.NO_BORDER).setPaddingBottom(5));
        table.addCell(new Cell().add(new Paragraph(value).setFont(font).setFontSize(10).setBold())
                .setBorder(Border.NO_BORDER).setPaddingBottom(5).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addReviewsTable(Document document, List<ReviewEntity> reviews, PdfFont headerFont, PdfFont normalFont) {
        // Add reviews header
        Paragraph reviewsHeader = new Paragraph("Detailed Reviews")
                .setFont(headerFont)
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
        document.add(reviewsHeader);
        
        // Create reviews table
        Table reviewsTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 4, 2, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // Add table headers
        String[] headers = {"Customer", "Rating", "Review", "Date", "Status"};
        for (String header : headers) {
            Cell headerCell = new Cell().add(new Paragraph(header).setFont(headerFont).setFontSize(10).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            reviewsTable.addHeaderCell(headerCell);
        }
        
        // Add review data
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        for (ReviewEntity review : reviews) {
            // Customer name
            reviewsTable.addCell(new Cell().add(new Paragraph(review.getCustomerName()).setFont(normalFont).setFontSize(9))
                    .setPadding(6));
            
            // Rating with stars
            String ratingText = "★".repeat((int) review.getRating()) + "☆".repeat(5 - (int) review.getRating()) + 
                               " (" + review.getRating() + ")";
            reviewsTable.addCell(new Cell().add(new Paragraph(ratingText).setFont(normalFont).setFontSize(8))
                    .setPadding(6).setTextAlignment(TextAlignment.CENTER));
            
            // Review text (truncated if too long)
            String reviewText = review.getText();
            if (reviewText.length() > 100) {
                reviewText = reviewText.substring(0, 97) + "...";
            }
            reviewsTable.addCell(new Cell().add(new Paragraph(reviewText).setFont(normalFont).setFontSize(8))
                    .setPadding(6));
            
            // Date
            reviewsTable.addCell(new Cell().add(new Paragraph(dateFormat.format(new Date(review.getCreatedAt()))).setFont(normalFont).setFontSize(8))
                    .setPadding(6).setTextAlignment(TextAlignment.CENTER));
            
            // Status
            String status = review.isFlagged() ? "Flagged" : "Active";
            if (review.getVendorReplyText() != null && !review.getVendorReplyText().isEmpty()) {
                status += " (Replied)";
            }
            reviewsTable.addCell(new Cell().add(new Paragraph(status).setFont(normalFont).setFontSize(8))
                    .setPadding(6).setTextAlignment(TextAlignment.CENTER));
        }
        
        document.add(reviewsTable);
    }

    private void addReportFooter(Document document, PdfFont normalFont) {
        // Add some space before footer
        document.add(new Paragraph("\n"));
        
        // Add footer text
        Paragraph footer = new Paragraph("This report was generated by Food Van Vendor Dashboard")
                .setFont(normalFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        
        document.add(footer);
    }

    private void exportToJSON() {
        // Show progress
        progressLoading.setVisibility(View.VISIBLE);
        
        try {
            // Get current filtered reviews
            List<ReviewEntity> reviews = getCurrentReviews();
            if (reviews == null || reviews.isEmpty()) {
                showError("No reviews to export");
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Create JSON file
            File jsonFile = createExportFile("reviews", "json");
            if (jsonFile == null) {
                progressLoading.setVisibility(View.GONE);
                return;
            }

            // Write JSON data
            writeJSONData(jsonFile, reviews);
            
            // Show success and offer to share
            progressLoading.setVisibility(View.GONE);
            showExportSuccessDialog(jsonFile, "JSON");
            
        } catch (Exception e) {
            progressLoading.setVisibility(View.GONE);
            showError("Export failed: " + e.getMessage());
        }
    }

    private File createExportFile(String baseName, String extension) {
        try {
            // Create filename with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String fileName = baseName + "_" + timestamp + "." + extension;
            
            // Create file in Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            File exportFile = new File(downloadsDir, fileName);
            if (exportFile.createNewFile()) {
                return exportFile;
            } else {
                showError("Failed to create export file");
                return null;
            }
            
        } catch (IOException e) {
            showError("Failed to create export file: " + e.getMessage());
            return null;
        }
    }

    private void writeCSVData(File csvFile, List<ReviewEntity> reviews) throws IOException {
        FileWriter writer = new FileWriter(csvFile);
        
        // Write CSV header
        writer.append("Review ID,Customer Name,Rating,Review Text,Date,Order ID,Vendor Reply,Reply Date,Status\n");
        
        // Write review data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        for (ReviewEntity review : reviews) {
            writer.append(escapeCSV(review.getReviewId())).append(",");
            writer.append(escapeCSV(review.getCustomerName())).append(",");
            writer.append(String.valueOf(review.getRating())).append(",");
            writer.append(escapeCSV(review.getText())).append(",");
            writer.append(escapeCSV(dateFormat.format(new Date(review.getCreatedAt())))).append(",");
            writer.append(escapeCSV(review.getOrderId() != null ? review.getOrderId() : "")).append(",");
            writer.append(escapeCSV(review.getVendorReplyText() != null ? review.getVendorReplyText() : "")).append(",");
            writer.append(escapeCSV(review.getVendorReplyCreatedAt() > 0 ? 
                dateFormat.format(new Date(review.getVendorReplyCreatedAt())) : "")).append(",");
            writer.append(escapeCSV(review.isFlagged() ? "Flagged" : "Active")).append("\n");
        }
        
        writer.flush();
        writer.close();
    }

    private void writeJSONData(File jsonFile, List<ReviewEntity> reviews) throws IOException {
        FileWriter writer = new FileWriter(jsonFile);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        
        writer.append("{\n");
        writer.append("  \"exportDate\": \"").append(dateFormat.format(new Date())).append("\",\n");
        writer.append("  \"vendorId\": \"").append(vendorId).append("\",\n");
        writer.append("  \"totalReviews\": ").append(String.valueOf(reviews.size())).append(",\n");
        writer.append("  \"reviews\": [\n");
        
        for (int i = 0; i < reviews.size(); i++) {
            ReviewEntity review = reviews.get(i);
            writer.append("    {\n");
            writer.append("      \"reviewId\": \"").append(review.getReviewId()).append("\",\n");
            writer.append("      \"customerName\": \"").append(escapeJSON(review.getCustomerName())).append("\",\n");
            writer.append("      \"rating\": ").append(String.valueOf(review.getRating())).append(",\n");
            writer.append("      \"reviewText\": \"").append(escapeJSON(review.getText())).append("\",\n");
            writer.append("      \"date\": \"").append(dateFormat.format(new Date(review.getCreatedAt()))).append("\",\n");
            writer.append("      \"orderId\": \"").append(review.getOrderId() != null ? review.getOrderId() : "").append("\",\n");
            writer.append("      \"vendorReply\": \"").append(review.getVendorReplyText() != null ? escapeJSON(review.getVendorReplyText()) : "").append("\",\n");
            writer.append("      \"replyDate\": \"").append(review.getVendorReplyCreatedAt() > 0 ? 
                dateFormat.format(new Date(review.getVendorReplyCreatedAt())) : "").append("\",\n");
            writer.append("      \"status\": \"").append(review.isFlagged() ? "Flagged" : "Active").append("\"\n");
            writer.append("    }");
            if (i < reviews.size() - 1) {
                writer.append(",");
            }
            writer.append("\n");
        }
        
        writer.append("  ]\n");
        writer.append("}\n");
        
        writer.flush();
        writer.close();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private List<ReviewEntity> getCurrentReviews() {
        // For now, return sample data for testing since ViewModel is not fully implemented
        // In a real implementation, this would get the current filtered reviews from ViewModel
        return createSampleReviews(); // reviewViewModel.getCurrentFilteredReviews();
    }

    private List<ReviewEntity> createSampleReviews() {
        // Create sample reviews for testing export functionality
        List<ReviewEntity> sampleReviews = new java.util.ArrayList<>();
        
        ReviewEntity review1 = new ReviewEntity();
        review1.setReviewId("REV001");
        review1.setCustomerId("USER001");
        review1.setCustomerName("John Doe");
        review1.setVendorId(vendorId);
        review1.setRating(5);
        review1.setText("Excellent food and service! The biryani was amazing.");
        review1.setCreatedAt(System.currentTimeMillis() - 86400000); // 1 day ago
        review1.setOrderId("ORD001");
        review1.setVendorReplyText("Thank you for your kind words!");
        review1.setVendorReplyCreatedAt(System.currentTimeMillis() - 43200000); // 12 hours ago
        review1.setFlagged(false);
        sampleReviews.add(review1);
        
        ReviewEntity review2 = new ReviewEntity();
        review2.setReviewId("REV002");
        review2.setCustomerId("USER002");
        review2.setCustomerName("Jane Smith");
        review2.setVendorId(vendorId);
        review2.setRating(4);
        review2.setText("Good food, but delivery was a bit slow.");
        review2.setCreatedAt(System.currentTimeMillis() - 172800000); // 2 days ago
        review2.setOrderId("ORD002");
        review2.setVendorReplyText("We apologize for the delay. We're working to improve our delivery times.");
        review2.setVendorReplyCreatedAt(System.currentTimeMillis() - 129600000); // 1.5 days ago
        review2.setFlagged(false);
        sampleReviews.add(review2);
        
        ReviewEntity review3 = new ReviewEntity();
        review3.setReviewId("REV003");
        review3.setCustomerId("USER003");
        review3.setCustomerName("Mike Johnson");
        review3.setVendorId(vendorId);
        review3.setRating(3);
        review3.setText("Average experience. Food was okay but could be better.");
        review3.setCreatedAt(System.currentTimeMillis() - 259200000); // 3 days ago
        review3.setOrderId("ORD003");
        review3.setVendorReplyText(null); // No reply yet
        review3.setVendorReplyCreatedAt(0);
        review3.setFlagged(false);
        sampleReviews.add(review3);
        
        return sampleReviews;
    }

    private void showExportSuccessDialog(File exportFile, String format) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Successful")
                .setMessage(format + " file saved to Downloads:\n" + exportFile.getName())
                .setIcon(R.drawable.ic_check_circle)
                .setPositiveButton("Share", (dialog, which) -> shareFile(exportFile))
                .setNegativeButton("OK", null)
                .show();
    }

    private void shareFile(File file) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reviews Export - " + file.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Reviews export from Food Van app");
            
            startActivity(Intent.createChooser(shareIntent, "Share Reviews Export"));
        } catch (Exception e) {
            showError("Failed to share file: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_LONG).show();
    }

    // ReviewAdapter.OnReviewActionListener implementation
    @Override
    public void onReplyClick(ReviewEntity review) {
        // Reply composer is handled in the adapter
    }

    @Override
    public void onEditReplyClick(ReviewEntity review) {
        showEditReplyDialog(review);
    }

    @Override
    public void onDeleteReplyClick(ReviewEntity review) {
        showDeleteReplyConfirmation(review);
    }

    @Override
    public void onFlagReviewClick(ReviewEntity review) {
        showFlagReviewDialog(review);
    }

    @Override
    public void onSoftDeleteReviewClick(ReviewEntity review) {
        showDeleteReviewConfirmation(review);
    }

    @Override
    public void onSendReply(ReviewEntity review, String replyText) {
        if (reviewViewModel != null) {
            String vendorName = sessionManager.getUserName();
            reviewViewModel.addVendorReply(review.getReviewId(), replyText, vendorName);
        }
    }

    @Override
    public void onCancelReply(ReviewEntity review) {
        // Handled in adapter
    }

    @Override
    public void onOrderIdClick(ReviewEntity review) {
        // TODO: Navigate to order details
        Snackbar.make(snackbarAnchor, "Order details: " + review.getOrderId(), Snackbar.LENGTH_SHORT).show();
    }

    private void showEditReplyDialog(ReviewEntity review) {
        // TODO: Implement edit reply dialog
        Snackbar.make(snackbarAnchor, "Edit reply feature coming soon!", Snackbar.LENGTH_SHORT).show();
    }

    private void showDeleteReplyConfirmation(ReviewEntity review) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Reply")
                .setMessage("Are you sure you want to delete your reply? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (reviewViewModel != null) {
                        reviewViewModel.deleteVendorReply(review.getReviewId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFlagReviewDialog(ReviewEntity review) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Flag Review")
                .setMessage("Why are you flagging this review?")
                .setItems(new String[]{"Inappropriate content", "Spam", "Fake review", "Other"}, 
                        (dialog, which) -> {
                            String[] reasons = {"Inappropriate content", "Spam", "Fake review", "Other"};
                            if (reviewViewModel != null) {
                                reviewViewModel.flagReview(review.getReviewId(), reasons[which]);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteReviewConfirmation(ReviewEntity review) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hide Review")
                .setMessage("This will hide the review from public view. Are you sure?")
                .setPositiveButton("Hide", (dialog, which) -> {
                    if (reviewViewModel != null) {
                        reviewViewModel.softDeleteReview(review.getReviewId(), "Hidden by vendor");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
