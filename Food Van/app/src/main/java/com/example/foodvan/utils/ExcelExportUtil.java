package com.example.foodvan.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ExcelExportUtil - Professional Excel export utility for vendor analytics
 * Generates comprehensive analytics data in Excel format (.xlsx)
 */
public class ExcelExportUtil {

    private static final String EXCEL_DIRECTORY = "FoodVan_Reports";

    /**
     * Daily analytics data for Excel export
     */
    public static class DailyData {
        public String date;
        public int orders;
        public double earnings;
        public int completedOrders;
        public int cancelledOrders;

        public DailyData(String date, int orders, double earnings, int completedOrders, int cancelledOrders) {
            this.date = date;
            this.orders = orders;
            this.earnings = earnings;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
        }
    }

    /**
     * Summary analytics data
     */
    public static class SummaryData {
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

        public SummaryData(double todayEarnings, double weekEarnings, double monthEarnings,
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
     * Export analytics data to Excel
     */
    public static void exportToExcel(Context context, SummaryData summaryData, List<DailyData> dailyDataList, ExportCallback callback) {
        try {
            // Create directory if not exists
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXCEL_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "vendor_analytics_data_" + timestamp + ".xlsx";
            File excelFile = new File(directory, fileName);

            // Create workbook
            Workbook workbook = new XSSFWorkbook();

            // Create summary sheet
            createSummarySheet(workbook, summaryData);

            // Create daily data sheet
            createDailyDataSheet(workbook, dailyDataList);

            // Create charts sheet (optional)
            createChartsSheet(workbook, dailyDataList);

            // Write to file
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // Success callback
            if (callback != null) {
                callback.onSuccess(excelFile);
            }

            // Open file
            openExcelFile(context, excelFile);

        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Failed to export Excel: " + e.getMessage());
            }
        }
    }

    /**
     * Create summary sheet with key metrics
     */
    private static void createSummarySheet(Workbook workbook, SummaryData data) {
        Sheet sheet = workbook.createSheet("Analytics Summary");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Food Van Analytics Summary");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // Date
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Generated on: " + new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date()));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        rowNum++; // Empty row

        // Earnings Summary
        Row earningsHeaderRow = sheet.createRow(rowNum++);
        earningsHeaderRow.createCell(0).setCellValue("Earnings Summary");
        earningsHeaderRow.getCell(0).setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Row earningsLabelsRow = sheet.createRow(rowNum++);
        earningsLabelsRow.createCell(0).setCellValue("Period");
        earningsLabelsRow.createCell(1).setCellValue("Today");
        earningsLabelsRow.createCell(2).setCellValue("This Week");
        earningsLabelsRow.createCell(3).setCellValue("This Month");
        for (int i = 0; i < 4; i++) {
            earningsLabelsRow.getCell(i).setCellStyle(headerStyle);
        }

        Row earningsDataRow = sheet.createRow(rowNum++);
        earningsDataRow.createCell(0).setCellValue("Earnings (₹)");
        earningsDataRow.createCell(1).setCellValue(data.todayEarnings);
        earningsDataRow.createCell(2).setCellValue(data.weekEarnings);
        earningsDataRow.createCell(3).setCellValue(data.monthEarnings);
        for (int i = 0; i < 4; i++) {
            earningsDataRow.getCell(i).setCellStyle(dataStyle);
        }

        rowNum++; // Empty row

        // Order Summary
        Row orderHeaderRow = sheet.createRow(rowNum++);
        orderHeaderRow.createCell(0).setCellValue("Order Summary");
        orderHeaderRow.getCell(0).setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Row orderLabelsRow = sheet.createRow(rowNum++);
        orderLabelsRow.createCell(0).setCellValue("Metric");
        orderLabelsRow.createCell(1).setCellValue("Total");
        orderLabelsRow.createCell(2).setCellValue("Completed");
        orderLabelsRow.createCell(3).setCellValue("Pending");
        for (int i = 0; i < 4; i++) {
            orderLabelsRow.getCell(i).setCellStyle(headerStyle);
        }

        Row orderDataRow = sheet.createRow(rowNum++);
        orderDataRow.createCell(0).setCellValue("Orders");
        orderDataRow.createCell(1).setCellValue(data.totalOrders);
        orderDataRow.createCell(2).setCellValue(data.completedOrders);
        orderDataRow.createCell(3).setCellValue(data.pendingOrders);
        for (int i = 0; i < 4; i++) {
            orderDataRow.getCell(i).setCellStyle(dataStyle);
        }

        rowNum++; // Empty row

        // Performance Metrics
        Row perfHeaderRow = sheet.createRow(rowNum++);
        perfHeaderRow.createCell(0).setCellValue("Performance Metrics");
        perfHeaderRow.getCell(0).setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 1));

        addMetricRow(sheet, rowNum++, "Average Order Value", "₹" + String.format("%.2f", data.avgOrderValue), headerStyle, dataStyle);
        addMetricRow(sheet, rowNum++, "Customer Rating", String.format("%.1f ★", data.customerRating), headerStyle, dataStyle);
        addMetricRow(sheet, rowNum++, "Completion Rate", data.completionRate + "%", headerStyle, dataStyle);
        addMetricRow(sheet, rowNum++, "Highest Selling Item", data.highestSellingItem, headerStyle, dataStyle);
        addMetricRow(sheet, rowNum++, "Peak Business Hours", data.peakHours, headerStyle, dataStyle);

        // Set column widths manually (autoSizeColumn doesn't work on Android)
        sheet.setColumnWidth(0, 4000); // 20 characters
        sheet.setColumnWidth(1, 3000); // 15 characters
        sheet.setColumnWidth(2, 3000); // 15 characters
        sheet.setColumnWidth(3, 3000); // 15 characters
    }

    /**
     * Create daily data sheet with detailed analytics
     */
    private static void createDailyDataSheet(Workbook workbook, List<DailyData> dailyDataList) {
        Sheet sheet = workbook.createSheet("Daily Analytics");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Daily Analytics Data");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        rowNum++; // Empty row

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("Total Orders");
        headerRow.createCell(2).setCellValue("Earnings (₹)");
        headerRow.createCell(3).setCellValue("Completed Orders");
        headerRow.createCell(4).setCellValue("Cancelled Orders");

        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // Data rows
        if (dailyDataList == null || dailyDataList.isEmpty()) {
            // Generate sample data for last 30 days
            dailyDataList = generateSampleDailyData();
        }

        for (DailyData dailyData : dailyDataList) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(dailyData.date);
            dataRow.createCell(1).setCellValue(dailyData.orders);
            dataRow.createCell(2).setCellValue(dailyData.earnings);
            dataRow.createCell(3).setCellValue(dailyData.completedOrders);
            dataRow.createCell(4).setCellValue(dailyData.cancelledOrders);

            for (int i = 0; i < 5; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Set column widths manually (autoSizeColumn doesn't work on Android)
        sheet.setColumnWidth(0, 3000); // Date column
        sheet.setColumnWidth(1, 3000); // Orders column
        sheet.setColumnWidth(2, 4000); // Earnings column
        sheet.setColumnWidth(3, 3500); // Completed column
        sheet.setColumnWidth(4, 3500); // Cancelled column
    }

    /**
     * Create charts sheet with summary statistics
     */
    private static void createChartsSheet(Workbook workbook, List<DailyData> dailyDataList) {
        Sheet sheet = workbook.createSheet("Charts & Statistics");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Analytics Charts & Statistics");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        rowNum++; // Empty row

        // Calculate statistics
        if (dailyDataList != null && !dailyDataList.isEmpty()) {
            double totalEarnings = dailyDataList.stream().mapToDouble(d -> d.earnings).sum();
            int totalOrders = dailyDataList.stream().mapToInt(d -> d.orders).sum();
            double avgDailyEarnings = totalEarnings / dailyDataList.size();
            double avgDailyOrders = (double) totalOrders / dailyDataList.size();

            addMetricRow(sheet, rowNum++, "Total Period Earnings", "₹" + String.format("%.2f", totalEarnings), headerStyle, dataStyle);
            addMetricRow(sheet, rowNum++, "Total Period Orders", String.valueOf(totalOrders), headerStyle, dataStyle);
            addMetricRow(sheet, rowNum++, "Average Daily Earnings", "₹" + String.format("%.2f", avgDailyEarnings), headerStyle, dataStyle);
            addMetricRow(sheet, rowNum++, "Average Daily Orders", String.format("%.1f", avgDailyOrders), headerStyle, dataStyle);
        }

        // Set column widths manually (autoSizeColumn doesn't work on Android)
        sheet.setColumnWidth(0, 5000); // Metric name column
        sheet.setColumnWidth(1, 4000); // Value column
        sheet.setColumnWidth(2, 3000); // Extra column
    }

    /**
     * Generate sample daily data for demonstration
     */
    private static List<DailyData> generateSampleDailyData() {
        java.util.List<DailyData> sampleData = new java.util.ArrayList<>();
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

            sampleData.add(new DailyData(date, orders, earnings, completed, cancelled));
        }

        return sampleData;
    }

    /**
     * Add metric row to sheet
     */
    private static void addMetricRow(Sheet sheet, int rowNum, String label, String value, CellStyle headerStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(dataStyle);
    }

    /**
     * Create header cell style
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create title cell style
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create data cell style
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Open Excel file with default viewer
     */
    private static void openExcelFile(Context context, File excelFile) {
        try {
            Uri excelUri = FileProvider.getUriForFile(context, 
                context.getPackageName() + ".fileprovider", excelFile);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(excelUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No Excel viewer found. File saved to: " + excelFile.getAbsolutePath(), 
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
