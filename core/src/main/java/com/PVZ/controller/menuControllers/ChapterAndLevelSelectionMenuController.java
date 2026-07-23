package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.chapter.Chapter;
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
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private OutputDTO enterChapter(String chapterName, Integer stage) {
        if (chapterName == null || chapterName.isBlank()) {
            return new OutputDTO(false, "Invalid chapter.");
        }
        String name = chapterName.trim();
        if (name.matches("\\d+")) {
            int index = Integer.parseInt(name);
            com.PVZ.model.enums.ChapterEnum[] chapters = com.PVZ.model.enums.ChapterEnum.values();
            if (index < 1 || index > chapters.length) {
                return new OutputDTO(false, "Invalid chapter number: " + index
                    + ". Available: 1-" + chapters.length);
            }
            name = chapters[index - 1].name();
        }
        ChapterConfig config = ChapterLibrary.getChapterConfig(name);
        if (config == null) {
            return new OutputDTO(false, "Unknown chapter: " + chapterName);
        }
        AppStatus.currentChapterName = name;
        AppStatus.currentChapter = ChapterLibrary.getChapter(name);
        AppStatus.currentStageNumber = (stage != null && stage > 0) ? stage : 1;

        // Clear selection state using the renamed constants
        AppStatus.SELECTED_PLANTS.clear();
        AppStatus.BOOSTED_PLANTS.clear();
        AppStatus.CURRENT_STAGE_LOCKED_PLANTS.clear();
        AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES.clear();

        StageConfig stageConfig = ChapterLibrary.getStageConfig(
            AppStatus.currentChapterName, AppStatus.currentStageNumber);

        if (GameLauncher.isConveyorBeltStage(stageConfig)) {
            GameLauncher.launch(stageConfig);
            return new OutputDTO(true, "Entered stage directly: " + AppStatus.currentChapterName
                + " Stage " + AppStatus.currentStageNumber + " (Conveyor Belt).");
        }

        if (GameLauncher.isLockedPlantsStage(stageConfig)) {
            AppStatus.CURRENT_STAGE_LOCKED_PLANTS.addAll(GameLauncher.resolveLockedPlants(stageConfig));
            AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES.addAll(GameLauncher.resolveExclusiveFamilies(stageConfig));
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

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }
}
