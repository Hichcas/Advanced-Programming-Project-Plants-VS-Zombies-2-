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
    private int tideFrontier = 6;
    private int lastTideWave = -1;
    private int lastGraveWave = -1;
    private int maxTideColumn = 8;
    private boolean tideInitialized = false;
    private int tideTick = 0;
    private boolean tideRising = true;
    private static final int TIDE_RISE_TICKS = 30;
    private static final int TIDE_FALL_TICKS = 30;

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

        actions.put("risingTide", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null) {
                return;
            }
            // Find the leftmost WATER column once: the tide must never flood past the
            // existing sea into the player's lawn (doc: tide bounded by water).
            if (!tideInitialized) {
                for (int c = 0; c < 9; c++) {
                    boolean water = false;
                    for (int r = 0; r < 5; r++) {
                        Tile t = map.getTile(r, c);
                        if (t != null && (t.getType() == TileType.WATER || t.getType() == TileType.TIDE)) {
                            water = true;
                            break;
                        }
                    }
                    if (water) {
                        maxTideColumn = c;
                        break;
                    }
                }
                tideInitialized = true;
            }

            int currentWave = wm.getCurrentWave();
            if (currentWave > lastTideWave) {
                tideFrontier = Math.min(tideFrontier, maxTideColumn - 1);
                lastTideWave = currentWave;
            }

            // Oscillating tide: rise toward the water edge for TIDE_RISE_TICKS, then
            // fall back for TIDE_FALL_TICKS, repeating every wave (doc: "changes for a
            // moment, then changes again"). Only NORMAL tiles between the current tide
            // line and maxTideColumn are touched; the lawn (col < maxTideColumn edge) is safe.
            tideTick++;
            int phaseLen = tideRising ? TIDE_RISE_TICKS : TIDE_FALL_TICKS;
            if (tideTick >= phaseLen) {
                tideTick = 0;
                tideRising = !tideRising;
            }
            int activeCols;
            if (tideRising) {
                activeCols = maxTideColumn - (int) Math.round((1.0 * tideTick / TIDE_RISE_TICKS) * (maxTideColumn - 1));
            } else {
                activeCols = 1 + (int) Math.round((1.0 * tideTick / TIDE_FALL_TICKS) * (maxTideColumn - 1));
            }
            activeCols = Math.max(1, Math.min(maxTideColumn, activeCols));

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Tile tile = map.getTile(r, c);
                    if (tile == null) continue;
                    if (c >= maxTideColumn || tile.getType() == TileType.WATER) {
                        continue;
                    }
                    boolean inTide = c >= maxTideColumn - activeCols;
                    if (inTide && tile.getType() == TileType.NORMAL) {
                        Plant plant = map.getPlantAt(r, c);
                        boolean aquatic = plant != null && plant.getDefinition() != null
                                && plant.getDefinition().hasTag(PlantTag.WATER);
                        if (!aquatic) {
                            map.removePlant(r, c);
                        }
                        tile.setType(TileType.TIDE);
                    } else if (!inTide && tile.getType() == TileType.TIDE) {
                        tile.setType(TileType.NORMAL);
                    }
                }
            }
        });

        actions.put("magicalGraves", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            if (currentWave <= lastGraveWave) {
                return;
            }
            lastGraveWave = currentWave;
            // each wave, sprinkle a few new magical graves on empty normal tiles
            int toSpawn = 2 + random.nextInt(2);
            int placed = 0;
            for (int attempt = 0; attempt < 30 && placed < toSpawn; attempt++) {
                int r = random.nextInt(5);
                int c = random.nextInt(9);
                Tile tile = map.getTile(r, c);
                if (tile == null || tile.getType() != TileType.NORMAL) {
                    continue;
                }
                if (map.getPlantAt(r, c) != null) {
                    continue;
                }
                tile.setType(TileType.NECROMANCY);
                placed++;
                if (random.nextDouble() < 0.3) {
                    engine.addSun(50);
                }
            }
        });

        actions.put("lowCoastLaunch", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            // Doc: low coasts - if a zombie is on the water beneath a low coast tile,
            // it is thrown out / launched. We knock the zombie back and briefly stun it.
            for (Zombie z : engine.getAllZombies()) {
                if (z == null || z.isDead()) {
                    continue;
                }
                int col = map.worldToCol((float) z.getX());
                int row = (int) Math.round(z.getRow());
                if (col < 0 || col >= 9 || row < 0 || row >= 5) {
                    continue;
                }
                Tile waterTile = map.getTile(row, col);
                if (waterTile == null || waterTile.getType() != TileType.WATER) {
                    continue;
                }
                boolean nearLowCoast = false;
                for (int dc = -1; dc <= 1; dc++) {
                    int nc = col + dc;
                    if (nc < 0 || nc >= 9) continue;
                    Tile nt = map.getTile(row, nc);
                    if (nt != null && nt.getType() == TileType.LOW_COAST) {
                        nearLowCoast = true;
                        break;
                    }
                }
                if (nearLowCoast) {
                    z.setX(z.getX() + 120);
                    if (!z.isFrozen()) {
                        z.stunOnHit();
                    }
                }
            }
        });

        actions.put("necromancySpawn", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Tile tile = map.getTile(r, c);
                    if (tile == null || tile.getType() != TileType.NECROMANCY) {
                        continue;
                    }
                    if (random.nextDouble() < 0.04) {
                        double x = tile.getX() + tile.getWidth() * 0.8;
                        engine.spawnZombie("ZombieDarkDefault", r, (int) x);
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
