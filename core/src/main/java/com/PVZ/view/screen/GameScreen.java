package com.PVZ.view.screen;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameHud;
import com.PVZ.model.game.LevelStartOverlay;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.PauseMenuOverlay;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.SeedPacketBar;
import com.PVZ.model.game.WinLoseOverlay;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.model.user.UserRegistry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    private PauseMenuOverlay pauseMenuOverlay;
    private LevelStartOverlay levelStartOverlay;
    private WinLoseOverlay winLoseOverlay;
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
    private boolean pluckModeActive = false;
    private boolean plantFoodModeActive = false;
    private ImageButton shovelButton;
    private ImageButton plantFoodButton;

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
        pauseMenuOverlay = new PauseMenuOverlay(this::handleSaveAndExit, this::handleRestart);
        pauseMenuOverlay.setMissionText(resolveMissionText());
        stage.addActor(pauseMenuOverlay);
        levelStartOverlay = new LevelStartOverlay(resolveStageConfig(), () -> {
        });
        stage.addActor(levelStartOverlay);
        winLoseOverlay = new WinLoseOverlay(this::handleSaveAndExit, this::handleRestart);
        stage.addActor(winLoseOverlay);
        stage.addActor(buildPauseButton());
        if (gameEngine instanceof RegularGameEngine) {
            stage.addActor(buildPlantFoodButton());
            stage.addActor(buildShovelButton());
        }
        com.PVZ.view.screen.panels.CheatPanel.attachToggleButton(stage, dto ->
            new com.PVZ.controller.menuControllers.InGameMenuController().handle(dto));

        if (gameEngine.getMap() != null) {
            gameMap = gameEngine.getMap();
        } else {
            gameMap = new Map(480, 1235, 1655, 1170, 5, 9);
            gameEngine.setMap(gameMap);
        }
        shapeDebug = new ShapeRenderer();
        hudFont = FontManager.getInstance().getEnglishMenuFont();
        gameOverFont = FontManager.getInstance().getEnglishMenuFont();

        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            layoutSeedPacketBar(regularGameEngine);
        }

        levelStartOverlay.show();
    }

    private com.PVZ.model.game.chapter.StageConfig resolveStageConfig() {
        try {
            return com.PVZ.model.game.chapter.ChapterLibrary
                .getStageConfig(AppStatus.currentChapterName, AppStatus.currentStageNumber);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveMissionText() {
        return com.PVZ.model.game.chapter.StageRules.summary(resolveStageConfig());
    }

    private boolean isSimulationFrozen() {
        return (pauseMenuOverlay != null && pauseMenuOverlay.isPaused())
            || (levelStartOverlay != null && levelStartOverlay.isShowing())
            || (winLoseOverlay != null && winLoseOverlay.isShowing());
    }

    private Table buildPauseButton() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();
        ImageButton button = null;
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("ingame_pause", ImageButton.ImageButtonStyle.class)) {
                button = new ImageButton(skin, "ingame_pause");
            }
        } catch (Exception ignored) {
        }
        if (button == null) {
            button = new ImageButton(new ImageButton.ImageButtonStyle());
        }
        final ImageButton pauseButton = button;
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pauseMenuOverlay.toggle();
            }
        });
        overlay.add(pauseButton).size(70f, 70f).padTop(20f).padRight(20f);
        return overlay;
    }

    private Table buildPlantFoodButton() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();

        ImageButton button;
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("plantfood", ImageButton.ImageButtonStyle.class)) {
                button = new ImageButton(skin, "plantfood");
            } else {
                button = new ImageButton(new ImageButton.ImageButtonStyle());
            }
        } catch (Exception ex) {
            button = new ImageButton(new ImageButton.ImageButtonStyle());
        }

        plantFoodButton = button;
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!(AppStatus.getGameEngine() instanceof RegularGameEngine engine)) {
                    return;
                }
                if (engine.getPlantFoodManager().getPlantFoodCount() <= 0) {
                    System.out.println("No plant food available.");
                    return;
                }
                plantFoodModeActive = !plantFoodModeActive;
                if (plantFoodModeActive) {
                    pluckModeActive = false;
                    updateShovelButtonState();
                }
                updatePlantFoodButtonState();
            }
        });

        overlay.add(button).size(82f, 82f).padTop(20f).padRight(110f);
        return overlay;
    }

    private void updatePlantFoodButtonState() {
        if (plantFoodButton != null) {
            plantFoodButton.setColor(1f, 1f, 1f, plantFoodModeActive ? 1f : 0.78f);
        }
    }

    private boolean handleLootAtScreenPoint(int screenX, int screenY) {
        if (!(AppStatus.getGameEngine() instanceof RegularGameEngine regularEngine)) {
            return false;
        }
        Map map = regularEngine.getMap();
        if (map == null) {
            return false;
        }

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0f));
        String result = regularEngine.collectLootAtWorldPoint(world.x, world.y);
        if (result != null && !result.isBlank()) {
            System.out.println(result);
            if (result.startsWith("Collected Plant Food!")) {
                plantFoodModeActive = false;
                updatePlantFoodButtonState();
            }
            return true;
        }
        return false;
    }

    private boolean handlePlantFoodAtScreenPoint(int screenX, int screenY) {
        if (!(AppStatus.getGameEngine() instanceof RegularGameEngine regularEngine)) {
            return false;
        }
        Map map = regularEngine.getMap();
        if (map == null) {
            return false;
        }

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0f));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) {
            return false;
        }

        String result = regularEngine.feedPlant(col, row);
        System.out.println(result);
        if (result != null && result.startsWith("Plant fed at")) {
            plantFoodModeActive = false;
            updatePlantFoodButtonState();
        }
        return true;
    }

    private Table buildShovelButton() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();

        ImageButton button;
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("ingame_shovel", ImageButton.ImageButtonStyle.class)) {
                button = new ImageButton(skin, "ingame_shovel");
            } else {
                button = new ImageButton(new ImageButton.ImageButtonStyle());
            }
        } catch (Exception ex) {
            button = new ImageButton(new ImageButton.ImageButtonStyle());
        }

        shovelButton = button;
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pluckModeActive = !pluckModeActive;
                if (pluckModeActive) {
                    plantFoodModeActive = false;
                    updatePlantFoodButtonState();
                }
                updateShovelButtonState();
            }
        });

        overlay.add(button).size(82f, 82f).padTop(108f).padRight(20f);
        return overlay;
    }

    private void updateShovelButtonState() {
        if (shovelButton != null) {
            shovelButton.setColor(1f, 1f, 1f, pluckModeActive ? 1f : 0.78f);
        }
    }

    private boolean handlePluckAtScreenPoint(int screenX, int screenY) {
        if (!(AppStatus.getGameEngine() instanceof RegularGameEngine regularEngine)) {
            return false;
        }
        Map map = regularEngine.getMap();
        if (map == null) {
            return false;
        }

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0f));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) {
            return false;
        }

        String result = regularEngine.pluckPlant(col, row);
        System.out.println(result);
        if (result != null && result.startsWith("Plant plucked from")) {
            pluckModeActive = false;
            updateShovelButtonState();
        }
        return true;
    }

    private void handleSaveAndExit() {
        if (AppStatus.currentUser != null && AppStatus.currentUser.profile != null) {
            UserRegistry.saveUserToDatabase(AppStatus.currentUser.profile.getUsername());
        }
        if (gameEngine instanceof RegularGameEngine) {
            AppStatus.returnToChapterAndLevelSelection(null);
        } else {
            AppStatus.returnToMainMenu(null);
        }
    }

    private void handleRestart() {
        OutputDTOResultHolder result = restartCurrentStage();
        if (result.success) {
            ScreenManager.getInstance().performTransition(() -> new GameScreen(
                mapPath, musicPath, AppStatus.getGameEngine()));
        } else {
            System.err.println("GameScreen: restart failed: " + result.message);
        }
    }

    private static final class OutputDTOResultHolder {
        final boolean success;
        final String message;

        OutputDTOResultHolder(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private OutputDTOResultHolder restartCurrentStage() {
        com.PVZ.view.output.OutputDTO result = new com.PVZ.controller.menuControllers.PlantSelectionMenuController()
            .handle(new com.PVZ.view.input.DTO.PlantSelectionInputDTO(
                com.PVZ.model.enums.commands.PlantSelectionCommand.START_GAME, null));
        return new OutputDTOResultHolder(result.isSuccess(), result.getMessage());
    }

    @Override
    public void show() {
        multiplexer.clear();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isSimulationFrozen()) {
                    return false;
                }
                if (handleLootAtScreenPoint(screenX, screenY)) {
                    return true;
                }
                if (plantFoodModeActive && handlePlantFoodAtScreenPoint(screenX, screenY)) {
                    return true;
                }
                if (pluckModeActive && handlePluckAtScreenPoint(screenX, screenY)) {
                    return true;
                }
                return activeInputProcessor().touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if (isSimulationFrozen()) {
                    return false;
                }
                return activeInputProcessor().mouseMoved(screenX, screenY);
            }
        });
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
        applyCameraShake();

        refreshSeedPacketBar();
        GameEngine activeEngine = AppStatus.getGameEngine();
        if (activeEngine == null) {
            activeEngine = gameEngine;
        }

        GameOverState overState = updateGameOverState(activeEngine);
        drawBackgroundAndEngine(activeEngine, delta);
        drawMapBorders(activeEngine);
        drawDeadline(activeEngine);
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

                if (displayTime >= 1.2f && winLoseOverlay != null && !winLoseOverlay.isShowing()) {
                    winLoseOverlay.showResult(regularGameEngine.isGameOverWin());
                }
                return new GameOverState(true, regularGameEngine.isGameOverWin(), inEndOfGame);
            } else {
                gameOverShown = false;
                gameOverAlpha = 0f;
                if (winLoseOverlay != null && winLoseOverlay.isShowing()) {
                    winLoseOverlay.hide();
                }
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
        Texture activeBackgroundRight = activeEngine.getBackgroundOverrideRight();
        if (activeBackground == null) {
            activeBackground = backgroundTexture;
        }

        // ----- تنظیمات مقیاس و آفست (قابل دریافت از activeEngine یا متغیرهای کلاس) -----
        float scaleX = 1.20f;
        float scaleY = 1.30f;

        float offsetX = -100f;    // جابه‌جایی افقی کل عکس بزرگ (به پیکسل)
        float offsetY = -140f;    // جابه‌جایی عمودی کل عکس بزرگ (به پیکسل)

        gameBatch.begin();

        if (activeBackgroundRight != null && activeBackground != null) {
            // ----- حالت دو تکه (ترکیب دو تصویر به عنوان یک تصویر واحد) -----

            // ۱. محاسبه مقیاس پایه برای فیت شدن عمودی اولیه در VIRTUAL_HEIGHT
            float baseScale = VIRTUAL_HEIGHT / (float) activeBackground.getHeight();

            // ۲. ابعاد نهایی تصویر چپ بعد از اعمال scaleX و scaleY
            float leftW = activeBackground.getWidth() * baseScale * scaleX;
            float leftH = VIRTUAL_HEIGHT * scaleY;

            // ۳. ابعاد نهایی تصویر راست با همان ضریب مقیاس
            float rightW = activeBackgroundRight.getWidth() * baseScale * scaleX;
            float rightH = VIRTUAL_HEIGHT * scaleY;

            // ۴. نقطه‌ی مبدا پایین-چپ عکس یکپارچه (با احتساب آفست‌ها)
            float startX = 0f + offsetX;
            float startY = 0f + offsetY;

            // ۵. رسم تصویر چپ از نقطه مبدا
            gameBatch.draw(activeBackground, startX, startY, leftW, leftH);

            // ۶. رسم تصویر راست دقیقاً چسبیده به انتهای تصویر چپ
            gameBatch.draw(activeBackgroundRight, startX + leftW, startY, rightW, rightH);

        } else if (activeBackground != null) {
            // ----- حالت تک‌تصویری (مراحل اصلی) -----
            float finalW = VIRTUAL_WIDTH * scaleX;
            float finalH = VIRTUAL_HEIGHT * scaleY;

            float startX = 0f + offsetX;
            float startY = 0f + offsetY;

            gameBatch.draw(activeBackground, startX, startY, finalW, finalH);
        }

        gameBatch.end();

        float renderDelta = isSimulationFrozen() ? 0f : Math.min(delta, 1 / 30f);
        activeEngine.render(renderDelta, gameBatch);
    }

    private void drawMapBorders(GameEngine activeEngine) {
        if (!AppStatus.tileDebugEnabled) {
            return;
        }

        Map activeMap = activeEngine.getMap();
        if (activeMap == null) {
            activeMap = gameMap;
        }
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Line);
        activeMap.renderBorders(shapeDebug);
        shapeDebug.end();
    }

    private void drawDeadline(GameEngine activeEngine) {
        if (!(activeEngine instanceof RegularGameEngine regularEngine)) {
            return;
        }
        if (!(regularEngine.getSpecialLevel()
            instanceof com.PVZ.model.game.chapter.sepecialLevel.DeadLineLevel deadline)) {
            return;
        }
        Map activeMap = regularEngine.getMap();
        if (activeMap == null) {
            return;
        }
        // Each lane gets its own random deadline column; draw one segment per
        // lane exactly over that lane's tiles (map Y grows downward from startY).
        float tileHeight = activeMap.getTileHeight();
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Filled);
        shapeDebug.setColor(com.badlogic.gdx.graphics.Color.RED);
        for (int row = 0; row < activeMap.getRows(); row++) {
            float x = (float) deadline.getLineXForRow(activeMap, row);
            float y = activeMap.getStartY() - (row + 1) * tileHeight;
            shapeDebug.rect(x - 3f, y + 3f, 6f, tileHeight - 6f);
        }
        shapeDebug.setColor(com.badlogic.gdx.graphics.Color.WHITE);
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
            case TOMBSTONE:
                return "T";
            case WATER:
                return "~";
            case TIDE:
                return "^";
            case ICE:
                return "*";
            case SLIPPERY_UP:
                return "U";
            case SLIPPERY_DOWN:
                return "D";
            case NECROMANCY:
                return "N";
            case LOW_COAST:
                return "L";
            case CRATER:
                return "C";
            default:
                return ".";
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
        pluckModeActive = false;
        shovelButton = null;
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        System.out.println("[GameScreen] PVZ resources disposed cleanly.");
    }

    // camera shake part
    // ===================== شیک دوربین =====================
    private static final float SHAKE_DURATION = 0.4f;      // مدت لرزش (ثانیه)
    private static final float SHAKE_MAGNITUDE = 14f;      // شدت لرزش (پیکسل)
    private float shakeTimer = 0f;
    private final Random random = new Random();
    /**
     * صدا زدنش یک لرزش کوتاه به دوربین می‌دهد.
     * مثال استفاده: وقتی باس ضربه می‌زند -> activeCameraShake();
     */
    public void activeCameraShake() {
        shakeTimer = SHAKE_DURATION;
    }
    /**
     * اعمال لرزش به دوربین. این متد باید در ابتدای renderScreen صدا زده شود.
     */
    private void applyCameraShake() {
        float shakeOffsetX = 0f;
        float shakeOffsetY = 0f;

        if (shakeTimer > 0f) {
            shakeTimer -= Gdx.graphics.getDeltaTime();
            float fade = Math.max(shakeTimer, 0f) / SHAKE_DURATION;
            shakeOffsetX = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
            shakeOffsetY = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
        }

        // موقعیت پایه‌ی دوربین در این بازی ثابت است (وسط صفحه‌ی مجازی)
        camera.position.set(
            VIRTUAL_WIDTH / 2f + shakeOffsetX,
            VIRTUAL_HEIGHT / 2f + shakeOffsetY,
            0f
        );
        camera.update();
    }
}
