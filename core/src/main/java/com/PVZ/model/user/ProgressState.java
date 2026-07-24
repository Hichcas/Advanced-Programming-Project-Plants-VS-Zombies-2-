package com.PVZ.model.user;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MinigameEnum;

import java.util.HashMap;
import java.util.Map;

public class ProgressState {
    private Map<ChapterEnum, Integer> completedLevels;
    private Map<MinigameEnum, Integer> minigameClearedCounts;

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

    public int getClearedMinigameStages(MinigameEnum minigame) {
        return minigameClearedCounts.getOrDefault(minigame, 0);
    }

    public void clearMinigameStage(MinigameEnum minigame) {
        int current = getClearedMinigameStages(minigame);
        if (current < 3) {
            minigameClearedCounts.put(minigame, current + 1);
        }
    }

    public boolean isMinigameStageUnlocked(MinigameEnum minigame, int stage) {
        return getClearedMinigameStages(minigame) >= stage - 1;
    }
}
