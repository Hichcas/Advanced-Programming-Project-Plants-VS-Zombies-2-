package com.PVZ.model.game.chapter;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.WaveManager;

import java.util.HashMap;
import java.util.Random;
import java.util.function.BiConsumer;

public class Chapter {
    private final ChapterConfig config;
    private final HashMap<String, BiConsumer<com.PVZ.model.game.Map, RegularGameEngine>> actions = new HashMap<>();
    private final Random random = new Random();
    private int lastIceWindWave = -1;

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

        actions.put("iceWindTick", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            boolean waveChanged = currentWave > lastIceWindWave;

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Plant plant = map.getPlantAt(r, c);
                    if (plant == null || plant.isDead()) {
                        continue;
                    }
                    boolean isFire = plant.getStats().getBooleanExtra("freezeImmune", false);
                    if (!isFire && plant.getDefinition() != null) {
                        isFire = plant.getDefinition().hasTag(PlantTag.FIRE);
                    }
                    if (isFire) {
                        continue;
                    }

                    if (waveChanged) {
                        int lv = asInt(plant.getRuntimeState("freezeLevel"), 0);
                        if (lv < 3) {
                            lv++;
                        }
                        plant.putRuntimeState("freezeLevel", lv);
                        if (lv >= 3) {
                            Object existingIceHp = plant.getRuntimeState("iceHp");
                            if (existingIceHp == null || asInt(existingIceHp, 0) <= 0) {
                                plant.putRuntimeState("iceHp", 600);
                            }
                        }
                    }

                    int freezeLv = asInt(plant.getRuntimeState("freezeLevel"), 0);
                    if (freezeLv >= 3) {
                        int hp = asInt(plant.getRuntimeState("iceHp"), 0);
                        hp -= 60;
                        if (hp <= 0) {
                            plant.putRuntimeState("freezeLevel", 0);
                            plant.putRuntimeState("iceHp", 0);
                        } else {
                            plant.putRuntimeState("iceHp", hp);
                        }
                    }
                }
            }

            if (waveChanged) {
                lastIceWindWave = currentWave;
            }
        });

        actions.put("meltIceNearFire", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Plant plant = map.getPlantAt(r, c);
                    if (plant == null || plant.isDead()) {
                        continue;
                    }
                    boolean isFire = plant.getStats().getBooleanExtra("freezeImmune", false);
                    if (!isFire && plant.getDefinition() != null) {
                        isFire = plant.getDefinition().hasTag(PlantTag.FIRE);
                    }
                    if (!isFire) {
                        continue;
                    }
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = r + dr;
                            int nc = c + dc;
                            if (nr < 0 || nr >= 5 || nc < 0 || nc >= 9) continue;
                            Plant neighbor = map.getPlantAt(nr, nc);
                            if (neighbor == null || neighbor.isDead()) continue;
                            int neighborFreeze = asInt(neighbor.getRuntimeState("freezeLevel"), 0);
                            if (neighborFreeze >= 3) {
                                int hp = asInt(neighbor.getRuntimeState("iceHp"), 0);
                                hp -= 60;
                                if (hp <= 0) {
                                    neighbor.putRuntimeState("freezeLevel", 0);
                                    neighbor.putRuntimeState("iceHp", 0);
                                } else {
                                    neighbor.putRuntimeState("iceHp", hp);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    public void applySetup(com.PVZ.model.game.Map map, StageConfig stage) {
        if (map == null || stage == null) {
            return;
        }
        if (stage.getTombstones() != null) {
            for (StageConfig.TombstoneEntry t : stage.getTombstones()) {
                Tile tile = map.getTile(t.getRow(), t.getCol());
                if (tile != null) {
                    tile.setType(TileType.TOMBSTONE);
                    tile.setHp(t.getHp());
                }
            }
        }
        if (stage.getTiles() != null) {
            for (StageConfig.TileEntry te : stage.getTiles()) {
                Tile tile = map.getTile(te.getRow(), te.getCol());
                if (tile != null) {
                    tile.setType(TileType.valueOf(te.getType()));
                }
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
