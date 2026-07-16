package com.PVZ.model.quest;

import com.PVZ.model.enums.QuestPriority;
import com.PVZ.model.enums.QuestType;

public class Quest {
    private String id;
    private String title;
    private String description;
    private QuestType type;          // DAILY, STORY, EPIC
    private QuestPriority priority;  // CRITICAL, HIGH, MEDIUM, LOW
    private int targetCount;         // تعداد مورد نیاز برای تکمیل
    private int currentCount;        // پیشرفت فعلی
    private boolean completed;
    private boolean claimed;
    private Reward reward;           // پاداش (ارز، آنلاک، آیتم)

    public Quest() {
        // برای سریالایز
    }

    public Quest(String id, String title, String description, QuestType type, QuestPriority priority,
                 int targetCount, Reward reward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.completed = false;
        this.claimed = false;
        this.reward = reward;
    }

    // ---------- getters / setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public QuestType getType() { return type; }
    public void setType(QuestType type) { this.type = type; }

    public QuestPriority getPriority() { return priority; }
    public void setPriority(QuestPriority priority) { this.priority = priority; }

    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int targetCount) { this.targetCount = targetCount; }

    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) {
        this.currentCount = Math.min(currentCount, targetCount);
        this.completed = (this.currentCount >= targetCount);
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public Reward getReward() { return reward; }
    public void setReward(Reward reward) { this.reward = reward; }

    // ---------- متدهای کمکی ----------
    /** افزایش پیشرفت */
    public void incrementProgress(int amount) {
        setCurrentCount(currentCount + amount);
    }

    /** دریافت پاداش (کلیم کردن) */
    public void claim() {
        if (!completed || claimed) {
            throw new IllegalStateException("Quest not ready to claim.");
        }
        claimed = true;
    }

    /** آیا کوئست هنوز فعال است؟ (تکمیل نشده و کلیم نشده) */
    public boolean isActive() {
        return !completed && !claimed;
    }

    /** بازنشانی پیشرفت (برای کوئست‌های روزانه) */
    public void resetProgress() {
        this.currentCount = 0;
        this.completed = false;
    }

    // ---------- کلاس داخلی Reward ----------
    public static class Reward {
        public enum RewardType { COINS, DIAMONDS, UNLOCK_PLANT, SEED_PACKETS }

        private RewardType type;
        private int amount;
        private String target; // e.g., plant type name for unlock

        public Reward() {}

        public Reward(RewardType type, int amount, String target) {
            this.type = type;
            this.amount = amount;
            this.target = target;
        }

        // ---------- getters / setters ----------
        public RewardType getType() { return type; }
        public void setType(RewardType type) { this.type = type; }

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }
}
