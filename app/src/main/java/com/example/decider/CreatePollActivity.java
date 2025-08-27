package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreatePollActivity extends AppCompatActivity {
    
    private TextInputEditText editTextQuestion;
    private RecyclerView recyclerViewOptions;
    private Button buttonAddOption;
    private RadioGroup radioGroupMode;
    private SwitchMaterial switchAutoLock;
    private LinearLayout layoutTimerSettings;
    private EditText editTextTimer;
    private Button buttonCreatePoll;
    private Button buttonUseTemplate;
    private Button buttonSaveTemplate;
    private Button buttonBack;
    
    private PollOptionsAdapter optionsAdapter;
    private List<String> options;
    private PollStorage storage;
    private PollTemplate selectedTemplate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_poll);
            
            storage = new PollStorage(this);
            options = new ArrayList<>();
            initializeViews();
            setupUI();
            
            // Check if we're creating from a template
            String templateId = getIntent().getStringExtra("template_id");
            if (templateId != null) {
                loadTemplate(templateId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            editTextQuestion = findViewById(R.id.edit_text_question);
            recyclerViewOptions = findViewById(R.id.recycler_view_options);
            buttonAddOption = findViewById(R.id.button_add_option);
            radioGroupMode = findViewById(R.id.radio_group_mode);
            switchAutoLock = findViewById(R.id.switch_auto_lock);
            layoutTimerSettings = findViewById(R.id.layout_timer_settings);
            editTextTimer = findViewById(R.id.edit_text_timer);
            buttonCreatePoll = findViewById(R.id.button_create_poll);
            buttonUseTemplate = findViewById(R.id.button_use_template);
            buttonSaveTemplate = findViewById(R.id.button_save_template);
            buttonBack = findViewById(R.id.button_back);
            
            // Validate that all views were found
            if (editTextQuestion == null || recyclerViewOptions == null || buttonAddOption == null ||
                radioGroupMode == null || switchAutoLock == null || layoutTimerSettings == null ||
                editTextTimer == null || buttonCreatePoll == null || buttonUseTemplate == null ||
                buttonSaveTemplate == null || buttonBack == null) {
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
    
    private void setupUI() {
        try {
            // Setup options RecyclerView
            if (optionsAdapter == null) {
                optionsAdapter = new PollOptionsAdapter(options, this::removeOption);
            }
            if (recyclerViewOptions != null) {
                recyclerViewOptions.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewOptions.setAdapter(optionsAdapter);
            }
            
            // Add default options
            addOption("Lựa chọn 1");
            addOption("Lựa chọn 2");
            
            // Setup click listeners
            if (buttonAddOption != null) {
                buttonAddOption.setOnClickListener(v -> addOption(""));
            }
            
            if (buttonBack != null) {
                buttonBack.setOnClickListener(v -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            
            if (buttonUseTemplate != null) {
                buttonUseTemplate.setOnClickListener(v -> {
                    if (storage != null && storage.hasTemplates()) {
                        Intent intent = new Intent(CreatePollActivity.this, TemplateLibraryActivity.class);
                        intent.putExtra("select_mode", true);
                        startActivityForResult(intent, 100);
                    } else {
                        Toast.makeText(CreatePollActivity.this, "Chưa có mẫu nào được lưu", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            if (buttonSaveTemplate != null) {
                buttonSaveTemplate.setOnClickListener(v -> saveCurrentAsTemplate());
            }
            
            // Setup auto timer switch
            if (switchAutoLock != null && layoutTimerSettings != null) {
                switchAutoLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    layoutTimerSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                });
            }
            
            // Setup create poll button
            if (buttonCreatePoll != null) {
                buttonCreatePoll.setOnClickListener(v -> createPoll());
            }
            
            // Add text watcher for question field
            if (editTextQuestion != null) {
                editTextQuestion.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateCreateButtonState();
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi thiết lập giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void addOption(String text) {
        try {
            if (options != null && optionsAdapter != null) {
                options.add(text != null ? text : "");
                optionsAdapter.notifyItemInserted(options.size() - 1);
                updateCreateButtonState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void removeOption(int position) {
        try {
            if (options != null && options.size() > 2 && position >= 0 && position < options.size()) { // Keep at least 2 options
                options.remove(position);
                if (optionsAdapter != null) {
                    optionsAdapter.notifyItemRemoved(position);
                }
                updateCreateButtonState();
            } else {
                Toast.makeText(this, "Cần ít nhất 2 lựa chọn", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateCreateButtonState() {
        try {
            if (editTextQuestion == null || buttonCreatePoll == null) {
                return;
            }
            
            String question = editTextQuestion.getText().toString().trim();
            boolean hasValidOptions = false;
            if (options != null && options.size() >= 2) {
                for (String option : options) {
                    if (option != null && !option.trim().isEmpty()) {
                        hasValidOptions = true;
                        break;
                    }
                }
            }
            
            buttonCreatePoll.setEnabled(!question.isEmpty() && hasValidOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createPoll() {
        try {
            // Validate input fields
            if (editTextQuestion == null) {
                Toast.makeText(this, "Lỗi: Không thể truy cập trường câu hỏi", Toast.LENGTH_LONG).show();
                return;
            }
            
            String question = editTextQuestion.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập câu hỏi", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Filter out empty options
            List<String> validOptions = new ArrayList<>();
            if (options != null) {
                for (String option : options) {
                    if (option != null && !option.trim().isEmpty()) {
                        validOptions.add(option.trim());
                    }
                }
            }
            
            if (validOptions.size() < 2) {
                Toast.makeText(this, "Cần ít nhất 2 lựa chọn có nội dung", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Determine voting mode
            Poll.VotingMode votingMode = Poll.VotingMode.SINGLE_CHOICE;
            if (radioGroupMode != null) {
                int checkedId = radioGroupMode.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_ranked_choice) {
                    votingMode = Poll.VotingMode.RANKED_CHOICE;
                } else if (checkedId == R.id.radio_random_spinner) {
                    votingMode = Poll.VotingMode.RANDOM_SPINNER;
                }
            }
            
            // Create poll with safe parameters
            Poll poll = new Poll("poll_" + System.currentTimeMillis(), question, validOptions, votingMode);
            
            // Set timer if enabled
            if (switchAutoLock != null && switchAutoLock.isChecked() && editTextTimer != null) {
                try {
                    String timerText = editTextTimer.getText().toString();
                    if (!timerText.isEmpty()) {
                        int timerMinutes = Integer.parseInt(timerText);
                        if (timerMinutes > 0) {
                            poll.setHasTimer(true);
                            poll.setTimerMinutes(timerMinutes);
                        } else {
                            Toast.makeText(this, "Vui lòng nhập số phút hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Vui lòng nhập số phút hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Save poll
            if (storage != null) {
                storage.savePoll(poll);
                storage.setCurrentPoll(poll);
            }
            
            Toast.makeText(this, "Đã tạo cuộc bình chọn thành công!", Toast.LENGTH_SHORT).show();
            
            // Go to results screen to show poll code and manage
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("poll_id", poll.getId());
            intent.putExtra("is_creator", true);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            // Log error and show user-friendly message
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi tạo cuộc bình chọn: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadTemplate(String templateId) {
        try {
            if (templateId == null || storage == null) {
                return;
            }
            
            PollTemplate template = storage.getTemplateById(templateId);
            if (template != null) {
                selectedTemplate = template;
                
                if (editTextQuestion != null) {
                    editTextQuestion.setText(template.getQuestion());
                }
                
                // Clear existing options and add template options
                if (options != null) {
                    options.clear();
                    if (template.getOptions() != null) {
                        options.addAll(template.getOptions());
                    }
                    if (optionsAdapter != null) {
                        optionsAdapter.notifyDataSetChanged();
                    }
                }
                
                // Set voting mode
                if (radioGroupMode != null) {
                    switch (template.getDefaultVotingMode()) {
                        case SINGLE_CHOICE:
                            radioGroupMode.check(R.id.radio_single_choice);
                            break;
                        case RANKED_CHOICE:
                            radioGroupMode.check(R.id.radio_ranked_choice);
                            break;
                        case RANDOM_SPINNER:
                            radioGroupMode.check(R.id.radio_random_spinner);
                            break;
                    }
                }
                
                // Set timer
                if (switchAutoLock != null) {
                    switchAutoLock.setChecked(template.isHasDefaultTimer());
                    if (template.isHasDefaultTimer() && editTextTimer != null) {
                        editTextTimer.setText(String.valueOf(template.getDefaultTimerMinutes()));
                    }
                }
                
                updateCreateButtonState();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi tải mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
                String templateId = data.getStringExtra("selected_template_id");
                if (templateId != null) {
                    loadTemplate(templateId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveCurrentAsTemplate() {
        try {
            // Validate input
            if (editTextQuestion == null) {
                Toast.makeText(this, "Lỗi: Không thể truy cập trường câu hỏi", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String question = editTextQuestion.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập câu hỏi trước khi lưu mẫu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Filter out empty options
            List<String> validOptions = new ArrayList<>();
            if (options != null) {
                for (String option : options) {
                    if (option != null && !option.trim().isEmpty()) {
                        validOptions.add(option.trim());
                    }
                }
            }
            
            if (validOptions.size() < 2) {
                Toast.makeText(this, "Cần ít nhất 2 lựa chọn có nội dung để lưu mẫu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show dialog to get template name
            showSaveTemplateDialog(question, validOptions);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi lưu mẫu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSaveTemplateDialog(String question, List<String> validOptions) {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            
            // Create custom dialog layout
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 40, 50, 40);
            
            // Template name input
            com.google.android.material.textfield.TextInputLayout inputLayout = 
                new com.google.android.material.textfield.TextInputLayout(this);
            inputLayout.setHint("Tên mẫu (VD: Ăn trưa hàng ngày)");
            
            com.google.android.material.textfield.TextInputEditText editTextTemplateName = 
                new com.google.android.material.textfield.TextInputEditText(this);
            editTextTemplateName.setText(question); // Default to question text
            editTextTemplateName.selectAll();
            
            inputLayout.addView(editTextTemplateName);
            dialogLayout.addView(inputLayout);
            
            builder.setView(dialogLayout)
                    .setTitle("💾 Lưu mẫu")
                    .setMessage("Lưu cấu hình hiện tại thành mẫu để sử dụng lại sau này")
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        try {
                            String templateName = editTextTemplateName.getText().toString().trim();
                            if (!templateName.isEmpty()) {
                                saveTemplate(templateName, question, validOptions);
                            } else {
                                Toast.makeText(this, "Vui lòng nhập tên mẫu", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi khi lưu mẫu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
                    
            // Focus on input field
            editTextTemplateName.requestFocus();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveTemplate(String templateName, String question, List<String> validOptions) {
        try {
            if (storage == null) {
                Toast.makeText(this, "Lỗi: Không thể truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create template
            PollTemplate template = new PollTemplate(templateName, question, validOptions);
            
            // Set voting mode
            if (radioGroupMode != null) {
                int checkedId = radioGroupMode.getCheckedRadioButtonId();
                if (checkedId == R.id.radio_ranked_choice) {
                    template.setDefaultVotingMode(Poll.VotingMode.RANKED_CHOICE);
                } else if (checkedId == R.id.radio_random_spinner) {
                    template.setDefaultVotingMode(Poll.VotingMode.RANDOM_SPINNER);
                } else {
                    template.setDefaultVotingMode(Poll.VotingMode.SINGLE_CHOICE);
                }
            }
            
            // Set timer settings
            if (switchAutoLock != null && switchAutoLock.isChecked() && editTextTimer != null) {
                try {
                    String timerText = editTextTimer.getText().toString();
                    if (!timerText.isEmpty()) {
                        int timerMinutes = Integer.parseInt(timerText);
                        if (timerMinutes > 0) {
                            template.setHasDefaultTimer(true);
                            template.setDefaultTimerMinutes(timerMinutes);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Timer parsing failed, keep default (no timer)
                }
            }
            
            // Save template
            storage.saveTemplate(template);
            
            Toast.makeText(this, "✅ Đã lưu mẫu \"" + templateName + "\" thành công!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}