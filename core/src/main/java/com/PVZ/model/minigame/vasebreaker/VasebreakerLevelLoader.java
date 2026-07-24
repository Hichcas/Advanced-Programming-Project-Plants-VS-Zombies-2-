package com.PVZ.model.minigame.vasebreaker;

import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class VasebreakerLevelLoader {

    public VasebreakerLevelDefinition loadLevel(int levelId) {
        JsonNode section = MinigamesDataLoader.section("vasebreaker");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                if (levelNode.path("id").asInt(-1) == levelId) {
                    return MinigamesDataLoader.mapper()
                            .convertValue(levelNode, VasebreakerLevelDefinition.class);
                }
            }
        }
        System.err.println("[VasebreakerLevelLoader] level " + levelId + " not found -> fallback level.");
        return fallbackLevel();
    }

    private VasebreakerLevelDefinition fallbackLevel() {
        // Safety net only: should never be hit once all 3 vasebreaker levels are
        // defined in minigames.json. Mirrors level 1's random/full-pool behaviour
        // instead of a tiny fixed layout, so a missing entry doesn't silently
        // degrade the game (fewer plant types, no gargantuar variety, etc).
        VasebreakerLevelDefinition def = new VasebreakerLevelDefinition();
        def.setId(1);
        def.setRows(5);
        def.setCols(9);
        def.setRandom(true);
        def.setVaseCount(45);
        def.setPlantVaseChance(0.22);
        def.setGargantuarVaseChance(0.08);
        def.setNormalEmptyChance(0.12);
        def.setNormalZombieChance(0.6);
        def.setNormalSeedChance(0.28);
        def.setZombiePool(List.of());
        def.setPlantPool(List.of());
        def.setSeedPacketLifetimeSeconds(8f);
        return def;
    }
}
