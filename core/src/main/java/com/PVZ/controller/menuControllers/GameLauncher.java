package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.Wave;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;
import java.util.List;


final class GameLauncher {

    private static final String STAGE_TYPE_CONVEYOR_BELT = "CONVEYOR_BELT";

    private GameLauncher() {
    }

    static boolean isConveyorBeltStage(StageConfig stageConfig) {
        return stageConfig != null && STAGE_TYPE_CONVEYOR_BELT.equalsIgnoreCase(stageConfig.getType());
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

        AppStatus.setGameEngine(engine);
        AppStatus.currentMenuType = MenuType.IN_GAME;
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
