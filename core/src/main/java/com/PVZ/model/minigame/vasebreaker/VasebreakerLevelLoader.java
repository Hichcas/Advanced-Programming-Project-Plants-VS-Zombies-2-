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
        VasebreakerLevelDefinition def = new VasebreakerLevelDefinition();
        def.setId(1);
        def.setRows(5);
        def.setCols(9);
        def.setVases(List.of(
                "0,0,GARGANTUAR",
                "2,4,PLANT"
        ));
        def.setZombiePool(List.of("ZombieTutorialDefault"));
        def.setPlantPool(List.of("PEASHOOTER", "SUNFLOWER"));
        def.setSeedPacketLifetimeSeconds(8f);
        return def;
    }
}
