package com.example.foodvan.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.models.FAQ;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.SupportContact;
import com.example.foodvan.models.SupportTicket;
import com.example.foodvan.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Help & Support operations
 * Handles Firebase Realtime Database operations for FAQs, support tickets, and contacts
 */
public class HelpSupportRepository {

    private static final String TAG = "HelpSupportRepository";
    
    // Firebase paths
    private static final String FAQS_PATH = "support/faqs";
    private static final String SUPPORT_CONTACTS_PATH = "config/supportContacts";
    private static final String SUPPORT_TICKETS_PATH = "supportTickets";
    private static final String ORDERS_PATH = "orders";

    private final Context context;
    private final DatabaseReference databaseRef;
    private final FirebaseAuth firebaseAuth;
    private final SessionManager sessionManager;

    // Callback interfaces
    public interface OnFAQsLoadListener {
        void onSuccess(List<FAQ> faqs);
        void onError(String error);
    }

    public interface OnSupportContactLoadListener {
        void onSuccess(SupportContact contact);
        void onError(String error);
    }

    public interface OnTicketSubmitListener {
        void onSuccess(String ticketId);
        void onError(String error);
    }

    public interface OnTicketsLoadListener {
        void onSuccess(List<SupportTicket> tickets);
        void onError(String error);
    }

    public interface OnOrdersLoadListener {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }

    public HelpSupportRepository(Context context) {
        this.context = context;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Get current user ID
     */
    private String getCurrentUserId() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return sessionManager.getUserId();
    }

    /**
     * Load FAQs from Firebase
     */
    public void loadFAQs(OnFAQsLoadListener listener) {
        databaseRef.child(FAQS_PATH)
                .orderByChild("order")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<FAQ> faqs = new ArrayList<>();
                        
                        for (DataSnapshot faqSnapshot : snapshot.getChildren()) {
                            try {
                                FAQ faq = faqSnapshot.getValue(FAQ.class);
                                if (faq != null) {
                                    faq.setId(faqSnapshot.getKey());
                                    faqs.add(faq);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing FAQ: " + e.getMessage());
                            }
                        }
                        
                        // Sort by order
                        Collections.sort(faqs, (f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()));
                        
                        // If no FAQs in database, return sample FAQs
                        if (faqs.isEmpty()) {
                            faqs = getSampleFAQs();
                        }
                        
                        listener.onSuccess(faqs);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading FAQs: " + error.getMessage());
                        // Return sample FAQs on error
                        listener.onSuccess(getSampleFAQs());
                    }
                });
    }

    /**
     * Load support contact information from Firebase
     */
    public void loadSupportContact(OnSupportContactLoadListener listener) {
        databaseRef.child(SUPPORT_CONTACTS_PATH)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SupportContact contact = snapshot.getValue(SupportContact.class);
                        if (contact != null) {
                            listener.onSuccess(contact);
                        } else {
                            // Return default contact if not found
                            listener.onSuccess(SupportContact.getDefaultContact());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading support contact: " + error.getMessage());
                        // Return default contact on error
                        listener.onSuccess(SupportContact.getDefaultContact());
                    }
                });
    }

    /**
     * Submit a new support ticket
     */
    public void submitTicket(SupportTicket ticket, OnTicketSubmitListener listener) {
        String customerId = getCurrentUserId();
        if (customerId == null) {
            listener.onError("User not authenticated");
            return;
        }

        // Generate ticket ID
        String ticketId = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        ticket.setTicketId(ticketId);
        ticket.setCustomerId(customerId);
        ticket.setCreatedAt(System.currentTimeMillis());
        ticket.setUpdatedAt(System.currentTimeMillis());

        // Set priority based on category
        if (SupportTicket.CATEGORY_PAYMENT.equals(ticket.getCategory())) {
            ticket.setPriority(SupportTicket.PRIORITY_HIGH);
        } else if (SupportTicket.CATEGORY_WRONG_ORDER.equals(ticket.getCategory())) {
            ticket.setPriority(SupportTicket.PRIORITY_HIGH);
        }

        databaseRef.child(SUPPORT_TICKETS_PATH)
                .child(ticketId)
                .setValue(ticket)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ticket submitted successfully: " + ticketId);
                    listener.onSuccess(ticketId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting ticket: " + e.getMessage());
                    listener.onError("Failed to submit ticket: " + e.getMessage());
                });
    }

    /**
     * Load customer's support tickets
     */
    public void loadCustomerTickets(OnTicketsLoadListener listener) {
        String customerId = getCurrentUserId();
        if (customerId == null) {
            listener.onError("User not authenticated");
            return;
        }

        databaseRef.child(SUPPORT_TICKETS_PATH)
                .orderByChild("customerId")
                .equalTo(customerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<SupportTicket> tickets = new ArrayList<>();
                        
                        for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                            try {
                                SupportTicket ticket = ticketSnapshot.getValue(SupportTicket.class);
                                if (ticket != null) {
                                    tickets.add(ticket);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing ticket: " + e.getMessage());
                            }
                        }
                        
                        // Sort by created date (newest first)
                        Collections.sort(tickets, (t1, t2) -> 
                                Long.compare(t2.getCreatedAt(), t1.getCreatedAt()));
                        
                        listener.onSuccess(tickets);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading tickets: " + error.getMessage());
                        listener.onError("Failed to load tickets: " + error.getMessage());
                    }
                });
    }

    /**
     * Load recent orders for the customer (for Report Issue form)
     */
    public void loadRecentOrders(int limit, OnOrdersLoadListener listener) {
        String customerId = getCurrentUserId();
        if (customerId == null) {
            listener.onError("User not authenticated");
            return;
        }

        databaseRef.child(ORDERS_PATH)
                .orderByChild("customerId")
                .equalTo(customerId)
                .limitToLast(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            try {
                                Order order = orderSnapshot.getValue(Order.class);
                                if (order != null) {
                                    orders.add(order);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing order: " + e.getMessage());
                            }
                        }
                        
                        // Sort by order time (newest first)
                        Collections.sort(orders, (o1, o2) -> 
                                Long.compare(o2.getOrderTime(), o1.getOrderTime()));
                        
                        listener.onSuccess(orders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading orders: " + error.getMessage());
                        listener.onError("Failed to load orders: " + error.getMessage());
                    }
                });
    }

    /**
     * Get a specific ticket by ID
     */
    public void getTicketById(String ticketId, OnTicketLoadListener listener) {
        databaseRef.child(SUPPORT_TICKETS_PATH)
                .child(ticketId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SupportTicket ticket = snapshot.getValue(SupportTicket.class);
                        if (ticket != null) {
                            listener.onSuccess(ticket);
                        } else {
                            listener.onError("Ticket not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError("Failed to load ticket: " + error.getMessage());
                    }
                });
    }

    public interface OnTicketLoadListener {
        void onSuccess(SupportTicket ticket);
        void onError(String error);
    }

    /**
     * Sample FAQs for when Firebase data is not available
     */
    private List<FAQ> getSampleFAQs() {
        List<FAQ> faqs = new ArrayList<>();
        
        faqs.add(new FAQ(
            "faq1",
            "How do I track my order?",
            "You can track your order in real-time from the 'My Orders' section. Once you place an order, you'll see the live location of the food van on the map.",
            "Orders",
            1
        ));
        
        faqs.add(new FAQ(
            "faq2",
            "How can I cancel my order?",
            "You can cancel your order from the 'My Orders' section before it's confirmed by the vendor. Once confirmed, please contact support for cancellation.",
            "Orders",
            2
        ));
        
        faqs.add(new FAQ(
            "faq3",
            "What payment methods are accepted?",
            "We accept Cash on Delivery, UPI payments (Google Pay, PhonePe, Paytm), and Credit/Debit cards through our secure payment gateway.",
            "Payments",
            3
        ));
        
        faqs.add(new FAQ(
            "faq4",
            "How do I get a refund?",
            "Refunds are processed within 5-7 business days for cancelled orders. For payment issues, please report through the 'Report Issue' option.",
            "Payments",
            4
        ));
        
        faqs.add(new FAQ(
            "faq5",
            "How accurate is the food van location?",
            "Our GPS tracking updates in real-time. However, in areas with poor network coverage, there might be slight delays in location updates.",
            "Location",
            5
        ));
        
        faqs.add(new FAQ(
            "faq6",
            "Can I schedule an order for later?",
            "Currently, we only support immediate orders. Scheduled ordering feature will be available soon!",
            "Orders",
            6
        ));
        
        faqs.add(new FAQ(
            "faq7",
            "How do I update my delivery address?",
            "Go to Profile > Saved Addresses to add, edit, or delete your delivery addresses. You can also change the address during checkout.",
            "Account",
            7
        ));
        
        faqs.add(new FAQ(
            "faq8",
            "What should I do if my order is wrong?",
            "Please report the issue immediately through 'Report Issue' and select 'Wrong Order' category. Our support team will assist you.",
            "Orders",
            8
        ));
        
        return faqs;
    }
}
