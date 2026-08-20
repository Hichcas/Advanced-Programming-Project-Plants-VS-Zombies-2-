package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.quest.PlantFamilyMapper;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;

public class PlantHandler {

    public static String plantPlant(RegularGameEngine engine, PlantType type, int x, int y) {
        if (engine.map == null) return "Map is not ready.";
        if (type == null) return "Unknown plant type.";
        int row = engine.normalizeIndex(y);
        int col = engine.normalizeIndex(x);
        if (!engine.map.isWithinBounds(row, col)) return "Invalid tile.";
        Tile targetTile = engine.map.getTile(row, col);
        if (targetTile != null && targetTile.isScorched()) {
            return "Cannot plant on a tile while it is on fire!";
        }
        TileType targetTileType = targetTile != null ? targetTile.getType() : TileType.NORMAL;
        if (targetTileType == TileType.CRATER) return "This tile is a permanent Doom-shroom crater and cannot be planted on.";
        boolean isWater = targetTileType == TileType.WATER || targetTileType == TileType.TIDE;
        Plant existingTop = engine.map.getPlantAt(row, col);
        Plant existingBase = engine.map.getBasePlantAt(row, col);
        boolean isLilyPad = type == PlantType.LILY_PAD;
        boolean isAquatic = PlantLibrary.findByType(type).map(def -> def.hasTag(PlantTag.WATER)).orElse(false);
        boolean addingPeaPodHead = type == PlantType.PEA_POD
            && existingTop != null
            && existingTop.getType() == PlantType.PEA_POD;
        if (isAquatic && !isWater) return "Aquatic plants must be planted on water tiles.";
        if (isWater && !isAquatic && !isLilyPad && existingBase == null)
            return "Non-aquatic plants need a Lily Pad on water tiles.";
        if (isLilyPad && !isWater) return "Lily Pad must be planted on water tiles.";
        if (isLilyPad && existingBase != null) return "This tile already has a Lily Pad.";
        if (type != PlantType.GRAVE_BUSTER && (targetTileType == TileType.TOMBSTONE || targetTileType == TileType.NECROMANCY)) {
            return "Cannot plant on a grave.";
        }
        if (type == PlantType.GRAVE_BUSTER && targetTileType != TileType.TOMBSTONE && targetTileType != TileType.NECROMANCY) {
            return "Grave Buster can only be planted on graves.";
        }
        if (!addingPeaPodHead) {
            if (isWater && !isAquatic && !isLilyPad && existingTop != null) return "Tile is occupied.";
            if (!isWater && existingTop != null) return "Tile is occupied.";
        }
        String availabilityError = checkPlantAvailability(engine, type);
        if (availabilityError != null) return availabilityError;

        if (addingPeaPodHead) {
            int heads = 1;
            Object state = existingTop.getRuntimeState("peaPodHeads");
            if (state instanceof Number n) heads = n.intValue();
            if (heads >= 5) return "Pea Pod is already at 5 heads.";
            if (!engine.conveyorBeltMode) {
                int cost = existingTop.getStats().getCost();
                if (engine.getSunCount() < cost) return "Not enough sun.";
                engine.addSun(-cost);
            }
            heads++;
            existingTop.putRuntimeState("peaPodHeads", heads);
            String idleState = heads == 1 ? "idle" : "idle" + heads;
            com.PVZ.model.entity.PlantAnimation.trigger(existingTop.getInstance(), idleState, 1.0);
            handlePostPlanting(engine, type, existingTop);
            return "Pea Pod grew to " + heads + " heads at (" + col + ", " + row + ").";
        }

        // Imitater copies a selected plant for the actual board slot so the copied plant
        // gets the normal behavior/rendering pipeline rather than remaining an inert shell.
        PlantType plantedType = type;
        if (type == PlantType.IMITATER) {
            PlantType copyTarget = null;
            for (PlantType selected : AppStatus.SELECTED_PLANTS) {
                if (selected != null && selected != PlantType.IMITATER) {
                    copyTarget = selected;
                    break;
                }
            }
            if (copyTarget == null) {
                return "Select another plant beside Imitater so it can copy that plant.";
            }
            plantedType = copyTarget;
        }

        Plant plant = createPlantInstance(engine, plantedType);
        if (plant == null) return "Cannot create plant.";
        if (plantedType == PlantType.PEA_POD) {
            plant.putRuntimeState("peaPodHeads", 1);
        }
        if (!engine.conveyorBeltMode) {
            int cost = type == PlantType.IMITATER
                ? PlantLibrary.getEffectiveCost(PlantType.IMITATER)
                : plant.getStats().getCost();
            if (engine.getSunCount() < cost) return "Not enough sun.";
            engine.addSun(-cost);
        }
        if (isLilyPad)
            engine.map.setBasePlant(row, col, plant);
        else if (isWater && !isAquatic)
            engine.map.setPlant(row, col, plant);
        else
            engine.map.setPlant(row, col, plant);
        plant.setPlanted(true);
        plant.putRuntimeState("row", row);
        plant.putRuntimeState("col", col);
        plant.putRuntimeState("lane", row);
        handlePostPlanting(engine, type, plant);
        if (AppStatus.currentUser != null && AppStatus.currentUser.questState != null) {
            AppStatus.currentUser.questState.getQuestManager().onPlantPlaced(type);
        }
        engine.questPlantTypesUsed.add(type);
        engine.questPlantFamiliesUsed.add(PlantFamilyMapper.getFamily(type));
        if (type == PlantType.IMITATER) {
            return "Planted Imitater copying " + plantedType.getDisplayName()
                + " at (" + col + ", " + row + ").";
        }
        return "Planted " + type.getDisplayName() + " at (" + col + ", " + row + ").";
    }

    private static String checkPlantAvailability(RegularGameEngine engine, PlantType type) {
        if (engine.conveyorBeltMode) {
            if (!engine.conveyorBeltQueue.contains(type))
                return "No " + type.getDisplayName() + " seed packet is available on the belt.";
        } else if (engine.isFreePlantingPhase()) {
            // PLANT WHAT YOU GET: before the player starts the zombie waves, planting never recharges.
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
        } else if (engine.isFreePlantingPhase()) {
            // No recharge timer is started while the player is still freely planting before the waves begin.
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
