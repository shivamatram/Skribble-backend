package com.example.foodvan.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodvan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for attachment RecyclerView in Report Issue form
 */
public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {

    private final List<Uri> attachmentUris;
    private final OnAttachmentClickListener clickListener;

    public interface OnAttachmentClickListener {
        void onAttachmentRemove(int position);
        void onAttachmentClick(Uri uri);
    }

    public AttachmentAdapter(List<Uri> attachmentUris, OnAttachmentClickListener clickListener) {
        this.attachmentUris = attachmentUris;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        Uri uri = attachmentUris.get(position);
        holder.bind(uri, position);
    }

    @Override
    public int getItemCount() {
        return attachmentUris.size();
    }

    class AttachmentViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardAttachment;
        private final ImageView ivThumbnail;
        private final MaterialButton btnRemove;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardAttachment = itemView.findViewById(R.id.card_attachment);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(Uri uri, int position) {
            // Load image thumbnail using Glide
            Glide.with(itemView.getContext())
                .load(uri)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .transform(new RoundedCorners(12))
                .into(ivThumbnail);
            
            // Set click listeners
            cardAttachment.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onAttachmentClick(uri);
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onAttachmentRemove(position);
                }
            });
            
            // Set content descriptions for accessibility
            cardAttachment.setContentDescription("Attachment image " + (position + 1) + ". Tap to view.");
            btnRemove.setContentDescription("Remove attachment " + (position + 1));
        }
    }
}
