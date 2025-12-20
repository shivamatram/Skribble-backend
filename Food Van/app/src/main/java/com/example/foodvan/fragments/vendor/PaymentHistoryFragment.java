package com.example.foodvan.fragments.vendor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.PaymentHistoryAdapter;
import com.example.foodvan.models.PaymentTransaction;
import com.example.foodvan.viewmodels.PaymentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying payment history
 * Features:
 * - List of all payment transactions
 * - Filter by date range, status, payment method
 * - Search by transaction ID
 * - Pull to refresh
 */
public class PaymentHistoryFragment extends Fragment implements PaymentHistoryAdapter.OnPaymentClickListener {

    private static final String TAG = "PaymentHistory";

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MaterialCardView cardFilters, cardEmptyState;
    private ChipGroup chipGroupStatus, chipGroupMethod;
    private TextInputEditText etSearch;
    private MaterialButton btnDateRange, btnClearFilters;
    private TextView tvEmptyMessage, tvDateRange;

    // Data & Services
    private PaymentViewModel paymentViewModel;
    private PaymentHistoryAdapter adapter;
    private List<PaymentTransaction> allTransactions;
    private List<PaymentTransaction> filteredTransactions;

    // Filter states
    private String selectedStatus = "All";
    private String selectedMethod = "All";
    private String searchQuery = "";
    private long startDate = 0;
    private long endDate = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        initializeViewModel();
        setupRecyclerView();
        setupFilters();
        setupClickListeners();
        observeData();
        loadPaymentHistory();
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        cardFilters = view.findViewById(R.id.card_filters);
        cardEmptyState = view.findViewById(R.id.card_empty_state);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        chipGroupMethod = view.findViewById(R.id.chip_group_method);
        etSearch = view.findViewById(R.id.et_search);
        btnDateRange = view.findViewById(R.id.btn_date_range);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        tvDateRange = view.findViewById(R.id.tv_date_range);
    }

    private void initializeViewModel() {
        paymentViewModel = new ViewModelProvider(requireActivity()).get(PaymentViewModel.class);
    }

    private void setupRecyclerView() {
        allTransactions = new ArrayList<>();
        filteredTransactions = new ArrayList<>();
        adapter = new PaymentHistoryAdapter(filteredTransactions, this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup status filter chips
        setupStatusChips();
        
        // Setup payment method filter chips
        setupMethodChips();
        
        // Setup search functionality
        setupSearch();
    }

    private void setupStatusChips() {
        String[] statuses = {"All", "Success", "Pending", "Failed"};
        
        for (String status : statuses) {
            Chip chip = new Chip(getContext());
            chip.setText(status);
            chip.setCheckable(true);
            chip.setChecked("All".equals(status));
            
            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    selectedStatus = status;
                    uncheckOtherStatusChips(chip);
                    applyFilters();
                }
            });
            
            chipGroupStatus.addView(chip);
        }
    }

    private void setupMethodChips() {
        String[] methods = {"All", "UPI", "Card", "Bank Transfer", "Wallet"};
        
        for (String method : methods) {
            Chip chip = new Chip(getContext());
            chip.setText(method);
            chip.setCheckable(true);
            chip.setChecked("All".equals(method));
            
            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    selectedMethod = method;
                    uncheckOtherMethodChips(chip);
                    applyFilters();
                }
            });
            
            chipGroupMethod.addView(chip);
        }
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            searchQuery = etSearch.getText().toString().trim();
            applyFilters();
            return true;
        });
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPaymentHistory);
        
        btnDateRange.setOnClickListener(v -> showDateRangePicker());
        
        btnClearFilters.setOnClickListener(v -> clearAllFilters());
    }

    private void observeData() {
        paymentViewModel.getPaymentHistory().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions.clear();
            if (transactions != null) {
                allTransactions.addAll(transactions);
            }
            applyFilters();
        });

        paymentViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        paymentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    private void loadPaymentHistory() {
        paymentViewModel.loadPaymentHistory();
    }

    private void applyFilters() {
        filteredTransactions.clear();
        
        for (PaymentTransaction transaction : allTransactions) {
            if (matchesFilters(transaction)) {
                filteredTransactions.add(transaction);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean matchesFilters(PaymentTransaction transaction) {
        // Status filter
        if (!"All".equals(selectedStatus) && !selectedStatus.equals(transaction.getStatus())) {
            return false;
        }
        
        // Method filter
        if (!"All".equals(selectedMethod) && !selectedMethod.equals(transaction.getPaymentMethod())) {
            return false;
        }
        
        // Search filter
        if (!TextUtils.isEmpty(searchQuery)) {
            String query = searchQuery.toLowerCase();
            if (!transaction.getTransactionId().toLowerCase().contains(query) &&
                !transaction.getDescription().toLowerCase().contains(query)) {
                return false;
            }
        }
        
        // Date range filter
        if (startDate > 0 && endDate > 0) {
            long transactionDate = transaction.getTimestamp();
            if (transactionDate < startDate || transactionDate > endDate) {
                return false;
            }
        }
        
        return true;
    }

    private void uncheckOtherStatusChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStatus.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }

    private void uncheckOtherMethodChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupMethod.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMethod.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = 
            MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");
        
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                startDate = selection.first;
                endDate = selection.second;
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String dateRangeText = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));
                tvDateRange.setText(dateRangeText);
                tvDateRange.setVisibility(View.VISIBLE);
                
                applyFilters();
            }
        });
        
        picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void clearAllFilters() {
        // Reset status filter
        selectedStatus = "All";
        for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStatus.getChildAt(i);
            chip.setChecked("All".equals(chip.getText().toString()));
        }
        
        // Reset method filter
        selectedMethod = "All";
        for (int i = 0; i < chipGroupMethod.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMethod.getChildAt(i);
            chip.setChecked("All".equals(chip.getText().toString()));
        }
        
        // Reset search
        searchQuery = "";
        etSearch.setText("");
        
        // Reset date range
        startDate = 0;
        endDate = 0;
        tvDateRange.setVisibility(View.GONE);
        
        applyFilters();
    }

    private void updateEmptyState() {
        if (filteredTransactions.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            
            if (allTransactions.isEmpty()) {
                tvEmptyMessage.setText("No payment transactions found.\nTransactions will appear here once you start receiving payments.");
            } else {
                tvEmptyMessage.setText("No transactions match your current filters.\nTry adjusting your search criteria.");
            }
        } else {
            cardEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPaymentClick(PaymentTransaction transaction) {
        // Handle payment transaction click
        // Could open a detailed view or show transaction details
        showTransactionDetails(transaction);
    }

    private void showTransactionDetails(PaymentTransaction transaction) {
        // For now, show a snackbar with transaction details
        String message = "Transaction ID: " + transaction.getTransactionId() + 
                        "\nAmount: ₹" + transaction.getAmount() + 
                        "\nStatus: " + transaction.getStatus();
        
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
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
