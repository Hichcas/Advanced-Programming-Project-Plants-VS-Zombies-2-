package com.PVZ.model.minigame.wallnutbowling;

import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public class WallnutBowlingLevelLoader {

    public WallnutBowlingLevelDefinition loadLevel(int levelId) {
        JsonNode section = MinigamesDataLoader.section("wallnut_bowling");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                if (levelNode.path("id").asInt(-1) == levelId) {
                    return MinigamesDataLoader.mapper()
                            .convertValue(levelNode, WallnutBowlingLevelDefinition.class);
                }
            }
        }
        System.err.println("[WallnutBowlingLevelLoader] level " + levelId + " not found -> fallback level.");
        return fallbackLevel(levelId);
    }

    private WallnutBowlingLevelDefinition fallbackLevel(int levelId) {
        WallnutBowlingLevelDefinition def = new WallnutBowlingLevelDefinition();
        def.setId(levelId);
        def.setRows(5);
        def.setCols(9);
        def.setRedLineCol(3);
        def.setNutPool(List.of(
                Map.of("type", "NORMAL", "weight", 70),
                Map.of("type", "EXPLOSIVE", "weight", 20),
                Map.of("type", "GIANT", "weight", 10)
        ));
        def.setZombiePool(List.of());
        def.setTotalZombies(10);
        def.setWaveIntervalSeconds(6.0);
        def.setMinWaveIntervalSeconds(2.0);
        def.setWaveIntervalDecreasePerWave(0.3);
        def.setNutSpeed(450.0);
        return def;
    }
}
