package com.example.decider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
    
    private List<PollTemplate> templates;
    private TemplateActionListener listener;
    
    public interface TemplateActionListener {
        void onTemplateClick(PollTemplate template);
        void onTemplateDelete(PollTemplate template);
    }
    
    public TemplateAdapter(List<PollTemplate> templates, TemplateActionListener listener) {
        this.templates = templates;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_template, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PollTemplate template = templates.get(position);
        
        holder.textViewTemplateName.setText(template.getName());
        holder.textViewOptionsCount.setText(template.getOptions().size() + " lựa chọn");
        
        // Set voting mode text
        String votingModeText;
        switch (template.getDefaultVotingMode()) {
            case SINGLE_CHOICE:
                votingModeText = "Bình chọn 1 lần";
                break;
            case RANKED_CHOICE:
                votingModeText = "Bình chọn xếp hạng";
                break;
            case RANDOM_SPINNER:
                votingModeText = "Quay ngẫu nhiên";
                break;
            default:
                votingModeText = "Bình chọn 1 lần";
                break;
        }
        
        if (template.isHasDefaultTimer()) {
            votingModeText += " • " + template.getDefaultTimerMinutes() + " phút";
        }
        
        holder.textViewVotingMode.setText(votingModeText);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateClick(template);
            }
        });
        
        holder.buttonDeleteTemplate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateDelete(template);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return templates.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTemplateName;
        TextView textViewVotingMode;
        TextView textViewOptionsCount;
        ImageButton buttonDeleteTemplate;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTemplateName = itemView.findViewById(R.id.text_view_template_name);
            textViewVotingMode = itemView.findViewById(R.id.text_view_voting_mode);
            textViewOptionsCount = itemView.findViewById(R.id.text_view_options_count);
            buttonDeleteTemplate = itemView.findViewById(R.id.button_delete_template);
        }
    }
}