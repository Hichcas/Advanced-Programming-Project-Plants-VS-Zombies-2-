package com.PVZ.model.game.chapter;

import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.RegularGameEngine;

import java.util.HashMap;
import java.util.Random;
import java.util.function.BiConsumer;

public class Chapter {
    private final ChapterConfig config;
    private final HashMap<String, BiConsumer<com.PVZ.model.game.Map, RegularGameEngine>> actions = new HashMap<>();
    private final Random random = new Random();

    public Chapter(ChapterConfig config) {
        this.config = config;
        registerActions();
    }

    private void registerActions() {
        actions.put("updateTombstones", (map, engine) -> {
            if (map == null) {
                return;
            }
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Tile tile = map.getTile(r, c);
                    if (tile != null && tile.getType() == TileType.TOMBSTONE && tile.getHp() <= 0) {
                        tile.setType(TileType.NORMAL);
                        tile.setHp(0);
                    }
                }
            }
        });

        actions.put("raZombieStealSun", (map, engine) -> {
            if (engine == null) {
                return;
            }
            for (Zombie z : engine.getAllZombies()) {
                if (z != null && !z.isDead() && "ZombieRa".equals(z.getAlias())) {
                    z.stealNearbySun(engine.getSunManager());
                }
            }
        });

        actions.put("explorerBurnRow", (map, engine) -> {
            if (engine == null || map == null) {
                return;
            }
            for (Zombie z : engine.getAllZombies()) {
                if (z != null && !z.isDead() && "ZombieExplorer".equals(z.getAlias())) {
                    z.burnPlantsAhead(map, engine.getBattleController());
                }
            }
        });

        actions.put("tombraiserSpawnGrave", (map, engine) -> {
            if (map == null) {
                return;
            }
            for (Zombie z : engine.getAllZombies()) {
                if (z != null && !z.isDead() && "ZombieTombRaiser".equals(z.getAlias())) {
                    z.maybeSpawnGraves(map, random);
                }
            }
        });
    }

    public void applySetup(com.PVZ.model.game.Map map, StageConfig stage) {
        if (map == null || stage == null || stage.getTombstones() == null) {
            return;
        }
        for (StageConfig.TombstoneEntry t : stage.getTombstones()) {
            Tile tile = map.getTile(t.getRow(), t.getCol());
            if (tile != null) {
                tile.setType(TileType.TOMBSTONE);
                tile.setHp(t.getHp());
            }
        }
    }

    public void update(com.PVZ.model.game.Map map, RegularGameEngine engine) {
        if (config.getUpdate() == null) {
            return;
        }
        for (ChapterConfig.UpdateAction action : config.getUpdate()) {
            BiConsumer<com.PVZ.model.game.Map, RegularGameEngine> act = actions.get(action.getAction());
            if (act != null) {
                act.accept(map, engine);
            }
        }
    }

    public ChapterConfig getConfig() {
        return config;
    }
}
