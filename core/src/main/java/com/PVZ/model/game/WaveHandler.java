package com.PVZ.model.game;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;
import java.util.List;

public class WaveHandler {

    public static void enableConveyorBelt(RegularGameEngine engine, double intervalSeconds) {
        engine.conveyorBeltMode = true;
        engine.conveyorIntervalSeconds = intervalSeconds > 0 ? intervalSeconds : 12.0;
        engine.conveyorTimer = engine.conveyorIntervalSeconds;
        spawnConveyorPlant(engine);
    }

    public static void enableLockedPlants(RegularGameEngine engine, java.util.Collection<PlantType> locked) {
        engine.lockedPlantsMode = true;
        engine.lockedPlantsForStage.clear();
        if (locked != null) engine.lockedPlantsForStage.addAll(locked);
    }

    public static void updateConveyorBelt(RegularGameEngine engine, double delta) {
        if (!engine.conveyorBeltMode) return;
        engine.conveyorTimer -= delta;
        if (engine.conveyorTimer > 0) return;
        engine.conveyorTimer = engine.conveyorIntervalSeconds;
        spawnConveyorPlant(engine);
    }

    private static void spawnConveyorPlant(RegularGameEngine engine) {
        List<PlantType> pool = conveyorPlantPool(engine);
        if (pool.isEmpty()) return;
        PlantType type = pool.get(engine.random.nextInt(pool.size()));
        engine.conveyorBeltQueue.add(type);
        System.out.println("The conveyor belt brought a " + type.getDisplayName() + " seed packet.");
    }

    private static List<PlantType> conveyorPlantPool(RegularGameEngine engine) {
        List<PlantType> pool = new ArrayList<>();
        if (AppStatus.currentUser != null && AppStatus.currentUser.collectionState != null) {
            pool.addAll(AppStatus.currentUser.collectionState.getUnlockedPlants());
        }
        return pool;
    }

    public static String startZombieWavesText(RegularGameEngine engine) {
        engine.zombieWavesStarted = true;
        engine.skySunTimer = 0.0;
        if (engine.waveManager != null) engine.waveManager.start();
        return "Zombie waves started.";
    }

    public static void startWaves(RegularGameEngine engine) {
        engine.zombieWavesStarted = true;
        engine.skySunTimer = 0.0;
        if (engine.waveManager != null) engine.waveManager.start();
    }
}
