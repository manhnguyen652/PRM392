package com.example.decider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    
    private List<String> results;
    private Poll poll;
    private Map<String, Integer> voteCounts;
    private int totalVotes;
    
    public ResultsAdapter(List<String> results, Poll poll) {
        this.results = results;
        this.poll = poll;
        calculateVoteCounts();
    }
    
    private void calculateVoteCounts() {
        voteCounts = new HashMap<>();
        totalVotes = 0;
        
        // Initialize all options with 0 votes
        for (String option : poll.getOptions()) {
            voteCounts.put(option, 0);
        }
        
        // Count votes based on voting mode
        switch (poll.getVotingMode()) {
            case SINGLE_CHOICE:
                for (Vote vote : poll.getVotes().values()) {
                    if (vote.getSingleChoice() != null) {
                        voteCounts.put(vote.getSingleChoice(), 
                            voteCounts.get(vote.getSingleChoice()) + 1);
                        totalVotes++;
                    }
                }
                break;
                
            case RANKED_CHOICE:
                // For ranked choice, calculate points
                for (Vote vote : poll.getVotes().values()) {
                    List<String> rankings = vote.getRankings();
                    if (rankings != null) {
                        for (int i = 0; i < rankings.size(); i++) {
                            String option = rankings.get(i);
                            int points = poll.getOptions().size() - i;
                            voteCounts.put(option, voteCounts.get(option) + points);
                        }
                        totalVotes++;
                    }
                }
                break;
                
            case RANDOM_SPINNER:
                // For spinner, winner gets all votes
                if (!results.isEmpty()) {
                    voteCounts.put(results.get(0), 1);
                    totalVotes = 1;
                }
                break;
        }
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String option = results.get(position);
        int votes = voteCounts.get(option);
        
        holder.textViewOption.setText(option);
        
        if (poll.getVotingMode() == Poll.VotingMode.RANKED_CHOICE) {
            holder.textViewVotes.setText(votes + " điểm");
        } else {
            holder.textViewVotes.setText(votes + " phiếu");
        }
        
        // Calculate percentage
        int percentage = totalVotes > 0 ? (votes * 100) / totalVotes : 0;
        holder.textViewPercentage.setText(percentage + "%");
        holder.progressBarVotes.setProgress(percentage);
        
        // Highlight winner
        if (position == 0) {
            holder.textViewRank.setText("🏆");
            holder.textViewRank.setTextSize(20);
        } else {
            holder.textViewRank.setText(String.valueOf(position + 1));
            holder.textViewRank.setTextSize(16);
        }
    }
    
    @Override
    public int getItemCount() {
        return results.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        TextView textViewOption;
        TextView textViewVotes;
        TextView textViewPercentage;
        ProgressBar progressBarVotes;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.text_view_rank);
            textViewOption = itemView.findViewById(R.id.text_view_option);
            textViewVotes = itemView.findViewById(R.id.text_view_votes);
            textViewPercentage = itemView.findViewById(R.id.text_view_percentage);
            progressBarVotes = itemView.findViewById(R.id.progress_bar_votes);
        }
    }
}