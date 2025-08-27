package com.example.decider;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

public class ResultsActivity extends AppCompatActivity {
    
    private TextView textViewQuestionResult;
    private FrameLayout frameLayoutResultContainer;
    private RecyclerView recyclerViewResults;
    private ImageView imageViewSpinnerWheel;
    private Button buttonSpin;
    private Button buttonSaveTemplate;
    private Button buttonShowInviteCode;
    private Button buttonBack;
    
    private Poll poll;
    private PollStorage storage;
    private boolean isCreator;
    private ResultsAdapter resultsAdapter;
    private Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        
        storage = new PollStorage(this);
        isCreator = getIntent().getBooleanExtra("is_creator", false);
        
        initializeViews();
        loadPoll();
        setupUI();
    }
    
    private void initializeViews() {
        textViewQuestionResult = findViewById(R.id.text_view_question_result);
        frameLayoutResultContainer = findViewById(R.id.frame_layout_result_container);
        recyclerViewResults = findViewById(R.id.recycler_view_results);
        imageViewSpinnerWheel = findViewById(R.id.image_view_spinner_wheel);
        buttonSpin = findViewById(R.id.button_spin);
        buttonSaveTemplate = findViewById(R.id.button_save_template);
        buttonShowInviteCode = findViewById(R.id.button_show_invite_code);
        buttonBack = findViewById(R.id.button_back);
    }
    
    private void loadPoll() {
        String pollId = getIntent().getStringExtra("poll_id");
        if (pollId != null) {
            poll = storage.getPollById(pollId);
        } else {
            poll = storage.getCurrentPoll();
        }
        
        if (poll == null) {
            Toast.makeText(this, "Cuộc bình chọn không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    
    private void setupUI() {
        textViewQuestionResult.setText(poll.getQuestion());
        
        // Close poll if not already closed
        if (poll.isActive() && poll.isExpired()) {
            poll.closePoll();
            storage.savePoll(poll);
        }
        
        // Show invite code if this is a newly created poll
        if (isCreator && poll.isActive()) {
            showInviteCodeDialog();
        }
        
        // Setup based on voting mode
        switch (poll.getVotingMode()) {
            case SINGLE_CHOICE:
            case RANKED_CHOICE:
                showRegularResults();
                break;
            case RANDOM_SPINNER:
                showSpinnerMode();
                break;
        }
        
        // Setup save template button (only for creator)
        if (isCreator) {
            buttonSaveTemplate.setVisibility(View.VISIBLE);
            buttonSaveTemplate.setOnClickListener(v -> saveAsTemplate());
            
            // Show invite code button for active polls
            if (poll.isActive()) {
                buttonShowInviteCode.setVisibility(View.VISIBLE);
                buttonShowInviteCode.setOnClickListener(v -> showInviteCodeDialog());
            } else {
                buttonShowInviteCode.setVisibility(View.GONE);
            }
        } else {
            buttonSaveTemplate.setVisibility(View.GONE);
            buttonShowInviteCode.setVisibility(View.GONE);
        }

        // Setup back button
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
    
    private void showInviteCodeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🎉 Cuộc bình chọn đã được tạo!")
            .setMessage("Mã mời: " + poll.getInviteCode() + "\n\nChia sẻ mã này với người khác để họ có thể tham gia bình chọn.")
            .setPositiveButton("Sao chép mã", (dialog, which) -> {
                // Copy invite code to clipboard
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = 
                    android.content.ClipData.newPlainText("Mã mời", poll.getInviteCode());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép mã mời vào clipboard", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("Chia sẻ", (dialog, which) -> {
                // Share invite code
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mã mời tham gia bình chọn");
                shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "Mã mời: " + poll.getInviteCode() + "\n\nHãy nhập mã này vào ứng dụng Decider để tham gia bình chọn: " + poll.getQuestion());
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"));
            })
            .setNegativeButton("Đóng", null)
            .setCancelable(false)
            .show();
    }
    
    private void showRegularResults() {
        recyclerViewResults.setVisibility(View.VISIBLE);
        imageViewSpinnerWheel.setVisibility(View.GONE);
        buttonSpin.setVisibility(View.GONE);
        
        // Calculate and show results
        poll.closePoll();
        List<String> results = poll.getResults();
        
        resultsAdapter = new ResultsAdapter(results, poll);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(resultsAdapter);
        
        // Check for ties and show magic hat option
        if (poll.hasTiedResults() && isCreator) {
            showMagicHatOption();
        }
    }
    
    private void showSpinnerMode() {
        recyclerViewResults.setVisibility(View.GONE);
        imageViewSpinnerWheel.setVisibility(View.VISIBLE);
        buttonSpin.setVisibility(View.VISIBLE);
        
        buttonSpin.setText("Quay Thưởng!");
        buttonSpin.setOnClickListener(v -> spinWheel());
        
        // Create dynamic spinner wheel
        createSpinnerWheel();
    }
    
    private void createSpinnerWheel() {
        // For now, use the placeholder spinner
        // In a real implementation, you would dynamically create a wheel
        // with the poll options as segments
        imageViewSpinnerWheel.setImageResource(R.drawable.ic_spinner_placeholder);
    }
    
    private void spinWheel() {
        buttonSpin.setEnabled(false);
        buttonSpin.setText("Đang quay...");
        
        // Calculate random rotation (multiple full rotations + final position)
        int baseRotations = 3 + random.nextInt(3); // 3-5 full rotations
        float finalAngle = random.nextFloat() * 360; // Random final position
        float totalRotation = baseRotations * 360 + finalAngle;
        
        // Animate the wheel
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(
            imageViewSpinnerWheel, "rotation", 0f, totalRotation);
        rotationAnimator.setDuration(3000); // 3 seconds
        rotationAnimator.setInterpolator(new DecelerateInterpolator());
        
        rotationAnimator.start();
        
        // Show result after animation
        new Handler().postDelayed(() -> {
            showSpinnerResult(finalAngle);
        }, 3000);
    }
    
    private void showSpinnerResult(float finalAngle) {
        // Calculate which option won based on final angle
        List<String> options = poll.getOptions();
        float segmentAngle = 360f / options.size();
        int winnerIndex = (int) ((360 - finalAngle) / segmentAngle) % options.size();
        String winner = options.get(winnerIndex);
        
        // Show result dialog
        new AlertDialog.Builder(this)
            .setTitle("🎉 Kết quả quay thưởng!")
            .setMessage("Chiến thắng thuộc về:\n\n\"" + winner + "\"")
            .setPositiveButton("Tuyệt vời!", (dialog, which) -> {
                // Update poll with result
                poll.getResults().clear();
                poll.getResults().add(winner);
                poll.getResults().addAll(options.stream()
                    .filter(o -> !o.equals(winner))
                    .toList());
                storage.savePoll(poll);
                
                // Switch to regular results view
                showFinalSpinnerResults(winner);
            })
            .setCancelable(false)
            .show();
    }
    
    private void showFinalSpinnerResults(String winner) {
        imageViewSpinnerWheel.setVisibility(View.GONE);
        buttonSpin.setVisibility(View.GONE);
        recyclerViewResults.setVisibility(View.VISIBLE);
        
        resultsAdapter = new ResultsAdapter(poll.getResults(), poll);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(resultsAdapter);
    }
    
    private void showMagicHatOption() {
        new AlertDialog.Builder(this)
            .setTitle("🎩 Kết quả hòa!")
            .setMessage("Có vẻ như kết quả đang hòa nhau. Bạn có muốn sử dụng \"Chiếc Nón Kỳ Diệu\" để tìm ra người chiến thắng cuối cùng không?")
            .setPositiveButton("Dùng Nón Kỳ Diệu", (dialog, which) -> useMagicHat())
            .setNegativeButton("Giữ kết quả hòa", null)
            .show();
    }
    
    private void useMagicHat() {
        // Get tied options
        List<String> results = poll.getResults();
        List<String> tiedOptions = results.subList(0, Math.min(2, results.size()));
        
        // Show magic hat animation
        new AlertDialog.Builder(this)
            .setTitle("🎩✨ Chiếc Nón Kỳ Diệu đang hoạt động...")
            .setMessage("Đang rút thăm...")
            .setCancelable(false)
            .create()
            .show();
        
        // Simulate magic hat delay
        new Handler().postDelayed(() -> {
            String magicWinner = tiedOptions.get(random.nextInt(tiedOptions.size()));
            
            // Update results with magic hat winner
            results.remove(magicWinner);
            results.add(0, magicWinner);
            poll.setResults(results);
            storage.savePoll(poll);
            
            // Show magic result
            new AlertDialog.Builder(this)
                .setTitle("🎩✨ Chiếc Nón Kỳ Diệu đã quyết định!")
                .setMessage("Người chiến thắng là:\n\n\"" + magicWinner + "\"")
                .setPositiveButton("Chấp nhận kết quả", (dialog, which) -> {
                    resultsAdapter.notifyDataSetChanged();
                })
                .setCancelable(false)
                .show();
        }, 2000);
    }
    
    private void saveAsTemplate() {
        // Show dialog to get template name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_template, null);
        builder.setView(dialogView);
        
        TextView editTextTemplateName = dialogView.findViewById(R.id.edit_text_template_name);
        editTextTemplateName.setText(poll.getQuestion());
        
        builder.setTitle("Lưu mẫu")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String templateName = editTextTemplateName.getText().toString().trim();
                    if (!templateName.isEmpty()) {
                        PollTemplate template = new PollTemplate(poll);
                        template.setName(templateName);
                        storage.saveTemplate(template);
                        Toast.makeText(this, "Đã lưu mẫu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên mẫu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}