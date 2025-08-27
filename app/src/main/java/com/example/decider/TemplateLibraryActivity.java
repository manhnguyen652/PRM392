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

import java.util.List;

public class TemplateLibraryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerViewTemplates;
    private TextView textViewEmpty;
    private Button buttonBack;
    private TemplateAdapter templateAdapter;
    private PollStorage storage;
    private boolean isSelectMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_template_library);
            
            storage = new PollStorage(this);
            isSelectMode = getIntent().getBooleanExtra("select_mode", false);
            
            initializeViews();
            setupUI();
            loadTemplates();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra khi khởi tạo thư viện mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            recyclerViewTemplates = findViewById(R.id.recycler_view_templates);
            textViewEmpty = findViewById(R.id.text_view_empty);
            buttonBack = findViewById(R.id.button_back);
            
            // Validate that all views were found
            if (recyclerViewTemplates == null || textViewEmpty == null || buttonBack == null) {
                throw new RuntimeException("Không thể khởi tạo giao diện - thiếu view components");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khởi tạo views: " + e.getMessage());
        }
    }
    
    private void setupUI() {
        setTitle(isSelectMode ? "Chọn mẫu" : "Thư viện mẫu");
        
        recyclerViewTemplates.setLayoutManager(new LinearLayoutManager(this));
        
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    
    private void loadTemplates() {
        try {
            if (storage == null) {
                Toast.makeText(this, "Lỗi: Không thể truy cập dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            List<PollTemplate> templates = storage.getAllTemplates();
            
            if (templates == null || templates.isEmpty()) {
                if (recyclerViewTemplates != null) {
                    recyclerViewTemplates.setVisibility(View.GONE);
                }
                if (textViewEmpty != null) {
                    textViewEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                if (recyclerViewTemplates != null) {
                    recyclerViewTemplates.setVisibility(View.VISIBLE);
                }
                if (textViewEmpty != null) {
                    textViewEmpty.setVisibility(View.GONE);
                }
                
                templateAdapter = new TemplateAdapter(templates, new TemplateAdapter.TemplateActionListener() {
                    @Override
                    public void onTemplateClick(PollTemplate template) {
                        try {
                            if (template == null) {
                                Toast.makeText(TemplateLibraryActivity.this, "Lỗi: Mẫu không hợp lệ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(TemplateLibraryActivity.this, "Lỗi khi mở mẫu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onTemplateDelete(PollTemplate template) {
                        try {
                            if (template == null || template.getId() == null) {
                                Toast.makeText(TemplateLibraryActivity.this, "Lỗi: Không thể xóa mẫu", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            storage.deleteTemplate(template.getId());
                            loadTemplates(); // Refresh list
                            Toast.makeText(TemplateLibraryActivity.this, "Đã xóa mẫu", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(TemplateLibraryActivity.this, "Lỗi khi xóa mẫu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                if (recyclerViewTemplates != null) {
                    recyclerViewTemplates.setAdapter(templateAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải danh sách mẫu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}