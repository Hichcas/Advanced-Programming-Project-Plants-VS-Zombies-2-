package com.PVZ.model.user;

public class UserStats {

    private int gamesPlayed;
    private int coins;
    private int diamonds;
    private int stagesCompleted;
    private int highestScore;
    private int dailyQuestsCompleted;
    private int nonDailyQuestsCompleted;

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public int getStagesCompleted() {
        return stagesCompleted;
    }

    public void setStagesCompleted(int stagesCompleted) {
        this.stagesCompleted = stagesCompleted;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getDailyQuestsCompleted() {
        return dailyQuestsCompleted;
    }

    public void setDailyQuestsCompleted(int n) {
        this.dailyQuestsCompleted = n;
    }

    public int getNonDailyQuestsCompleted() {
        return nonDailyQuestsCompleted;
    }

    public void setNonDailyQuestsCompleted(int n) {
        this.nonDailyQuestsCompleted = n;
    }

    public void incrementDailyQuestsCompleted() {
        this.dailyQuestsCompleted++;
    }

    public void incrementNonDailyQuestsCompleted() {
        this.nonDailyQuestsCompleted++;
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    public void addDiamonds(int amount) {
        this.diamonds += amount;
    }

    public boolean spendDiamonds(int amount) {
        if (this.diamonds >= amount) {
            this.diamonds -= amount;
            return true;
        }
        return false;
    }

    public void updateHighestScore(int newScore) {
        if (newScore > this.highestScore) {
            this.highestScore = newScore;
        }
    }

    public void incrementStagesCompleted() {
        this.stagesCompleted++;
    }
}
