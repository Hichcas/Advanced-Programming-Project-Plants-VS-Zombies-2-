package com.PVZ.model.leaderboard;

import com.PVZ.database.UserDatabase;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.network.common.JsonCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Leaderboard {

    public static List<LeaderboardEntry> getEntries(LeaderboardSortField sort, boolean ascending) {
        UserRegistry.saveAllDirtyUsers();
        List<LeaderboardEntry> entries = new ArrayList<>();
        try {
            List<String> usernames = UserDatabase.loadIndex();
            for (String username : usernames) {
                User user = UserRegistry.getUser(username);
                if (user != null) {
                    entries.add(buildEntry(user));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load leaderboard: " + e.getMessage());
        }

        Comparator<LeaderboardEntry> comparator;
        if (sort == null) {
            comparator = Comparator.comparing(LeaderboardEntry::getUsername,
                Comparator.nullsLast(String::compareToIgnoreCase));
        } else {
            comparator = switch (sort) {
                case USERNAME -> Comparator.comparing(LeaderboardEntry::getUsername,
                    Comparator.nullsLast(String::compareToIgnoreCase));
                case LAST_STAGE -> Comparator.comparing(LeaderboardEntry::getLastStageInfo,
                    Comparator.nullsLast(String::compareToIgnoreCase));
                case MINIGAMES -> Comparator.comparingInt(LeaderboardEntry::getMinigamesCompleted);
                case DAILY_QUESTS -> Comparator.comparingInt(LeaderboardEntry::getDailyQuestsCompleted);
                case NON_DAILY_QUESTS -> Comparator.comparingInt(LeaderboardEntry::getNonDailyQuestsCompleted);
                case HIGHEST_SCORE -> Comparator.comparingInt(LeaderboardEntry::getHighestScore);
            };
        }

        entries.sort(ascending ? comparator : comparator.reversed());
        return entries;
    }

    public static List<LeaderboardEntry> parseEntries(Object rawEntries) {
        if (rawEntries == null) {
            return Collections.emptyList();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = JsonCodec.mapper();
            com.fasterxml.jackson.databind.JavaType listType =
                mapper.getTypeFactory().constructCollectionType(List.class, LeaderboardEntry.class);
            List<LeaderboardEntry> parsed = mapper.convertValue(rawEntries, listType);
            return parsed != null ? parsed : Collections.emptyList();
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

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
