package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    
    private PollStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        storage = new PollStorage(this);
        setupUI();
    }
    
    private void setupUI() {
        Button btnCreatePoll = findViewById(R.id.button_create_poll);
        Button btnJoinPoll = findViewById(R.id.button_join_poll);
        Button btnViewTemplates = findViewById(R.id.button_view_templates);
        
        btnCreatePoll.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePollActivity.class);
            startActivity(intent);
        });
        
        btnJoinPoll.setOnClickListener(v -> {
            // For demo purposes, automatically join the current poll if exists
            Poll currentPoll = storage.getCurrentPoll();
            if (currentPoll != null && currentPoll.isActive()) {
                Intent intent = new Intent(this, VoteActivity.class);
                intent.putExtra("poll_id", currentPoll.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có cuộc bình chọn nào đang hoạt động", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnViewTemplates.setOnClickListener(v -> {
            Intent intent = new Intent(this, TemplateLibraryActivity.class);
            startActivity(intent);
        });
    }
}