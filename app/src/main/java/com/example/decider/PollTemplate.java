package com.example.decider;

import java.util.List;
import java.util.ArrayList;

public class PollTemplate {
    private String id;
    private String name;
    private String question;
    private List<String> options;
    private Poll.VotingMode defaultVotingMode;
    private boolean hasDefaultTimer;
    private int defaultTimerMinutes;
    private long createdTime;
    
    public PollTemplate() {
        this.options = new ArrayList<>();
        this.defaultVotingMode = Poll.VotingMode.SINGLE_CHOICE;
        this.createdTime = System.currentTimeMillis();
    }
    
    public PollTemplate(String name, String question, List<String> options) {
        this();
        this.name = name;
        this.question = question;
        this.options = new ArrayList<>(options);
        this.id = generateId();
    }
    
    public PollTemplate(Poll poll) {
        this();
        this.name = poll.getQuestion();
        this.question = poll.getQuestion();
        this.options = new ArrayList<>(poll.getOptions());
        this.defaultVotingMode = poll.getVotingMode();
        this.hasDefaultTimer = poll.isHasTimer();
        this.defaultTimerMinutes = poll.getTimerMinutes();
        this.id = generateId();
    }
    
    private String generateId() {
        return "template_" + System.currentTimeMillis() + "_" + Math.random();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public Poll.VotingMode getDefaultVotingMode() { return defaultVotingMode; }
    public void setDefaultVotingMode(Poll.VotingMode defaultVotingMode) { this.defaultVotingMode = defaultVotingMode; }
    
    public boolean isHasDefaultTimer() { return hasDefaultTimer; }
    public void setHasDefaultTimer(boolean hasDefaultTimer) { this.hasDefaultTimer = hasDefaultTimer; }
    
    public int getDefaultTimerMinutes() { return defaultTimerMinutes; }
    public void setDefaultTimerMinutes(int defaultTimerMinutes) { this.defaultTimerMinutes = defaultTimerMinutes; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    // Helper method to create a new poll from this template
    public Poll createPoll() {
        Poll poll = new Poll();
        poll.setId("poll_" + System.currentTimeMillis());
        poll.setQuestion(this.question);
        poll.setOptions(new ArrayList<>(this.options));
        poll.setVotingMode(this.defaultVotingMode);
        poll.setHasTimer(this.hasDefaultTimer);
        poll.setTimerMinutes(this.defaultTimerMinutes);
        poll.setStartTime(System.currentTimeMillis());
        return poll;
    }
}