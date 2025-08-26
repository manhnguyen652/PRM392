package com.example.decider;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> 
        implements ItemTouchHelperCallback.ItemTouchHelperAdapter {
    
    private List<String> options;
    private OnStartDragListener dragListener;
    
    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
    
    public RankingAdapter(List<String> options) {
        this.options = options;
    }
    
    public void setDragListener(OnStartDragListener dragListener) {
        this.dragListener = dragListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_ranking, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String option = options.get(position);
        holder.textViewOption.setText(option);
        holder.textViewRank.setText(String.valueOf(position + 1));
        
        // Set up drag handle
        holder.imageViewDragHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (dragListener != null) {
                    dragListener.onStartDrag(holder);
                }
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return options.size();
    }
    
    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(options, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        
        // Update all rank numbers
        notifyItemRangeChanged(Math.min(fromPosition, toPosition), 
                              Math.abs(fromPosition - toPosition) + 1);
    }
    
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        moveItem(fromPosition, toPosition);
    }
    
    public List<String> getRankedOptions() {
        return options;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        TextView textViewOption;
        ImageView imageViewDragHandle;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.text_view_rank);
            textViewOption = itemView.findViewById(R.id.text_view_option);
            imageViewDragHandle = itemView.findViewById(R.id.image_view_drag_handle);
        }
    }
}