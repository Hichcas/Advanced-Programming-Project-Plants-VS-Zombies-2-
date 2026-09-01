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
    private int lastTideWave = -1;
    private int lastGraveWave = -1;
    private int lastNecroWave = -1;
    private int lastLowCoastWave = -1;
    private int tideFloodedColumn = 9;
    private int lastSandstormWave = -1;

    public Chapter(ChapterConfig config) {
        this.config = config;
        registerActions();
    }

    private void registerActions() {
        registerUpdateTombstones();
        registerRaZombieStealSun();
        registerExplorerBurnRow();
        registerTombraiserSpawnGrave();
        registerSandstormWave();
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
                    if (tile != null && (tile.getType() == TileType.TOMBSTONE ||
                        tile.getType() == TileType.NECROMANCY) && tile.getHp() <= 0) {
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

    private void registerSandstormWave() {
        actions.put("sandstormWave", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null || !wm.isStarted()) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            if (currentWave <= lastSandstormWave) {
                return;
            }
            lastSandstormWave = currentWave;
            int count = (wm.isFinalWave() || currentWave % 2 == 0) ? 2 + random.nextInt(2) : 1;
            if (engine.getSandstormManager() != null) {
                engine.getSandstormManager().triggerSandstorm(engine, count);
            }
        });
    }

    private float iceWindTimer = 0f;
    private static final float ICE_WIND_INTERVAL = 12.0f;

    private void registerIceWindTick() {
        actions.put("iceWindTick", (map, engine) -> {
            if (map == null || engine == null) {
                return;
            }
            WaveManager wm = engine.getWaveManager();
            if (wm == null || !wm.isStarted()) {
                return;
            }
            int currentWave = wm.getCurrentWave();
            boolean waveChanged = currentWave > lastIceWindWave;

            iceWindTimer += 0.05f;
            boolean shouldBlow = (waveChanged && lastIceWindWave != -1) || (iceWindTimer >= ICE_WIND_INTERVAL);

            if (lastIceWindWave == -1) {
                lastIceWindWave = currentWave;
            }

            if (shouldBlow) {
                iceWindTimer = 0f;
                lastIceWindWave = currentWave;
                applyIceWindToPlants(map, true);
                if (engine.getIceWindManager() != null) {
                    engine.getIceWindManager().triggerIceWind(engine, new int[]{0, 1, 2, 3, 4});
                }
            } else {
                applyIceWindToPlants(map, false);
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

            if (tideFloodedColumn == 9) {
                int lowestCoast = 9;
                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 9; c++) {
                        Tile t = map.getTile(r, c);
                        if (t != null && (t.getType() == TileType.LOW_COAST ||
                            t.getType() == TileType.WATER || t.getType() == TileType.TIDE)) {
                            if (c < lowestCoast) lowestCoast = c;
                        }
                    }
                }
                tideFloodedColumn = lowestCoast;
            }

            int currentWave = wm.getCurrentWave();
            if (currentWave > lastTideWave && currentWave > 0) {
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
            com.PVZ.model.status.AppStatus.showAnnouncement("LOW COAST!");
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
            com.PVZ.model.status.AppStatus.showAnnouncement("NECROMANCY!");
            spawnFromNecromancyTiles(map, engine);
        });
    }

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

                if (waveChanged) {
                    if (freezeLv < 3) {
                        freezeLv++;
                        plant.putRuntimeState("freezeLevel", freezeLv);
                        if (freezeLv >= 3) {
                            plant.putRuntimeState("iceHp", 600);
                        }
                    }
                }

                if (freezeLv >= 3) {
                    plant.disableForTicks(5);
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

    private boolean isWaterPlant(Plant plant) {
        if (plant == null) return false;
        if (plant.getType() != null) {
            String name = plant.getType().name().toUpperCase();
            if (name.contains("LILY") || name.contains("KELP") || name.contains("WATER") ||
                name.contains("AQUA") || name.contains("TANGLE")) {
                return true;
            }
        }
        if (plant.getDefinition() != null) {
            if (plant.getDefinition().hasTag(PlantTag.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void advanceTide(com.PVZ.model.game.Map map) {
        for (int step = 0; step < 2; step++) {
            if (tideFloodedColumn < 0) {
                break;
            }
            int targetCol = tideFloodedColumn;
            tideFloodedColumn--;
            for (int r = 0; r < 5; r++) {
                Tile tile = map.getTile(r, targetCol);
                if (tile == null) {
                    continue;
                }
                Plant topPlant = map.getPlantAt(r, targetCol);
                Plant basePlant = map.getBasePlantAt(r, targetCol);
                boolean protectedByLilyPad = (basePlant != null && isWaterPlant(basePlant));

                if (topPlant != null && !protectedByLilyPad) {
                    if (!isWaterPlant(topPlant)) {
                        System.out.println("[RisingTide] Water flooded column " + targetCol + "! Non-aquatic plant " +
                            topPlant.getType() + " at (" + targetCol + ", " + r + ") drowned in the rising tide.");
                        topPlant.takeDamage(99999);
                        map.removePlant(r, targetCol);
                    }
                }
                if (basePlant != null && !isWaterPlant(basePlant)) {
                    basePlant.takeDamage(99999);
                    map.removeBasePlant(r, targetCol);
                }
                tile.setType(TileType.TIDE);
            }
        }
    }

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
            tile.setMaxHp(700);

            double roll = random.nextDouble();
            if (roll < 0.60) {
                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_NOOP);
            } else if (roll < 0.90) {
                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_SUN);
            } else {
                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_PLANTFOOD);
            }
            placed++;

            if (engine != null) {
                float[] center = engine.getPlantWorldCenter(r, c);
                engine.addTimedPamEffect(
                    "768/FULL/EFFECTS/TOMBSTONE_DARK_SPAWN_EFFECT/TOMBSTONE_DARK_SPAWN_EFFECT.PAM",
                    "animation", 1.3333, 1.0f, center[0], center[1]);
            }
        }
    }

    private void spawnFromNecromancyTiles(com.PVZ.model.game.Map map, RegularGameEngine engine) {
        String[] darkZombies = {"ZombieDarkDefault", "ZombieDarkImpDefault", "ZombieCamelDefault"};
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = map.getTile(r, c);
                if (tile == null || tile.getType() != TileType.NECROMANCY) {
                    continue;
                }
                if (random.nextDouble() < 0.50) {
                    String zAlias = darkZombies[random.nextInt(darkZombies.length)];
                    engine.spawnZombie(zAlias, r, c);
                    if (engine != null) {
                        float[] center = engine.getPlantWorldCenter(r, c);
                        engine.addTimedPamEffect(
                            "768/FULL/EFFECTS/TOMBSTONE_DARK_BASE_DAMAGE/TOMBSTONE_DARK_BASE_DAMAGE.PAM",
                            "animation", 1.0, 1.0f, center[0], center[1]);
                    }
                    tile.setType(TileType.NORMAL);
                    tile.setHp(0);
                }
            }
        }
    }

    public void applySetup(com.PVZ.model.game.Map map, StageConfig stage) {
        if (map == null || stage == null) {return;}
        if (stage.getTombstones() != null) {
            int tombIndex = 0;
            for (StageConfig.TombstoneEntry t : stage.getTombstones()) {
                Tile tile = map.getTile(t.getRow(), t.getCol());
                if (tile != null) {
                    tile.setType(TileType.TOMBSTONE);
                    int hp = t.getHp() > 0 ? t.getHp() : 700;
                    tile.setHp(hp);tile.setMaxHp(hp);
                    if (config != null && "DARK_AGES".equalsIgnoreCase(config.getName())) {
                        if (tombIndex % 3 == 0) {
                            tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_SUN);} else if (tombIndex % 3 == 1) {
                            tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_PLANTFOOD);} else {
                            tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_NOOP);}
                        tombIndex++;} else {
                        tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.EGYPT);}}}}
        if (stage.getTiles() != null) {
            int tileIndex = 0;
            for (StageConfig.TileEntry te : stage.getTiles()) {
                Tile tile = map.getTile(te.getRow(), te.getCol());
                if (tile != null) {
                    TileType tt = TileType.valueOf(te.getType());
                    tile.setType(tt);
                    if (tt == TileType.NECROMANCY || tt == TileType.TOMBSTONE) {
                        tile.setHp(700);tile.setMaxHp(700);
                        if (config != null && "DARK_AGES".equalsIgnoreCase(config.getName())) {
                            if (tileIndex % 3 == 0) {
                                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_SUN);
                            } else if (tileIndex % 3 == 1) {
                                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_PLANTFOOD);} else {
                                tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.DARK_NOOP);}
                            tileIndex++;} else {
                            tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.EGYPT);}}}}}
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

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }
}
