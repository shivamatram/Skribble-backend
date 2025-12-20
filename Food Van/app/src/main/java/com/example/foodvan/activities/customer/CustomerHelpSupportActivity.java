package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.FAQAdapter;
import com.example.foodvan.adapters.RecentOrderAdapter;
import com.example.foodvan.adapters.SupportTicketAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.SupportContact;
import com.example.foodvan.models.SupportTicket;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.viewmodels.HelpSupportViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomerHelpSupportActivity - Complete Help & Support screen for customers
 * Features: FAQs, Report Issue, Contact Support, Ticket History
 */
public class CustomerHelpSupportActivity extends AppCompatActivity {

    private static final String TAG = "CustomerHelpSupport";

    // ViewModel
    private HelpSupportViewModel viewModel;
    private SessionManager sessionManager;

    // Views
    private MaterialToolbar toolbar;
    private MaterialCardView cardQuickActions, cardContactSupport, cardFaqs, cardRecentOrders, cardTickets;
    private MaterialCardView cardFaqAction, cardReportAction, cardOrderHelpAction, cardContactAction;
    private RecyclerView rvFaqs, rvRecentOrders, rvTickets;
    private ProgressBar progressFaqs, progressTickets;
    private TextView tvFaqEmpty, tvOrdersEmpty, tvWorkingHours;
    private TextView tvAppVersion, tvDeviceModel, tvOsVersion;
    private LinearLayout layoutTicketsEmpty;
    private ImageView ivCloseOrders;
    private MaterialButton btnEmail, btnWhatsapp, btnCall;

    // Adapters
    private FAQAdapter faqAdapter;
    private RecentOrderAdapter recentOrderAdapter;
    private SupportTicketAdapter ticketAdapter;

    // Current state
    private SupportContact currentContact;
    private Order selectedOrder;
    private String attachmentUri;
    private Bitmap attachedBitmap;
    
    // Constants for image selection
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_help_support);

        initializeComponents();
        initializeViews();
        setupToolbar();
        setupAdapters();
        setupClickListeners();
        setupObservers();
        
        // Load all data
        viewModel.loadAllData();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HelpSupportViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // Quick Actions
        cardQuickActions = findViewById(R.id.cardQuickActions);
        cardFaqAction = findViewById(R.id.cardFaqAction);
        cardReportAction = findViewById(R.id.cardReportAction);
        cardOrderHelpAction = findViewById(R.id.cardOrderHelpAction);
        cardContactAction = findViewById(R.id.cardContactAction);
        
        // Contact Support
        cardContactSupport = findViewById(R.id.cardContactSupport);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);
        btnEmail = findViewById(R.id.btnEmail);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnCall = findViewById(R.id.btnCall);
        
        // FAQs
        cardFaqs = findViewById(R.id.cardFaqs);
        rvFaqs = findViewById(R.id.rvFaqs);
        progressFaqs = findViewById(R.id.progressFaqs);
        tvFaqEmpty = findViewById(R.id.tvFaqEmpty);
        
        // Recent Orders
        cardRecentOrders = findViewById(R.id.cardRecentOrders);
        rvRecentOrders = findViewById(R.id.rvRecentOrders);
        tvOrdersEmpty = findViewById(R.id.tvOrdersEmpty);
        ivCloseOrders = findViewById(R.id.ivCloseOrders);
        
        // Tickets
        cardTickets = findViewById(R.id.cardTickets);
        rvTickets = findViewById(R.id.rvTickets);
        progressTickets = findViewById(R.id.progressTickets);
        layoutTicketsEmpty = findViewById(R.id.layoutTicketsEmpty);
        
        // System Info
        tvAppVersion = findViewById(R.id.tvAppVersion);
        tvDeviceModel = findViewById(R.id.tvDeviceModel);
        tvOsVersion = findViewById(R.id.tvOsVersion);
        
        // Set system info
        updateSystemInfo();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupAdapters() {
        // FAQ Adapter
        faqAdapter = new FAQAdapter(position -> {
            // Toggle is handled in adapter
        });
        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        rvFaqs.setAdapter(faqAdapter);
        rvFaqs.setNestedScrollingEnabled(false);

        // Recent Order Adapter
        recentOrderAdapter = new RecentOrderAdapter(order -> {
            selectedOrder = order;
            showReportIssueDialog(order);
        });
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvRecentOrders.setAdapter(recentOrderAdapter);
        rvRecentOrders.setNestedScrollingEnabled(false);

        // Ticket Adapter
        ticketAdapter = new SupportTicketAdapter(ticket -> {
            showTicketDetailsDialog(ticket);
        });
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(ticketAdapter);
        rvTickets.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        // Quick Actions
        cardFaqAction.setOnClickListener(v -> scrollToFaqs());
        cardReportAction.setOnClickListener(v -> showReportIssueDialog(null));
        cardOrderHelpAction.setOnClickListener(v -> showRecentOrders());
        cardContactAction.setOnClickListener(v -> scrollToContact());
        
        // Close Recent Orders
        ivCloseOrders.setOnClickListener(v -> hideRecentOrders());
        
        // Contact Buttons
        btnEmail.setOnClickListener(v -> openEmailSupport());
        btnWhatsapp.setOnClickListener(v -> openWhatsAppSupport());
        btnCall.setOnClickListener(v -> callSupport());
    }

    private void setupObservers() {
        // FAQs
        viewModel.getFaqs().observe(this, faqs -> {
            if (faqs != null && !faqs.isEmpty()) {
                faqAdapter.setFaqs(faqs);
                tvFaqEmpty.setVisibility(View.GONE);
                rvFaqs.setVisibility(View.VISIBLE);
            } else {
                tvFaqEmpty.setVisibility(View.VISIBLE);
                rvFaqs.setVisibility(View.GONE);
            }
        });

        viewModel.getFaqsLoading().observe(this, loading -> {
            progressFaqs.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Support Contact
        viewModel.getSupportContact().observe(this, contact -> {
            if (contact != null) {
                currentContact = contact;
                updateContactUI(contact);
            }
        });

        // Tickets
        viewModel.getTickets().observe(this, tickets -> {
            if (tickets != null && !tickets.isEmpty()) {
                ticketAdapter.setTickets(tickets);
                layoutTicketsEmpty.setVisibility(View.GONE);
                rvTickets.setVisibility(View.VISIBLE);
            } else {
                layoutTicketsEmpty.setVisibility(View.VISIBLE);
                rvTickets.setVisibility(View.GONE);
            }
        });

        viewModel.getTicketsLoading().observe(this, loading -> {
            progressTickets.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Recent Orders
        viewModel.getRecentOrders().observe(this, orders -> {
            if (orders != null && !orders.isEmpty()) {
                recentOrderAdapter.setOrders(orders);
                tvOrdersEmpty.setVisibility(View.GONE);
                rvRecentOrders.setVisibility(View.VISIBLE);
            } else {
                tvOrdersEmpty.setVisibility(View.VISIBLE);
                rvRecentOrders.setVisibility(View.GONE);
            }
        });

        // Submit Success
        viewModel.getSubmitSuccess().observe(this, ticketId -> {
            if (ticketId != null && !ticketId.isEmpty()) {
                showSnackbar("Issue submitted successfully. Ticket ID: #" + ticketId.substring(0, 8));
                viewModel.clearSubmitSuccess();
            }
        });

        // Submit Error
        viewModel.getSubmitError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar("Error: " + error);
                viewModel.clearSubmitError();
            }
        });
    }

    private void updateContactUI(SupportContact contact) {
        if (contact.getWorkingHours() != null && !contact.getWorkingHours().isEmpty()) {
            tvWorkingHours.setText("Working Hours: " + contact.getWorkingHours());
        }
        
        btnEmail.setEnabled(contact.hasEmail());
        btnWhatsapp.setEnabled(contact.hasWhatsapp());
        btnCall.setEnabled(contact.hasPhone());
    }

    private void updateSystemInfo() {
        // App Version
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvAppVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("1.0.0");
        }

        // Device Model
        tvDeviceModel.setText(Build.MANUFACTURER + " " + Build.MODEL);

        // OS Version
        tvOsVersion.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
    }

    // ==================== Quick Actions ====================

    private void scrollToFaqs() {
        cardFaqs.post(() -> {
            findViewById(R.id.nestedScrollView).scrollTo(0, cardFaqs.getTop());
        });
    }

    private void scrollToContact() {
        cardContactSupport.post(() -> {
            findViewById(R.id.nestedScrollView).scrollTo(0, cardContactSupport.getTop());
        });
    }

    private void showRecentOrders() {
        cardRecentOrders.setVisibility(View.VISIBLE);
        cardRecentOrders.post(() -> {
            findViewById(R.id.nestedScrollView).scrollTo(0, cardRecentOrders.getTop());
        });
    }

    private void hideRecentOrders() {
        cardRecentOrders.setVisibility(View.GONE);
    }

    // ==================== Report Issue Dialog ====================

    private void showReportIssueDialog(Order prefilledOrder) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_issue, null);
        
        // Get dialog views
        TextInputLayout tilOrder = dialogView.findViewById(R.id.tilOrder);
        AutoCompleteTextView actvOrder = dialogView.findViewById(R.id.actvOrder);
        TextInputLayout tilCategory = dialogView.findViewById(R.id.tilCategory);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputLayout tilDescription = dialogView.findViewById(R.id.tilDescription);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        LinearLayout layoutOrderWarning = dialogView.findViewById(R.id.layoutOrderWarning);
        LinearLayout layoutSubmitting = dialogView.findViewById(R.id.layoutSubmitting);
        LinearLayout layoutButtons = dialogView.findViewById(R.id.layoutButtons);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        MaterialCardView cardAttachment = dialogView.findViewById(R.id.cardAttachment);
        MaterialCardView cardAttachedImage = dialogView.findViewById(R.id.cardAttachedImage);
        ImageView ivAttachedPreview = dialogView.findViewById(R.id.ivAttachedPreview);
        ImageButton btnRemoveImage = dialogView.findViewById(R.id.btnRemoveImage);

        // Setup category dropdown
        String[] categories = SupportTicket.getAllCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);

        // Setup order dropdown
        List<Order> orders = viewModel.getRecentOrders().getValue();
        if (orders != null && !orders.isEmpty()) {
            List<String> orderStrings = new ArrayList<>();
            orderStrings.add("None");
            for (Order order : orders) {
                String orderId = order.getOrderId();
                if (orderId != null && orderId.length() > 8) {
                    orderId = orderId.substring(0, 8);
                }
                orderStrings.add("#" + orderId + " - " + order.getVanName());
            }
            ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, orderStrings);
            actvOrder.setAdapter(orderAdapter);
        }

        // Pre-fill order if provided
        if (prefilledOrder != null) {
            String orderId = prefilledOrder.getOrderId();
            if (orderId != null && orderId.length() > 8) {
                orderId = orderId.substring(0, 8);
            }
            actvOrder.setText("#" + orderId + " - " + prefilledOrder.getVanName(), false);
            selectedOrder = prefilledOrder;
        }

        // Restore saved form state
        String savedDescription = viewModel.getReportDescription().getValue();
        String savedCategory = viewModel.getReportCategory().getValue();
        String savedAttachmentUri = viewModel.getAttachmentUri().getValue();
        
        if (savedDescription != null) {
            etDescription.setText(savedDescription);
        }
        if (savedCategory != null) {
            actvCategory.setText(savedCategory, false);
        }
        
        // Restore attachment if exists
        if (savedAttachmentUri != null && !savedAttachmentUri.isEmpty()) {
            attachmentUri = savedAttachmentUri;
            showAttachedImage(cardAttachment, cardAttachedImage, ivAttachedPreview);
        }

        // Show warning for payment/order issues
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (SupportTicket.CATEGORY_PAYMENT.equals(selected) || 
                SupportTicket.CATEGORY_WRONG_ORDER.equals(selected) ||
                SupportTicket.CATEGORY_LATE_DELIVERY.equals(selected)) {
                layoutOrderWarning.setVisibility(View.VISIBLE);
            } else {
                layoutOrderWarning.setVisibility(View.GONE);
            }
        });

        // Attachment click handler
        cardAttachment.setOnClickListener(v -> {
            showImageSelectionDialog();
        });

        // Remove image handler
        btnRemoveImage.setOnClickListener(v -> {
            attachmentUri = null;
            attachedBitmap = null;
            cardAttachedImage.setVisibility(View.GONE);
            cardAttachment.setVisibility(View.VISIBLE);
        });

        // Create dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            // Save form state before closing
            viewModel.saveFormState(
                etDescription.getText() != null ? etDescription.getText().toString() : "",
                actvCategory.getText().toString(),
                selectedOrder != null ? selectedOrder.getOrderId() : null,
                attachmentUri
            );
            dialog.dismiss();
        });

        // Submit button
        btnSubmit.setOnClickListener(v -> {
            // Validate
            String category = actvCategory.getText().toString().trim();
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

            if (category.isEmpty()) {
                tilCategory.setError("Please select a category");
                return;
            }
            tilCategory.setError(null);

            if (description.isEmpty()) {
                tilDescription.setError("Please describe your issue");
                return;
            }
            if (description.length() < 10) {
                tilDescription.setError("Description is too short");
                return;
            }
            tilDescription.setError(null);

            // Get order ID
            String orderId = null;
            String vendorId = null;
            String vendorName = null;
            
            // Parse selected order
            String orderSelection = actvOrder.getText().toString();
            if (!orderSelection.isEmpty() && !orderSelection.equals("None")) {
                // Find the order from the list
                List<Order> ordersList = viewModel.getRecentOrders().getValue();
                if (ordersList != null) {
                    for (Order order : ordersList) {
                        String shortId = order.getOrderId();
                        if (shortId != null && shortId.length() > 8) {
                            shortId = shortId.substring(0, 8);
                        }
                        if (orderSelection.contains(shortId)) {
                            orderId = order.getOrderId();
                            vendorId = order.getVendorId();
                            vendorName = order.getVanName();
                            break;
                        }
                    }
                }
            }

            // Show loading state
            layoutButtons.setVisibility(View.GONE);
            layoutSubmitting.setVisibility(View.VISIBLE);

            // Get customer info
            String customerName = sessionManager.getUserName();
            String customerEmail = sessionManager.getUserEmail();

            // Submit ticket
            viewModel.submitTicket(
                category,
                description,
                orderId,
                vendorId,
                vendorName,
                customerName,
                customerEmail
            );

            // Observe submit result
            viewModel.getSubmitting().observe(this, submitting -> {
                if (!submitting) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // ==================== Ticket Details Dialog ====================

    private void showTicketDetailsDialog(SupportTicket ticket) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Ticket " + ticket.getShortTicketId());
        
        StringBuilder details = new StringBuilder();
        details.append("Status: ").append(ticket.getStatus()).append("\n\n");
        details.append("Category: ").append(ticket.getCategory()).append("\n\n");
        details.append("Description:\n").append(ticket.getDescription()).append("\n\n");
        details.append("Created: ").append(ticket.getFormattedCreatedDate()).append("\n");
        details.append("Updated: ").append(ticket.getFormattedUpdatedDate());
        
        if (ticket.hasOrderContext()) {
            details.append("\n\nOrder ID: ").append(ticket.getOrderId());
        }
        
        if (ticket.getResponse() != null && !ticket.getResponse().isEmpty()) {
            details.append("\n\n--- Support Response ---\n").append(ticket.getResponse());
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // ==================== Contact Support Actions ====================

    private void openEmailSupport() {
        if (currentContact == null || !currentContact.hasEmail()) {
            showSnackbar("Email support is not available");
            return;
        }

        String subject = "Support Request from Food Van App";
        String body = buildEmailBody();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + currentContact.getEmail()));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            showSnackbar("No email app found");
        }
    }

    private String buildEmailBody() {
        StringBuilder body = new StringBuilder();
        body.append("Hello Food Van Support,\n\n");
        body.append("I need help with...\n\n");
        body.append("---\n");
        body.append("App Version: ").append(tvAppVersion.getText()).append("\n");
        body.append("Device: ").append(tvDeviceModel.getText()).append("\n");
        body.append("OS: ").append(tvOsVersion.getText()).append("\n");
        body.append("User ID: ").append(sessionManager.getUserId()).append("\n");
        return body.toString();
    }

    private void openWhatsAppSupport() {
        if (currentContact == null || !currentContact.hasWhatsapp()) {
            showSnackbar("WhatsApp support is not available");
            return;
        }

        String url = currentContact.getWhatsappUrl();
        String message = "Hello, I need help with the Food Van App.";
        url += "?text=" + Uri.encode(message);

        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
        whatsappIntent.setData(Uri.parse(url));

        try {
            startActivity(whatsappIntent);
        } catch (Exception e) {
            showSnackbar("WhatsApp is not installed");
        }
    }

    private void callSupport() {
        if (currentContact == null || !currentContact.hasPhone()) {
            showSnackbar("Phone support is not available");
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse(currentContact.getPhoneUri()));

        try {
            startActivity(callIntent);
        } catch (Exception e) {
            showSnackbar("Unable to make call");
        }
    }

    // ==================== Image Attachment Methods ====================

    private void showImageSelectionDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Attach Screenshot")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take photo
                        dispatchTakePictureIntent();
                    } else {
                        // Choose from gallery
                        dispatchSelectPictureIntent();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            showSnackbar("Camera app not found");
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (selectPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(selectPictureIntent, REQUEST_IMAGE_SELECT);
        } else {
            showSnackbar("Gallery app not found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                // Handle captured image
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    attachedBitmap = imageBitmap;
                    attachmentUri = "captured_image"; // Placeholder
                    // Show the attached image in the dialog
                    showSnackbar("Screenshot attached successfully");
                }
            } else if (requestCode == REQUEST_IMAGE_SELECT && data != null && data.getData() != null) {
                // Handle selected image
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    attachmentUri = selectedImageUri.toString();
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), selectedImageUri);
                            attachedBitmap = ImageDecoder.decodeBitmap(source);
                        } else {
                            attachedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        }
                        showSnackbar("Screenshot attached successfully");
                    } catch (Exception e) {
                        showSnackbar("Error loading image: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void showAttachedImage(MaterialCardView cardAttachment, MaterialCardView cardAttachedImage, ImageView ivAttachedPreview) {
        if (attachedBitmap != null) {
            cardAttachment.setVisibility(View.GONE);
            cardAttachedImage.setVisibility(View.VISIBLE);
            ivAttachedPreview.setImageBitmap(attachedBitmap);
        }
    }

    // ==================== Utility Methods ====================

    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
