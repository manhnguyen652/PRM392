package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedPollsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSavedPolls;
    private TextView textViewEmptyTitle;
    private TextView textViewEmptySubtitle;
    private Button buttonBack;

    private PollStorage storage;
    private SavedPollsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_polls);

        storage = new PollStorage(this);
        initializeViews();
        setupUI();
        loadPolls();
    }

    private void initializeViews() {
        recyclerViewSavedPolls = findViewById(R.id.recycler_view_saved_polls);
        textViewEmptyTitle = findViewById(R.id.text_view_empty_title);
        textViewEmptySubtitle = findViewById(R.id.text_view_empty_subtitle);
        buttonBack = findViewById(R.id.button_back);
    }

    private void setupUI() {
        recyclerViewSavedPolls.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SavedPollsAdapter(new ArrayList<>(), new SavedPollsAdapter.ActionListener() {
            @Override
            public void onViewResults(Poll poll) {
                try {
                    Intent intent = new Intent(SavedPollsActivity.this, ResultsActivity.class);
                    intent.putExtra("poll_id", poll.getId());
                    intent.putExtra("is_creator", true);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SavedPollsActivity.this, "Không thể mở kết quả", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onVote(Poll poll) {
                try {
                    if (!poll.isActive()) {
                        Toast.makeText(SavedPollsActivity.this, "Cuộc bình chọn đã kết thúc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(SavedPollsActivity.this, VoteActivity.class);
                    intent.putExtra("poll_id", poll.getId());
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SavedPollsActivity.this, "Không thể mở màn hình bình chọn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDelete(Poll poll) {
                try {
                    storage.deletePoll(poll.getId());
                    Toast.makeText(SavedPollsActivity.this, "Đã xóa cuộc bình chọn", Toast.LENGTH_SHORT).show();
                    loadPolls();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SavedPollsActivity.this, "Không thể xóa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerViewSavedPolls.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadPolls() {
        try {
            List<Poll> polls = storage.getAllPolls();
            if (polls == null || polls.isEmpty()) {
                recyclerViewSavedPolls.setVisibility(View.GONE);
                textViewEmptyTitle.setVisibility(View.VISIBLE);
                textViewEmptySubtitle.setVisibility(View.VISIBLE);
            } else {
                recyclerViewSavedPolls.setVisibility(View.VISIBLE);
                textViewEmptyTitle.setVisibility(View.GONE);
                textViewEmptySubtitle.setVisibility(View.GONE);
                adapter.updateData(polls);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải danh sách cuộc bình chọn", Toast.LENGTH_LONG).show();
        }
    }
}

