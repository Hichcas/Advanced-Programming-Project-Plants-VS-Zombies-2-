package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.quest.PlantFamilyMapper;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.PlantSelectionInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.StringJoiner;


public class PlantSelectionMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof PlantSelectionInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_ALL_PLANTS -> showAllPlants();
            case SHOW_AVAILABLE_PLANTS -> showAvailablePlants();
            case ADD_PLANT -> addPlant(dto.getPlantType());
            case REMOVE_PLANT -> removePlant(dto.getPlantType());
            case BOOST_PLANT -> boostPlant(dto.getPlantType());
            case START_GAME -> startGame();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private OutputDTO showAllPlants() {
        User user = AppStatus.currentUser;
        StringJoiner joiner = new StringJoiner("\n");
        for (PlantType type : PlantType.values()) {
            boolean lockedForStage = AppStatus.CURRENT_STAGE_LOCKED_PLANTS.contains(type);
            boolean ownedGlobally = user != null && user.collectionState != null
                && user.collectionState.isPlantUnlocked(type);
            String tag;
            if (lockedForStage) {
                tag = lockedTag();
            } else if (isFamilyLockedByOtherPick(type)) {
                tag = familyLockedTag();
            } else if (!ownedGlobally) {
                tag = notOwnedTag();
            } else {
                tag = "";
            }
            joiner.add(type.getDisplayName() + tag);
        }
        return new OutputDTO(true, joiner.toString());
    }

    private OutputDTO showAvailablePlants() {
        User user = AppStatus.currentUser;
        if (user == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (user.collectionState.getUnlockedPlants().isEmpty()) {
            return new OutputDTO(true, "No available plants.");
        }
        StringJoiner joiner = new StringJoiner("\n");
        user.collectionState.getUnlockedPlants().forEach(p -> {
            boolean lockedForStage = AppStatus.CURRENT_STAGE_LOCKED_PLANTS.contains(p);
            String tag = lockedForStage ? lockedTag()
                : (isFamilyLockedByOtherPick(p) ? familyLockedTag() : "");
            joiner.add(p.getDisplayName() + tag);
        });
        return new OutputDTO(true, joiner.toString());
    }

    private boolean isFamilyLockedByOtherPick(PlantType type) {
        PlantFamily family = PlantFamilyMapper.getFamily(type);
        if (!AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES.contains(family)) {
            return false;
        }
        for (PlantType selected : AppStatus.SELECTED_PLANTS) {
            if (selected != type && PlantFamilyMapper.getFamily(selected) == family) {
                return true;
            }
        }
        return false;
    }

    private String lockedTag() {
        return " \u001B[33m(LOCKED - this level)\u001B[0m";
    }

    private String familyLockedTag() {
        return " \u001B[33m(LOCKED - already picked this family)\u001B[0m";
    }

    private String notOwnedTag() {
        return " \u001B[90m(NOT OWNED)\u001B[0m";
    }

    private OutputDTO addPlant(String plantType) {
        User user = AppStatus.currentUser;
        if (user == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (AppStatus.SELECTED_PLANTS.size() >= 8) {
            return new OutputDTO(false, "Plant slots are full.");
        }
        try {
            PlantType type = PlantType.fromName(plantType);
            if (!user.collectionState.isPlantUnlocked(type)) {
                return new OutputDTO(false, "\u001B[90mPlant is locked.\u001B[0m");
            }
            if (AppStatus.CURRENT_STAGE_LOCKED_PLANTS.contains(type)) {
                return new OutputDTO(false, "\u001B[33mPlant is locked for this level: "
                    + type.getDisplayName() + "\u001B[0m");
            }
            if (isFamilyLockedByOtherPick(type)) {
                return new OutputDTO(false, "\u001B[33mYou already picked a plant from "
                    + PlantFamilyMapper.getFamily(type) + " family for this level: "
                    + type.getDisplayName() + "\u001B[0m");
            }
            if (!AppStatus.SELECTED_PLANTS.add(type)) {
                return new OutputDTO(false, "Plant already selected.");
            }
            return new OutputDTO(true, "Plant added.");
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO removePlant(String plantType) {
        try {
            PlantType type = PlantType.fromName(plantType);
            if (!AppStatus.SELECTED_PLANTS.remove(type)) {
                return new OutputDTO(false, "Plant is not selected.");
            }
            AppStatus.BOOSTED_PLANTS.remove(type);
            return new OutputDTO(true, "Plant removed.");
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO boostPlant(String plantType) {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (user.userStats.getDiamonds() < 2) {
            return new OutputDTO(false, "Not enough diamonds.");
        }
        try {
            PlantType type = PlantType.fromName(plantType);
            if (!AppStatus.SELECTED_PLANTS.contains(type)) {
                return new OutputDTO(false, "Plant is not selected.");
            }
            if (!user.userStats.spendDiamonds(2)) {
                return new OutputDTO(false, "Not enough diamonds.");
            }
            AppStatus.BOOSTED_PLANTS.add(type);
            return new OutputDTO(true, "Plant boosted.");
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO startGame() {
        if (AppStatus.currentChapter == null || AppStatus.currentChapterName == null) {
            return new OutputDTO(false, "No chapter selected.");
        }

        StageConfig stageConfig = ChapterLibrary.getStageConfig(
            AppStatus.currentChapterName, AppStatus.currentStageNumber);
        if (stageConfig == null) {
            return new OutputDTO(false, "Invalid stage.");
        }

        if (stageConfig.getPlantLimit() > 0 && AppStatus.SELECTED_PLANTS.size() > stageConfig.getPlantLimit()) {
            return new OutputDTO(false, "Too many plants selected for this stage.");
        }

        GameLauncher.launch(stageConfig);

        return new OutputDTO(true, "Game started: " + AppStatus.currentChapterName
            + " Stage " + AppStatus.currentStageNumber);
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
