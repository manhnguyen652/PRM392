package com.example.decider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TemplateLibraryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerViewTemplates;
    private TextView textViewEmpty;
    private TemplateAdapter templateAdapter;
    private PollStorage storage;
    private boolean isSelectMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_library);
        
        storage = new PollStorage(this);
        isSelectMode = getIntent().getBooleanExtra("select_mode", false);
        
        initializeViews();
        setupUI();
        loadTemplates();
    }
    
    private void initializeViews() {
        recyclerViewTemplates = findViewById(R.id.recycler_view_templates);
        textViewEmpty = findViewById(R.id.text_view_empty);
    }
    
    private void setupUI() {
        setTitle(isSelectMode ? "Chọn mẫu" : "Thư viện mẫu");
        
        recyclerViewTemplates.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void loadTemplates() {
        List<PollTemplate> templates = storage.getAllTemplates();
        
        if (templates.isEmpty()) {
            recyclerViewTemplates.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTemplates.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
            
            templateAdapter = new TemplateAdapter(templates, new TemplateAdapter.TemplateActionListener() {
                @Override
                public void onTemplateClick(PollTemplate template) {
                    if (isSelectMode) {
                        // Return selected template
                        Intent result = new Intent();
                        result.putExtra("selected_template_id", template.getId());
                        setResult(RESULT_OK, result);
                        finish();
                    } else {
                        // Navigate to create poll with template
                        Intent intent = new Intent(TemplateLibraryActivity.this, CreatePollActivity.class);
                        intent.putExtra("template_id", template.getId());
                        startActivity(intent);
                    }
                }
                
                @Override
                public void onTemplateDelete(PollTemplate template) {
                    storage.deleteTemplate(template.getId());
                    loadTemplates(); // Refresh list
                    Toast.makeText(TemplateLibraryActivity.this, "Đã xóa mẫu", Toast.LENGTH_SHORT).show();
                }
            });
            
            recyclerViewTemplates.setAdapter(templateAdapter);
        }
    }
}