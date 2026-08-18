package com.PVZ.model;

import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.enums.GraveVariant;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.PlantHandler;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.Chapter;
import com.PVZ.model.game.chapter.ChapterConfig;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.util.GameInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TombstoneLogicTest {

    private Map map;
    private RegularGameEngine engine;
    private GameStatus gameStatus;

    @BeforeAll
    public static void initAll() throws Exception {
        GameInitialization.initialize();
    }

    @BeforeEach
    public void setup() {
        gameStatus = new GameStatus();
        gameStatus.setSunflower(500);
        engine = new RegularGameEngine(gameStatus, new ArrayList<>());
        map = new Map(0f, 500f, 900f, 500f, 5, 9);
        engine.setMap(map);
    }

    @Test
    public void testGraveVariantClipMapping() {
        // 5 damage states matching user specification:
        // undamaged (> 0.80)
        assertEquals("undamaged", GraveVariant.getClipForHpRatio(1.0f));
        assertEquals("undamaged", GraveVariant.getClipForHpRatio(0.85f));

        // damage1 (0.60 to 0.80)
        assertEquals("damage1", GraveVariant.getClipForHpRatio(0.80f));
        assertEquals("damage1", GraveVariant.getClipForHpRatio(0.65f));

        // damage2 (0.40 to 0.60)
        assertEquals("damage2", GraveVariant.getClipForHpRatio(0.60f));
        assertEquals("damage2", GraveVariant.getClipForHpRatio(0.45f));

        // damage3 (0.20 to 0.40)
        assertEquals("damage3", GraveVariant.getClipForHpRatio(0.40f));
        assertEquals("damage3", GraveVariant.getClipForHpRatio(0.25f));

        // damage4 (<= 0.20)
        assertEquals("damage4", GraveVariant.getClipForHpRatio(0.20f));
        assertEquals("damage4", GraveVariant.getClipForHpRatio(0.05f));
    }

    @Test
    public void testChapterSetupTombstones() {
        ChapterConfig egyptConfig = new ChapterConfig();
        egyptConfig.setName("ANCIENT_EGYPT");
        Chapter egyptChapter = new Chapter(egyptConfig);

        StageConfig stage = new StageConfig();
        List<StageConfig.TombstoneEntry> tombstones = new ArrayList<>();
        StageConfig.TombstoneEntry t1 = new StageConfig.TombstoneEntry();
        t1.setRow(1);
        t1.setCol(3);
        t1.setHp(700);
        tombstones.add(t1);
        stage.setTombstones(tombstones);

        egyptChapter.applySetup(map, stage);

        Tile tile = map.getTile(1, 3);
        assertNotNull(tile);
        assertEquals(TileType.TOMBSTONE, tile.getType());
        assertEquals(700, tile.getHp());
        assertEquals(GraveVariant.EGYPT, tile.getGraveVariant());
    }

    @Test
    public void testPlantingRestrictionsOnGraves() {
        Tile tile = map.getTile(2, 2);
        tile.setType(TileType.TOMBSTONE);
        tile.setHp(700);
        tile.setGraveVariant(GraveVariant.EGYPT);

        // 1. Ordinary plant (Peashooter) cannot be planted on a tombstone
        String peaResult = PlantHandler.plantPlant(engine, PlantType.PEASHOOTER, 2, 2);
        assertEquals("Cannot plant on a grave.", peaResult);
        assertNull(tile.getPlant());

        // 2. Grave Buster cannot be planted on normal tile
        String gbNormalResult = PlantHandler.plantPlant(engine, PlantType.GRAVE_BUSTER, 0, 0);
        assertEquals("Grave Buster can only be planted on graves.", gbNormalResult);

        // 3. Grave Buster CAN be planted on a tombstone tile
        String gbResult = PlantHandler.plantPlant(engine, PlantType.GRAVE_BUSTER, 2, 2);
        assertTrue(gbResult.contains("Grave Buster") || gbResult.contains("Planted"));
        assertNotNull(tile.getPlant());
    }

    @Test
    public void testStraightProjectileBlockedAndDamagesGrave() {
        Tile tile = map.getTile(0, 3);
        tile.setType(TileType.TOMBSTONE);
        tile.setHp(700);
        tile.setMaxHp(700);
        tile.setGraveVariant(GraveVariant.EGYPT);

        float worldX = tile.getX() + 20f;
        float worldY = tile.getY() + 20f;
        Projectile pea = new Projectile();
        pea.setType(ProjectileType.PEA);
        pea.setDamage(20);
        pea.initWorldPosition(worldX, worldY, 300f);

        BattleController bc = engine.getBattleController();
        boolean collided = bc.handleTileCollisionForTest(pea);
        assertTrue(collided);
        assertEquals(680, tile.getHp());
    }

    @Test
    public void testLobbedProjectileBypassesGraveTileCollision() {
        Tile tile = map.getTile(0, 3);
        tile.setType(TileType.TOMBSTONE);
        tile.setHp(700);
        tile.setGraveVariant(GraveVariant.EGYPT);

        float worldX = tile.getX() + 20f;
        float worldY = tile.getY() + 20f;
        Projectile lob = new Projectile();
        lob.setType(ProjectileType.LOB);
        lob.setDamage(40);
        lob.initArcPosition(worldX, worldY, 300f);

        BattleController bc = engine.getBattleController();
        boolean collided = bc.handleTileCollisionForTest(lob);
        assertFalse(collided, "Lobbed projectiles should not collide with tombstone tiles");
        assertEquals(700, tile.getHp());
    }

    @Test
    public void testGraveBusterEatingCycleDestroysTombstone() {
        Tile tile = map.getTile(1, 1);
        tile.setType(TileType.TOMBSTONE);
        tile.setHp(700);
        tile.setMaxHp(700);
        tile.setGraveVariant(GraveVariant.EGYPT);

        PlantHandler.plantPlant(engine, PlantType.GRAVE_BUSTER, 1, 1);
        assertNotNull(tile.getPlant());

        // Update for 2 seconds -> should damage grave progressively
        tile.getPlant().update(engine, 2.0);
        assertTrue(tile.getHp() < 700);
        assertTrue(tile.getHp() > 0);
        assertEquals(TileType.TOMBSTONE, tile.getType());

        // Update for another 3 seconds (total 5.0s > 4.5s) -> grave should be destroyed
        tile.getPlant().update(engine, 3.0);
        assertEquals(TileType.NORMAL, tile.getType());
        assertEquals(0, tile.getHp());
        assertNull(tile.getPlant());
    }

    @Test
    public void testDarkAgesSunGraveDropReward() {
        Tile tile = map.getTile(0, 2);
        tile.setType(TileType.TOMBSTONE);
        tile.setHp(20);
        tile.setMaxHp(700);
        tile.setGraveVariant(GraveVariant.DARK_SUN);

        int sunBefore = engine.getSunCount();

        // Projectile deals lethal damage
        float worldX = tile.getX() + 20f;
        float worldY = tile.getY() + 20f;
        Projectile pea = new Projectile();
        pea.setType(ProjectileType.PEA);
        pea.setDamage(50);
        pea.initWorldPosition(worldX, worldY, 300f);

        BattleController bc = engine.getBattleController();
        bc.handleTileCollisionForTest(pea);

        assertEquals(TileType.NORMAL, tile.getType());
        assertEquals(0, tile.getHp());
        assertEquals(sunBefore + 100, engine.getSunCount(), "Sun grave should give 100 sun when destroyed");
    }
}
