package com.PVZ.model.user;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MinigameEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * پیشرفت کاربر در مراحل اصلی (فصل‌ها) و مینی‌گیم‌ها.
 */
public class ProgressState {
    // بالاترین مرحلهٔ گذرانده‌شده در هر فصل (مقادیر ۰ تا ۴)
    private Map<ChapterEnum, Integer> completedLevels;

    // تعداد مراحل کامل‌شده در هر مینی‌گیم (۰ تا ۳)
    private Map<MinigameEnum, Integer> minigameClearedCounts;

    // ---------- سازنده ----------
    public ProgressState() {
        completedLevels = new HashMap<>();
        minigameClearedCounts = new HashMap<>();

        for (ChapterEnum ch : ChapterEnum.values()) {
            completedLevels.put(ch, -1);
        }
        completedLevels.put(ChapterEnum.ANCIENT_EGYPT, 1);
        for (MinigameEnum mg : MinigameEnum.values()) {
            minigameClearedCounts.put(mg, 0);
        }
    }

    // ---------- Getter / Setter (برای سریالایز) ----------
    public Map<ChapterEnum, Integer> getCompletedLevels() {
        return completedLevels;
    }

    public void setCompletedLevels(Map<ChapterEnum, Integer> completedLevels) {
        this.completedLevels = completedLevels;
    }

    public Map<MinigameEnum, Integer> getMinigameClearedCounts() {
        return minigameClearedCounts;
    }

    public void setMinigameClearedCounts(Map<MinigameEnum, Integer> minigameClearedCounts) {
        this.minigameClearedCounts = minigameClearedCounts;
    }

    // ---------- متدهای کمکی ----------

    public int getCompletedLevel(ChapterEnum chapter) {
        return completedLevels.getOrDefault(chapter, -1);
    }

    public void completeLevel(ChapterEnum chapter, int stage) {
        int current = getCompletedLevel(chapter);
        if (stage > current) {
            completedLevels.put(chapter, stage);
        }
    }

    public boolean isLevelUnlocked(ChapterEnum chapter, int stage) {
        return getCompletedLevel(chapter) >= stage;
    }

    public void lockLevel(ChapterEnum chapter, int stage) {
        int current = getCompletedLevel(chapter);
        int newVal = stage - 2;
        if (newVal < current) {
            completedLevels.put(chapter, newVal);
        }
    }

    public void resetChapter(ChapterEnum chapter) {
        completedLevels.put(chapter, chapter == ChapterEnum.ANCIENT_EGYPT ? 1 : -1);
    }

    public void resetAll() {
        for (ChapterEnum ch : ChapterEnum.values()) {
            completedLevels.put(ch, ch == ChapterEnum.ANCIENT_EGYPT ? 1 : -1);
        }
    }

    /** دریافت تعداد مراحل کامل‌شده در یک مینی‌گیم */
    public int getClearedMinigameStages(MinigameEnum minigame) {
        return minigameClearedCounts.getOrDefault(minigame, 0);
    }

    /** افزودن یک مرحلهٔ کامل‌شده به مینی‌گیم (در صورت کمتر از ۳ بودن) */
    public void clearMinigameStage(MinigameEnum minigame) {
        int current = getClearedMinigameStages(minigame);
        if (current < 3) {
            minigameClearedCounts.put(minigame, current + 1);
        }
    }

    /** آیا مرحلهٔ خاصی از یک مینی‌گیم باز است؟ */
    public boolean isMinigameStageUnlocked(MinigameEnum minigame, int stage) {
        // stage از ۱ شروع می‌شود؛ مرحلهٔ قبل باید کامل شده باشد
        return getClearedMinigameStages(minigame) >= stage - 1;
    }
}
