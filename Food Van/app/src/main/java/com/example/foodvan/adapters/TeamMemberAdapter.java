package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodvan.R;
import com.example.foodvan.models.TeamMember;
import java.util.List;

/**
 * TeamMemberAdapter - RecyclerView adapter for displaying team members
 */
public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {
    
    private List<TeamMember> teamMembers;
    
    public TeamMemberAdapter(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }
    
    @NonNull
    @Override
    public TeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_member, parent, false);
        return new TeamMemberViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TeamMemberViewHolder holder, int position) {
        TeamMember member = teamMembers.get(position);
        holder.bind(member);
    }
    
    @Override
    public int getItemCount() {
        return teamMembers != null ? teamMembers.size() : 0;
    }
    
    public void updateData(List<TeamMember> newMembers) {
        this.teamMembers = newMembers;
        notifyDataSetChanged();
    }
    
    static class TeamMemberViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView roleText;
        
        public TeamMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_member_name);
            roleText = itemView.findViewById(R.id.tv_member_role);
        }
        
        public void bind(TeamMember member) {
            nameText.setText(member.getName());
            roleText.setText(member.getRole());
        }
    }
}