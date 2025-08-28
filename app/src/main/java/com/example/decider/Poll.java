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
    private long endTime; // thời điểm kết thúc
    
    public Poll() {
        this.options = new ArrayList<>();
        this.votes = new HashMap<>();
        this.results = new ArrayList<>();
        this.votingMode = VotingMode.SINGLE_CHOICE;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();
        this.inviteCode = generateInviteCode();
        this.endTime = 0L;
        // Không gọi calculateResults() ở đây vì options còn rỗng
    }
    
    public Poll(String id, String question, List<String> options, VotingMode votingMode) {
        this();
        this.id = id != null ? id : "poll_" + System.currentTimeMillis();
        this.question = question != null ? question : "";
        if (options != null && !options.isEmpty()) {
            this.options = new ArrayList<>(options);
        } else {
            this.options = new ArrayList<>();
        }
        this.votingMode = votingMode != null ? votingMode : VotingMode.SINGLE_CHOICE;
        this.endTime = 0L;
        // Không gọi calculateResults() ở đây vì chưa có votes
    }
    
    // Tạo mã mời ngẫu nhiên 6 ký tự
    private String generateInviteCode() {
        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder code = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            return code.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "ABC123"; // Fallback code
        }
    }
    
    // Getters and setters
    public String getId() { 
        try {
            return id != null ? id : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public void setId(String id) { 
        try {
            this.id = id != null ? id : "";
        } catch (Exception e) {
            e.printStackTrace();
            this.id = "";
        }
    }
    
    public String getQuestion() { 
        try {
            return question != null ? question : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public void setQuestion(String question) { 
        try {
            this.question = question != null ? question : "";
        } catch (Exception e) {
            e.printStackTrace();
            this.question = "";
        }
    }
    
    public List<String> getOptions() { 
        try {
            return options != null ? options : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public void setOptions(List<String> options) {
        try {
            if (options != null) {
                this.options = new ArrayList<>(options);
            } else {
                this.options = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.options = new ArrayList<>();
        }
    }
    
    public VotingMode getVotingMode() { 
        try {
            return votingMode != null ? votingMode : VotingMode.SINGLE_CHOICE;
        } catch (Exception e) {
            e.printStackTrace();
            return VotingMode.SINGLE_CHOICE;
        }
    }
    
    public void setVotingMode(VotingMode votingMode) { 
        try {
            this.votingMode = votingMode != null ? votingMode : VotingMode.SINGLE_CHOICE;
        } catch (Exception e) {
            e.printStackTrace();
            this.votingMode = VotingMode.SINGLE_CHOICE;
        }
    }
    
    public boolean isHasTimer() { return hasTimer; }
    public void setHasTimer(boolean hasTimer) { this.hasTimer = hasTimer; }
    
    public int getTimerMinutes() { return timerMinutes; }
    public void setTimerMinutes(int timerMinutes) { this.timerMinutes = timerMinutes; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Map<String, Vote> getVotes() { 
        try {
            return votes != null ? votes : new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    public void setVotes(Map<String, Vote> votes) {
        try {
            if (votes != null) {
                this.votes = new HashMap<>(votes);
            } else {
                this.votes = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.votes = new HashMap<>();
        }
    }
    
    public List<String> getResults() { 
        try {
            return results != null ? results : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public void setResults(List<String> results) {
        try {
            if (results != null) {
                this.results = new ArrayList<>(results);
            } else {
                this.results = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.results = new ArrayList<>();
        }
    }
    
    public String getInviteCode() { 
        try {
            return inviteCode != null ? inviteCode : generateInviteCode();
        } catch (Exception e) {
            e.printStackTrace();
            return generateInviteCode();
        }
    }
    public void setInviteCode(String inviteCode) {
        try {
            this.inviteCode = inviteCode != null ? inviteCode : generateInviteCode();
        } catch (Exception e) {
            e.printStackTrace();
            this.inviteCode = generateInviteCode();
        }
    }
    
    // Helper methods
    public void addVote(String userId, Vote vote) {
        try {
            if (userId != null && vote != null && votes != null) {
                votes.put(userId, vote);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isExpired() {
        try {
            if (!hasTimer) return false;
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed > (timerMinutes * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public long getRemainingTime() {
        try {
            if (!hasTimer) return Long.MAX_VALUE;
            long elapsed = System.currentTimeMillis() - startTime;
            long remaining = (timerMinutes * 60 * 1000) - elapsed;
            return Math.max(0, remaining);
        } catch (Exception e) {
            e.printStackTrace();
            return Long.MAX_VALUE;
        }
    }
    
    public void closePoll() {
        try {
            isActive = false;
            if (endTime <= 0L) {
                endTime = System.currentTimeMillis();
            }
            // Chỉ tính toán kết quả khi có options và votes
            if (options != null && !options.isEmpty()) {
                calculateResults();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void calculateResults() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void calculateSingleChoiceResults() {
        try {
            if (options == null || options.isEmpty()) return;
            
            Map<String, Integer> counts = new HashMap<>();
            for (String option : options) {
                if (option != null) {
                    counts.put(option, 0);
                }
            }
            
            if (votes != null) {
                for (Vote vote : votes.values()) {
                    if (vote != null && vote.getSingleChoice() != null) {
                        counts.put(vote.getSingleChoice(), counts.getOrDefault(vote.getSingleChoice(), 0) + 1);
                    }
                }
            }
            
            results.addAll(options);
            results.sort((a, b) -> {
                try {
                    if (a != null && b != null) {
                        return counts.getOrDefault(b, 0) - counts.getOrDefault(a, 0);
                    }
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void calculateRankedChoiceResults() {
        try {
            if (options == null || options.isEmpty()) return;
            
            Map<String, Integer> scores = new HashMap<>();
            for (String option : options) {
                if (option != null) {
                    scores.put(option, 0);
                }
            }
            
            if (votes != null) {
                for (Vote vote : votes.values()) {
                    if (vote != null) {
                        List<String> rankings = vote.getRankings();
                        if (rankings != null) {
                            for (int i = 0; i < rankings.size(); i++) {
                                String option = rankings.get(i);
                                if (option != null) {
                                    int points = options.size() - i; // First place gets most points
                                    scores.put(option, scores.getOrDefault(option, 0) + points);
                                }
                            }
                        }
                    }
                }
            }
            
            results.addAll(options);
            results.sort((a, b) -> {
                try {
                    if (a != null && b != null) {
                        return scores.getOrDefault(b, 0) - scores.getOrDefault(a, 0);
                    }
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean hasTiedResults() {
        try {
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
                if (first != null && second != null) {
                    return counts.getOrDefault(first, 0).equals(counts.getOrDefault(second, 0));
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}