package com.PVZ.screen;

import com.PVZ.model.enums.MenuType;
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
    private final BitmapFont gameOverFont;
    private Texture backgroundTexture;
    private float gameOverAlpha = 0f;
    private boolean gameOverShown = false;

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
        gameOverFont = FontManager.getInstance().getEnglishMenuFont();

        gameEngine.setMap(gameMap);

        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            layoutSeedPacketBar(regularGameEngine);
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
            layoutSeedPacketBar(regularGameEngine);
        }
    }

    private void layoutSeedPacketBar(RegularGameEngine regularGameEngine) {
        List<PlantType> loadout = seedBarLoadout(regularGameEngine);
        if (regularGameEngine.isConveyorBeltMode()) {
            regularGameEngine.getSeedPacketBar().layout(loadout, 30f, VIRTUAL_HEIGHT - 260f, false);
        } else if (regularGameEngine.isLockedPlantsMode()) {
            regularGameEngine.getSeedPacketBar().layoutVertical(loadout, 30f, VIRTUAL_HEIGHT - 150f);
        } else {
            regularGameEngine.getSeedPacketBar().layout(loadout, 40f, VIRTUAL_HEIGHT - 150f);
        }
    }

    private List<PlantType> seedBarLoadout(RegularGameEngine regularGameEngine) {
        if (regularGameEngine.isConveyorBeltMode()) {
            return new ArrayList<>(regularGameEngine.getConveyorBeltQueue());
        }
        return new ArrayList<>(AppStatus.SELECTED_PLANTS);
    }

    @Override
    protected void renderScreen(float delta) {
        refreshSeedPacketBar();
        GameEngine activeEngine = AppStatus.getGameEngine();
        if (activeEngine == null) {
            activeEngine = gameEngine;
        }

        GameOverState overState = updateGameOverState(activeEngine);
        drawBackgroundAndEngine(activeEngine, delta);
        drawMapBorders(activeEngine);
        drawSeedPacketBar(activeEngine);
        drawGameOverOverlay(overState);
        drawTileDebug(activeEngine);
    }

    private GameOverState updateGameOverState(GameEngine activeEngine) {
        if (activeEngine instanceof RegularGameEngine regularGameEngine) {
            if (regularGameEngine.isGameOverTriggered()) {
                if (!gameOverShown) {
                    gameOverShown = true;
                    gameOverAlpha = 0f;
                }
                boolean inEndOfGame = AppStatus.currentMenuType == MenuType.END_OF_GAME;
                float displayTime = regularGameEngine.getGameOverTimer();
                if (displayTime < 1.0f) {
                    gameOverAlpha = Math.min(1.0f, displayTime);
                } else {
                    gameOverAlpha = 1.0f;
                }
                return new GameOverState(true, regularGameEngine.isGameOverWin(), inEndOfGame);
            } else {
                gameOverShown = false;
                gameOverAlpha = 0f;
                return new GameOverState(false, false, false);
            }
        }
        return new GameOverState(false, false, false);
    }

    private static class GameOverState {
        final boolean isGameOver;
        final boolean isWin;
        final boolean isEndOfGame;

        GameOverState(boolean isGameOver, boolean isWin, boolean isEndOfGame) {
            this.isGameOver = isGameOver;
            this.isWin = isWin;
            this.isEndOfGame = isEndOfGame;
        }
    }

    private void drawBackgroundAndEngine(GameEngine activeEngine, float delta) {
        gameBatch.setProjectionMatrix(camera.combined);
        Texture activeBackground = activeEngine.getBackgroundOverride();
        if (activeBackground == null) {
            activeBackground = backgroundTexture;
        }
        gameBatch.begin();
        if (activeBackground != null) {
            gameBatch.draw(activeBackground, 0, 0, VIRTUAL_WIDTH + 500, VIRTUAL_HEIGHT);
        }
        gameBatch.end();

        float renderDelta = Math.min(delta, 1 / 30f);
        activeEngine.render(renderDelta, gameBatch);
    }

    private void drawMapBorders(GameEngine activeEngine) {
        Map activeMap = activeEngine.getMap();
        if (activeMap == null) {
            activeMap = gameMap;
        }
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Line);
        activeMap.renderBorders(shapeDebug);
        shapeDebug.end();
    }

    private void drawSeedPacketBar(GameEngine activeEngine) {
        if (activeEngine instanceof RegularGameEngine regularEngine) {
            drawSeedBar(regularEngine, regularEngine);
        } else if (activeEngine instanceof com.PVZ.model.game.ZombotanyGameEngine zombotanyEngine) {
            drawSeedBar(zombotanyEngine, zombotanyEngine);
        }
    }

    private void drawSeedBar(com.PVZ.model.game.SeedBarEngine seedEngine,
                             GameEngine engineWithSeedBar) {
        SeedPacketBar seedBar = null;
        PlantType selected = null;
        if (engineWithSeedBar instanceof RegularGameEngine reg) {
            seedBar = reg.getSeedPacketBar();
            selected = reg.getSelectedPlantType();
        } else if (engineWithSeedBar instanceof com.PVZ.model.game.ZombotanyGameEngine z) {
            seedBar = z.getSeedPacketBar();
            selected = z.getSelectedPlantType();
        }
        if (seedBar == null) {
            return;
        }

        shapeDebug.begin(ShapeRenderer.ShapeType.Filled);
        seedBar.drawBackgrounds(shapeDebug, seedEngine, selected);
        shapeDebug.end();

        gameBatch.begin();
        seedBar.drawIconsAndLabels(gameBatch, hudFont, seedEngine);
        gameBatch.end();
    }

    private void drawGameOverOverlay(GameOverState state) {
        if (!state.isGameOver || gameOverAlpha <= 0) {
            return;
        }
        gameBatch.begin();
        gameOverFont.setColor(1, 1, 1, gameOverAlpha);
        String message = state.isWin ? "LEVEL COMPLETE!" : "GAME OVER";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
            new com.badlogic.gdx.graphics.g2d.GlyphLayout(gameOverFont, message);
        float x = VIRTUAL_WIDTH / 2f - layout.width / 2f;
        float y = VIRTUAL_HEIGHT / 2f + layout.height / 2f;
        gameOverFont.draw(gameBatch, message, x, y);
        if (state.isEndOfGame) {
            String hint = "Type 'show stats' or 'menu exit'";
            gameOverFont.setColor(1, 1, 0, gameOverAlpha);
            com.badlogic.gdx.graphics.g2d.GlyphLayout hintLayout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(gameOverFont, hint);
            float hintX = VIRTUAL_WIDTH / 2f - hintLayout.width / 2f;
            float hintY = y - 60f;
            gameOverFont.draw(gameBatch, hint, hintX, hintY);
        }
        gameOverFont.setColor(1, 1, 1, 1);
        gameBatch.end();
    }

    private void drawTileDebug(GameEngine activeEngine) {
        Map activeMap = activeEngine.getMap();
        if (activeMap == null) {
            activeMap = gameMap;
        }
        if (!AppStatus.tileDebugEnabled || activeMap == null) {
            return;
        }
        gameBatch.begin();
        for (int r = 0; r < activeMap.getRows(); r++) {
            for (int c = 0; c < activeMap.getCols(); c++) {
                com.PVZ.model.entity.Tile tile = activeMap.getTile(r, c);
                if (tile == null || tile.getType() == com.PVZ.model.enums.TileType.NORMAL) {
                    continue;
                }
                String label = tileDebugLabel(tile.getType());
                float cx = tile.getX() + tile.getWidth() * 0.5f;
                float cy = tile.getY() + tile.getHeight() * 0.5f;
                float x = cx - label.length() * 5f;
                float y = cy + hudFont.getCapHeight() * 0.5f;
                hudFont.setColor(0, 1, 0, 1);
                hudFont.draw(gameBatch, label, x, y);
            }
        }
        hudFont.setColor(1, 1, 1, 1);
        gameBatch.end();
    }

    private static String tileDebugLabel(com.PVZ.model.enums.TileType type) {
        if (type == null) return ".";
        switch (type) {
            case TOMBSTONE: return "T";
            case WATER: return "~";
            case TIDE: return "^";
            case ICE: return "*";
            case SLIPPERY_UP: return "U";
            case SLIPPERY_DOWN: return "D";
            case NECROMANCY: return "N";
            case LOW_COAST: return "L";
            case CRATER: return "C";
            default: return ".";
        }
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }
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
