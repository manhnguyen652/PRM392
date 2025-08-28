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
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {
    
    private TextView textViewQuestionResult;
    private FrameLayout frameLayoutResultContainer;
    private RecyclerView recyclerViewResults;
    private ImageView imageViewSpinnerWheel;
    private SpinningWheelView spinningWheelView;
    private Button buttonSpin;
    private Button buttonSaveTemplate;
    private Button buttonShowInviteCode;
    private Button buttonBack;
    private Button buttonEndPoll;
    
    private Poll poll;
    private PollStorage storage;
    private boolean isCreator;
    private ResultsAdapter resultsAdapter;
    private Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_results);
            
            storage = new PollStorage(this);
            isCreator = getIntent().getBooleanExtra("is_creator", false);
            
            initializeViews();
            loadPoll();
            setupUI();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            textViewQuestionResult = findViewById(R.id.text_view_question_result);
            frameLayoutResultContainer = findViewById(R.id.frame_layout_result_container);
            recyclerViewResults = findViewById(R.id.recycler_view_results);
            imageViewSpinnerWheel = findViewById(R.id.image_view_spinner_wheel);
            spinningWheelView = findViewById(R.id.spinning_wheel_view);
            buttonSpin = findViewById(R.id.button_spin);
            buttonSaveTemplate = findViewById(R.id.button_save_template);
            buttonShowInviteCode = findViewById(R.id.button_show_invite_code);
            buttonBack = findViewById(R.id.button_back);
            buttonEndPoll = findViewById(R.id.button_end_poll);
            
            // Validate that all views were found
            if (textViewQuestionResult == null || frameLayoutResultContainer == null || 
                recyclerViewResults == null || imageViewSpinnerWheel == null || 
                spinningWheelView == null || buttonSpin == null || buttonSaveTemplate == null || 
                buttonShowInviteCode == null || buttonBack == null) {
                Toast.makeText(this, "Lỗi: Không thể khởi tạo giao diện", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi: Không thể khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void loadPoll() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi tải cuộc bình chọn: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupUI() {
        try {
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

                // End poll button
                if (buttonEndPoll != null) {
                    if (poll.isActive()) {
                        buttonEndPoll.setVisibility(View.VISIBLE);
                        buttonEndPoll.setOnClickListener(v -> confirmEndPoll());
                    } else {
                        buttonEndPoll.setVisibility(View.GONE);
                    }
                }
            } else {
                buttonSaveTemplate.setVisibility(View.GONE);
                buttonShowInviteCode.setVisibility(View.GONE);
                if (buttonEndPoll != null) {
                    buttonEndPoll.setVisibility(View.GONE);
                }
            }

            // Setup back button
            buttonBack.setOnClickListener(v -> {
                Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void showInviteCodeDialog() {
        try {
            new AlertDialog.Builder(this)
                .setTitle("🎉 Cuộc bình chọn đã được tạo!")
                .setMessage("Mã mời: " + poll.getInviteCode() + "\n\nChia sẻ mã này với người khác để họ có thể tham gia bình chọn.")
                .setPositiveButton("Sao chép mã", (dialog, which) -> {
                    try {
                        // Copy invite code to clipboard
                        android.content.ClipboardManager clipboard = 
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = 
                            android.content.ClipData.newPlainText("Mã mời", poll.getInviteCode());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Đã sao chép mã mời vào clipboard", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Không thể sao chép mã mời", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Chia sẻ", (dialog, which) -> {
                    try {
                        // Share invite code
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mã mời tham gia bình chọn");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, 
                            "Mã mời: " + poll.getInviteCode() + "\n\nHãy nhập mã này vào ứng dụng Decider để tham gia bình chọn: " + poll.getQuestion());
                        startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Không thể chia sẻ mã mời", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .setCancelable(false)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi hiển thị mã mời", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showRegularResults() {
        try {
            recyclerViewResults.setVisibility(View.VISIBLE);
            imageViewSpinnerWheel.setVisibility(View.GONE);
            buttonSpin.setVisibility(View.GONE);
            
            // Chỉ đóng poll nếu đã hết hạn
            if (poll != null && poll.isActive() && poll.isExpired()) {
                poll.closePoll();
                if (storage != null) {
                    storage.savePoll(poll);
                }
            }
            
            // Lấy kết quả hiện tại
            List<String> results = null;
            if (poll != null) {
                results = poll.getResults();
            }
            
            if (results == null || results.isEmpty()) {
                // Nếu chưa có kết quả, hiển thị options theo thứ tự
                if (poll != null && poll.getOptions() != null) {
                    results = new ArrayList<>(poll.getOptions());
                } else {
                    results = new ArrayList<>();
                }
            }
            
            if (resultsAdapter == null) {
                resultsAdapter = new ResultsAdapter(results, poll);
            } else {
                resultsAdapter.updateResults(results);
            }
            
            if (recyclerViewResults != null) {
                recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewResults.setAdapter(resultsAdapter);
            }
            
            // Check for ties and show magic hat option
            if (poll != null && poll.hasTiedResults() && isCreator) {
                showMagicHatOption();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi hiển thị kết quả: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmEndPoll() {
        try {
            new AlertDialog.Builder(this)
                .setTitle("Kết thúc cuộc bình chọn?")
                .setMessage("Sau khi kết thúc, kết quả sẽ được chốt và lưu lại.")
                .setPositiveButton("Kết thúc", (dialog, which) -> endPollAndPersist())
                .setNegativeButton("Hủy", null)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endPollAndPersist() {
        try {
            if (poll == null || storage == null) return;
            if (!poll.isActive()) return;

            // Close poll, compute results, mark end time
            poll.closePoll();

            // Persist updated poll with final results/end time
            storage.savePoll(poll);

            // Update UI: hide invite, end poll button, refresh results
            if (buttonShowInviteCode != null) {
                buttonShowInviteCode.setVisibility(View.GONE);
            }
            if (buttonEndPoll != null) {
                buttonEndPoll.setVisibility(View.GONE);
            }

            showRegularResults();
            Toast.makeText(this, "Đã kết thúc và lưu kết quả", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể kết thúc cuộc bình chọn", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSpinnerMode() {
        try {
            if (recyclerViewResults != null) {
                recyclerViewResults.setVisibility(View.GONE);
            }
            if (imageViewSpinnerWheel != null) {
                imageViewSpinnerWheel.setVisibility(View.GONE);
            }
            if (spinningWheelView != null) {
                spinningWheelView.setVisibility(View.VISIBLE);
                spinningWheelView.setOptions(poll.getOptions());
                spinningWheelView.setOnSpinCompleteListener(this::onSpinComplete);
            }
            if (buttonSpin != null) {
                buttonSpin.setVisibility(View.VISIBLE);
                buttonSpin.setText("Quay Thưởng!");
                buttonSpin.setOnClickListener(v -> spinWheel());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void spinWheel() {
        try {
            if (buttonSpin == null || spinningWheelView == null) {
                return;
            }
            
            buttonSpin.setEnabled(false);
            buttonSpin.setText("Đang quay...");
            
            // Use the custom spinning wheel view
            spinningWheelView.spin();
        } catch (Exception e) {
            e.printStackTrace();
            if (buttonSpin != null) {
                buttonSpin.setEnabled(true);
                buttonSpin.setText("Quay Thưởng!");
            }
        }
    }
    
    private void onSpinComplete(String selectedOption) {
        try {
            // Re-enable the spin button
            if (buttonSpin != null) {
                buttonSpin.setEnabled(true);
                buttonSpin.setText("Quay Thưởng!");
            }
            
            // Show result dialog
            new AlertDialog.Builder(this)
                .setTitle("🎉 Kết quả quay thưởng!")
                .setMessage("Chiến thắng thuộc về:\n\n\"" + selectedOption + "\"")
                .setPositiveButton("Tuyệt vời!", (dialog, which) -> {
                    try {
                        // Update poll with result
                        List<String> options = new ArrayList<>(poll.getOptions());
                        List<String> results = new ArrayList<>();
                        results.add(selectedOption);
                        
                        // Add other options in random order
                        options.remove(selectedOption);
                        java.util.Collections.shuffle(options);
                        results.addAll(options);
                        
                        poll.setResults(results);
                        if (storage != null) {
                            storage.savePoll(poll);
                        }
                        
                        // Switch to regular results view
                        showFinalSpinnerResults(selectedOption);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setCancelable(false)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showSpinnerResult(float finalAngle) {
        try {
            // Calculate which option won based on final angle
            if (poll == null || poll.getOptions() == null || poll.getOptions().isEmpty()) {
                return;
            }
            
            List<String> options = poll.getOptions();
            float segmentAngle = 360f / options.size();
            int winnerIndex = (int) ((360 - finalAngle) / segmentAngle) % options.size();
            
            if (winnerIndex >= 0 && winnerIndex < options.size()) {
                String winner = options.get(winnerIndex);
                
                // Show result dialog
                new AlertDialog.Builder(this)
                    .setTitle("🎉 Kết quả quay thưởng!")
                    .setMessage("Chiến thắng thuộc về:\n\n\"" + winner + "\"")
                    .setPositiveButton("Tuyệt vời!", (dialog, which) -> {
                        try {
                            // Update poll with result
                            if (poll.getResults() != null) {
                                poll.getResults().clear();
                                poll.getResults().add(winner);
                                poll.getResults().addAll(options.stream()
                                    .filter(o -> !o.equals(winner))
                                    .toList());
                                if (storage != null) {
                                    storage.savePoll(poll);
                                }
                            }
                            
                            // Switch to regular results view
                            showFinalSpinnerResults(winner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .setCancelable(false)
                    .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showFinalSpinnerResults(String winner) {
        try {
            if (imageViewSpinnerWheel != null) {
                imageViewSpinnerWheel.setVisibility(View.GONE);
            }
            if (spinningWheelView != null) {
                spinningWheelView.setVisibility(View.GONE);
            }
            if (buttonSpin != null) {
                buttonSpin.setVisibility(View.GONE);
            }
            if (recyclerViewResults != null) {
                recyclerViewResults.setVisibility(View.VISIBLE);
            }
            
            if (poll != null && poll.getResults() != null) {
                resultsAdapter = new ResultsAdapter(poll.getResults(), poll);
                if (recyclerViewResults != null) {
                    recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
                    recyclerViewResults.setAdapter(resultsAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showMagicHatOption() {
        try {
            new AlertDialog.Builder(this)
                .setTitle("🎩 Kết quả hòa!")
                .setMessage("Có vẻ như kết quả đang hòa nhau. Bạn có muốn sử dụng \"Chiếc Nón Kỳ Diệu\" để tìm ra người chiến thắng cuối cùng không?")
                .setPositiveButton("Dùng Nón Kỳ Diệu", (dialog, which) -> {
                    try {
                        useMagicHat();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Giữ kết quả hòa", null)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void useMagicHat() {
        try {
            // Get tied options
            if (poll == null || poll.getResults() == null || poll.getResults().size() < 2) {
                return;
            }
            
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
                try {
                    if (tiedOptions != null && !tiedOptions.isEmpty()) {
                        String magicWinner = tiedOptions.get(random.nextInt(tiedOptions.size()));
                        
                        // Update results with magic hat winner
                        if (results != null) {
                            results.remove(magicWinner);
                            results.add(0, magicWinner);
                            poll.setResults(results);
                            if (storage != null) {
                                storage.savePoll(poll);
                            }
                        }
                        
                        // Show magic result
                        new AlertDialog.Builder(this)
                            .setTitle("🎩✨ Chiếc Nón Kỳ Diệu đã quyết định!")
                            .setMessage("Người chiến thắng là:\n\n\"" + magicWinner + "\"")
                            .setPositiveButton("Chấp nhận kết quả", (dialog, which) -> {
                                try {
                                    if (resultsAdapter != null) {
                                        resultsAdapter.notifyDataSetChanged();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                            .setCancelable(false)
                            .show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveAsTemplate() {
        try {
            if (poll == null || storage == null) {
                return;
            }
            
            // Show dialog to get template name
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_template, null);
            builder.setView(dialogView);
            
            TextView editTextTemplateName = dialogView.findViewById(R.id.edit_text_template_name);
            if (editTextTemplateName != null && poll.getQuestion() != null) {
                editTextTemplateName.setText(poll.getQuestion());
            }
            
            builder.setTitle("Lưu mẫu")
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        try {
                            String templateName = "";
                            if (editTextTemplateName != null) {
                                templateName = editTextTemplateName.getText().toString().trim();
                            }
                            
                            if (!templateName.isEmpty()) {
                                PollTemplate template = new PollTemplate(poll);
                                template.setName(templateName);
                                storage.saveTemplate(template);
                                Toast.makeText(this, "Đã lưu mẫu thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Vui lòng nhập tên mẫu", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Có lỗi xảy ra khi lưu mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi hiển thị dialog lưu mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}