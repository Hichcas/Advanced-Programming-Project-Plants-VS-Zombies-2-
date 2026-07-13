package com.PVZ.model.leaderboard;

import com.PVZ.database.UserDatabase;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.user.User;
import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard {

    // متود اصلی — فراخوانی از LeaderboardMenuController.showLeaderboard()
    public static List<LeaderboardEntry> getEntries(LeaderboardSortField sort, boolean ascending) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        try {
            List<String> usernames = UserDatabase.loadIndex();
            for (String username : usernames) {
                User user = UserDatabase.load(username);
                if (user != null) {
                    entries.add(buildEntry(user));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load leaderboard: " + e.getMessage());
        }

        Comparator<LeaderboardEntry> comparator;
        if (sort == null) {
            comparator = Comparator.comparing(LeaderboardEntry::getUsername);
        } else {
            comparator = switch (sort) {
                case USERNAME -> Comparator.comparing(LeaderboardEntry::getUsername);
                case LAST_STAGE -> Comparator.comparing(LeaderboardEntry::getLastStageInfo);
                case MINIGAMES -> Comparator.comparingInt(LeaderboardEntry::getMinigamesCompleted);
                case DAILY_QUESTS -> Comparator.comparingInt(LeaderboardEntry::getDailyQuestsCompleted);
                case NON_DAILY_QUESTS -> Comparator.comparingInt(LeaderboardEntry::getNonDailyQuestsCompleted);
                case HIGHEST_SCORE -> Comparator.comparingInt(LeaderboardEntry::getHighestScore);
            };
        }

        entries.sort(ascending ? comparator : comparator.reversed());
        return entries;
    }

    // ساخت یک ردیف — فراخوانی از getEntries()
    private static LeaderboardEntry buildEntry(User user) {
        String username = user.profile != null ? user.profile.getUsername() : "Unknown";
        String lastStageInfo = resolveLastStageInfo(user);
        int minigamesCompleted = resolveTotalMinigames(user);
        int dailyQuests = 0;
        int nonDailyQuests = 0;
        int highestScore = 0;
        if (user.userStats != null) {
            dailyQuests = user.userStats.getDailyQuestsCompleted();
            nonDailyQuests = user.userStats.getNonDailyQuestsCompleted();
            highestScore = user.userStats.getHighestScore();
        }
        return new LeaderboardEntry(username, lastStageInfo, minigamesCompleted,
                dailyQuests, nonDailyQuests, highestScore);
    }

    // گرفتن آخرین مرحله و فصل — فراخوانی از buildEntry()
    private static String resolveLastStageInfo(User user) {
        if (user.progressState == null || user.progressState.getCompletedLevels() == null) {
            return "No progress";
        }
        int maxStage = 0;
        ChapterEnum bestChapter = null;
        for (Map.Entry<ChapterEnum, Integer> entry : user.progressState.getCompletedLevels().entrySet()) {
            if (entry.getValue() > maxStage) {
                maxStage = entry.getValue();
                bestChapter = entry.getKey();
            }
        }
        if (bestChapter == null || maxStage == 0) {
            return "No progress";
        }
        return "Stage " + maxStage + ", " + bestChapter.getDisplayName();
    }

    // مجموع مینی‌گیم‌ها — فراخوانی از buildEntry()
    private static int resolveTotalMinigames(User user) {
        if (user.progressState == null || user.progressState.getMinigameClearedCounts() == null) {
            return 0;
        }
        int total = 0;
        for (int count : user.progressState.getMinigameClearedCounts().values()) {
            total += count;
        }
        return total;
    }
}
