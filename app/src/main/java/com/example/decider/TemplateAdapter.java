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
        try {
            if (position < 0 || position >= templates.size()) {
                return;
            }
            
            PollTemplate template = templates.get(position);
            if (template == null) {
                return;
            }
            
            // Set template name with null check
            String templateName = template.getName();
            if (templateName == null || templateName.trim().isEmpty()) {
                templateName = "Mẫu không có tên";
            }
            holder.textViewTemplateName.setText(templateName);
            
            // Set options count with null check
            int optionsCount = 0;
            if (template.getOptions() != null) {
                optionsCount = template.getOptions().size();
            }
            holder.textViewOptionsCount.setText(optionsCount + " lựa chọn");
            
            // Set voting mode text with null check
            String votingModeText = "Bình chọn 1 lần"; // Default
            if (template.getDefaultVotingMode() != null) {
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
            }
            
            if (template.isHasDefaultTimer() && template.getDefaultTimerMinutes() > 0) {
                votingModeText += " • " + template.getDefaultTimerMinutes() + " phút";
            }
            
            holder.textViewVotingMode.setText(votingModeText);
            
            // Set click listeners with null checks
            holder.itemView.setOnClickListener(v -> {
                if (listener != null && template != null) {
                    listener.onTemplateClick(template);
                }
            });
            
            holder.buttonDeleteTemplate.setOnClickListener(v -> {
                if (listener != null && template != null) {
                    listener.onTemplateDelete(template);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Set fallback values to prevent crash
            holder.textViewTemplateName.setText("Lỗi tải mẫu");
            holder.textViewVotingMode.setText("Không xác định");
            holder.textViewOptionsCount.setText("0 lựa chọn");
        }
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