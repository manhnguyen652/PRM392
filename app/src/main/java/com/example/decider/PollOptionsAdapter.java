package com.example.decider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PollOptionsAdapter extends RecyclerView.Adapter<PollOptionsAdapter.ViewHolder> {
    
    private List<String> options;
    private OnOptionDeleteListener deleteListener;
    
    public interface OnOptionDeleteListener {
        void onDelete(int position);
    }
    
    public PollOptionsAdapter(List<String> options, OnOptionDeleteListener deleteListener) {
        this.options = options;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_option_edit, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String option = options.get(position);
        
        // Remove previous text watcher to avoid conflicts
        if (holder.textWatcher != null) {
            holder.editTextOption.removeTextChangedListener(holder.textWatcher);
        }
        
        holder.editTextOption.setText(option);
        holder.editTextOption.setHint("Lựa chọn " + (position + 1));
        
        // Create new text watcher
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    options.set(currentPosition, s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        holder.editTextOption.addTextChangedListener(holder.textWatcher);
        
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(holder.getAdapterPosition());
            }
        });
        
        // Show delete button only if there are more than 2 options
        holder.buttonDelete.setVisibility(options.size() > 2 ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public int getItemCount() {
        return options.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editTextOption;
        ImageButton buttonDelete;
        TextWatcher textWatcher;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextOption = itemView.findViewById(R.id.edit_text_option);
            buttonDelete = itemView.findViewById(R.id.button_delete_option);
        }
    }
}