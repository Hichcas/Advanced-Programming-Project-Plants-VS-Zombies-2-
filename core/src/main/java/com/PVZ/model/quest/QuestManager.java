package com.PVZ.model.quest;

import com.PVZ.model.enums.QuestPriority;
import com.PVZ.model.enums.QuestType;
import com.PVZ.model.quest.Quest.Reward;

import java.time.*;
import java.util.*;

public class QuestManager {
    private List<Quest> activeQuests = new ArrayList<>();
    private LocalDate lastDailyRefresh;   // تاریخ آخرین ریست روزانه

    // استفاده از ArrayList برای تغییرپذیری (دیگر immutable نیست)
    private static final List<Quest> DAILY_POOL = new ArrayList<>(List.of(
        new Quest("daily_kill_10", "Zombie Hunter", "Kill 10 zombies", QuestType.DAILY, QuestPriority.LOW, 10,
            new Reward(Reward.RewardType.COINS, 100, null)),
        new Quest("daily_sun_1000", "Sun Collector", "Collect 1000 sun", QuestType.DAILY, QuestPriority.LOW, 1000,
            new Reward(Reward.RewardType.COINS, 80, null)),
        new Quest("daily_plant_5", "Gardener", "Plant 5 plants", QuestType.DAILY, QuestPriority.MEDIUM, 5,
            new Reward(Reward.RewardType.SEED_PACKETS, 3, null))
    ));

    // زمان ریست روزانه (نیمه شب)
    public static LocalDateTime getNextMidnight() {
        LocalDate today = LocalDate.now();
        return today.plusDays(1).atStartOfDay();
    }

    public String getTimeUntilReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = getNextMidnight();
        Duration d = Duration.between(now, next);
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        return hours + "h " + minutes + "m";
    }

    // فراخوانی در شروع هر روز (یا موقع ورود به بخش کوئست)
    public void refreshDailyIfNeeded() {
        LocalDate today = LocalDate.now();
        if (lastDailyRefresh != null && lastDailyRefresh.equals(today)) return;

        // حذف کوئست‌های روزانه قدیمی
        activeQuests.removeIf(q -> q.getType() == QuestType.DAILY);

        // انتخاب تصادفی ۲ کوئست از DAILY_POOL (بدون تغییر pool اصلی)
        List<Quest> shuffled = new ArrayList<>(DAILY_POOL);
        Collections.shuffle(shuffled);
        int toAdd = Math.min(2, shuffled.size());
        for (int i = 0; i < toAdd; i++) {
            Quest template = shuffled.get(i);
            // کپی با id یکتا (برای جلوگیری از تداخل با کوئست‌های روز بعد)
            Quest newDaily = new Quest(
                template.getId() + "_" + System.currentTimeMillis(),
                template.getTitle(),
                template.getDescription(),
                QuestType.DAILY,
                template.getPriority(),
                template.getTargetCount(),
                template.getReward()
            );
            activeQuests.add(newDaily);
        }
        lastDailyRefresh = today;
    }

    // متدهای دریافت رویداد
    public void onZombieKilled(String zombieType, int count) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && q.getId().startsWith("daily_kill")) {
                q.incrementProgress(count);
            }
        }
    }

    public void onSunCollected(int amount) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && q.getId().startsWith("daily_sun")) {
                q.incrementProgress(amount);
            }
        }
    }

    public void onPlantPlaced(String plantType) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && q.getId().startsWith("daily_plant")) {
                q.incrementProgress(1);
            }
        }
    }

    // ... سایر event handler ها برای شروط پیچیده (end-of-level) در صورت نیاز اضافه می‌شوند.

    public List<Quest> getActiveQuests() {
        return activeQuests;
    }

    public boolean claimQuest(String questId) {
        for (Quest q : activeQuests) {
            if (q.getId().equals(questId) && q.isCompleted() && !q.isClaimed()) {
                q.claim();
                return true;
            }
        }
        return false;
    }

    // Getter/Setter برای lastDailyRefresh (برای سریالایز)
    public LocalDate getLastDailyRefresh() { return lastDailyRefresh; }
    public void setLastDailyRefresh(LocalDate lastDailyRefresh) { this.lastDailyRefresh = lastDailyRefresh; }
}
