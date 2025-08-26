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
    
    private PollOptionsAdapter optionsAdapter;
    private List<String> options;
    private PollStorage storage;
    private PollTemplate selectedTemplate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }
    
    private void initializeViews() {
        editTextQuestion = findViewById(R.id.edit_text_question);
        recyclerViewOptions = findViewById(R.id.recycler_view_options);
        buttonAddOption = findViewById(R.id.button_add_option);
        radioGroupMode = findViewById(R.id.radio_group_mode);
        switchAutoLock = findViewById(R.id.switch_auto_lock);
        layoutTimerSettings = findViewById(R.id.layout_timer_settings);
        editTextTimer = findViewById(R.id.edit_text_timer);
        buttonCreatePoll = findViewById(R.id.button_create_poll);
        buttonUseTemplate = findViewById(R.id.button_use_template);
    }
    
    private void setupUI() {
        // Setup options RecyclerView
        optionsAdapter = new PollOptionsAdapter(options, this::removeOption);
        recyclerViewOptions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOptions.setAdapter(optionsAdapter);
        
        // Add default options
        addOption("Lựa chọn 1");
        addOption("Lựa chọn 2");
        
        // Setup click listeners
        buttonAddOption.setOnClickListener(v -> addOption(""));
        
        buttonUseTemplate.setOnClickListener(v -> {
            if (storage.hasTemplates()) {
                Intent intent = new Intent(this, TemplateLibraryActivity.class);
                intent.putExtra("select_mode", true);
                startActivityForResult(intent, 100);
            } else {
                Toast.makeText(this, "Chưa có mẫu nào được lưu", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup auto timer switch
        switchAutoLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutTimerSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        // Setup create poll button
        buttonCreatePoll.setOnClickListener(v -> createPoll());
        
        // Add text watcher for question field
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
    
    private void addOption(String text) {
        options.add(text);
        optionsAdapter.notifyItemInserted(options.size() - 1);
        updateCreateButtonState();
    }
    
    private void removeOption(int position) {
        if (options.size() > 2) { // Keep at least 2 options
            options.remove(position);
            optionsAdapter.notifyItemRemoved(position);
            updateCreateButtonState();
        } else {
            Toast.makeText(this, "Cần ít nhất 2 lựa chọn", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateCreateButtonState() {
        String question = editTextQuestion.getText().toString().trim();
        boolean hasValidOptions = options.size() >= 2 && 
            options.stream().anyMatch(option -> !option.trim().isEmpty());
        
        buttonCreatePoll.setEnabled(!question.isEmpty() && hasValidOptions);
    }
    
    private void createPoll() {
        String question = editTextQuestion.getText().toString().trim();
        
        // Filter out empty options
        List<String> validOptions = new ArrayList<>();
        for (String option : options) {
            if (!option.trim().isEmpty()) {
                validOptions.add(option.trim());
            }
        }
        
        if (validOptions.size() < 2) {
            Toast.makeText(this, "Cần ít nhất 2 lựa chọn có nội dung", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Determine voting mode
        Poll.VotingMode votingMode = Poll.VotingMode.SINGLE_CHOICE;
        int checkedId = radioGroupMode.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_ranked_choice) {
            votingMode = Poll.VotingMode.RANKED_CHOICE;
        } else if (checkedId == R.id.radio_random_spinner) {
            votingMode = Poll.VotingMode.RANDOM_SPINNER;
        }
        
        // Create poll
        Poll poll = new Poll("poll_" + System.currentTimeMillis(), question, validOptions, votingMode);
        
        // Set timer if enabled
        if (switchAutoLock.isChecked()) {
            try {
                int timerMinutes = Integer.parseInt(editTextTimer.getText().toString());
                poll.setHasTimer(true);
                poll.setTimerMinutes(timerMinutes);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số phút hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Save poll
        storage.savePoll(poll);
        storage.setCurrentPoll(poll);
        
        Toast.makeText(this, "Đã tạo cuộc bình chọn thành công!", Toast.LENGTH_SHORT).show();
        
        // Go to results screen to show poll code and manage
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("poll_id", poll.getId());
        intent.putExtra("is_creator", true);
        startActivity(intent);
        finish();
    }
    
    private void loadTemplate(String templateId) {
        PollTemplate template = storage.getTemplateById(templateId);
        if (template != null) {
            selectedTemplate = template;
            editTextQuestion.setText(template.getQuestion());
            
            // Clear existing options and add template options
            options.clear();
            options.addAll(template.getOptions());
            optionsAdapter.notifyDataSetChanged();
            
            // Set voting mode
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
            
            // Set timer
            switchAutoLock.setChecked(template.isHasDefaultTimer());
            if (template.isHasDefaultTimer()) {
                editTextTimer.setText(String.valueOf(template.getDefaultTimerMinutes()));
            }
            
            updateCreateButtonState();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String templateId = data.getStringExtra("selected_template_id");
            if (templateId != null) {
                loadTemplate(templateId);
            }
        }
    }
}