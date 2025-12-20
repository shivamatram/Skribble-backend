package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodvan.R;
import com.example.foodvan.models.ChatMessage;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private Context context;
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;
    
    public ChatMessageAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.isSentByCurrentUser() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        
        if (viewType == VIEW_TYPE_SENT) {
            view = inflater.inflate(R.layout.item_chat_message_sent, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_chat_message_received, parent, false);
        }
        
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
    
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    public void addMessage(ChatMessage message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;
        private TextView senderName;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text);
            timeText = itemView.findViewById(R.id.tv_message_time);
            senderName = itemView.findViewById(R.id.tv_sender_name);
        }
        
        public void bind(ChatMessage message) {
            if (messageText != null) {
                messageText.setText(message.getMessage());
            }
            
            if (timeText != null) {
                timeText.setText(timeFormat.format(new java.util.Date(message.getTimestamp())));
            }
            
            if (senderName != null && !message.isSentByCurrentUser()) {
                senderName.setText(message.getSenderName());
                senderName.setVisibility(View.VISIBLE);
            } else if (senderName != null) {
                senderName.setVisibility(View.GONE);
            }
        }
    }
}
