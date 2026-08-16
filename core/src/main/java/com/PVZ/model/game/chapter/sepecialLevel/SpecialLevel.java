package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

public interface SpecialLevel {

    void onGameStart(RegularGameEngine engine, Map map, StageConfig stage);

    void onTick(RegularGameEngine engine, Map map);

    void onPlantDestroyed(int row, int col, RegularGameEngine engine);

    boolean disableFallingSun();

    boolean isLossConditionMet();

    default boolean isWinConditionMet() {
        return false;
    }

    String getName();
}
