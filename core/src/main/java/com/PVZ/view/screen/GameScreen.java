package com.PVZ.view.screen;

import com.PVZ.view.renderer.WorldBackgroundRenderer;
import com.PVZ.view.renderer.EntityRenderer;
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
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
    private Label timedWarLabel;
    private Label plantFoodCountLabel;
    private long plantFoodFlashUntil = 0L;

    private final List<String> previewZombies = new ArrayList<>();
    private float introTimer = 0f;
    private static final float INTRO_PAN_RIGHT_DURATION = 0.8f;
    private static final float INTRO_HOLD_RIGHT_DURATION = 1.4f;
    private static final float INTRO_PAN_LEFT_DURATION = 1.5f;
    private static final float MAX_PAN_OFFSET = 950f;
    private static final float MAX_ZOOM_OUT = 1.15f;

    private float cameraIntroOffsetX = 0f;
    private float cameraIntroZoom = 1.0f;
    private boolean introStarted = false;
    private boolean introFinished = false;

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

        initPreviewZombies();

        gameHud = new GameHud();
        stage.addActor(gameHud);
        pauseMenuOverlay = new PauseMenuOverlay(this::handleSaveAndExit, this::handleRestart);
        pauseMenuOverlay.setMissionText(resolveMissionText());
        stage.addActor(pauseMenuOverlay);
        levelStartOverlay = new LevelStartOverlay(resolveStageConfig(), () -> {
            introStarted = true;
            introTimer = 0f;
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

        if (gameEngine instanceof RegularGameEngine regularGameEngine
            && regularGameEngine.getSpecialLevel()
            instanceof com.PVZ.model.game.chapter.sepecialLevel.TimedWarLevel) {
            Label.LabelStyle timerStyle = new Label.LabelStyle(hudFont, Color.WHITE);
            try {
                Skin skin = PvzSkin.get();
                if (skin != null) {
                    timerStyle = new Label.LabelStyle();
                    timerStyle.font = skin.getFont("FBUSV8C5EI_1_outline");
                    timerStyle.fontColor = Color.WHITE;
                    timerStyle.background = skin.getDrawable("image_ui_powerups_powerup_cost_10");
                }
            } catch (Exception ex) {
                System.err.println("GameScreen: PvzSkin timer style unavailable, using default font.");
            }
            timedWarLabel = new Label("", timerStyle);
            timedWarLabel.setSize(1100f, 90f);
            timedWarLabel.setPosition(VIRTUAL_WIDTH / 2f - 550f, 26f);
            timedWarLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            stage.addActor(timedWarLabel);
        }

        if (gameEngine instanceof RegularGameEngine regularGameEngine) {
            layoutSeedPacketBar(regularGameEngine);
        }

        levelStartOverlay.show();
    }

    private void initPreviewZombies() {
        com.PVZ.model.game.chapter.StageConfig sc = resolveStageConfig();
        if (sc != null && sc.getWaves() != null) {
            java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
            for (com.PVZ.model.game.chapter.StageConfig.WaveEntry we : sc.getWaves()) {
                if (we.getEntries() != null) {
                    for (com.PVZ.model.game.chapter.StageConfig.ZombieSpawn zs : we.getEntries()) {
                        if (zs.getZombie() != null && !zs.getZombie().isBlank()) {
                            seen.add(zs.getZombie().trim());
                        }
                    }
                }
            }
            previewZombies.addAll(seen);
        }
    }

    private void updateIntro(float delta) {
        if (!introStarted) {
            cameraIntroOffsetX = 0f;
            cameraIntroZoom = 1.0f;
            return;
        }
        if (!introFinished) {
            introTimer += delta;
            if (introTimer < INTRO_PAN_RIGHT_DURATION) {
                // 1. Pan right & zoom out slightly
                float t = introTimer / INTRO_PAN_RIGHT_DURATION;
                float smoothT = t * t * (3f - 2f * t);
                cameraIntroOffsetX = MAX_PAN_OFFSET * smoothT;
                cameraIntroZoom = 1.0f + (MAX_ZOOM_OUT - 1.0f) * smoothT;
            } else if (introTimer < INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION) {
                // 2. Hold at right showing incoming zombies
                cameraIntroOffsetX = MAX_PAN_OFFSET;
                cameraIntroZoom = MAX_ZOOM_OUT;
            } else if (introTimer < INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION + INTRO_PAN_LEFT_DURATION) {
                // 3. Pan left back to lawn & zoom in
                float t = (introTimer - (INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION)) / INTRO_PAN_LEFT_DURATION;
                float smoothT = t * t * (3f - 2f * t);
                cameraIntroOffsetX = MAX_PAN_OFFSET * (1f - smoothT);
                cameraIntroZoom = MAX_ZOOM_OUT - (MAX_ZOOM_OUT - 1.0f) * smoothT;
            } else {
                // 4. Lock onto lawn, start gameplay
                cameraIntroOffsetX = 0f;
                cameraIntroZoom = 1.0f;
                introFinished = true;
            }
        }
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
        return (levelStartOverlay != null && levelStartOverlay.isShowing())
            || (introStarted && !introFinished)
            || (pauseMenuOverlay != null && pauseMenuOverlay.isPaused())
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
        overlay.setTouchable(Touchable.childrenOnly);

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
                togglePlantFoodMode();
            }
        });

        // Live counter badge in the corner of the button.
        plantFoodCountLabel = new Label("0",
            new Label.LabelStyle(FontManager.getInstance().getEnglishMenuFont(), Color.YELLOW));
        plantFoodCountLabel.setFontScale(0.9f);

        Stack stack = new Stack();
        stack.add(button);
        Table badgeLayer = new Table();
        badgeLayer.add(plantFoodCountLabel).expand().bottom().right().padBottom(6f).padRight(8f);
        stack.add(badgeLayer);

        overlay.add(stack).size(82f, 82f).padTop(20f).padRight(110f);
        return overlay;
    }

    private void togglePlantFoodMode() {
        if (!(AppStatus.getGameEngine() instanceof RegularGameEngine engine)) {
            return;
        }
        if (engine.getPlantFoodManager().getPlantFoodCount() <= 0) {
            // Out of food: flash the button red instead of failing silently.
            plantFoodFlashUntil = System.currentTimeMillis() + 450;
            if (plantFoodButton != null) {
                plantFoodButton.clearActions();
                plantFoodButton.addAction(Actions.sequence(
                    Actions.color(Color.RED, 0.08f),
                    Actions.color(new Color(1f, 1f, 1f, 0.4f), 0.3f)));
            }
            System.out.println("No plant food available.");
            return;
        }
        plantFoodModeActive = !plantFoodModeActive;
        if (plantFoodModeActive) {
            pluckModeActive = false;
            updateShovelButtonState();
            if (plantFoodButton != null) {
                plantFoodButton.addAction(Actions.sequence(
                    Actions.scaleTo(1.25f, 1.25f, 0.08f),
                    Actions.scaleTo(1f, 1f, 0.15f)));
            }
        }
        updatePlantFoodButtonState();
    }

    private void updatePlantFoodHud() {
        if (!(AppStatus.getGameEngine() instanceof RegularGameEngine engine)) {
            return;
        }
        int count = engine.getPlantFoodManager().getPlantFoodCount();
        if (plantFoodCountLabel != null) {
            String text = String.valueOf(count);
            if (!text.equals(plantFoodCountLabel.getText().toString())) {
                plantFoodCountLabel.setText(text);
                plantFoodCountLabel.addAction(Actions.sequence(
                    Actions.scaleTo(1.6f, 1.6f, 0.1f),
                    Actions.scaleTo(1f, 1f, 0.15f)));
            }
        }
        if (count <= 0 && plantFoodModeActive) {
            plantFoodModeActive = false;
        }
        // Don't stomp the red "empty" flash with the per-frame tint.
        if (plantFoodButton != null && System.currentTimeMillis() >= plantFoodFlashUntil) {
            float alpha = count <= 0 ? 0.4f : (plantFoodModeActive ? 1f : 0.85f);
            plantFoodButton.setColor(1f, 1f, 1f, alpha);
        }
    }

    private void updatePlantFoodButtonState() {
        if (plantFoodButton != null) {
            boolean hasFood = !(AppStatus.getGameEngine() instanceof RegularGameEngine engine)
                || engine.getPlantFoodManager().getPlantFoodCount() > 0;
            float alpha = hasFood ? (plantFoodModeActive ? 1f : 0.85f) : 0.4f;
            plantFoodButton.setColor(1f, 1f, 1f, alpha);
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
        updateIntro(delta);
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
        updateTimedWarLabel(activeEngine);
        updatePlantFoodHud();
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
        gameBatch.begin();

        String chapterName = AppStatus.currentChapterName;
        Map activeMap = activeEngine.getMap() != null ? activeEngine.getMap() : gameMap;
        boolean renderedComposite = false;
        if (chapterName != null && activeMap != null) {
            renderedComposite = WorldBackgroundRenderer.getInstance().render(
                gameBatch, chapterName, activeMap.getStartX(), activeMap.getStartY(),
                activeMap.getTotalWidth(), activeMap.getTotalHeight()
            );
        }

        if (!renderedComposite) {
            Texture activeBackground = activeEngine.getBackgroundOverride();
            Texture activeBackgroundRight = activeEngine.getBackgroundOverrideRight();
            if (activeBackground == null) {
                activeBackground = backgroundTexture;
            }

            float scaleX = 1.20f;
            float scaleY = 1.30f;
            float offsetX = -100f;
            float offsetY = -140f;

            if (activeBackgroundRight != null && activeBackground != null) {
                float baseScale = VIRTUAL_HEIGHT / (float) activeBackground.getHeight();
                float leftW = activeBackground.getWidth() * baseScale * scaleX;
                float leftH = VIRTUAL_HEIGHT * scaleY;
                float rightW = activeBackgroundRight.getWidth() * baseScale * scaleX;
                float rightH = VIRTUAL_HEIGHT * scaleY;
                float startX = 0f + offsetX;
                float startY = 0f + offsetY;
                gameBatch.draw(activeBackground, startX, startY, leftW, leftH);
                gameBatch.draw(activeBackgroundRight, startX + leftW, startY, rightW, rightH);
            } else if (activeBackground != null) {
                float finalW = VIRTUAL_WIDTH * scaleX;
                float finalH = VIRTUAL_HEIGHT * scaleY;
                float startX = 0f + offsetX;
                float startY = 0f + offsetY;
                gameBatch.draw(activeBackground, startX, startY, finalW, finalH);
            }
        }

        drawPreviewZombies(gameBatch);
        gameBatch.end();

        float renderDelta = isSimulationFrozen() ? 0f : Math.min(delta, 1 / 30f);
        activeEngine.render(renderDelta, gameBatch);
    }

    private void drawPreviewZombies(SpriteBatch batch) {
        if (!introFinished && !previewZombies.isEmpty() && gameMap != null) {
            float baseY = gameMap.getStartY();
            float startPreviewX = gameMap.getStartX() + gameMap.getTotalWidth() + 120f;
            for (int i = 0; i < previewZombies.size(); i++) {
                String alias = previewZombies.get(i);
                int col = i % 3;
                int row = i / 3;
                float zx = startPreviewX + col * 170f;
                float zy = baseY - 220f - (row * 190f);
                EntityRenderer.getInstance().renderZombieAlias(batch, alias, "idle", introTimer, zx, zy);
            }
        }
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

    private void updateTimedWarLabel(GameEngine activeEngine) {
        if (timedWarLabel == null) {
            return;
        }
        if (!(activeEngine instanceof RegularGameEngine regularEngine)) {
            return;
        }
        if (!(regularEngine.getSpecialLevel()
            instanceof com.PVZ.model.game.chapter.sepecialLevel.TimedWarLevel timedWar)) {
            return;
        }
        double remaining = timedWar.getRemainingSeconds();
        StringBuilder text = new StringBuilder("TIME: ").append(String.format("%.1fs", remaining));
        if (timedWar.hasKillGoal() && timedWar.hasSunGoal()) {
            text.append("  |  ZOMBIES LEFT: ").append(timedWar.getKillsRemaining(regularEngine))
                .append("  |  SUN LEFT: ").append(timedWar.getSunRemaining(regularEngine));
        } else if (timedWar.hasKillGoal()) {
            text.append("  |  ZOMBIES LEFT: ").append(timedWar.getKillsRemaining(regularEngine));
        } else {
            text.append("  |  SUN LEFT: ").append(timedWar.getSunRemaining(regularEngine));
        }
        timedWarLabel.setText(text.toString());
        com.badlogic.gdx.graphics.Color color = com.badlogic.gdx.graphics.Color.WHITE;
        if (remaining <= 15) {
            color = com.badlogic.gdx.graphics.Color.RED;
        } else if (remaining <= timedWar.getTimeLimitSeconds() * 0.25) {
            color = com.badlogic.gdx.graphics.Color.ORANGE;
        }
        timedWarLabel.setColor(color);
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

        // موقعیت پایه‌ی دوربین در این بازی شامل افکت‌های شیک و انیمیشن اینترو است
        camera.zoom = cameraIntroZoom;
        camera.position.set(
            VIRTUAL_WIDTH / 2f + shakeOffsetX + cameraIntroOffsetX,
            VIRTUAL_HEIGHT / 2f + shakeOffsetY,
            0f
        );
        camera.update();
    }
}
