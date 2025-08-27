package com.example.decider;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

public class Poll {
    public enum VotingMode {
        SINGLE_CHOICE,
        RANKED_CHOICE,
        RANDOM_SPINNER
    }
    
    private String id;
    private String question;
    private List<String> options;
    private VotingMode votingMode;
    private boolean hasTimer;
    private int timerMinutes;
    private long startTime;
    private boolean isActive;
    private Map<String, Vote> votes; // userId -> Vote
    private List<String> results; // calculated results
    private String inviteCode; // Mã mời để tham gia
    
    public Poll() {
        this.options = new ArrayList<>();
        this.votes = new HashMap<>();
        this.results = new ArrayList<>();
        this.votingMode = VotingMode.SINGLE_CHOICE;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();
        this.inviteCode = generateInviteCode();
        // Không gọi calculateResults() ở đây vì options còn rỗng
    }
    
    public Poll(String id, String question, List<String> options, VotingMode votingMode) {
        this();
        this.id = id;
        this.question = question;
        if (options != null) {
            this.options = new ArrayList<>(options);
        }
        this.votingMode = votingMode;
        // Không gọi calculateResults() ở đây vì chưa có votes
    }
    
    // Tạo mã mời ngẫu nhiên 6 ký tự
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = new ArrayList<>(options); }
    
    public VotingMode getVotingMode() { return votingMode; }
    public void setVotingMode(VotingMode votingMode) { this.votingMode = votingMode; }
    
    public boolean isHasTimer() { return hasTimer; }
    public void setHasTimer(boolean hasTimer) { this.hasTimer = hasTimer; }
    
    public int getTimerMinutes() { return timerMinutes; }
    public void setTimerMinutes(int timerMinutes) { this.timerMinutes = timerMinutes; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Map<String, Vote> getVotes() { return votes; }
    public void setVotes(Map<String, Vote> votes) { this.votes = votes; }
    
    public List<String> getResults() { return results; }
    public void setResults(List<String> results) { this.results = results; }
    
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    
    // Helper methods
    public void addVote(String userId, Vote vote) {
        votes.put(userId, vote);
    }
    
    public boolean isExpired() {
        if (!hasTimer) return false;
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > (timerMinutes * 60 * 1000);
    }
    
    public long getRemainingTime() {
        if (!hasTimer) return Long.MAX_VALUE;
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = (timerMinutes * 60 * 1000) - elapsed;
        return Math.max(0, remaining);
    }
    
    public void closePoll() {
        isActive = false;
        // Chỉ tính toán kết quả khi có options và votes
        if (options != null && !options.isEmpty()) {
            calculateResults();
        }
    }
    
    private void calculateResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.clear();
        
        if (options == null || options.isEmpty()) {
            return; // Không thể tính toán kết quả nếu không có options
        }
        
        switch (votingMode) {
            case SINGLE_CHOICE:
                calculateSingleChoiceResults();
                break;
            case RANKED_CHOICE:
                calculateRankedChoiceResults();
                break;
            case RANDOM_SPINNER:
                // For random spinner, results are determined by spinning
                results.addAll(options);
                break;
        }
    }
    
    private void calculateSingleChoiceResults() {
        if (options == null || options.isEmpty()) return;
        
        Map<String, Integer> counts = new HashMap<>();
        for (String option : options) {
            counts.put(option, 0);
        }
        
        if (votes != null) {
            for (Vote vote : votes.values()) {
                if (vote != null && vote.getSingleChoice() != null) {
                    counts.put(vote.getSingleChoice(), counts.get(vote.getSingleChoice()) + 1);
                }
            }
        }
        
        results.addAll(options);
        results.sort((a, b) -> counts.get(b) - counts.get(a));
    }
    
    private void calculateRankedChoiceResults() {
        if (options == null || options.isEmpty()) return;
        
        Map<String, Integer> scores = new HashMap<>();
        for (String option : options) {
            scores.put(option, 0);
        }
        
        if (votes != null) {
            for (Vote vote : votes.values()) {
                if (vote != null) {
                    List<String> rankings = vote.getRankings();
                    if (rankings != null) {
                        for (int i = 0; i < rankings.size(); i++) {
                            String option = rankings.get(i);
                            int points = options.size() - i; // First place gets most points
                            scores.put(option, scores.get(option) + points);
                        }
                    }
                }
            }
        }
        
        results.addAll(options);
        results.sort((a, b) -> scores.get(b) - scores.get(a));
    }
    
    public boolean hasTiedResults() {
        if (results == null || results.size() < 2) return false;
        
        Map<String, Integer> counts = new HashMap<>();
        if (votes != null) {
            for (Vote vote : votes.values()) {
                if (vote != null && votingMode == VotingMode.SINGLE_CHOICE && vote.getSingleChoice() != null) {
                    counts.put(vote.getSingleChoice(), counts.getOrDefault(vote.getSingleChoice(), 0) + 1);
                }
            }
        }
        
        if (results.size() >= 2) {
            String first = results.get(0);
            String second = results.get(1);
            return counts.getOrDefault(first, 0).equals(counts.getOrDefault(second, 0));
        }
        
        return false;
    }
}