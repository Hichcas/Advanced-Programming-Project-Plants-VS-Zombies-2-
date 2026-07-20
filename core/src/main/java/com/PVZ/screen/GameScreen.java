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

        String bgInternal = mapPath;
        if (com.badlogic.gdx.Gdx.files.internal(bgInternal).exists()) {
            backgroundTexture = new Texture(com.badlogic.gdx.Gdx.files.internal(bgInternal));
        } else {
            System.out.println("GameScreen: background not found: " + bgInternal);
            backgroundTexture = null;
        }

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
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return activeInputProcessor().touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return activeInputProcessor().mouseMoved(screenX, screenY);
            }
        });
        multiplexer.addProcessor(stage);
        super.show();

        refreshSeedPacketBar();
    }

    private com.badlogic.gdx.InputProcessor activeInputProcessor() {
        GameEngine activeEngine = AppStatus.getGameEngine();
        return (activeEngine != null ? activeEngine : gameEngine).inputProcessor;
    }

    private void refreshSeedPacketBar() {
        GameEngine activeEngine = AppStatus.getGameEngine();
        if (activeEngine instanceof RegularGameEngine regularGameEngine) {
            List<PlantType> loadout = new ArrayList<>(AppStatus.selectedPlants);
            regularGameEngine.getSeedPacketBar().layout(loadout, 40f, VIRTUAL_HEIGHT - 150f);
        }
    }

    @Override
    protected void renderScreen(float delta) {
        // Refresh seed packet bar from latest loadout
        refreshSeedPacketBar();

        // Use the engine from AppStatus (may have been replaced by startGame)
        GameEngine activeEngine = AppStatus.getGameEngine();
        if (activeEngine == null) {
            activeEngine = gameEngine;
        }

        // Set projection for rendering
        gameBatch.setProjectionMatrix(camera.combined);

        // Draw background (minigames may override this, e.g. Vasebreaker's own backdrop)
        Texture activeBackground = activeEngine.getBackgroundOverride();
        if (activeBackground == null) {
            activeBackground = backgroundTexture;
        }
        gameBatch.begin();
        if (activeBackground != null) {
            gameBatch.draw(activeBackground, 0, 0, VIRTUAL_WIDTH + 500, VIRTUAL_HEIGHT);
        }
        gameBatch.end();

        // Update game logic
        activeEngine.render(Math.min(delta, 1 / 30f), gameBatch);

        // Grid borders (use the active engine's own map, in case it differs from the default one)
        Map activeMap = activeEngine.getMap();
        if (activeMap == null) {
            activeMap = gameMap;
        }
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Line);
        activeMap.renderBorders(shapeDebug);
        shapeDebug.end();

        // Seed packet bar
        if (activeEngine instanceof RegularGameEngine regularGameEngine) {
            SeedPacketBar seedBar = regularGameEngine.getSeedPacketBar();

            shapeDebug.begin(ShapeRenderer.ShapeType.Filled);
            seedBar.drawBackgrounds(shapeDebug, regularGameEngine, regularGameEngine.getSelectedPlantType());
            shapeDebug.end();

            gameBatch.begin();
            seedBar.drawIconsAndLabels(gameBatch, hudFont);
            gameBatch.end();
        }

        // Tile debug overlay: draw the default-mechanic label on each special tile.
        if (com.PVZ.model.status.AppStatus.tileDebugEnabled && activeMap != null) {
            gameBatch.begin();
            for (int r = 0; r < activeMap.getRows(); r++) {
                for (int c = 0; c < activeMap.getCols(); c++) {
                    com.PVZ.model.entity.Tile tile = activeMap.getTile(r, c);
                    if (tile == null || tile.getType() == com.PVZ.model.enums.TileType.NORMAL) {
                        continue;
                    }
                    String label = tileDebugLabel(tile.getType());
                    float x = tile.getX() + tile.getWidth() * 0.5f - 18;
                    float y = tile.getY() + tile.getHeight() * 0.5f + 8;
                    hudFont.setColor(1, 1, 0, 1);
                    hudFont.draw(gameBatch, label, x, y);
                    hudFont.setColor(1, 1, 1, 1);
                }
            }
            gameBatch.end();
        }
    }

    private static String tileDebugLabel(com.PVZ.model.enums.TileType type) {
        if (type == null) return "";
        switch (type) {
            case TOMBSTONE: return "TOMB";
            case WATER: return "WATER";
            case TIDE: return "TIDE";
            case ICE: return "ICE";
            case SLIPPERY_UP: return "SLIP_U";
            case SLIPPERY_DOWN: return "SLIP_D";
            case NECROMANCY: return "NECRO";
            case LOW_COAST: return "LOWC";
            case CRATER: return "CRATER";
            default: return "";
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
