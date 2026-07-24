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

        // مقداردهی اولیه (همه صفر) – اختیاری، getCompletedLevel/Stage به‌طور پیش‌فرض صفر برمی‌گرداند
        for (ChapterEnum ch : ChapterEnum.values()) {
            completedLevels.put(ch, 0);
        }
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

    /** دریافت بالاترین مرحلهٔ گذرانده‌شده در یک فصل (۰ یعنی هیچ‌کدام) */
    public int getCompletedLevel(ChapterEnum chapter) {
        return completedLevels.getOrDefault(chapter, 0);
    }

    /** ثبت گذراندن یک مرحله در فصل (فقط در صورتی که از مقدار فعلی بزرگ‌تر باشد ذخیره می‌شود) */
    public void completeLevel(ChapterEnum chapter, int stage) {
        int current = getCompletedLevel(chapter);
        if (stage > current) {
            completedLevels.put(chapter, stage);
        }
    }

    /** آیا مرحله‌ای خاص در یک فصل باز شده است؟ (باز شدن مراحل به‌صورت ترتیبی) */
    public boolean isLevelUnlocked(ChapterEnum chapter, int stage) {
        return getCompletedLevel(chapter) >= stage - 1;
    }

    /** قفل کردن یک مرحله (تنها در صورتی که از مقدار فعلی کوچک‌تر باشد ذخیره می‌شود) */
    public void lockLevel(ChapterEnum chapter, int stage) {
        int current = getCompletedLevel(chapter);
        int newVal = Math.max(0, stage - 1);
        if (newVal < current) {
            completedLevels.put(chapter, newVal);
        }
    }

    /** بازنشانی یک فصل به حالت اولیه (هیچ مرحله‌ای کامل نشده) */
    public void resetChapter(ChapterEnum chapter) {
        completedLevels.put(chapter, 0);
    }

    /** بازنشانی همه فصل‌ها به حالت اولیه */
    public void resetAll() {
        for (ChapterEnum ch : ChapterEnum.values()) {
            completedLevels.put(ch, 0);
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
