package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.chapter.ChapterConfig;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class ChapterAndLevelSelectionMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof ChapterAndLevelSelectionInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case ENTER_CHAPTER -> enterChapter(dto.getChapterName(), dto.getStage());
            case ENTER_COLLECTION -> enterCollection();
            case ENTER_GREENHOUSE -> enterGreenhouse();
            case ENTER_TRAVEL_LOG -> enterTravelLog();
            case ENTER_LEADERBOARD -> enterLeaderboard();
            case SHOW_COIN_WALLET -> showCoins();
            case SHOW_GEM_WALLET -> showDiamonds();
            case CHEAT_ADD -> cheatAdd(dto.getAmount(), dto.getCurrency());
            case SHOW_CHAPTERS -> showChapters();
            case CHEAT_UNLOCK_ALL -> cheatUnlockAll();
            case CHEAT_COMPLETE_CHAPTER -> cheatCompleteChapter(dto.getChapterName());
            case CHEAT_COMPLETE_STAGE -> cheatCompleteStage(dto.getChapterName(), dto.getStage());
            case CHEAT_LOCK_ALL -> cheatLockAll();
            case CHEAT_LOCK_CHAPTER -> cheatLockChapter(dto.getChapterName());
            case CHEAT_LOCK_STAGE -> cheatLockStage(dto.getChapterName(), dto.getStage());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private OutputDTO enterChapter(String chapterName, Integer stage) {
        if (chapterName == null || chapterName.isBlank()) {
            return new OutputDTO(false, "Invalid chapter.");
        }
        String name = resolveChapterName(chapterName.trim());
        if (name == null) {
            return new OutputDTO(false, "Invalid chapter number. Available: 1-4");
        }
        ChapterConfig config = ChapterLibrary.getChapterConfig(name);
        if (config == null) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
        AppStatus.currentChapterName = name;
        AppStatus.currentChapter = ChapterLibrary.getChapter(name);
        int stageNum = (stage != null && stage > 0) ? stage : 1;
        AppStatus.currentStageNumber = stageNum;

        User user = AppStatus.currentUser;
        if (user != null && user.progressState != null) {
            try {
                ChapterEnum chapterEnum = ChapterEnum.valueOf(name.toUpperCase());
                if (!user.progressState.isLevelUnlocked(chapterEnum, stageNum)) {
                    return new OutputDTO(false, "Stage " + stageNum + " is locked. Complete the previous stage first.");
                }
            } catch (IllegalArgumentException e) {
                return new OutputDTO(false, "Unknown chapter: " + name);
            }
        }

        AppStatus.SELECTED_PLANTS.clear();
        AppStatus.BOOSTED_PLANTS.clear();
        AppStatus.CURRENT_STAGE_LOCKED_PLANTS.clear();
        AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES.clear();
        AppStatus.CURRENT_STAGE_TAG_EXCLUSIVITY_ENABLED = false;

        StageConfig stageConfig = ChapterLibrary.getStageConfig(name, stageNum);
        return prepareStageForGame(stageConfig);
    }

    private OutputDTO prepareStageForGame(StageConfig stageConfig) {
        if (GameLauncher.isConveyorBeltStage(stageConfig)) {
            GameLauncher.launch(stageConfig);
            return new OutputDTO(true, "Entered stage directly: " + AppStatus.currentChapterName
                + " Stage " + AppStatus.currentStageNumber + " (Conveyor Belt).");
        }
        if (GameLauncher.isLockedPlantsStage(stageConfig)) {
            AppStatus.CURRENT_STAGE_LOCKED_PLANTS.addAll(GameLauncher.resolveLockedPlants(stageConfig));
            AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES.addAll(GameLauncher.resolveExclusiveFamilies(stageConfig));
            // Only LOCKED_PLANTS stages use the Tags column for "picking one plant locks
            // every other plant sharing a tag" - other stage types must never trigger this.
            AppStatus.CURRENT_STAGE_TAG_EXCLUSIVITY_ENABLED = true;
        }
        if (GameLauncher.isPlantWhatYouGetStage(stageConfig)) {
            // Sun income is fixed at level start, so sun-producing plants are locked out -
            // but this must NOT enable the tag-exclusivity side effect above.
            AppStatus.CURRENT_STAGE_LOCKED_PLANTS.addAll(GameLauncher.resolveSunProducerPlants());
        }
        AppStatus.currentMenuType = MenuType.PLANT_SELECTION;
        return new OutputDTO(true, "Entered Plant Selection Menu.");
    }

    private OutputDTO enterCollection() {
        AppStatus.currentMenuType = MenuType.COLLECTION;
        return new OutputDTO(true, "Entered Collection Menu.");
    }

    private OutputDTO enterGreenhouse() {
        AppStatus.currentMenuType = MenuType.GREENHOUSE;
        return new OutputDTO(true, "Entered Greenhouse Menu.");
    }

    private OutputDTO enterTravelLog() {
        AppStatus.currentMenuType = MenuType.TRAVEL_LOG;
        return new OutputDTO(true, "Entered Travel Log Menu.");
    }

    private OutputDTO enterLeaderboard() {
        AppStatus.currentMenuType = MenuType.LEADERBOARD;
        return new OutputDTO(true, "Entered Leaderboard Menu.");
    }

    private OutputDTO showCoins() {
        User currentUser = AppStatus.currentUser;
        if (currentUser == null || currentUser.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        return new OutputDTO(true, "Coin wallet: " + currentUser.userStats.getCoins());
    }

    private OutputDTO showDiamonds() {
        User currentUser = AppStatus.currentUser;
        if (currentUser == null || currentUser.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        return new OutputDTO(true, "Gem wallet: " + currentUser.userStats.getDiamonds());
    }

    private OutputDTO cheatAdd(Integer amount, String currency) {
        User currentUser = AppStatus.currentUser;
        if (currentUser == null || currentUser.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (amount == null || amount <= 0 || currency == null) {
            return new OutputDTO(false, "Invalid cheat command.");
        }
        switch (currency.toLowerCase()) {
            case "coin" -> currentUser.userStats.addCoins(amount);
            case "diamond" -> currentUser.userStats.addDiamonds(amount);
            default -> {
                return new OutputDTO(false, "Invalid cheat currency.");
            }
        }
        UserRegistry.markDirty(currentUser.profile.getUsername());
        return new OutputDTO(true, "Cheat applied successfully.");
    }

    private OutputDTO showChapters() {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        StringBuilder sb = new StringBuilder();
        ChapterEnum[] chapters = ChapterEnum.values();
        for (int i = 0; i < chapters.length; i++) {
            ChapterEnum chapterEnum = chapters[i];
            ChapterConfig config = ChapterLibrary.getChapterConfig(chapterEnum.name());
            if (config == null) continue;
            sb.append(i + 1).append(". ").append(chapterEnum.name()).append(":\n");
            for (StageConfig stage : config.getStages()) {
                int stageNum = stage.getStageNumber();
                boolean stageUnlocked = user.progressState.isLevelUnlocked(chapterEnum, stageNum);
                sb.append("  Stage ").append(stageNum).append(" [")
                    .append(stageUnlocked ? "UNLOCKED" : "LOCKED").append("]\n");
            }
        }
        return new OutputDTO(true, sb.toString().stripTrailing());
    }

    private OutputDTO cheatUnlockAll() {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        for (ChapterEnum chapter : ChapterEnum.values()) {
            user.progressState.completeLevel(chapter, 99);
        }
        UserRegistry.markDirty(user.profile.getUsername());
        return new OutputDTO(true, "All chapters and stages unlocked.");
    }

    private OutputDTO cheatCompleteChapter(String chapterName) {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (chapterName == null || chapterName.isBlank()) {
            return new OutputDTO(false, "Invalid chapter name.");
        }
        String name = resolveChapterName(chapterName.trim());
        if (name == null) {
            return new OutputDTO(false, "Invalid chapter. Available: 1-4");
        }
        try {
            ChapterEnum chapterEnum = ChapterEnum.valueOf(name);
            ChapterConfig config = ChapterLibrary.getChapterConfig(name);
            if (config == null) {
                return new OutputDTO(false, "Unknown chapter: " + chapterName);
            }
            user.progressState.completeLevel(chapterEnum, config.getStages().size());
            UserRegistry.markDirty(user.profile.getUsername());
            return new OutputDTO(true, "All stages of " + chapterEnum.getDisplayName() + " completed.");
        } catch (IllegalArgumentException e) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
    }

    private OutputDTO cheatCompleteStage(String chapterName, Integer stage) {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (chapterName == null || chapterName.isBlank() || stage == null || stage <= 0) {
            return new OutputDTO(false, "Invalid command. Usage: menu cheat complete-stage -c CHAPTER -s STAGE");
        }
        String name = resolveChapterName(chapterName.trim());
        if (name == null) {
            return new OutputDTO(false, "Invalid chapter. Available: 1-4");
        }
        try {
            ChapterEnum chapterEnum = ChapterEnum.valueOf(name);
            user.progressState.completeLevel(chapterEnum, stage);
            UserRegistry.markDirty(user.profile.getUsername());
            return new OutputDTO(true, "Stage " + stage + " of " + chapterEnum.getDisplayName() + " completed.");
        } catch (IllegalArgumentException e) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
    }

    private OutputDTO cheatLockAll() {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        user.progressState.resetAll();
        UserRegistry.markDirty(user.profile.getUsername());
        return new OutputDTO(true, "All chapters and stages locked.");
    }

    private OutputDTO cheatLockChapter(String chapterName) {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (chapterName == null || chapterName.isBlank()) {
            return new OutputDTO(false, "Invalid chapter name.");
        }
        String name = resolveChapterName(chapterName.trim());
        if (name == null) {
            return new OutputDTO(false, "Invalid chapter. Available: 1-4");
        }
        try {
            ChapterEnum chapterEnum = ChapterEnum.valueOf(name);
            user.progressState.resetChapter(chapterEnum);
            UserRegistry.markDirty(user.profile.getUsername());
            return new OutputDTO(true, chapterEnum.getDisplayName() + " locked.");
        } catch (IllegalArgumentException e) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
    }

    private OutputDTO cheatLockStage(String chapterName, Integer stage) {
        User user = AppStatus.currentUser;
        if (user == null || user.progressState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (chapterName == null || chapterName.isBlank() || stage == null || stage <= 0) {
            return new OutputDTO(false, "Invalid command. Usage: menu cheat lock-stage -c CHAPTER -s STAGE");
        }
        String name = resolveChapterName(chapterName.trim());
        if (name == null) {
            return new OutputDTO(false, "Invalid chapter. Available: 1-4");
        }
        try {
            ChapterEnum chapterEnum = ChapterEnum.valueOf(name);
            user.progressState.lockLevel(chapterEnum, stage);
            UserRegistry.markDirty(user.profile.getUsername());
            return new OutputDTO(true, "Stage " + stage + " of " + chapterEnum.getDisplayName() + " locked.");
        } catch (IllegalArgumentException e) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
    }

    private String resolveChapterName(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.matches("\\d+")) {
            int index = Integer.parseInt(trimmed);
            ChapterEnum[] chapters = ChapterEnum.values();
            if (index >= 1 && index <= chapters.length) {
                return chapters[index - 1].name();
            }
            return null;
        }
        String upper = trimmed.toUpperCase();
        try {
            ChapterEnum.valueOf(upper);
            return upper;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }
}
