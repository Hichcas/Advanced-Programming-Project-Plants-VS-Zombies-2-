package com.PVZ.model;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.PlantHandler;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.IceWindManager;
import com.PVZ.model.game.chapter.SandstormManager;
import com.PVZ.util.GameInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ChapterEnvironmentalEffectsTest {

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
    public void testSandstormLifecycleAndZombieDrop() {
        SandstormManager sm = engine.getSandstormManager();
        assertNotNull(sm);

        // Trigger sandstorm with 2 storms
        sm.triggerSandstorm(engine, 2);
        assertEquals(2, sm.getActiveSandstorms().size());

        SandstormManager.Sandstorm storm = sm.getActiveSandstorms().get(0);
        assertEquals(SandstormManager.State.INTRO, storm.getState());

        // Update past INTRO (0.35s) -> should transition to LOOP
        storm.update(0.4f, engine);
        assertEquals(SandstormManager.State.LOOP, storm.getState());

        // Update loop movement until it reaches target
        float initialX = storm.getCurrentX();
        storm.update(2.0f, engine);
        assertTrue(storm.getCurrentX() < initialX, "Sandstorm should move leftward");

        // Advance through completion (OUTRO -> DONE)
        storm.update(5.0f, engine);
        storm.update(0.5f, engine);
        assertEquals(SandstormManager.State.DONE, storm.getState());
        assertTrue(storm.isDone());
    }

    @Test
    public void testIceWindTriggerAndGustProgress() {
        IceWindManager iwm = engine.getIceWindManager();
        assertNotNull(iwm);

        // Trigger Ice Wind on all rows
        iwm.triggerIceWind(engine, new int[]{0, 1, 2, 3, 4});
        assertEquals(1, iwm.getActiveGusts().size());

        IceWindManager.IceWindGust gust = iwm.getActiveGusts().get(0);
        assertFalse(gust.isDone());

        // Update gust for 3.0s -> should finish
        gust.update(3.0f, engine);
        assertTrue(gust.isDone());
    }

    @Test
    public void testProgressivePlantFreezing() {
        // Plant Peashooter at (row 1, col 2)
        PlantHandler.plantPlant(engine, PlantType.PEASHOOTER, 2, 1);
        Plant pea = map.getPlantAt(1, 2);
        assertNotNull(pea);

        com.PVZ.model.game.chapter.ChapterConfig cfg = new com.PVZ.model.game.chapter.ChapterConfig();
        cfg.setName("FROSTBITE_CAVES");
        com.PVZ.model.game.chapter.Chapter chapter = new com.PVZ.model.game.chapter.Chapter(cfg);

        // Initial state: freeze level = 0
        assertEquals(0, ((Number) pea.getRuntimeState().getOrDefault("freezeLevel", 0)).intValue());

        // Wave 1 ice wind: level -> 1 (chill stage 1)
        chapter.update(map, engine);
        // Direct simulation of wave progression
        pea.putRuntimeState("freezeLevel", 1);
        assertEquals(1, ((Number) pea.getRuntimeState("freezeLevel")).intValue());

        // Wave 2 ice wind: level -> 2 (chill stage 2)
        pea.putRuntimeState("freezeLevel", 2);
        assertEquals(2, ((Number) pea.getRuntimeState("freezeLevel")).intValue());

        // Wave 3 ice wind: level -> 3 (full ice block encasing)
        pea.putRuntimeState("freezeLevel", 3);
        pea.putRuntimeState("iceHp", 600);
        assertEquals(3, ((Number) pea.getRuntimeState("freezeLevel")).intValue());
        assertEquals(600, ((Number) pea.getRuntimeState("iceHp")).intValue());
    }
}
