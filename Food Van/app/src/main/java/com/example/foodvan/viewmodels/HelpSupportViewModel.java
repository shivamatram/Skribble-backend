package com.example.foodvan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.FAQ;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.SupportContact;
import com.example.foodvan.models.SupportTicket;
import com.example.foodvan.repositories.HelpSupportRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Help & Support screen
 * Manages state for FAQs, support tickets, and contact information
 */
public class HelpSupportViewModel extends AndroidViewModel {

    private static final String TAG = "HelpSupportViewModel";

    private final HelpSupportRepository repository;

    // LiveData for FAQs
    private final MutableLiveData<List<FAQ>> faqs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> faqsLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> faqsError = new MutableLiveData<>();

    // LiveData for Support Contact
    private final MutableLiveData<SupportContact> supportContact = new MutableLiveData<>();
    private final MutableLiveData<Boolean> contactLoading = new MutableLiveData<>(false);

    // LiveData for Tickets
    private final MutableLiveData<List<SupportTicket>> tickets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> ticketsLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> ticketsError = new MutableLiveData<>();

    // LiveData for Recent Orders
    private final MutableLiveData<List<Order>> recentOrders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> ordersLoading = new MutableLiveData<>(false);

    // LiveData for Ticket Submission
    private final MutableLiveData<Boolean> submitting = new MutableLiveData<>(false);
    private final MutableLiveData<String> submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> submitError = new MutableLiveData<>();

    // Form state (in-memory, survives config changes via ViewModel)
    private final MutableLiveData<String> reportDescription = new MutableLiveData<>();
    private final MutableLiveData<String> reportCategory = new MutableLiveData<>();
    private final MutableLiveData<String> selectedOrderId = new MutableLiveData<>();
    private final MutableLiveData<String> attachmentUri = new MutableLiveData<>();

    public HelpSupportViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HelpSupportRepository(application);
    }

    /**
     * Save form state
     */
    public void saveFormState(String description, String category, String orderId, String attachmentUri) {
        reportDescription.setValue(description);
        reportCategory.setValue(category);
        selectedOrderId.setValue(orderId);
        this.attachmentUri.setValue(attachmentUri);
    }

    /**
     * Clear form state after successful submission
     */
    public void clearFormState() {
        reportDescription.setValue(null);
        reportCategory.setValue(null);
        selectedOrderId.setValue(null);
        attachmentUri.setValue(null);
    }

    // ==================== Load Data Methods ====================

    /**
     * Load FAQs from repository
     */
    public void loadFAQs() {
        faqsLoading.setValue(true);
        faqsError.setValue(null);

        repository.loadFAQs(new HelpSupportRepository.OnFAQsLoadListener() {
            @Override
            public void onSuccess(List<FAQ> faqList) {
                faqs.postValue(faqList);
                faqsLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                faqsError.postValue(error);
                faqsLoading.postValue(false);
            }
        });
    }

    /**
     * Load support contact information
     */
    public void loadSupportContact() {
        contactLoading.setValue(true);

        repository.loadSupportContact(new HelpSupportRepository.OnSupportContactLoadListener() {
            @Override
            public void onSuccess(SupportContact contact) {
                supportContact.postValue(contact);
                contactLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                // Use default contact on error
                supportContact.postValue(SupportContact.getDefaultContact());
                contactLoading.postValue(false);
            }
        });
    }

    /**
     * Load customer's support tickets
     */
    public void loadTickets() {
        ticketsLoading.setValue(true);
        ticketsError.setValue(null);

        repository.loadCustomerTickets(new HelpSupportRepository.OnTicketsLoadListener() {
            @Override
            public void onSuccess(List<SupportTicket> ticketList) {
                tickets.postValue(ticketList);
                ticketsLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                ticketsError.postValue(error);
                ticketsLoading.postValue(false);
            }
        });
    }

    /**
     * Load recent orders for report issue form
     */
    public void loadRecentOrders() {
        ordersLoading.setValue(true);

        repository.loadRecentOrders(5, new HelpSupportRepository.OnOrdersLoadListener() {
            @Override
            public void onSuccess(List<Order> orderList) {
                recentOrders.postValue(orderList);
                ordersLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                ordersLoading.postValue(false);
            }
        });
    }

    /**
     * Load all data for Help & Support screen
     */
    public void loadAllData() {
        loadFAQs();
        loadSupportContact();
        loadTickets();
        loadRecentOrders();
    }

    // ==================== Submit Ticket ====================

    /**
     * Submit a new support ticket
     */
    public void submitTicket(String category, String description, String orderId, 
                             String vendorId, String vendorName, 
                             String customerName, String customerEmail) {
        submitting.setValue(true);
        submitError.setValue(null);
        submitSuccess.setValue(null);

        SupportTicket ticket = new SupportTicket();
        ticket.setCategory(category);
        ticket.setDescription(description);
        ticket.setOrderId(orderId);
        ticket.setVendorId(vendorId);
        ticket.setVendorName(vendorName);
        ticket.setCustomerName(customerName);
        ticket.setCustomerEmail(customerEmail);

        repository.submitTicket(ticket, new HelpSupportRepository.OnTicketSubmitListener() {
            @Override
            public void onSuccess(String ticketId) {
                submitSuccess.postValue(ticketId);
                submitting.postValue(false);
                clearFormState();
                // Reload tickets after successful submission
                loadTickets();
            }

            @Override
            public void onError(String error) {
                submitError.postValue(error);
                submitting.postValue(false);
            }
        });
    }

    // ==================== FAQ Expand/Collapse ====================

    /**
     * Toggle FAQ expanded state
     */
    public void toggleFAQ(int position) {
        List<FAQ> currentFaqs = faqs.getValue();
        if (currentFaqs != null && position >= 0 && position < currentFaqs.size()) {
            currentFaqs.get(position).toggleExpanded();
            faqs.setValue(currentFaqs);
        }
    }

    // ==================== Getters for LiveData ====================

    public LiveData<List<FAQ>> getFaqs() {
        return faqs;
    }

    public LiveData<Boolean> getFaqsLoading() {
        return faqsLoading;
    }

    public LiveData<String> getFaqsError() {
        return faqsError;
    }

    public LiveData<SupportContact> getSupportContact() {
        return supportContact;
    }

    public LiveData<Boolean> getContactLoading() {
        return contactLoading;
    }

    public LiveData<List<SupportTicket>> getTickets() {
        return tickets;
    }

    public LiveData<Boolean> getTicketsLoading() {
        return ticketsLoading;
    }

    public LiveData<String> getTicketsError() {
        return ticketsError;
    }

    public LiveData<List<Order>> getRecentOrders() {
        return recentOrders;
    }

    public LiveData<Boolean> getOrdersLoading() {
        return ordersLoading;
    }

    public LiveData<Boolean> getSubmitting() {
        return submitting;
    }

    public LiveData<String> getSubmitSuccess() {
        return submitSuccess;
    }

    public LiveData<String> getSubmitError() {
        return submitError;
    }

    // Form state getters
    public LiveData<String> getReportDescription() {
        return reportDescription;
    }

    public LiveData<String> getReportCategory() {
        return reportCategory;
    }

    public LiveData<String> getSelectedOrderId() {
        return selectedOrderId;
    }

    public LiveData<String> getAttachmentUri() {
        return attachmentUri;
    }

    /**
     * Clear submit success message after it's been shown
     */
    public void clearSubmitSuccess() {
        submitSuccess.setValue(null);
    }

    /**
     * Clear submit error message after it's been shown
     */
    public void clearSubmitError() {
        submitError.setValue(null);
    }
}
