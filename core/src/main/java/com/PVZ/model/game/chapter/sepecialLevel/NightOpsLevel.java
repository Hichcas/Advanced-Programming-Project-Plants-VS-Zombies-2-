package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

public class NightOpsLevel implements SpecialLevel {

    @Override
    public void onGameStart(RegularGameEngine engine, Map map, StageConfig stage) {
        System.out.println("[NightOps] Night Ops level started — sky sun disabled.");
    }

    @Override
    public void onTick(RegularGameEngine engine, Map map) {
    }

    @Override
    public void onPlantDestroyed(int row, int col, RegularGameEngine engine) {
    }

    @Override
    public boolean disableFallingSun() {
        return true;
    }

    @Override
    public boolean isLossConditionMet() {
        return false;
    }

    @Override
    public String getName() {
        return "NIGHT_OPS";
    }
}
