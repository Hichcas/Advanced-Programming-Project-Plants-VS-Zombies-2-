package com.PVZ.model.minigame.zombotany;

import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class ZombotanyLevelLoader {

    public ZombotanyLevelDefinition loadLevel(int levelId) {
        JsonNode section = MinigamesDataLoader.section("zombotany");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                if (levelNode.path("id").asInt(-1) == levelId) {
                    return MinigamesDataLoader.mapper()
                            .convertValue(levelNode, ZombotanyLevelDefinition.class);
                }
            }
        }
        System.err.println("[ZombotanyLevelLoader] level " + levelId + " not found -> fallback level.");
        return fallbackLevel(levelId);
    }

    public List<ZombotanyLevelDefinition> loadAll() {
        java.util.List<ZombotanyLevelDefinition> result = new java.util.ArrayList<>();
        JsonNode section = MinigamesDataLoader.section("zombotany");
        JsonNode levels = section.get("levels");
        if (levels != null && levels.isArray()) {
            for (JsonNode levelNode : levels) {
                result.add(MinigamesDataLoader.mapper()
                        .convertValue(levelNode, ZombotanyLevelDefinition.class));
            }
        }
        return result;
    }

    private ZombotanyLevelDefinition fallbackLevel(int levelId) {
        ZombotanyLevelDefinition def = new ZombotanyLevelDefinition();
        def.setId(levelId);
        def.setRows(5);
        def.setCols(9);
        def.setStartingSun(150);
        def.setPlantPool(List.of(
                "PEASHOOTER", "SUNFLOWER", "WALL_NUT", "SNOW_PEA", "CHERRY_BOMB", "SQUASH"));

        ZombotanyLevelDefinition.WaveEntryDef e1 = new ZombotanyLevelDefinition.WaveEntryDef();
        e1.setZombie("ZombieTutorialDefault");
        e1.setCount(3);
        e1.setSpawnDelay(2.5f);
        ZombotanyLevelDefinition.WaveEntryDef e2 = new ZombotanyLevelDefinition.WaveEntryDef();
        e2.setZombie("ZombotanyPeashooterDefault");
        e2.setCount(1);
        e2.setSpawnDelay(3.0f);
        ZombotanyLevelDefinition.WaveDef w1 = new ZombotanyLevelDefinition.WaveDef();
        w1.setStartDelay(8.0f);
        w1.setEntries(List.of(e1, e2));

        ZombotanyLevelDefinition.WaveEntryDef e3 = new ZombotanyLevelDefinition.WaveEntryDef();
        e3.setZombie("ZombotanyWallnutDefault");
        e3.setCount(1);
        e3.setSpawnDelay(4.0f);
        ZombotanyLevelDefinition.WaveEntryDef e4 = new ZombotanyLevelDefinition.WaveEntryDef();
        e4.setZombie("ZombotanySquashDefault");
        e4.setCount(2);
        e4.setSpawnDelay(2.5f);
        ZombotanyLevelDefinition.WaveEntryDef e5 = new ZombotanyLevelDefinition.WaveEntryDef();
        e5.setZombie("ZombotanyJalapenoDefault");
        e5.setCount(1);
        e5.setSpawnDelay(5.0f);
        ZombotanyLevelDefinition.WaveDef w2 = new ZombotanyLevelDefinition.WaveDef();
        w2.setStartDelay(12.0f);
        w2.setEntries(List.of(e3, e4, e5));

        def.setWaves(List.of(w1, w2));
        return def;
    }
}
