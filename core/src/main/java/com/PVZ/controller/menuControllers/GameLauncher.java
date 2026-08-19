package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.ChapterEnum;
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
import com.PVZ.model.quest.QuestManager;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
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
                String raw = entry.getFamily().trim().toUpperCase();
                if (raw.equals("MINTS") || raw.equals("MINT_FAMILIES")) raw = "MINT";
                families.add(PlantFamily.valueOf(raw));
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
            PlantFamily family = PlantFamilyMapper.getExclusivityFamily(type);
            if (!exclusiveFamilies.contains(family)) {
                continue;
            }
            if (!seen.add(family)) {
                toRemove.add(type);
            }
        }
        return toRemove;
    }

    static Set<PlantType> resolveExtraTagPicks(Set<PlantType> selectedPlants) {
        Set<PlantType> toRemove = new LinkedHashSet<>();
        if (selectedPlants == null || selectedPlants.isEmpty()) return toRemove;

        Set<String> seenTags = new java.util.LinkedHashSet<>();
        for (PlantType type : selectedPlants) {
            PlantDefinition def = PlantLibrary.findByType(type).orElse(null);
            if (def == null || def.getTags() == null) continue;
            boolean conflicts = false;
            for (String raw : def.getTags()) {
                String tag = raw == null ? "" : raw.trim().toLowerCase();
                if (tag.isEmpty() || "-".equals(tag)) continue;
                if (seenTags.contains(tag)) { conflicts = true; break; }
            }
            if (conflicts) {
                toRemove.add(type);
            } else {
                for (String raw : def.getTags()) {
                    String tag = raw == null ? "" : raw.trim().toLowerCase();
                    if (!tag.isEmpty() && !"-".equals(tag)) seenTags.add(tag);
                }
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
        if (AppStatus.currentChapterName != null) {
            AppStatus.currentChapter = com.PVZ.model.game.chapter.ChapterLibrary.getChapter(AppStatus.currentChapterName);
        }
        engine.setMap(new com.PVZ.model.game.Map(480, 1235, 1655, 1170, 5, 9));
        if (AppStatus.currentChapter != null) {
            AppStatus.currentChapter.applySetup(engine.getMap(), stageConfig);
        }
        if (isConveyorBeltStage(stageConfig)) {
            engine.enableConveyorBelt(stageConfig.getConveyorInterval());
        }
        if (isLockedPlantsStage(stageConfig)) {
            Set<PlantType> locked = resolveLockedPlants(stageConfig);
            AppStatus.SELECTED_PLANTS.removeAll(locked);
            Set<PlantType> extraFamilyPicks = resolveExtraFamilyPicks(
                AppStatus.SELECTED_PLANTS, AppStatus.CURRENT_STAGE_EXCLUSIVE_FAMILIES);
            AppStatus.SELECTED_PLANTS.removeAll(extraFamilyPicks);
            locked.addAll(extraFamilyPicks);

            Set<PlantType> extraTagPicks = resolveExtraTagPicks(AppStatus.SELECTED_PLANTS);
            AppStatus.SELECTED_PLANTS.removeAll(extraTagPicks);
            locked.addAll(extraTagPicks);
            engine.enableLockedPlants(locked);
        }
        SpecialLevelLauncher.launch(engine, stageConfig);
        if (AppStatus.currentUser != null && AppStatus.currentUser.questState != null) {
            QuestManager qm = AppStatus.currentUser.questState.getQuestManager();
            ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
            int difficulty = AppStatus.currentUser.appStats.getDifficultyLevel();
            boolean isDay = !stageConfig.isDisableFallingSun();
            qm.onLevelStart(chapter, difficulty, isDay);
            engine.questPlantsLost = 0;
            engine.questLawnmowerKills = 0;
            engine.questLawnlessCol1Kills = 0;
            engine.questPlantTypesUsed.clear();
            engine.questPlantFamiliesUsed.clear();
            if (engine.getBattleController() != null) {
                engine.getBattleController().clearQuestNotifiedZombies();
            }
        }
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
