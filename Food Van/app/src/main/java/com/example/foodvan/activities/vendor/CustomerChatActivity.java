package com.example.foodvan.activities.vendor;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ChatMessageAdapter;
import com.example.foodvan.models.ChatMessage;
import com.example.foodvan.viewmodels.CustomerChatViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CustomerChatActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView customerAvatar;
    private TextView customerName, onlineStatus;
    private RecyclerView messagesRecyclerView;
    private LinearLayout typingIndicatorLayout, emptyStateLayout;
    private TextInputEditText messageInput;
    private FloatingActionButton sendButton;
    private MaterialButton attachmentButton;

    // ViewModel and Adapter
    private CustomerChatViewModel viewModel;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();

    // Customer info
    private String customerId;
    private String customerNameStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar - BEFORE setContentView
        setupStatusBar();
        
        setContentView(R.layout.activity_customer_chat);

        getCustomerInfoFromIntent();
        initializeViewModel();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupMessageInput();
        observeViewModel();
        loadMessages();
    }

    private void getCustomerInfoFromIntent() {
        customerId = getIntent().getStringExtra("CUSTOMER_ID");
        customerNameStr = getIntent().getStringExtra("CUSTOMER_NAME");
        
        if (customerId == null) {
            customerId = "demo_customer_123";
        }
        if (customerNameStr == null) {
            customerNameStr = "Customer";
        }
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(CustomerChatViewModel.class);
        viewModel.setCustomerId(customerId);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        customerAvatar = findViewById(R.id.customer_avatar);
        customerName = findViewById(R.id.customer_name);
        onlineStatus = findViewById(R.id.online_status);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        typingIndicatorLayout = findViewById(R.id.typing_indicator_layout);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        attachmentButton = findViewById(R.id.attachment_button);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set customer info
        customerName.setText(customerNameStr);
        onlineStatus.setText("Online");
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupMessageInput() {
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                sendButton.setEnabled(hasText);
                sendButton.setAlpha(hasText ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);

        attachmentButton.setOnClickListener(v -> {
            showToast("Attachment feature - Coming Soon");
        });
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, chatMessages -> {
            if (chatMessages != null) {
                messages.clear();
                messages.addAll(chatMessages);
                adapter.notifyDataSetChanged();
                scrollToBottom();
                updateEmptyState();
            }
        });

        viewModel.getIsTyping().observe(this, isTyping -> {
            typingIndicatorLayout.setVisibility(isTyping ? View.VISIBLE : View.GONE);
            if (isTyping) {
                scrollToBottom();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showToast(error);
            }
        });
    }

    private void loadMessages() {
        viewModel.loadMessages();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Send the message text to the ViewModel
        // The ViewModel will create the ChatMessage object internally
        viewModel.sendMessage(messageText);
        messageInput.setText("");
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private void updateEmptyState() {
        if (messages.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private String getCurrentVendorId() {
        // Return current vendor ID from Firebase Auth or session
        return "vendor_123";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure orange status bar is applied when activity resumes
        setupStatusBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.disconnect();
        }
    }

    /**
     * Setup orange status bar to match the navbar theme (same as Order History)
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            
            // Clear any existing flags
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            
            // Enable drawing system bar backgrounds
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // Set orange status bar color - using exact color value
            int orangeColor = 0xFFFF6B35; // #FF6B35
            window.setStatusBarColor(orangeColor);
            
            // Ensure white status bar icons on orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                // Remove light status bar flag to get white icons
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(flags);
            }
        }
    }
}
