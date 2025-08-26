package com.example.decider;

import java.util.List;
import java.util.ArrayList;

public class Vote {
    private String userId;
    private String singleChoice; // For single choice voting
    private List<String> rankings; // For ranked choice voting (ordered by preference)
    private long timestamp;
    
    public Vote() {
        this.rankings = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public Vote(String userId) {
        this();
        this.userId = userId;
    }
    
    // Constructor for single choice
    public Vote(String userId, String singleChoice) {
        this(userId);
        this.singleChoice = singleChoice;
    }
    
    // Constructor for ranked choice
    public Vote(String userId, List<String> rankings) {
        this(userId);
        this.rankings = new ArrayList<>(rankings);
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSingleChoice() { return singleChoice; }
    public void setSingleChoice(String singleChoice) { this.singleChoice = singleChoice; }
    
    public List<String> getRankings() { return rankings; }
    public void setRankings(List<String> rankings) { this.rankings = rankings; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}