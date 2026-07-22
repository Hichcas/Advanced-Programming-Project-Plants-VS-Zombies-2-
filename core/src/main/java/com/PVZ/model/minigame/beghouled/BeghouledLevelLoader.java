package com.PVZ.model.minigame.beghouled;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class BeghouledLevelLoader {

    public BeghouledLevelDefinition loadLevel(int levelId) {
        JsonNode section = MinigamesDataLoader.section("beghouled");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                if (levelNode.path("id").asInt(-1) == levelId) {
                    return MinigamesDataLoader.mapper()
                            .convertValue(levelNode, BeghouledLevelDefinition.class);
                }
            }
        }
        System.err.println("[BeghouledLevelLoader] level " + levelId + " not found -> fallback level.");
        return fallbackLevel(levelId);
    }

    private BeghouledLevelDefinition fallbackLevel(int levelId) {
        BeghouledLevelDefinition def = new BeghouledLevelDefinition();
        def.setId(levelId);
        def.setRows(5);
        def.setCols(9);
        def.setTargetMatches(15 + levelId * 5);
        def.setStartingSun(200);
        def.setZombieSpawnIntervalSeconds(8.0);
        def.setPlantTypes(List.of(
                PlantType.PEASHOOTER.name(),
                PlantType.WALL_NUT.name(),
                PlantType.PUFF_SHROOM.name(),
                PlantType.CABBAGE_PULT.name(),
                PlantType.SUNFLOWER.name()
        ));
        def.setZombieAliases(List.of(
                "ZombieTutorialDefault",
                "ZombieTutorialArmor1Default",
                "ZombieMummyDefault",
                "ZombieIceageDefault",
                "ZombieBeachDefault"
        ));
        def.setUpgrades(List.of(
                new BeghouledUpgrade(PlantType.PEASHOOTER, PlantType.REPEATER, 500),
                new BeghouledUpgrade(PlantType.REPEATER, PlantType.THREEPEATER, 1500),
                new BeghouledUpgrade(PlantType.WALL_NUT, PlantType.TALL_NUT, 500),
                new BeghouledUpgrade(PlantType.PUFF_SHROOM, PlantType.FUME_SHROOM, 250),
                new BeghouledUpgrade(PlantType.CABBAGE_PULT, PlantType.MELON_PULT, 1000),
                new BeghouledUpgrade(PlantType.MELON_PULT, PlantType.WINTER_MELON, 750)
        ));
        return def;
    }
}
