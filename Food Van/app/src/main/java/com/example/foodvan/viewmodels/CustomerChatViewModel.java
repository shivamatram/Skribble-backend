package com.example.foodvan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.foodvan.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class CustomerChatViewModel extends ViewModel {
    private MutableLiveData<List<ChatMessage>> messagesLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;
    private MutableLiveData<String> errorLiveData;
    
    private List<ChatMessage> messagesList;
    private String customerId;
    private String vendorId;
    
    public CustomerChatViewModel() {
        messagesLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        messagesList = new ArrayList<>();
    }
    
    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public void initialize(String customerId, String vendorId) {
        this.customerId = customerId;
        this.vendorId = vendorId;
        loadMessages();
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public LiveData<Boolean> getIsTyping() {
        // For now, return a simple MutableLiveData
        MutableLiveData<Boolean> isTyping = new MutableLiveData<>();
        isTyping.setValue(false);
        return isTyping;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorLiveData;
    }
    
    public void disconnect() {
        // Clean up any connections
        messagesList.clear();
        messagesLiveData.setValue(new ArrayList<>());
    }
    
    public void loadMessages() {
        isLoadingLiveData.setValue(true);
        
        // Simulate loading messages - in real app, this would load from Firebase
        try {
            // For now, just create some sample messages
            messagesList.clear();
            
            // Add sample messages
            ChatMessage message1 = new ChatMessage(
                "Hello! I'd like to place an order.",
                customerId,
                vendorId,
                ChatMessage.MessageType.SENT
            );
            message1.setTimestamp(System.currentTimeMillis() - 300000); // 5 minutes ago
            
            ChatMessage message2 = new ChatMessage(
                "Sure! What would you like to order?",
                vendorId,
                customerId,
                ChatMessage.MessageType.RECEIVED
            );
            message2.setTimestamp(System.currentTimeMillis() - 240000); // 4 minutes ago
            
            messagesList.add(message1);
            messagesList.add(message2);
            
            messagesLiveData.setValue(new ArrayList<>(messagesList));
            isLoadingLiveData.setValue(false);
            
        } catch (Exception e) {
            errorLiveData.setValue("Failed to load messages: " + e.getMessage());
            isLoadingLiveData.setValue(false);
        }
    }
    
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            errorLiveData.setValue("Message cannot be empty");
            return;
        }
        
        try {
            ChatMessage newMessage = new ChatMessage(
                messageText.trim(),
                customerId,
                vendorId,
                ChatMessage.MessageType.SENT
            );
            newMessage.setTimestamp(System.currentTimeMillis());
            
            messagesList.add(newMessage);
            messagesLiveData.setValue(new ArrayList<>(messagesList));
            
            // In real app, send to Firebase here
            // For now, simulate vendor response after a delay
            simulateVendorResponse(messageText);
            
        } catch (Exception e) {
            errorLiveData.setValue("Failed to send message: " + e.getMessage());
        }
    }
    
    private void simulateVendorResponse(String customerMessage) {
        // Simulate vendor response after 2 seconds
        new android.os.Handler().postDelayed(() -> {
            String response = generateVendorResponse(customerMessage);
            ChatMessage vendorMessage = new ChatMessage(
                response,
                vendorId,
                customerId,
                ChatMessage.MessageType.RECEIVED
            );
            vendorMessage.setTimestamp(System.currentTimeMillis());
            
            messagesList.add(vendorMessage);
            messagesLiveData.setValue(new ArrayList<>(messagesList));
        }, 2000);
    }
    
    private String generateVendorResponse(String customerMessage) {
        String message = customerMessage.toLowerCase();
        
        if (message.contains("order") || message.contains("food")) {
            return "Great! Check out our menu and let me know what you'd like.";
        } else if (message.contains("price") || message.contains("cost")) {
            return "Our prices are very reasonable. You can see them in the menu.";
        } else if (message.contains("delivery") || message.contains("time")) {
            return "Delivery usually takes 20-30 minutes depending on your location.";
        } else if (message.contains("location") || message.contains("address")) {
            return "We deliver within 5km radius. What's your delivery address?";
        } else if (message.contains("thank") || message.contains("thanks")) {
            return "You're welcome! Happy to help.";
        } else {
            return "Thanks for your message! How can I help you today?";
        }
    }
    
    public void markMessagesAsRead() {
        for (ChatMessage message : messagesList) {
            if (!message.isSentByCurrentUser()) {
                message.setRead(true);
            }
        }
        messagesLiveData.setValue(new ArrayList<>(messagesList));
    }
    
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up resources if needed
    }
}
