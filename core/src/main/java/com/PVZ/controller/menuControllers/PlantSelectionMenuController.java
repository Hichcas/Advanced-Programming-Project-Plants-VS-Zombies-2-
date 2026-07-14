
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.Wave;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.PlantSelectionInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.ArrayList;
import java.util.List;
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
        StringJoiner joiner = new StringJoiner("\n");
        for (PlantType type : PlantType.values()) {
            joiner.add(type.getDisplayName());
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
        user.collectionState.getUnlockedPlants().forEach(p -> joiner.add(p.getDisplayName()));
        return new OutputDTO(true, joiner.toString());
    }

    private OutputDTO addPlant(String plantType) {
        User user = AppStatus.currentUser;
        if (user == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (AppStatus.selectedPlants.size() >= 8) {
            return new OutputDTO(false, "Plant slots are full.");
        }
        try {
            PlantType type = PlantType.fromName(plantType);
            if (!user.collectionState.isPlantUnlocked(type)) {
                return new OutputDTO(false, "Plant is locked.");
            }
            if (!AppStatus.selectedPlants.add(type)) {
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
            if (!AppStatus.selectedPlants.remove(type)) {
                return new OutputDTO(false, "Plant is not selected.");
            }
            AppStatus.boostedPlants.remove(type);
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
            if (!AppStatus.selectedPlants.contains(type)) {
                return new OutputDTO(false, "Plant is not selected.");
            }
            if (!user.userStats.spendDiamonds(2)) {
                return new OutputDTO(false, "Not enough diamonds.");
            }
            AppStatus.boostedPlants.add(type);
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

        if (stageConfig.getPlantLimit() > 0 && AppStatus.selectedPlants.size() > stageConfig.getPlantLimit()) {
            return new OutputDTO(false, "Too many plants selected for this stage.");
        }

        List<Wave> waves = buildWaves(stageConfig);
        int initialSun = stageConfig.isDisableFallingSun() ? 150 : 200;
        GameStatus gameStatus = new GameStatus();
        gameStatus.setSunflower(initialSun);

        RegularGameEngine engine = new RegularGameEngine(gameStatus, waves);

        GameEngine oldEngine = AppStatus.getGameEngine();
        if (oldEngine != null && oldEngine.getMap() != null) {
            engine.setMap(oldEngine.getMap());
        }

        AppStatus.currentChapter.applySetup(engine.getMap(), stageConfig);

        AppStatus.setGameEngine(engine);
        engine.startWaves();
        AppStatus.currentMenuType = MenuType.IN_GAME;

        return new OutputDTO(true, "Game started: " + AppStatus.currentChapterName
                + " Stage " + AppStatus.currentStageNumber);
    }

    private List<Wave> buildWaves(StageConfig stageConfig) {
        List<Wave> waves = new ArrayList<>();
        if (stageConfig.getWaves() == null) {
            return waves;
        }
        for (StageConfig.WaveEntry we : stageConfig.getWaves()) {
            List<Wave.WaveEntry> entries = new ArrayList<>();
            if (we.getEntries() != null) {
                for (StageConfig.ZombieSpawn zs : we.getEntries()) {
                    entries.add(new Wave.WaveEntry(zs.getZombie(), zs.getCount(),
                            (float) zs.getSpawnDelay()));
                }
            }
            waves.add(new Wave(entries, (float) we.getStartDelay()));
        }
        return waves;
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
