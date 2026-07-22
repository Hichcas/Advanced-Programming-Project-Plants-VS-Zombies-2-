package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;

public class PlantHandler {

    public static String plantPlant(RegularGameEngine engine, PlantType type, int x, int y) {
        if (engine.map == null) return "Map is not ready.";
        if (type == null) return "Unknown plant type.";

        int row = engine.normalizeIndex(y);
        int col = engine.normalizeIndex(x);
        if (!engine.map.isWithinBounds(row, col)) return "Invalid tile.";
        if (engine.map.getPlantAt(row, col) != null) return "Tile is occupied.";

        String availabilityError = checkPlantAvailability(engine, type);
        if (availabilityError != null) return availabilityError;

        Plant plant = createPlantInstance(engine, type);
        if (plant == null) return "Cannot create plant.";

        TileType targetTileType = engine.map.getTile(row, col).getType();
        boolean plantIsAquatic = plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.WATER);
        if ((targetTileType == TileType.WATER || targetTileType == TileType.TIDE) && !plantIsAquatic) {
            return "Non-aquatic plants cannot be planted on water tiles.";
        }
        if (plantIsAquatic && targetTileType != TileType.WATER && targetTileType != TileType.TIDE) {
            return "Aquatic plants must be planted on water tiles.";
        }

        if (!engine.conveyorBeltMode) {
            int cost = plant.getStats().getCost();
            if (engine.getSunCount() < cost) return "Not enough sun.";
            engine.addSun(-cost);
        }

        plant.setPlanted(true);
        plant.putRuntimeState("row", row);
        plant.putRuntimeState("col", col);
        plant.putRuntimeState("lane", row);
        engine.map.setPlant(row, col, plant);

        handlePostPlanting(engine, type, plant);
        return "Planted " + type.getDisplayName() + " at (" + col + ", " + row + ").";
    }

    private static String checkPlantAvailability(RegularGameEngine engine, PlantType type) {
        if (engine.conveyorBeltMode) {
            if (!engine.conveyorBeltQueue.contains(type))
                return "No " + type.getDisplayName() + " seed packet is available on the belt.";
        } else if (engine.isOnCooldown(type)) {
            return type.getDisplayName() + " is still recharging.";
        }

        if (!engine.conveyorBeltMode && !AppStatus.SELECTED_PLANTS.isEmpty()
            && !AppStatus.SELECTED_PLANTS.contains(type)) {
            return "Plant was not selected for this level: " + type.getDisplayName();
        }

        if (engine.lockedPlantsMode && engine.lockedPlantsForStage.contains(type)) {
            return "Plant is locked for this level: " + type.getDisplayName();
        }

        if (AppStatus.currentUser != null && AppStatus.currentUser.collectionState != null) {
            var collection = AppStatus.currentUser.collectionState;
            if (!collection.isPlantUnlocked(type)) return "Plant is locked: " + type.getDisplayName();
        }
        return null;
    }

    private static Plant createPlantInstance(RegularGameEngine engine, PlantType type) {
        int userLevel = 1;
        if (AppStatus.currentUser != null && AppStatus.currentUser.collectionState != null) {
            userLevel = AppStatus.currentUser.collectionState.getPlantLevel(type) + 1;
        }
        return PlantFactory.createPlant(type, userLevel);
    }

    private static void handlePostPlanting(RegularGameEngine engine, PlantType type, Plant plant) {
        if (engine.conveyorBeltMode) {
            engine.conveyorBeltQueue.remove(type);
        } else {
            double recharge = plant.getStats().getRechargeSeconds();
            if (recharge > 0) engine.rechargeRemaining.put(type, recharge);
        }
        if (AppStatus.BOOSTED_PLANTS.contains(type)) {
            plant.applyPlantFood(engine);
        }
    }

    public static String removeCooldownCheat(RegularGameEngine engine) {
        engine.rechargeRemaining.clear();
        if (engine.map != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 9; col++) {
                    Plant plant = engine.map.getPlantAt(row, col);
                    if (plant == null) continue;
                    for (String key : new ArrayList<>(plant.getRuntimeState().keySet())) {
                        if (key.endsWith("Timer")) plant.putRuntimeState(key, 999.0);
                    }
                }
            }
        }
        return "Cooldowns removed.";
    }
}
