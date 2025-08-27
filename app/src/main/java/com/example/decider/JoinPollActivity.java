package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class JoinPollActivity extends AppCompatActivity {
    
    private EditText editTextInviteCode;
    private Button buttonJoinPoll;
    private Button buttonBack;
    private TextView textViewInstructions;
    private PollStorage storage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_poll);
        
        storage = new PollStorage(this);
        initializeViews();
        setupUI();
    }
    
    private void initializeViews() {
        editTextInviteCode = findViewById(R.id.edit_text_invite_code);
        buttonJoinPoll = findViewById(R.id.button_join_poll);
        buttonBack = findViewById(R.id.button_back);
        textViewInstructions = findViewById(R.id.text_view_instructions);
    }
    
    private void setupUI() {
        // Setup instructions
        textViewInstructions.setText("Nhập mã mời 6 ký tự để tham gia cuộc bình chọn");
        
        // Setup invite code input
        editTextInviteCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateJoinButtonState();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Setup join button
        buttonJoinPoll.setOnClickListener(v -> joinPoll());
        
        // Setup back button
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Initial button state
        updateJoinButtonState();
    }
    
    private void updateJoinButtonState() {
        String inviteCode = editTextInviteCode.getText().toString().trim();
        buttonJoinPoll.setEnabled(inviteCode.length() == 6);
    }
    
    private void joinPoll() {
        String inviteCode = editTextInviteCode.getText().toString().trim().toUpperCase();
        
        if (inviteCode.length() != 6) {
            Toast.makeText(this, "Mã mời phải có 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tìm poll theo mã mời
        Poll poll = storage.getPollByInviteCode(inviteCode);
        
        if (poll == null) {
            Toast.makeText(this, "Không tìm thấy cuộc bình chọn với mã này", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!poll.isActive()) {
            Toast.makeText(this, "Cuộc bình chọn này đã kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lưu poll hiện tại và chuyển đến màn hình bình chọn
        storage.setCurrentPoll(poll);
        Toast.makeText(this, "Đã tham gia cuộc bình chọn: " + poll.getQuestion(), Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, VoteActivity.class);
        intent.putExtra("poll_id", poll.getId());
        startActivity(intent);
        finish();
    }
}