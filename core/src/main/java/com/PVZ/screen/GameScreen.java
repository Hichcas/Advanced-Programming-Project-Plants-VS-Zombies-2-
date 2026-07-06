package com.PVZ.screen;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameHud;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.SeedPacketBar;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.manager.FontManager;
import com.PVZ.screen.manager.MusicManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    private final GameEngine gameEngine;
    private final String mapPath;
    private final String musicPath;
    private Map gameMap;
    private ShapeRenderer shapeDebug;
    private final BitmapFont hudFont;

    // تکسچر پس‌زمینه
    private Texture backgroundTexture;

    public GameScreen(String mapPath, String musicPath, GameEngine gameEngine) {
        super();
        this.gameBatch = new SpriteBatch();
        this.mapPath = mapPath;
        this.musicPath = musicPath;

        backgroundTexture = new Texture(mapPath);

        MusicManager.getInstance().playMusic(musicPath);
        this.gameEngine = gameEngine;
        AppStatus.setGameEngine(gameEngine);

        gameHud = new GameHud();
        stage.addActor(gameHud);

        gameMap = new Map(550, 1240, 1600, 1170, 5, 9);
        shapeDebug = new ShapeRenderer();
        hudFont = FontManager.getInstance().getEnglishMenuFont();

        gameEngine.setMap(gameMap);

        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            List<PlantType> loadout = new ArrayList<>(AppStatus.selectedPlants);
            regularGameEngine.getSeedPacketBar().layout(loadout, 40f, VIRTUAL_HEIGHT - 150f);
        }
    }

    @Override
    public void show() {
        multiplexer.clear();
        multiplexer.addProcessor(gameEngine.inputProcessor);
        multiplexer.addProcessor(stage);
        super.show();

        refreshSeedPacketBar();
    }

    private void refreshSeedPacketBar() {
        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            List<PlantType> loadout = new ArrayList<>(AppStatus.selectedPlants);
            regularGameEngine.getSeedPacketBar().layout(loadout, 40f, VIRTUAL_HEIGHT - 150f);
        }
    }

    @Override
    protected void renderScreen(float delta) {
        // نوار بذرها باید از آخرین loadout کاربر refresh شود
        refreshSeedPacketBar();

        // ست کردن پروجکشن برای رندر
        gameBatch.setProjectionMatrix(camera.combined);

        // رسم پس‌زمینه
        gameBatch.begin();
        gameBatch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH + 500, VIRTUAL_HEIGHT);
        gameBatch.end();

        // به‌روزرسانی منطق بازی
        gameEngine.render(Math.min(delta, 1 / 30f), gameBatch);

        // مرزهای گرید
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Line);
        gameMap.renderBorders(shapeDebug);
        shapeDebug.end();

        // نوار بذرها
        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            SeedPacketBar seedBar = regularGameEngine.getSeedPacketBar();

            shapeDebug.begin(ShapeRenderer.ShapeType.Filled);
            seedBar.drawBackgrounds(shapeDebug, regularGameEngine, regularGameEngine.getSelectedPlantType());
            shapeDebug.end();

            gameBatch.begin();
            seedBar.drawIconsAndLabels(gameBatch, hudFont);
            gameBatch.end();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeDebug != null) {
            shapeDebug.dispose();
        }
        if (gameBatch != null) {
            gameBatch.dispose();
        }
        if (gameEngine != null) {
            gameEngine.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        System.out.println("[GameScreen] PVZ resources disposed cleanly.");
    }

}
