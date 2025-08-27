package com.example.decider;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PollStorage {
    private static final String PREFS_NAME = "decider_prefs";
    private static final String KEY_POLLS = "polls";
    private static final String KEY_TEMPLATES = "templates";
    private static final String KEY_CURRENT_POLL = "current_poll";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public PollStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // Poll management
    public void savePoll(Poll poll) {
        try {
            List<Poll> polls = getAllPolls();
            
            // Remove existing poll with same ID
            polls.removeIf(p -> p.getId().equals(poll.getId()));
            
            // Add updated poll
            polls.add(poll);
            
            String json = gson.toJson(polls);
            prefs.edit().putString(KEY_POLLS, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Poll> getAllPolls() {
        try {
            String json = prefs.getString(KEY_POLLS, "[]");
            Type listType = new TypeToken<List<Poll>>(){}.getType();
            List<Poll> polls = gson.fromJson(json, listType);
            return polls != null ? polls : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Poll getPollById(String id) {
        try {
            List<Poll> polls = getAllPolls();
            for (Poll poll : polls) {
                if (poll != null && poll.getId() != null && poll.getId().equals(id)) {
                    return poll;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Poll getPollByInviteCode(String inviteCode) {
        try {
            List<Poll> polls = getAllPolls();
            for (Poll poll : polls) {
                if (poll != null && poll.getInviteCode() != null && 
                    poll.getInviteCode().equals(inviteCode) && poll.isActive()) {
                    return poll;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void deletePoll(String id) {
        List<Poll> polls = getAllPolls();
        polls.removeIf(p -> p.getId().equals(id));
        String json = gson.toJson(polls);
        prefs.edit().putString(KEY_POLLS, json).apply();
    }
    
    // Current poll management
    public void setCurrentPoll(Poll poll) {
        try {
            String json = gson.toJson(poll);
            prefs.edit().putString(KEY_CURRENT_POLL, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Poll getCurrentPoll() {
        try {
            String json = prefs.getString(KEY_CURRENT_POLL, null);
            if (json != null) {
                return gson.fromJson(json, Poll.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void clearCurrentPoll() {
        prefs.edit().remove(KEY_CURRENT_POLL).apply();
    }
    
    // Template management
    public void saveTemplate(PollTemplate template) {
        List<PollTemplate> templates = getAllTemplates();
        
        // Remove existing template with same ID
        templates.removeIf(t -> t.getId().equals(template.getId()));
        
        // Add updated template
        templates.add(template);
        
        String json = gson.toJson(templates);
        prefs.edit().putString(KEY_TEMPLATES, json).apply();
    }
    
    public List<PollTemplate> getAllTemplates() {
        String json = prefs.getString(KEY_TEMPLATES, "[]");
        Type listType = new TypeToken<List<PollTemplate>>(){}.getType();
        List<PollTemplate> templates = gson.fromJson(json, listType);
        return templates != null ? templates : new ArrayList<>();
    }
    
    public PollTemplate getTemplateById(String id) {
        List<PollTemplate> templates = getAllTemplates();
        for (PollTemplate template : templates) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        return null;
    }
    
    public void deleteTemplate(String id) {
        List<PollTemplate> templates = getAllTemplates();
        templates.removeIf(t -> t.getId().equals(id));
        String json = gson.toJson(templates);
        prefs.edit().putString(KEY_TEMPLATES, json).apply();
    }
    
    // Utility methods
    public void clearAllData() {
        prefs.edit().clear().apply();
    }
    
    public boolean hasTemplates() {
        return !getAllTemplates().isEmpty();
    }
}