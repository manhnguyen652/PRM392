package com.example.decider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    
    private List<String> results;
    private Poll poll;
    private Map<String, Integer> voteCounts;
    private int totalVotes;
    
    public ResultsAdapter(List<String> results, Poll poll) {
        this.results = results != null ? new ArrayList<>(results) : new ArrayList<>();
        this.poll = poll;
        calculateVoteCounts();
    }
    
    private void calculateVoteCounts() {
        try {
            voteCounts = new HashMap<>();
            totalVotes = 0;
            
            if (poll == null || poll.getOptions() == null) {
                return;
            }
            
            // Initialize all options with 0 votes
            for (String option : poll.getOptions()) {
                if (option != null) {
                    voteCounts.put(option, 0);
                }
            }
            
            // Count votes based on voting mode
            if (poll.getVotes() != null) {
                switch (poll.getVotingMode()) {
                    case SINGLE_CHOICE:
                        for (Vote vote : poll.getVotes().values()) {
                            if (vote != null && vote.getSingleChoice() != null) {
                                voteCounts.put(vote.getSingleChoice(), 
                                    voteCounts.getOrDefault(vote.getSingleChoice(), 0) + 1);
                                totalVotes++;
                            }
                        }
                        break;
                        
                    case RANKED_CHOICE:
                        // For ranked choice, calculate points
                        for (Vote vote : poll.getVotes().values()) {
                            if (vote != null) {
                                List<String> rankings = vote.getRankings();
                                if (rankings != null) {
                                    for (int i = 0; i < rankings.size(); i++) {
                                        String option = rankings.get(i);
                                        if (option != null) {
                                            int points = poll.getOptions().size() - i;
                                            voteCounts.put(option, voteCounts.getOrDefault(option, 0) + points);
                                        }
                                    }
                                    totalVotes++;
                                }
                            }
                        }
                        break;
                        
                    case RANDOM_SPINNER:
                        // For spinner, winner gets all votes
                        if (results != null && !results.isEmpty() && results.get(0) != null) {
                            voteCounts.put(results.get(0), 1);
                            totalVotes = 1;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            voteCounts = new HashMap<>();
            totalVotes = 0;
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
        try {
            if (results == null || position >= results.size() || results.get(position) == null) {
                return;
            }
            
            String option = results.get(position);
            int votes = voteCounts.getOrDefault(option, 0);
            
            if (holder.textViewOption != null) {
                holder.textViewOption.setText(option);
            }
            
            if (holder.textViewVotes != null) {
                if (poll != null && poll.getVotingMode() == Poll.VotingMode.RANKED_CHOICE) {
                    holder.textViewVotes.setText(votes + " điểm");
                } else {
                    holder.textViewVotes.setText(votes + " phiếu");
                }
            }
            
            // Calculate percentage
            int percentage = totalVotes > 0 ? (votes * 100) / totalVotes : 0;
            if (holder.textViewPercentage != null) {
                holder.textViewPercentage.setText(percentage + "%");
            }
            if (holder.progressBarVotes != null) {
                holder.progressBarVotes.setProgress(percentage);
            }
            
            // Highlight winner
            if (holder.textViewRank != null) {
                if (position == 0) {
                    holder.textViewRank.setText("🏆");
                    holder.textViewRank.setTextSize(20);
                } else {
                    holder.textViewRank.setText(String.valueOf(position + 1));
                    holder.textViewRank.setTextSize(16);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }
    
    public void updateResults(List<String> newResults) {
        try {
            if (newResults != null) {
                this.results = new ArrayList<>(newResults);
                calculateVoteCounts();
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        TextView textViewOption;
        TextView textViewVotes;
        TextView textViewPercentage;
        ProgressBar progressBarVotes;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                textViewRank = itemView.findViewById(R.id.text_view_rank);
                textViewOption = itemView.findViewById(R.id.text_view_option);
                textViewVotes = itemView.findViewById(R.id.text_view_votes);
                textViewPercentage = itemView.findViewById(R.id.text_view_percentage);
                progressBarVotes = itemView.findViewById(R.id.progress_bar_votes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}