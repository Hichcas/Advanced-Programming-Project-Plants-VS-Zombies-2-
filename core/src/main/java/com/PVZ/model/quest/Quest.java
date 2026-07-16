package com.PVZ.model.quest;

import com.PVZ.model.enums.QuestPriority;
import com.PVZ.model.enums.QuestType;
import com.PVZ.model.enums.PlantType;
import java.util.HashMap;
import java.util.Map;

public class Quest {
    private String id;
    private String title;
    private String descriptionTemplate;   // contains placeholders like {plant}, {col}, etc.
    private QuestType type;
    private QuestPriority priority;

    private String conditionKey;
    private int targetCount;
    private int currentCount;
    private boolean completed;
    private boolean claimed;
    private Reward reward;

    private Map<String, Object> parameters;   // e.g., "plant" -> PlantType.PEASHOOTER, "col" -> 2
    private transient Map<String, Object> runtimeState;

    public Quest() {
        this.parameters = new HashMap<>();
    }

    public Quest(String id, String title, String descriptionTemplate, QuestType type, QuestPriority priority,
                 String conditionKey, int targetCount, Reward reward, Map<String, Object> parameters) {
        this.id = id;
        this.title = title;
        this.descriptionTemplate = descriptionTemplate;
        this.type = type;
        this.priority = priority;
        this.conditionKey = conditionKey;
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.completed = false;
        this.claimed = false;
        this.reward = reward;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.runtimeState = new HashMap<>();
    }

    // ---------- getters/setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescriptionTemplate() { return descriptionTemplate; }
    public void setDescriptionTemplate(String descriptionTemplate) { this.descriptionTemplate = descriptionTemplate; }

    public QuestType getType() { return type; }
    public void setType(QuestType type) { this.type = type; }

    public QuestPriority getPriority() { return priority; }
    public void setPriority(QuestPriority priority) { this.priority = priority; }

    public String getConditionKey() { return conditionKey; }
    public void setConditionKey(String conditionKey) { this.conditionKey = conditionKey; }

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

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public Map<String, Object> getRuntimeState() {
        if (runtimeState == null) runtimeState = new HashMap<>();
        return runtimeState;
    }

    public void incrementProgress(int amount) {
        setCurrentCount(currentCount + amount);
    }

    public void claim() {
        if (!completed || claimed) throw new IllegalStateException("Quest not ready to claim.");
        claimed = true;
    }

    public boolean isActive() {
        return !completed && !claimed;
    }

    public void resetProgress() {
        this.currentCount = 0;
        this.completed = false;
    }

    /**
     * Returns the description with placeholders replaced by their parameter values.
     * e.g., "Kill 10 zombies using only {plant}" -> "Kill 10 zombies using only Peashooter"
     */
    public String getFormattedDescription() {
        String desc = descriptionTemplate;
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String replacement = formatParamValue(entry.getValue());
                desc = desc.replace(placeholder, replacement);
            }
        }
        return desc;
    }

    private String formatParamValue(Object value) {
        if (value instanceof PlantType) {
            return ((PlantType) value).getDisplayName();
        }
        if (value instanceof com.PVZ.model.enums.ChapterEnum) {
            return ((com.PVZ.model.enums.ChapterEnum) value).getDisplayName();
        }
        return String.valueOf(value);
    }

    // ---------- Reward ----------
    public static class Reward {
        public enum RewardType { COINS, DIAMONDS, UNLOCK_PLANT, SEED_PACKETS }

        private RewardType type;
        private int amount;
        private PlantType targetPlant;   // used for UNLOCK_PLANT reward

        public Reward() {}
        public Reward(RewardType type, int amount, PlantType targetPlant) {
            this.type = type; this.amount = amount; this.targetPlant = targetPlant;
        }

        public RewardType getType() { return type; }
        public void setType(RewardType type) { this.type = type; }

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }

        public PlantType getTargetPlant() { return targetPlant; }
        public void setTargetPlant(PlantType targetPlant) { this.targetPlant = targetPlant; }
    }
}
