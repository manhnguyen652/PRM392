package com.example.decider;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    
    private final ItemTouchHelperAdapter adapter;
    
    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
    }
    
    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public boolean isLongPressDragEnabled() {
        return false; // We'll handle drag start manually
    }
    
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false; // No swiping for ranking
    }
    
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }
    
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder, 
                         @NonNull RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
    
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used since swiping is disabled
    }
}