package com.PVZ.model.minigame.vasebreaker;

import com.PVZ.model.enums.PlantType;

public interface VasebreakerEngineCallback {
    void releaseZombieFromVase(String alias, int row, int col);
    void plantAt(int row, int col, PlantType plantType);
    void onLevelWon();
}
