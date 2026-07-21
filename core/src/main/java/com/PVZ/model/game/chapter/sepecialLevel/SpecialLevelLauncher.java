package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

public final class SpecialLevelLauncher {

    private SpecialLevelLauncher() {
    }

    public static void launch(RegularGameEngine engine, StageConfig stageConfig) {
        if (stageConfig == null || engine == null) {
            return;
        }

        SpecialLevel level = SpecialLevelFactory.create(stageConfig.getSpecialLevel());
        if (level == null) {
            return;
        }

        engine.setSpecialLevel(level);

        Map map = engine.getMap();
        level.onGameStart(engine, map, stageConfig);

        if (level.disableFallingSun() && engine.gameStatus != null) {
            engine.gameStatus.setNoSkySun(true);
        }

        System.out.println("[SpecialLevelLauncher] Attached: " + level.getName());
    }
}
