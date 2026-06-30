package com.PVZ.model.user;

public class UserStats {
        // ---------- اطلاعات آماری ----------
    private int gamesPlayed; // تعداد بازی‌های انجام‌شده
    private int coins; // سکه‌های فعلی
    private int diamonds; // الماس‌های فعلی
    private int stagesCompleted; // تعداد کل مراحل گذرانده‌شده (همهٔ فصل‌ها)
    private int highestScore; // بیشترین میوپوینت در بازی امتیازی
    // private int difficultyLevel; // سطح سختی فعلی (۱ تا ۵، پیش‌فرض ۳)

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

    // ---------- متدهای کمکی ----------
    /** افزایش تعداد بازی‌های انجام‌شده */
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    /** اضافه کردن سکه */
    public void addCoins(int amount) {
        this.coins += amount;
    }

    /** کسر سکه (در صورت کافی بودن موجودی true برمی‌گرداند) */
    public boolean spendCoins(int amount) {
        if (this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    /** اضافه کردن الماس */
    public void addDiamonds(int amount) {
        this.diamonds += amount;
    }

    /** کسر الماس (در صورت کافی بودن موجودی true برمی‌گرداند) */
    public boolean spendDiamonds(int amount) {
        if (this.diamonds >= amount) {
            this.diamonds -= amount;
            return true;
        }
        return false;
    }

    /** به‌روزرسانی بیشترین امتیاز اگر امتیاز جدید بیشتر باشد */
    public void updateHighestScore(int newScore) {
        if (newScore > this.highestScore) {
            this.highestScore = newScore;
        }
    }

    /** افزایش تعداد مراحل کامل‌شده */
    public void incrementStagesCompleted() {
        this.stagesCompleted++;
    }
}
