package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import com.example.foodvan.R;
import com.example.foodvan.models.ProfileSettingsItem;

import java.util.List;

public class ProfileSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ProfileSettingsItem> items;
    private OnSettingsItemClickListener listener;

    public interface OnSettingsItemClickListener {
        void onSettingsItemClick(ProfileSettingsItem item);
        void onToggleChanged(ProfileSettingsItem item, boolean isChecked);
    }

    public ProfileSettingsAdapter(Context context, List<ProfileSettingsItem> items, OnSettingsItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        
        switch (viewType) {
            case ProfileSettingsItem.TYPE_HEADER:
                View headerView = inflater.inflate(R.layout.item_settings_header, parent, false);
                return new HeaderViewHolder(headerView);
            case ProfileSettingsItem.TYPE_TOGGLE:
                View toggleView = inflater.inflate(R.layout.item_settings_toggle, parent, false);
                return new ToggleViewHolder(toggleView);
            case ProfileSettingsItem.TYPE_DANGER:
                View dangerView = inflater.inflate(R.layout.item_settings_danger, parent, false);
                return new DangerViewHolder(dangerView);
            default:
                View itemView = inflater.inflate(R.layout.item_settings_item, parent, false);
                return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileSettingsItem item = items.get(position);
        
        switch (holder.getItemViewType()) {
            case ProfileSettingsItem.TYPE_HEADER:
                bindHeaderViewHolder((HeaderViewHolder) holder, item);
                break;
            case ProfileSettingsItem.TYPE_TOGGLE:
                bindToggleViewHolder((ToggleViewHolder) holder, item);
                break;
            case ProfileSettingsItem.TYPE_DANGER:
                bindDangerViewHolder((DangerViewHolder) holder, item);
                break;
            default:
                bindItemViewHolder((ItemViewHolder) holder, item);
                break;
        }
    }

    private void bindHeaderViewHolder(HeaderViewHolder holder, ProfileSettingsItem item) {
        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconResId());
    }

    private void bindItemViewHolder(ItemViewHolder holder, ProfileSettingsItem item) {
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.ivIcon.setImageResource(item.getIconResId());
        holder.ivChevron.setVisibility(item.hasChevron() ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingsItemClick(item);
            }
        });
    }

    private void bindToggleViewHolder(ToggleViewHolder holder, ProfileSettingsItem item) {
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.ivIcon.setImageResource(item.getIconResId());
        holder.switchToggle.setChecked(item.isToggleEnabled());
        
        holder.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setToggleEnabled(isChecked);
            if (listener != null) {
                listener.onToggleChanged(item, isChecked);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            holder.switchToggle.setChecked(!holder.switchToggle.isChecked());
        });
    }

    private void bindDangerViewHolder(DangerViewHolder holder, ProfileSettingsItem item) {
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.ivIcon.setImageResource(item.getIconResId());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingsItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder Classes
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivIcon;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon, ivChevron;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivChevron = itemView.findViewById(R.id.iv_chevron);
        }
    }

    public static class ToggleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon;
        MaterialSwitch switchToggle;

        public ToggleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            switchToggle = itemView.findViewById(R.id.switch_toggle);
        }
    }

    public static class DangerViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon;

        public DangerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}
