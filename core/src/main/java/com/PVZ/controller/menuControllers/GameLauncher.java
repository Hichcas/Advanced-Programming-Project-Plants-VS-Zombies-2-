package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.Wave;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.game.chapter.sepecialLevel.SpecialLevelLauncher;
import com.PVZ.model.quest.PlantFamilyMapper;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


final class GameLauncher {

    private static final String STAGE_TYPE_CONVEYOR_BELT = "CONVEYOR_BELT";
    private static final String STAGE_TYPE_LOCKED_PLANTS = "LOCKED_PLANTS";

    private GameLauncher() {
    }

    static boolean isConveyorBeltStage(StageConfig stageConfig) {
        return stageConfig != null && STAGE_TYPE_CONVEYOR_BELT.equalsIgnoreCase(stageConfig.getType());
    }

    static boolean isLockedPlantsStage(StageConfig stageConfig) {
        return stageConfig != null && STAGE_TYPE_LOCKED_PLANTS.equalsIgnoreCase(stageConfig.getType());
    }

    static Set<PlantType> resolveLockedPlants(StageConfig stageConfig) {
        Set<PlantType> locked = new LinkedHashSet<>();
        if (stageConfig == null || stageConfig.getLockedPlants() == null) {
            return locked;
        }

        for (String name : stageConfig.getLockedPlants()) {
            try {
                locked.add(PlantType.fromName(name));
            } catch (Exception ignored) {
            }
        }

        return locked;
    }

    static Set<PlantFamily> resolveExclusiveFamilies(StageConfig stageConfig) {
        Set<PlantFamily> families = new LinkedHashSet<>();
        if (stageConfig == null || stageConfig.getLockedFamilies() == null) {
            return families;
        }

        for (StageConfig.FamilyLockEntry entry : stageConfig.getLockedFamilies()) {
            if (entry.getFamily() == null) {
                continue;
            }
            try {
                families.add(PlantFamily.valueOf(entry.getFamily().trim().toUpperCase()));
            } catch (Exception ignored) {
            }
        }

        return families;
    }

    static Set<PlantType> resolveExtraFamilyPicks(Set<PlantType> selectedPlants, Set<PlantFamily> exclusiveFamilies) {
        Set<PlantType> toRemove = new LinkedHashSet<>();
        if (selectedPlants == null || exclusiveFamilies == null || exclusiveFamilies.isEmpty()) {
            return toRemove;
        }
        Set<PlantFamily> seen = new LinkedHashSet<>();
        for (PlantType type : selectedPlants) {
            PlantFamily family = PlantFamilyMapper.getFamily(type);
            if (!exclusiveFamilies.contains(family)) {
                continue;
            }
            if (!seen.add(family)) {
                toRemove.add(type);
            }
        }
        return toRemove;
    }

    static RegularGameEngine launch(StageConfig stageConfig) {
        List<Wave> waves = buildWaves(stageConfig);
        int initialSun = stageConfig.isDisableFallingSun() ? 150 : 200;
        GameStatus gameStatus = new GameStatus();
        gameStatus.setSunflower(initialSun);
        gameStatus.setNoSkySun(stageConfig.isDisableFallingSun());

        RegularGameEngine engine = new RegularGameEngine(gameStatus, waves);
        engine.setBackgroundTexturePath(stageConfig.getMapTexture());

        GameEngine oldEngine = AppStatus.getGameEngine();
        if (oldEngine != null && oldEngine.getMap() != null) {
            engine.setMap(oldEngine.getMap());
        }

        AppStatus.currentChapter.applySetup(engine.getMap(), stageConfig);

        if (isConveyorBeltStage(stageConfig)) {
            engine.enableConveyorBelt(stageConfig.getConveyorInterval());
        }

        if (isLockedPlantsStage(stageConfig)) {
            Set<PlantType> locked = resolveLockedPlants(stageConfig);
            AppStatus.selectedPlants.removeAll(locked);

            Set<PlantType> extraFamilyPicks = resolveExtraFamilyPicks(
                    AppStatus.selectedPlants, AppStatus.currentStageExclusiveFamilies);
            AppStatus.selectedPlants.removeAll(extraFamilyPicks);
            locked.addAll(extraFamilyPicks);

            engine.enableLockedPlants(locked);
        }

        SpecialLevelLauncher.launch(engine, stageConfig);

        AppStatus.setGameEngine(engine);
        AppStatus.currentMenuType = MenuType.IN_GAME;
        engine.startWaves();
        return engine;
    }

    static List<Wave> buildWaves(StageConfig stageConfig) {
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
}
