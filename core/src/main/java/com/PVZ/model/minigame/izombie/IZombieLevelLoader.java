package com.PVZ.model.minigame.izombie;

import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class IZombieLevelLoader {

    public IZombieLevelDefinition loadLevel(int levelId) {
        JsonNode section = MinigamesDataLoader.section("i_zombie");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                if (levelNode.path("id").asInt(-1) == levelId) {
                    return MinigamesDataLoader.mapper()
                            .convertValue(levelNode, IZombieLevelDefinition.class);
                }
            }
        }
        System.err.println("[IZombieLevelLoader] level " + levelId + " not found -> fallback level.");
        return fallbackLevel(levelId);
    }

    private IZombieLevelDefinition fallbackLevel(int levelId) {
        IZombieLevelDefinition def = new IZombieLevelDefinition();
        def.setId(levelId);
        def.setRows(5);
        def.setCols(9);
        def.setRedLineCol(6);
        def.setStartingSun(150);
        def.setZombieRoster(List.of(
                new ZombieOption("ZombieTutorialDefault", 50, "Basic Zombie"),
                new ZombieOption("ZombieTutorialArmor1Default", 75, "Conehead Zombie"),
                new ZombieOption("ZombieMummyDefault", 50, "Mummy Zombie"),
                new ZombieOption("ZombieIceageDefault", 50, "Iceage Zombie"),
                new ZombieOption("ZombieBeachDefault", 60, "Beach Zombie")
        ));
        return def;
    }
}
