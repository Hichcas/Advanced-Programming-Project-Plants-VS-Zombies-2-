package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

public class LoveYourPlantsLevel implements SpecialLevel {

    private int plantDeaths = 0;
    private int maxAllowed = 5;

    @Override
    public void onGameStart(RegularGameEngine engine, Map map, StageConfig stage) {
        if (stage.getMaxPlantDeaths() > 0) {
            maxAllowed = stage.getMaxPlantDeaths();
        }
        plantDeaths = 0;
        System.out.println("[LoveYourPlants] Level started — max allowed plant deaths: " + maxAllowed);
    }

    @Override
    public void onTick(RegularGameEngine engine, Map map) {
        if (maxAllowed > 0 && plantDeaths >= (int) (maxAllowed * 0.8) && plantDeaths < maxAllowed) {
            System.out.println("[LoveYourPlants] WARNING: " + plantDeaths + "/" + maxAllowed + " plants lost!");
        }
    }

    @Override
    public void onPlantDestroyed(int row, int col, RegularGameEngine engine) {
        plantDeaths++;
        System.out.println("[LoveYourPlants] Plant destroyed at (" + row + "," + col
                + "). Deaths: " + plantDeaths + "/" + maxAllowed);
    }

    @Override
    public boolean disableFallingSun() {
        return false;
    }

    @Override
    public boolean isLossConditionMet() {
        return maxAllowed > 0 && plantDeaths >= maxAllowed;
    }

    @Override
    public String getName() {
        return "LOVE_YOUR_PLANTS";
    }

    public int getPlantDeaths() {
        return plantDeaths;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}
