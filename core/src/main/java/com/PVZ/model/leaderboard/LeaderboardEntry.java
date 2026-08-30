package com.PVZ.model.leaderboard;

public class LeaderboardEntry {
    private String username;
    private String lastStageInfo;
    private int minigamesCompleted;
    private int dailyQuestsCompleted;
    private int nonDailyQuestsCompleted;
    private int highestScore;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(String username, String lastStageInfo,
                            int minigamesCompleted, int dailyQuestsCompleted,
                            int nonDailyQuestsCompleted, int highestScore) {
        this.username = username;
        this.lastStageInfo = lastStageInfo;
        this.minigamesCompleted = minigamesCompleted;
        this.dailyQuestsCompleted = dailyQuestsCompleted;
        this.nonDailyQuestsCompleted = nonDailyQuestsCompleted;
        this.highestScore = highestScore;
    }

    public String getUsername() {
        return username;
    }

    public String getLastStageInfo() {
        return lastStageInfo;
    }

    public int getMinigamesCompleted() {
        return minigamesCompleted;
    }

    public int getDailyQuestsCompleted() {
        return dailyQuestsCompleted;
    }

    public int getNonDailyQuestsCompleted() {
        return nonDailyQuestsCompleted;
    }

    public int getHighestScore() {
        return highestScore;
    }
}
