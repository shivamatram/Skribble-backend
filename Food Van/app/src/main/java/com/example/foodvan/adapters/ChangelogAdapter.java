package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodvan.R;
import com.example.foodvan.models.ChangelogEntry;
import java.util.List;

/**
 * ChangelogAdapter - RecyclerView adapter for displaying changelog entries
 */
public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ChangelogViewHolder> {
    
    private List<ChangelogEntry> changelogEntries;
    
    public ChangelogAdapter(List<ChangelogEntry> changelogEntries) {
        this.changelogEntries = changelogEntries;
    }
    
    @NonNull
    @Override
    public ChangelogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_changelog, parent, false);
        return new ChangelogViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChangelogViewHolder holder, int position) {
        ChangelogEntry entry = changelogEntries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return changelogEntries != null ? changelogEntries.size() : 0;
    }
    
    public void updateData(List<ChangelogEntry> newEntries) {
        this.changelogEntries = newEntries;
        notifyDataSetChanged();
    }
    
    static class ChangelogViewHolder extends RecyclerView.ViewHolder {
        private TextView versionText;
        private TextView dateText;
        private TextView descriptionText;
        
        public ChangelogViewHolder(@NonNull View itemView) {
            super(itemView);
            versionText = itemView.findViewById(R.id.tv_version);
            dateText = itemView.findViewById(R.id.tv_date);
            descriptionText = itemView.findViewById(R.id.tv_description);
        }
        
        public void bind(ChangelogEntry entry) {
            versionText.setText("Version " + entry.getVersion());
            dateText.setText(entry.getDate());
            
            // Build description from title and highlights
            StringBuilder description = new StringBuilder();
            if (entry.getTitle() != null && !entry.getTitle().isEmpty()) {
                description.append(entry.getTitle());
            }
            if (entry.getHighlights() != null && !entry.getHighlights().isEmpty()) {
                if (description.length() > 0) {
                    description.append("\n");
                }
                for (String highlight : entry.getHighlights()) {
                    description.append("• ").append(highlight).append("\n");
                }
            }
            descriptionText.setText(description.toString().trim());
        }
    }
}