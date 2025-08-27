package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VoteActivity extends AppCompatActivity {
    
    private TextView textViewTimer;
    private TextView textViewQuestion;
    private RadioGroup radioGroupVoteOptions;
    private RecyclerView recyclerViewRankingOptions;
    private LinearLayout layoutRandomSpinner;
    private Button buttonSubmitVote;
    private Button buttonBack;
    
    private Poll poll;
    private PollStorage storage;
    private CountDownTimer countDownTimer;
    private RankingAdapter rankingAdapter;
    private ItemTouchHelper itemTouchHelper;
    private String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        
        storage = new PollStorage(this);
        userId = "user_" + System.currentTimeMillis(); // Simple user ID generation
        
        initializeViews();
        loadPoll();
        setupUI();
    }
    
    private void initializeViews() {
        textViewTimer = findViewById(R.id.text_view_timer);
        textViewQuestion = findViewById(R.id.text_view_question);
        radioGroupVoteOptions = findViewById(R.id.radio_group_vote_options);
        recyclerViewRankingOptions = findViewById(R.id.recycler_view_ranking_options);
        layoutRandomSpinner = findViewById(R.id.layout_random_spinner);
        buttonSubmitVote = findViewById(R.id.button_submit_vote);
        buttonBack = findViewById(R.id.button_back);
    }
    
    private void loadPoll() {
        String pollId = getIntent().getStringExtra("poll_id");
        if (pollId != null) {
            poll = storage.getPollById(pollId);
        } else {
            poll = storage.getCurrentPoll();
        }
        
        if (poll == null || !poll.isActive()) {
            Toast.makeText(this, "Cuộc bình chọn không tồn tại hoặc đã kết thúc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void setupUI() {
        textViewQuestion.setText(poll.getQuestion());
        
        // Setup timer
        if (poll.isHasTimer()) {
            setupTimer();
        } else {
            textViewTimer.setVisibility(View.GONE);
        }
        
        // Setup voting interface based on mode
        switch (poll.getVotingMode()) {
            case SINGLE_CHOICE:
                setupSingleChoiceVoting();
                break;
            case RANKED_CHOICE:
                setupRankedChoiceVoting();
                break;
            case RANDOM_SPINNER:
                setupRandomSpinnerVoting();
                break;
        }
        
        buttonSubmitVote.setOnClickListener(v -> submitVote());
        buttonBack.setOnClickListener(v -> goToMainActivity());
    }
    
    private void setupTimer() {
        long remainingTime = poll.getRemainingTime();
        
        if (remainingTime <= 0) {
            // Poll has expired
            poll.closePoll();
            storage.savePoll(poll);
            goToResults();
            return;
        }
        
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                textViewTimer.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
            }
            
            @Override
            public void onFinish() {
                textViewTimer.setText("Hết thời gian!");
                poll.closePoll();
                storage.savePoll(poll);
                goToResults();
            }
        }.start();
    }
    
    private void setupSingleChoiceVoting() {
        radioGroupVoteOptions.setVisibility(View.VISIBLE);
        recyclerViewRankingOptions.setVisibility(View.GONE);
        layoutRandomSpinner.setVisibility(View.GONE);
        
        // Clear existing options
        radioGroupVoteOptions.removeAllViews();
        
        // Add radio buttons for each option
        for (String option : poll.getOptions()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTextSize(16);
            radioButton.setPadding(0, 16, 0, 16);
            radioGroupVoteOptions.addView(radioButton);
        }
        
        buttonSubmitVote.setText("Xác nhận bình chọn");
    }
    
    private void setupRankedChoiceVoting() {
        radioGroupVoteOptions.setVisibility(View.GONE);
        recyclerViewRankingOptions.setVisibility(View.VISIBLE);
        layoutRandomSpinner.setVisibility(View.GONE);
        
        // Setup ranking RecyclerView
        List<String> options = new ArrayList<>(poll.getOptions());
        rankingAdapter = new RankingAdapter(options);
        recyclerViewRankingOptions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRankingOptions.setAdapter(rankingAdapter);
        
        // Setup drag and drop
        ItemTouchHelperCallback callback = new ItemTouchHelperCallback(rankingAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewRankingOptions);
        
        rankingAdapter.setDragListener(viewHolder -> 
            itemTouchHelper.startDrag(viewHolder));
        
        buttonSubmitVote.setText("Xác nhận xếp hạng");
    }
    
    private void setupRandomSpinnerVoting() {
        radioGroupVoteOptions.setVisibility(View.GONE);
        recyclerViewRankingOptions.setVisibility(View.GONE);
        layoutRandomSpinner.setVisibility(View.VISIBLE);
        
        // For random spinner, we don't need voting interface
        // Just show a message and allow going to results
        TextView messageView = new TextView(this);
        messageView.setText("Cuộc bình chọn này sử dụng chế độ quay ngẫu nhiên.\nKết quả sẽ được quyết định bằng vòng quay may mắn!");
        messageView.setTextSize(16);
        messageView.setPadding(16, 32, 16, 32);
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layoutRandomSpinner.addView(messageView);
        
        buttonSubmitVote.setText("Xem kết quả");
    }
    
    private void submitVote() {
        switch (poll.getVotingMode()) {
            case SINGLE_CHOICE:
                submitSingleChoiceVote();
                break;
            case RANKED_CHOICE:
                submitRankedChoiceVote();
                break;
            case RANDOM_SPINNER:
                // For random spinner, just go to results
                goToResults();
                break;
        }
    }
    
    private void submitSingleChoiceVote() {
        int checkedId = radioGroupVoteOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn một lựa chọn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        RadioButton selectedRadioButton = findViewById(checkedId);
        String selectedOption = selectedRadioButton.getText().toString();
        
        Vote vote = new Vote(userId, selectedOption);
        poll.addVote(userId, vote);
        storage.savePoll(poll);
        
        Toast.makeText(this, "Đã ghi nhận bình chọn của bạn!", Toast.LENGTH_SHORT).show();
        goToResults();
    }
    
    private void submitRankedChoiceVote() {
        List<String> rankings = rankingAdapter.getRankedOptions();
        if (rankings.isEmpty()) {
            Toast.makeText(this, "Vui lòng sắp xếp các lựa chọn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Vote vote = new Vote(userId, rankings);
        poll.addVote(userId, vote);
        storage.savePoll(poll);
        
        Toast.makeText(this, "Đã ghi nhận xếp hạng của bạn!", Toast.LENGTH_SHORT).show();
        goToResults();
    }
    
    private void goToResults() {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("poll_id", poll.getId());
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}