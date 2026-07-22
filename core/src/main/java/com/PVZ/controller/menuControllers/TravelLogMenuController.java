package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.quest.Quest;
import com.PVZ.model.quest.QuestManager;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.ProgressState;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.TravelLogInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TravelLogMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof TravelLogInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (dto.getCommand()) {
            case SHOW_QUESTS -> showQuests();
            case SHOW_PROGRESS -> showProgress();
            case SHOW_MINIGAMES -> showMinigames();
            case SHOW_PAGE -> showPage(dto.getPageName());
            case ENTER_VASEBREAKER -> enterVasebreaker();
            case ENTER_WALLNUT_BOWLING -> enterWallnutBowling();
            case ENTER_IZOMBIE -> enterIZombie();
            case ENTER_BEGHOULED -> enterBeghouled();
            case ENTER_ZOMBOTANY -> enterZombotany();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
            case ENTER_QUEST -> {
                AppStatus.currentMenuType = MenuType.QUEST;
                yield new OutputDTO(true, "Entered Quest Menu.");
            }
        };
    }

    private OutputDTO showPage(String pageName) {
        if (pageName == null || pageName.isBlank()) {
            return new OutputDTO(false, "Page name is required.");
        }
        String normalized = pageName.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "quests", "quest" -> showQuests();
            case "progress" -> showProgress();
            case "minigames", "minigame" -> showMinigames();
            case "vasebreaker" -> enterVasebreaker();
            case "wallnutbowling", "wallnut-bowling", "wallnut bowling" -> enterWallnutBowling();
            case "izombie", "i,zombie", "i zombie" -> enterIZombie();
            case "beghouled" -> enterBeghouled();
            case "zombotany" -> enterZombotany();
            default -> new OutputDTO(false, "Unknown travel log page: " + pageName);
        };
    }

    private OutputDTO showQuests() {
        User user = AppStatus.currentUser;
        if (user == null || user.questState == null || user.questState.getQuestManager() == null) {
            return new OutputDTO(false, "You must be logged in to view quests.");
        }

        QuestManager qm = user.questState.getQuestManager();
        qm.refreshDailyIfNeeded();
        qm.updateChapterQuests();

        List<Quest> quests = qm.getActiveQuests();
        if (quests == null || quests.isEmpty()) {
            return new OutputDTO(true, "No active quests right now.");
        }

        quests = new ArrayList<>(quests);
        quests.sort(Comparator.comparingInt(this::priorityRank).thenComparing(Quest::getId));

        StringBuilder sb = new StringBuilder("Quests:\n");
        for (Quest quest : quests) {
            sb.append(formatQuestLine(quest)).append('\n');
        }
        return new OutputDTO(true, sb.toString().trim());
    }

    private OutputDTO showProgress() {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in to view progress.");
        }

        ProgressState progress = user.progressState;
        StringBuilder sb = new StringBuilder("Progress:\n");
        sb.append("Story chapters:\n");
        for (ChapterEnum chapter : ChapterEnum.values()) {
            int stage = progress.getCompletedLevel(chapter);
            sb.append(" - ").append(chapter.getDisplayName())
              .append(": Stage ").append(stage)
              .append("/4\n");
        }

        int totalStoryStages = 0;
        for (ChapterEnum chapter : ChapterEnum.values()) {
            totalStoryStages += progress.getCompletedLevel(chapter);
        }
        sb.append("Total story stages cleared: ").append(totalStoryStages).append('\n');

        sb.append("Minigames:\n");
        for (MinigameEnum minigame : MinigameEnum.values()) {
            int cleared = progress.getClearedMinigameStages(minigame);
            sb.append(" - ").append(minigame.getDisplayName())
              .append(": ").append(cleared).append("/3\n");
        }

        int totalMinigameStages = 0;
        Map<MinigameEnum, Integer> minigameCounts = progress.getMinigameClearedCounts();
        if (minigameCounts != null) {
            for (int cleared : minigameCounts.values()) {
                totalMinigameStages += cleared;
            }
        }
        sb.append("Total minigame stages cleared: ").append(totalMinigameStages);
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO showMinigames() {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in to view minigames.");
        }

        ProgressState progress = user.progressState;
        StringBuilder sb = new StringBuilder("Minigames:\n");
        for (MinigameEnum minigame : MinigameEnum.values()) {
            int cleared = progress.getClearedMinigameStages(minigame);
            sb.append(" - ").append(minigame.getDisplayName())
              .append(": ").append(cleared).append("/3 cleared");
            if (cleared >= 3) {
                sb.append(" (completed)");
            }
            sb.append('\n');
        }
        return new OutputDTO(true, sb.toString().trim());
    }

    private String formatQuestLine(Quest quest) {
        String status;
        if (quest.isClaimed()) {
            status = "Claimed";
        } else if (quest.isCompleted()) {
            status = "Ready to claim";
        } else if (quest.getCurrentCount() > 0) {
            status = "In progress";
        } else {
            status = "Not started";
        }

        String progress = quest.getTargetCount() > 0
                ? quest.getCurrentCount() + "/" + quest.getTargetCount()
                : "Conditional";

        return "[" + quest.getId() + "] "
                + quest.getFormattedTitle()
                + " (" + quest.getType() + ") - "
                + progress + " - " + status;
    }

    private int priorityRank(Quest quest) {
        return switch (quest.getPriority()) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private OutputDTO enterVasebreaker() {
        AppStatus.currentMenuType = MenuType.VASEBREAKER;
        return new OutputDTO(true, "Entered Vasebreaker menu. Use 'vasebreaker start <id>' to begin a level.");
    }

    private OutputDTO enterWallnutBowling() {
        AppStatus.currentMenuType = MenuType.WALLNUT_BOWLING;
        return new OutputDTO(true, "Entered Wallnut Bowling menu. Use 'wallnutbowling start <id>' to begin a level.");
    }

    private OutputDTO enterIZombie() {
        AppStatus.currentMenuType = MenuType.I_ZOMBIE;
        return new OutputDTO(true, "Entered I, Zombie menu. Use 'izombie start <id>' to begin a level.");
    }

    private OutputDTO enterBeghouled() {
        AppStatus.currentMenuType = MenuType.BEGHOULED;
        return new OutputDTO(true, "Entered Beghouled menu. Use 'beghouled start <id>' to begin a level.");
    }

    private OutputDTO enterZombotany() {
        AppStatus.currentMenuType = MenuType.ZOMBOTANY;
        return new OutputDTO(true, "Entered Zombotany menu. Use 'zombotany start <id>' to begin a level.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
