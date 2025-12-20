package com.example.foodvan.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * PdfExportUtil - Professional PDF export utility for vendor analytics
 * Generates comprehensive analytics reports in PDF format
 */
public class PdfExportUtil {

    private static final String PDF_DIRECTORY = "FoodVan_Reports";
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(255, 107, 53); // #FF6B35
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(52, 58, 64); // #343A40

    /**
     * Analytics data model for PDF export
     */
    public static class AnalyticsData {
        public double todayEarnings;
        public double weekEarnings;
        public double monthEarnings;
        public int totalOrders;
        public int completedOrders;
        public int pendingOrders;
        public int cancelledOrders;
        public double avgOrderValue;
        public double customerRating;
        public int completionRate;
        public String highestSellingItem;
        public String peakHours;

        public AnalyticsData(double todayEarnings, double weekEarnings, double monthEarnings,
                           int totalOrders, int completedOrders, int pendingOrders, int cancelledOrders,
                           double avgOrderValue, double customerRating, int completionRate,
                           String highestSellingItem, String peakHours) {
            this.todayEarnings = todayEarnings;
            this.weekEarnings = weekEarnings;
            this.monthEarnings = monthEarnings;
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.pendingOrders = pendingOrders;
            this.cancelledOrders = cancelledOrders;
            this.avgOrderValue = avgOrderValue;
            this.customerRating = customerRating;
            this.completionRate = completionRate;
            this.highestSellingItem = highestSellingItem;
            this.peakHours = peakHours;
        }
    }

    /**
     * Export analytics data to PDF
     */
    public static void exportToPdf(Context context, AnalyticsData data, ExportCallback callback) {
        try {
            // Create directory if not exists
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), PDF_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "vendor_analytics_report_" + timestamp + ".pdf";
            File pdfFile = new File(directory, fileName);

            // Create PDF document
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add header
            addHeader(document);

            // Add earnings summary
            addEarningsSummary(document, data);

            // Add order insights
            addOrderInsights(document, data);

            // Add performance metrics
            addPerformanceMetrics(document, data);

            // Add footer
            addFooter(document);

            // Close document
            document.close();

            // Success callback
            if (callback != null) {
                callback.onSuccess(pdfFile);
            }
            
            // Open file
            openPdfFile(context, pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Failed to export PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Add header to PDF document
     */
    private static void addHeader(Document document) {
        // Title
        Paragraph title = new Paragraph("Food Van Analytics Report")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(title);

        // Subtitle with date
        String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        Paragraph subtitle = new Paragraph("Generated on " + currentDate)
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    /**
     * Add earnings summary section
     */
    private static void addEarningsSummary(Document document, AnalyticsData data) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Earnings Summary")
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Create earnings table
        Table earningsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Table headers
        earningsTable.addHeaderCell(createHeaderCell("Today"));
        earningsTable.addHeaderCell(createHeaderCell("This Week"));
        earningsTable.addHeaderCell(createHeaderCell("This Month"));

        // Table data
        earningsTable.addCell(createDataCell("₹" + String.format("%.2f", data.todayEarnings)));
        earningsTable.addCell(createDataCell("₹" + String.format("%.2f", data.weekEarnings)));
        earningsTable.addCell(createDataCell("₹" + String.format("%.2f", data.monthEarnings)));

        document.add(earningsTable);
    }

    /**
     * Add order insights section
     */
    private static void addOrderInsights(Document document, AnalyticsData data) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Order Insights")
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Create order insights table
        Table orderTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Table headers
        orderTable.addHeaderCell(createHeaderCell("Total Orders"));
        orderTable.addHeaderCell(createHeaderCell("Completed"));
        orderTable.addHeaderCell(createHeaderCell("Pending"));
        orderTable.addHeaderCell(createHeaderCell("Cancelled"));

        // Table data
        orderTable.addCell(createDataCell(String.valueOf(data.totalOrders)));
        orderTable.addCell(createDataCell(String.valueOf(data.completedOrders)));
        orderTable.addCell(createDataCell(String.valueOf(data.pendingOrders)));
        orderTable.addCell(createDataCell(String.valueOf(data.cancelledOrders)));

        document.add(orderTable);
    }

    /**
     * Add performance metrics section
     */
    private static void addPerformanceMetrics(Document document, AnalyticsData data) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Performance Metrics")
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Create metrics table
        Table metricsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Add metrics
        metricsTable.addCell(createMetricCell("Average Order Value"));
        metricsTable.addCell(createDataCell("₹" + String.format("%.2f", data.avgOrderValue)));

        metricsTable.addCell(createMetricCell("Customer Rating"));
        metricsTable.addCell(createDataCell(String.format("%.1f ★", data.customerRating)));

        metricsTable.addCell(createMetricCell("Order Completion Rate"));
        metricsTable.addCell(createDataCell(data.completionRate + "%"));

        metricsTable.addCell(createMetricCell("Highest Selling Item"));
        metricsTable.addCell(createDataCell(data.highestSellingItem));

        metricsTable.addCell(createMetricCell("Peak Business Hours"));
        metricsTable.addCell(createDataCell(data.peakHours));

        document.add(metricsTable);
    }

    /**
     * Add footer to PDF document
     */
    private static void addFooter(Document document) {
        Paragraph footer = new Paragraph("Generated by Food Van Analytics System")
                .setFontSize(10)
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        document.add(footer);
    }

    /**
     * Create header cell for tables
     */
    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    /**
     * Create data cell for tables
     */
    private static Cell createDataCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    /**
     * Create metric label cell
     */
    private static Cell createMetricCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setPadding(8);
    }

    /**
     * Open PDF file with default viewer
     */
    private static void openPdfFile(Context context, File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(context, 
                context.getPackageName() + ".fileprovider", pdfFile);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No PDF viewer found. File saved to: " + pdfFile.getAbsolutePath(), 
                Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Callback interface for export operations
     */
    public interface ExportCallback {
        void onSuccess(File file);
        void onError(String error);
    }
}
