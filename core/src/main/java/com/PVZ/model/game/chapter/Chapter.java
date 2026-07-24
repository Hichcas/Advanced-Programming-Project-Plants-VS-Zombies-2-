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

/**
 * Represents a chapter with special mechanics and actions that can be applied
 * during gameplay. Refactored to comply with Checkstyle (method length ≤ 50 lines).
 */
public class Chapter {
    private final ChapterConfig config;
    private final HashMap<String, BiConsumer<com.PVZ.model.game.Map, RegularGameEngine>> actions = new HashMap<>();
    private final Random random = new Random();

    private int lastIceWindWave = -1;
    private int lastTideWave = -1;
    private int lastGraveWave = -1;
    private int lastNecroWave = -1;
    private int lastLowCoastWave = -1;
    private int tideFloodedColumn = 9;

    public Chapter(ChapterConfig config) {
        this.config = config;
        registerActions();
    }

    // ---------- Action registration ----------

    private void registerActions() {
        registerUpdateTombstones();
        registerRaZombieStealSun();
        registerExplorerBurnRow();
        registerTombraiserSpawnGrave();
        registerIceWindTick();
        registerMeltIceNearFire();
        registerRisingTide();
        registerMagicalGraves();
        registerSpawnFromLowCoast();
        registerNecromancySpawn();
    }

    private void registerUpdateTombstones() {
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
    }

    private void registerRaZombieStealSun() {
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
    }

    private void registerExplorerBurnRow() {
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
    }

    private void registerTombraiserSpawnGrave() {
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

    private void registerIceWindTick() {
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

            applyIceWindToPlants(map, waveChanged);

            if (waveChanged) {
                lastIceWindWave = currentWave;
            }
        });
    }

    private void registerMeltIceNearFire() {
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
                    if (!isFirePlant(plant)) {
                        continue;
                    }
                    meltAdjacentIce(map, r, c);
                }
            }
        });
    }

    private void registerRisingTide() {
        actions.put("risingTide", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null) {
                return;
            }

            int currentWave = wm.getCurrentWave();
            if (currentWave > lastTideWave) {
                lastTideWave = currentWave;
                advanceTide(map);
            }
        });
    }

    private void registerMagicalGraves() {
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

            spawnMagicalGraves(map, engine);
        });
    }

    private void registerSpawnFromLowCoast() {
        actions.put("spawnFromLowCoast", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null || !wm.isStarted()) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            if (currentWave <= lastLowCoastWave) {
                return;
            }
            lastLowCoastWave = currentWave;
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Tile tile = map.getTile(r, c);
                    if (tile == null || tile.getType() != TileType.LOW_COAST) {
                        continue;
                    }
                    if (random.nextDouble() < 0.35) {
                        String[] beachZombies = {
                            "ZombieBeachDefault", "ZombieBeachSnorkel",
                            "ZombieBeachFastSwimmer"
                        };
                        String alias = beachZombies[random.nextInt(beachZombies.length)];
                        engine.spawnZombie(alias, r, c);
                    }
                }
            }
        });
    }

    private void registerNecromancySpawn() {
        actions.put("necromancySpawn", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null || !wm.isStarted()) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            if (currentWave <= lastNecroWave) {
                return;
            }
            lastNecroWave = currentWave;
            spawnFromNecromancyTiles(map, engine);
        });
    }

    // ---------- Ice Wind helpers ----------

    private void applyIceWindToPlants(com.PVZ.model.game.Map map, boolean waveChanged) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Plant plant = map.getPlantAt(r, c);
                if (plant == null || plant.isDead()) {
                    continue;
                }
                if (isFirePlant(plant)) {
                    continue;
                }

                int freezeLv = asInt(plant.getRuntimeState("freezeLevel"), 0);

                if (waveChanged && freezeLv < 3) {
                    plant.putRuntimeState("freezeLevel", 3);
                    Object existingIceHp = plant.getRuntimeState("iceHp");
                    if (existingIceHp == null || asInt(existingIceHp, 0) <= 0) {
                        plant.putRuntimeState("iceHp", 600);
                    }
                    freezeLv = 3;
                }

                if (freezeLv >= 3) {
                    plant.disableForTicks(2);
                    plant.takeDamage(6);
                    int hp = asInt(plant.getRuntimeState("iceHp"), 0);
                    hp -= 60;
                    if (hp <= 0) {
                        plant.putRuntimeState("freezeLevel", 0);
                        plant.putRuntimeState("iceHp", 0);
                        plant.putRuntimeState("disabledTicks", 0);
                    } else {
                        plant.putRuntimeState("iceHp", hp);
                    }
                }
            }
        }
    }

    private boolean isFirePlant(Plant plant) {
        boolean isFire = plant.getStats().getBooleanExtra("freezeImmune", false);
        if (!isFire && plant.getDefinition() != null) {
            isFire = plant.getDefinition().hasTag(PlantTag.FIRE);
        }
        return isFire;
    }

    private void meltAdjacentIce(com.PVZ.model.game.Map map, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (nr < 0 || nr >= 5 || nc < 0 || nc >= 9) continue;
                Plant neighbor = map.getPlantAt(nr, nc);
                if (neighbor == null || neighbor.isDead()) continue;
                int freeze = asInt(neighbor.getRuntimeState("freezeLevel"), 0);
                if (freeze >= 3) {
                    int hp = asInt(neighbor.getRuntimeState("iceHp"), 0);
                    hp -= 60;
                    if (hp <= 0) {
                        neighbor.putRuntimeState("freezeLevel", 0);
                        neighbor.putRuntimeState("iceHp", 0);
                        neighbor.putRuntimeState("disabledTicks", 0);
                    } else {
                        neighbor.putRuntimeState("iceHp", hp);
                    }
                }
            }
        }
    }

    // ---------- Tide helpers ----------

    private void advanceTide(com.PVZ.model.game.Map map) {
        if (tideFloodedColumn <= 0) {
            return;
        }
        tideFloodedColumn--;
        for (int r = 0; r < 5; r++) {
            Tile tile = map.getTile(r, tideFloodedColumn);
            if (tile == null) {
                continue;
            }
            Plant topPlant = map.getPlantAt(r, tideFloodedColumn);
            Plant basePlant = map.getBasePlantAt(r, tideFloodedColumn);
            boolean protectedByLilyPad = basePlant != null && basePlant.getDefinition() != null
                && basePlant.getDefinition().hasTag(PlantTag.WATER);
            if (topPlant != null && !protectedByLilyPad) {
                boolean aquatic = topPlant.getDefinition() != null
                    && topPlant.getDefinition().hasTag(PlantTag.WATER);
                if (!aquatic) {
                    map.removePlant(r, tideFloodedColumn);
                }
            }
            if (basePlant != null && !basePlant.getDefinition().hasTag(PlantTag.WATER)) {
                map.removeBasePlant(r, tideFloodedColumn);
            }
            tile.setType(TileType.TIDE);
        }
    }

    // ---------- Magical Graves helpers ----------

    private void spawnMagicalGraves(com.PVZ.model.game.Map map, RegularGameEngine engine) {
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
            tile.setHp(700);
            placed++;
            if (random.nextDouble() < 0.3) {
                engine.addSun(50);
            }
        }
    }

    // ---------- Necromancy Spawn helpers ----------

    private void spawnFromNecromancyTiles(com.PVZ.model.game.Map map, RegularGameEngine engine) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = map.getTile(r, c);
                if (tile == null || tile.getType() != TileType.NECROMANCY) {
                    continue;
                }
                if (random.nextDouble() < 0.50) {
                    engine.spawnZombie("ZombieDarkDefault", r, c);
                    tile.setType(TileType.NORMAL);
                    tile.setHp(0);
                }
            }
        }
    }

    // ---------- Public API ----------

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
                    if ("NECROMANCY".equals(te.getType())) {
                        tile.setHp(700);
                    }
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

    // ---------- Utilities ----------

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }
}
