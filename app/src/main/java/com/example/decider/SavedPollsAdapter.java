package com.example.decider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedPollsAdapter extends RecyclerView.Adapter<SavedPollsAdapter.ViewHolder> {

    interface ActionListener {
        void onViewResults(Poll poll);
        void onVote(Poll poll);
        void onDelete(Poll poll);
    }

    private final List<Poll> polls;
    private final ActionListener listener;

    public SavedPollsAdapter(List<Poll> polls, ActionListener listener) {
        this.polls = polls != null ? polls : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Poll> newPolls) {
        polls.clear();
        if (newPolls != null) {
            polls.addAll(newPolls);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_saved_poll, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Poll poll = polls.get(position);

        holder.textTitle.setText(poll.getQuestion());

        String modeText;
        switch (poll.getVotingMode()) {
            case RANKED_CHOICE:
                modeText = "Xếp hạng";
                break;
            case RANDOM_SPINNER:
                modeText = "Quay ngẫu nhiên";
                break;
            case SINGLE_CHOICE:
            default:
                modeText = "Một lựa chọn";
                break;
        }

        String statusText = poll.isActive() ? "Đang mở" : "Đã kết thúc";
        String subtitle = modeText + " • " + statusText + (poll.getInviteCode() != null ? " • Mã: " + poll.getInviteCode() : "");
        holder.textSubtitle.setText(subtitle);

        holder.buttonResults.setOnClickListener(v -> {
            if (listener != null) listener.onViewResults(poll);
        });
        holder.buttonVote.setOnClickListener(v -> {
            if (listener != null) listener.onVote(poll);
        });
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(poll);
        });

        holder.buttonVote.setEnabled(poll.isActive());
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textSubtitle;
        Button buttonResults;
        Button buttonVote;
        Button buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textSubtitle = itemView.findViewById(R.id.text_subtitle);
            buttonResults = itemView.findViewById(R.id.button_view_results);
            buttonVote = itemView.findViewById(R.id.button_vote);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }
    }
}

