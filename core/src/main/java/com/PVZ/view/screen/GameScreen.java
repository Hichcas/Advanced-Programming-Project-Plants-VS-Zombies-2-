package com.PVZ.view.screen;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.game.*;
import com.PVZ.view.renderer.WorldBackgroundRenderer;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.model.user.UserRegistry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.libpvz.textures.TextureBank;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    private PauseMenuOverlay pauseMenuOverlay;
    private LevelStartOverlay levelStartOverlay;
    private WinLoseOverlay winLoseOverlay;
    private NpcDialogueOverlay npcDialogueOverlay;
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
    private MenuButton startWaveButton;
    private MenuButton surrenderButton;
    private Table startWaveButtonRoot;
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
    private boolean npcDialogueStarted = false;

    private OnlineMatchResultOverlay onlineMatchResultOverlay;
    private OnlineMatchResultOverlay localMatchResultOverlay;
    private boolean onlineMatchExitRequested = false;
    private DrawOfferOverlay drawOfferOverlay;

    // ══════════════ پس‌زمینه‌های چپتر ══════════════
    private static final java.util.Map<ChapterEnum, String> CHAPTER_BG_LEFT = new java.util.HashMap<>();
    private static final java.util.Map<ChapterEnum, String> CHAPTER_BG_RIGHT = new java.util.HashMap<>();

    static {
        CHAPTER_BG_LEFT.put(ChapterEnum.ANCIENT_EGYPT, "IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
        CHAPTER_BG_RIGHT.put(ChapterEnum.ANCIENT_EGYPT, "IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT");

        CHAPTER_BG_LEFT.put(ChapterEnum.FROSTBITE_CAVES, "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
        CHAPTER_BG_RIGHT.put(ChapterEnum.FROSTBITE_CAVES, "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_RIGHT");

        CHAPTER_BG_LEFT.put(ChapterEnum.BIG_WAVE_BEACH, "IMAGE_BACKGROUNDS_BEACH_TEXTURE");
        CHAPTER_BG_RIGHT.put(ChapterEnum.BIG_WAVE_BEACH, "IMAGE_BACKGROUNDS_BEACH_TEXTURE_RIGHT");

        CHAPTER_BG_LEFT.put(ChapterEnum.DARK_AGES, "IMAGE_BACKGROUNDS_DARK_TEXTURE");
        CHAPTER_BG_RIGHT.put(ChapterEnum.DARK_AGES, "IMAGE_BACKGROUNDS_DARK_TEXTURE_RIGHT");
    }

    // ══════════════ تنظیمات پس‌زمینه هر چپتر ══════════════
    private static final class BackgroundSettings {
        final float scaleX;
        final float scaleY;
        final float offsetX;
        final float offsetY;

        BackgroundSettings(float scaleX, float scaleY, float offsetX, float offsetY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private static final java.util.Map<ChapterEnum, BackgroundSettings> CHAPTER_BG_SETTINGS =
        new java.util.HashMap<>();

    static {
        // مقادیر پیش‌فرض برای همه فصل‌ها (همون چیزی که الان کار می‌کنه)
        CHAPTER_BG_SETTINGS.put(ChapterEnum.ANCIENT_EGYPT,
            new BackgroundSettings(1.20f, 1.30f, -100f, -140f));
        CHAPTER_BG_SETTINGS.put(ChapterEnum.BIG_WAVE_BEACH,
            new BackgroundSettings(1.20f, 1.30f, -100f, -140f));
        CHAPTER_BG_SETTINGS.put(ChapterEnum.DARK_AGES,
            new BackgroundSettings(1.20f, 1.30f, -100f, -140f));

        CHAPTER_BG_SETTINGS.put(ChapterEnum.FROSTBITE_CAVES,
            new BackgroundSettings(1.23f, 1.32f, -100f, -170f));
    }

    public GameScreen(String mapPath, String musicPath, GameEngine gameEngine) {
        super();
        this.gameBatch = new SpriteBatch();
        this.mapPath = mapPath;
        this.musicPath = musicPath;

        if (!AppStatus.isMultiplayerMatch && !(gameEngine instanceof com.PVZ.model.game.IZombieGameEngine)) {
            levelStartOverlay = new LevelStartOverlay(resolveStageConfig(), () -> {
                introStarted = true;
                introTimer = 0f;
            });
            stage.addActor(levelStartOverlay);
            levelStartOverlay.show();
        }

        String bgInternal = mapPath;
        if (com.badlogic.gdx.Gdx.files.internal(bgInternal).exists()) {
            backgroundTexture = new Texture(com.badlogic.gdx.Gdx.files.internal(bgInternal));
        } else {
            System.out.println("GameScreen: background not found: " + bgInternal);
            backgroundTexture = null;
        }

        ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
        String actualMusic = (chapter != null && chapter.getMusicPath() != null)
            ? chapter.getMusicPath()
            : musicPath;

        MusicManager.getInstance().playMusic(actualMusic);
        this.gameEngine = gameEngine;
        AppStatus.setGameEngine(gameEngine);

        initPreviewZombies();

        gameHud = new GameHud();
        stage.addActor(gameHud);

        pauseMenuOverlay = new PauseMenuOverlay(this::handleSaveAndExit, this::handleRestart);
        pauseMenuOverlay.setMissionText(resolveMissionText());
        stage.addActor(pauseMenuOverlay);

        if (gameEngine instanceof IZombieMultiplayerGameEngine) {
            stage.addActor(buildMultiplayerActionButtons());
        }

        winLoseOverlay = new WinLoseOverlay(this::handleSaveAndExit, this::handleRestart);
        stage.addActor(winLoseOverlay);

        npcDialogueOverlay = new NpcDialogueOverlay();
        stage.addActor(npcDialogueOverlay);

        stage.addActor(buildPauseButton());
        if (gameEngine instanceof RegularGameEngine) {
            stage.addActor(buildPlantFoodButton());
            stage.addActor(buildShovelButton());
        }
        if (gameEngine instanceof RegularGameEngine rge && rge.isPlantWhatYouGetMode()) {
            startWaveButtonRoot = buildStartWaveButton();
            stage.addActor(startWaveButtonRoot);
        }
        com.PVZ.view.screen.panels.CheatPanel.attachToggleButton(stage, this::handleCheatAcrossGameModes);

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

        onlineMatchResultOverlay = new OnlineMatchResultOverlay(this::exitOnlineMatch);
        stage.addActor(onlineMatchResultOverlay);

        localMatchResultOverlay = new OnlineMatchResultOverlay(this::exitLocalVersusMatch);
        stage.addActor(localMatchResultOverlay);

        if (gameEngine instanceof IZombieMultiplayerGameEngine) {
            drawOfferOverlay = new DrawOfferOverlay(accept -> {
                if (gameEngine instanceof IZombieMultiplayerGameEngine onlineEngine) {
                    onlineEngine.respondDrawOffer(accept);
                }
            });
            stage.addActor(drawOfferOverlay);
        }
    }

    private void exitOnlineMatch() {
        onlineMatchExitRequested = true;
        if (onlineMatchResultOverlay != null) {
            onlineMatchResultOverlay.hide();
        }
        AppStatus.isMultiplayerMatch = false;
        AppStatus.multiplayerRole = null;
        AppStatus.multiplayerOpponent = null;
        AppStatus.multiplayerRoomId = null;
        AppStatus.multiplayerLevelId = 1;
        AppStatus.SELECTED_PLANTS.clear();
        AppStatus.SELECTED_ZOMBIES.clear();

        ScreenManager.getInstance().performTransition(MainMenuScreen::new);
    }

    private void exitLocalVersusMatch() {
        if (localMatchResultOverlay != null) {
            localMatchResultOverlay.hide();
        }
        AppStatus.isMultiplayerMatch = false;
        AppStatus.multiplayerRole = null;
        AppStatus.multiplayerOpponent = null;
        AppStatus.multiplayerRoomId = null;
        AppStatus.multiplayerLevelId = 1;
        AppStatus.SELECTED_PLANTS.clear();
        AppStatus.SELECTED_ZOMBIES.clear();

        ScreenManager.getInstance().performTransition(MainMenuScreen::new);
    }

    private Table buildMultiplayerActionButtons() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();
        overlay.setTouchable(Touchable.childrenOnly);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        com.badlogic.gdx.graphics.g2d.TextureRegion upRegion =
            EntityRenderer.getInstance().getTextures().region("IMAGE_UI_POWERUPS_POWER_FLAMETHROWER");
        com.badlogic.gdx.graphics.g2d.TextureRegion downRegion =
            EntityRenderer.getInstance().getTextures().region("IMAGE_UI_POWERUPS_POWER_FLAMETHROWER_DOWN");

        MenuButton surrenderBtn;
        MenuButton drawBtn;
        if (upRegion != null && downRegion != null) {
            surrenderBtn = new MenuButton(upRegion, "SURRENDER", font, downRegion, null, null, this::onSurrender);
            drawBtn = new MenuButton(upRegion, "DRAW", font, downRegion, null, null, this::onDraw);
        } else {
            surrenderBtn = new MenuButton("SURRENDER", font, this::onSurrender);
            drawBtn = new MenuButton("DRAW", font, this::onDraw);
        }
        surrenderBtn.setSize(180f, 60f);
        drawBtn.setSize(180f, 60f);

        overlay.add(surrenderBtn).size(180f, 60f).padTop(200f).padRight(20f).row();
        overlay.add(drawBtn).size(180f, 60f).padTop(270f).padRight(20f);

        return overlay;
    }

    private void onSurrender() {
        if (gameEngine instanceof IZombieMultiplayerGameEngine onlineEngine) {
            onlineEngine.surrender();
        }
    }

    private void onDraw() {
        if (gameEngine instanceof IZombieMultiplayerGameEngine onlineEngine) {
            onlineEngine.offerDraw();
        }
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
                float t = introTimer / INTRO_PAN_RIGHT_DURATION;
                float smoothT = t * t * (3f - 2f * t);
                cameraIntroOffsetX = MAX_PAN_OFFSET * smoothT;
                cameraIntroZoom = 1.0f + (MAX_ZOOM_OUT - 1.0f) * smoothT;
            } else if (introTimer < INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION) {
                cameraIntroOffsetX = MAX_PAN_OFFSET;
                cameraIntroZoom = MAX_ZOOM_OUT;
            } else if (introTimer < INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION + INTRO_PAN_LEFT_DURATION) {
                float t = (introTimer - (INTRO_PAN_RIGHT_DURATION + INTRO_HOLD_RIGHT_DURATION)) / INTRO_PAN_LEFT_DURATION;
                float smoothT = t * t * (3f - 2f * t);
                cameraIntroOffsetX = MAX_PAN_OFFSET * (1f - smoothT);
                cameraIntroZoom = MAX_ZOOM_OUT - (MAX_ZOOM_OUT - 1.0f) * smoothT;
            } else {
                cameraIntroOffsetX = 0f;
                cameraIntroZoom = 1.0f;
                if (!introFinished) {
                    introFinished = true;
                    if (!npcDialogueStarted && !AppStatus.isMultiplayerMatch) {
                        npcDialogueStarted = true;
                        if (npcDialogueOverlay != null) {
                            npcDialogueOverlay.showDialogue();
                        }
                    }
                }
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
        if (AppStatus.isMultiplayerMatch) {
            return onlineMatchResultOverlay != null && onlineMatchResultOverlay.isShowing()
                || drawOfferOverlay != null && drawOfferOverlay.isShowing();
        }
        if (AppStatus.getGameEngine() instanceof IZombieLocalVersusEngine) {
            return localMatchResultOverlay != null && localMatchResultOverlay.isShowing();
        }
        return (levelStartOverlay != null && levelStartOverlay.isShowing())
            || (introStarted && !introFinished)
            || (npcDialogueOverlay != null && npcDialogueOverlay.isShowing())
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

    private Table buildStartWaveButton() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.bottom();
        overlay.setTouchable(Touchable.childrenOnly);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        MenuButton button;
        try {
            Skin skin = PvzSkin.get();
            Drawable greenUp = skin.getDrawable("image_ui_generic_greenbutton_10");
            Drawable greenDown = skin.getDrawable("image_ui_generic_greenbutton_down_10");
            button = new MenuButton(greenUp, "START WAVE!", font, greenDown, null, null,
                this::startPlantWhatYouGetWaves);
        } catch (Exception ex) {
            button = new MenuButton("START WAVE!", font, this::startPlantWhatYouGetWaves);
        }
        button.setSize(320f, 100f);
        startWaveButton = button;
        overlay.add(button).size(320f, 100f).padBottom(40f);
        return overlay;
    }

    private void startPlantWhatYouGetWaves() {
        if (AppStatus.getGameEngine() instanceof RegularGameEngine rge
            && rge.isPlantWhatYouGetMode() && !rge.isZombieWavesStarted()) {
            rge.startWaves();
        }
    }

    private void updateStartWaveButtonVisibility() {
        if (startWaveButtonRoot == null) return;
        boolean shouldShow = AppStatus.getGameEngine() instanceof RegularGameEngine rge
            && rge.isPlantWhatYouGetMode() && !rge.isZombieWavesStarted();
        startWaveButtonRoot.setVisible(shouldShow);
        startWaveButtonRoot.setTouchable(shouldShow ? Touchable.childrenOnly : Touchable.disabled);
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
        if (map == null) return false;

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
        if (map == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0f));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

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
        if (map == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0f));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

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

        if (gameEngine instanceof IZombieMultiplayerGameEngine onlineEngine && !onlineEngine.isMatchFinished()) {
            onlineEngine.surrender();
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
        super.show();
        AppStatus.registerCameraShakeTrigger(this::activeCameraShake);
        multiplexer.clear();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (isSimulationFrozen()) {
                    return false;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.F) {
                    GameEngine active = AppStatus.getGameEngine();
                    if (active instanceof RegularGameEngine reg) {
                        CombatHandler.freezeAllZombies(reg, 5.0);
                        System.out.println("[Cheat Hotkey F] All zombies frozen for 5.0 seconds!");
                        return true;
                    }
                } else if (keycode == com.badlogic.gdx.Input.Keys.X) {
                    GameEngine active = AppStatus.getGameEngine();
                    if (active instanceof RegularGameEngine reg) {
                        BattleController bc = reg.getBattleController();
                        List<Zombie> list = reg.getZombieList();
                        for (int i = list.size() - 1; i >= 0; i--) {
                            Zombie z = list.get(i);
                            if (z != null && !z.isDead()) {
                                z.setDeathType(com.PVZ.model.enums.DeathType.ASH);
                                z.die(bc);
                            }
                        }
                        System.out.println("[Cheat Hotkey X] All zombies powdered into ash!");
                        return true;
                    }
                }
                if (activeInputProcessor() != null && activeInputProcessor().keyDown(keycode)) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (activeInputProcessor() != null && activeInputProcessor().keyUp(keycode)) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (activeInputProcessor() != null && activeInputProcessor().keyTyped(character)) {
                    return true;
                }
                return false;
            }

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
                return activeInputProcessor() != null && activeInputProcessor().touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return activeInputProcessor() != null && activeInputProcessor().touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return activeInputProcessor() != null && activeInputProcessor().touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if (isSimulationFrozen()) {
                    return false;
                }
                return activeInputProcessor() != null && activeInputProcessor().mouseMoved(screenX, screenY);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return activeInputProcessor() != null && activeInputProcessor().scrolled(amountX, amountY);
            }
        });
        super.show();

        if (drawOfferOverlay != null) {
            // لیسنر برای پیشنهاد تساوی
            com.PVZ.network.client.NetworkSession.client().on(
                com.PVZ.network.common.MessageType.DRAW_OFFER, msg -> {
                    Gdx.app.postRunnable(() -> {
                        if (drawOfferOverlay != null && !drawOfferOverlay.isShowing()) {
                            drawOfferOverlay.showOffer();
                        }
                    });
                });
        }

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
            regularGameEngine.getSeedPacketBar().layoutVertical(loadout, 30f, VIRTUAL_HEIGHT - 174f);
        } else {
            regularGameEngine.getSeedPacketBar().layout(loadout, 40f, VIRTUAL_HEIGHT - 174f);
        }
    }

    private List<PlantType> seedBarLoadout(RegularGameEngine regularGameEngine) {
        if (regularGameEngine.isConveyorBeltMode()) {
            return new ArrayList<>(regularGameEngine.getConveyorBeltQueue());
        }
        return new ArrayList<>(AppStatus.SELECTED_PLANTS);
    }

    private com.PVZ.view.output.OutputDTO handleCheatAcrossGameModes(
        com.PVZ.view.input.DTO.InGameInputDTO dto) {
        GameEngine active = AppStatus.getGameEngine() != null ? AppStatus.getGameEngine() : gameEngine;
        if (active instanceof com.PVZ.model.game.RegularGameEngine) {
            return new com.PVZ.controller.menuControllers.InGameMenuController().handle(dto);
        }

        if (!(active instanceof com.PVZ.model.game.ZombieEngine zombieEngine)) {
            return new com.PVZ.view.output.OutputDTO(false, "Cheats are unavailable in this game mode.");
        }

        try {
            return switch (dto.getCommand()) {
                case CHEAT_ADD_PLANT_SUN, CHEAT_ADD_SUNS -> {
                    int amount = dto.getAmount() == null ? 0 : dto.getAmount();
                    if (active instanceof com.PVZ.model.game.IZombieLocalVersusEngine versusEngine) {
                        versusEngine.addPlantSun(amount);
                        yield new com.PVZ.view.output.OutputDTO(true, "Added " + amount + " Plant Sun. Total: " + versusEngine.getPlantSun());
                    } else if (active instanceof com.PVZ.model.game.RegularGameEngine reg) {
                        reg.addSun(amount);
                        yield new com.PVZ.view.output.OutputDTO(true, "Added " + amount + " Plant Sun.");
                    }
                    zombieEngine.addSun(amount);
                    yield new com.PVZ.view.output.OutputDTO(true, "Added " + amount + " sun.");
                }
                case CHEAT_ADD_ZOMBIE_SUN -> {
                    int amount = dto.getAmount() == null ? 0 : dto.getAmount();
                    zombieEngine.addSun(amount);
                    yield new com.PVZ.view.output.OutputDTO(true, "Added " + amount + " Zombie Sun.");
                }
                case CHEAT_SPAWN_ZOMBIE -> {
                    String alias = dto.getZombieType();
                    int row = dto.getY() == null ? 2 : dto.getY();
                    int col = dto.getX() == null ? 8 : dto.getX();
                    com.PVZ.model.entity.zombies.base.Zombie z =
                        zombieEngine.spawnZombie(alias, row, col);
                    yield new com.PVZ.view.output.OutputDTO(z != null,
                        z != null ? "Zombie spawned: " + alias : "Could not spawn zombie.");
                }
                case CHEAT_RELEASE_NUKE, KILL_ALL_ZOMBIES -> {
                    java.util.List<com.PVZ.model.entity.zombies.base.Zombie> zombies =
                        zombieEngine.getZombiesInLane(-1);
                    if (zombies == null || zombies.isEmpty()) {
                        zombies = new java.util.ArrayList<>();
                        for (int r = 0; r < 5; r++) {
                            java.util.List<com.PVZ.model.entity.zombies.base.Zombie> lane = zombieEngine.getZombiesInLane(r);
                            if (lane != null) zombies.addAll(lane);
                        }
                    }
                    for (com.PVZ.model.entity.zombies.base.Zombie z : zombies) {
                        if (z != null && !z.isDead()) zombieEngine.kill(z);
                    }
                    yield new com.PVZ.view.output.OutputDTO(true, "All zombies killed.");
                }
                case CHEAT_REMOVE_COOLDOWN -> {
                    if (gameEngine instanceof com.PVZ.model.game.ZombotanyGameEngine z) {
                        z.clearPlantCooldowns();
                        yield new com.PVZ.view.output.OutputDTO(true, "Plant cooldowns removed.");
                    }
                    yield new com.PVZ.view.output.OutputDTO(true, "No plant cooldowns in this minigame.");
                }
                case CHEAT_ADD_PLANT_FOOD ->
                    new com.PVZ.view.output.OutputDTO(true, "Plant Food is not used by this minigame.");
                case CHEAT_SET_WATER, CHEAT_SET_DRY -> {
                    yield new com.PVZ.view.output.OutputDTO(false,
                        "Tile water/dry cheats are not supported by this minigame.");
                }
                default -> new com.PVZ.view.output.OutputDTO(false,
                    "Cheat is not supported by this minigame.");
            };
        } catch (RuntimeException ex) {
            return new com.PVZ.view.output.OutputDTO(false, "Cheat failed: " + ex.getMessage());
        }
    }

    @Override
    protected void renderScreen(float delta) {
        updateIntro(delta);
        applyCameraShake();

        String pendingAnnouncement = AppStatus.pollAnnouncement();
        if (pendingAnnouncement != null) {
            com.PVZ.view.screen.ui.AnnouncementPopup.show(stage, pendingAnnouncement,
                pendingAnnouncement.startsWith("MyoPoint") || pendingAnnouncement.startsWith("Game Over! Final MyoPoint")
                    ? com.badlogic.gdx.graphics.Color.GOLD
                    : com.badlogic.gdx.graphics.Color.WHITE);
        }

        refreshSeedPacketBar();
        GameEngine activeEngine = AppStatus.getGameEngine();
        if (activeEngine == null) {
            activeEngine = gameEngine;
        }

        // ... (میان‌برها و cheat ها به همان صورت که قبلاً بود)

        GameOverState overState = updateGameOverState(activeEngine);
        drawBackgroundAndEngine(activeEngine, delta);
        drawMapBorders(activeEngine);
        drawDeadline(activeEngine);
        updateTimedWarLabel(activeEngine);
        updatePlantFoodHud();
        updateStartWaveButtonVisibility();
        drawSeedPacketBar(activeEngine);
        drawGameOverOverlay(overState);
        drawTileDebug(activeEngine);
    }

    private GameOverState updateGameOverState(GameEngine activeEngine) {
        if (activeEngine instanceof IZombieLocalVersusEngine versusEngine) {
            if (!versusEngine.isMatchFinished()) {
                localMatchResultOverlay.hide();
            } else if (!localMatchResultOverlay.isShowing()) {
                localMatchResultOverlay.showResult(
                    versusEngine.isWonMatch(),
                    versusEngine.getMatchResultText()
                );
            }
            return new GameOverState(false, false, false);
        }

        if (activeEngine instanceof IZombieMultiplayerGameEngine onlineEngine) {
            if (!onlineMatchExitRequested && onlineEngine.isMatchFinished() && !onlineMatchResultOverlay.isShowing()) {
                if (onlineEngine.isDrawResult()) {
                    onlineMatchResultOverlay.showDraw(onlineEngine.getMatchResultText());
                } else {
                    onlineMatchResultOverlay.showResult(
                        onlineEngine.isWonMatch(),
                        onlineEngine.getMatchResultText()
                    );
                }
            } else if (!onlineEngine.isMatchFinished()) {
                onlineMatchResultOverlay.hide();
            }
            return new GameOverState(false, false, false);
        }

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

    private boolean drawChapterBackground(SpriteBatch batch, Map activeMap) {
        ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
        if (chapter == null) {
            return false;
        }

        String leftId = CHAPTER_BG_LEFT.get(chapter);
        String rightId = CHAPTER_BG_RIGHT.get(chapter);
        if (leftId == null) {
            return false;
        }

        TextureBank bank = EntityRenderer.getInstance().getTextures();
        if (bank == null) {
            return false;
        }

        TextureRegion leftRegion = bank.region(leftId);
        TextureRegion rightRegion = bank.region(rightId);

        if (leftRegion == null) {
            return false;
        }

        BackgroundSettings settings = CHAPTER_BG_SETTINGS.get(chapter);
        if (settings == null) {
            settings = new BackgroundSettings(1.20f, 1.30f, -100f, -140f);
        }

        float scaleX = settings.scaleX;
        float scaleY = settings.scaleY;
        float offsetX = settings.offsetX;
        float offsetY = settings.offsetY;

        float baseScale = VIRTUAL_HEIGHT / (float) leftRegion.getRegionHeight();
        float leftW = leftRegion.getRegionWidth() * baseScale * scaleX;
        float leftH = VIRTUAL_HEIGHT * scaleY;

        float startX = offsetX;
        float startY = offsetY;

        batch.draw(leftRegion, startX, startY, leftW, leftH);

        if (rightRegion != null) {
            float rightW = rightRegion.getRegionWidth() * baseScale * scaleX;
            float rightH = VIRTUAL_HEIGHT * scaleY;
            batch.draw(rightRegion, startX + leftW, startY, rightW, rightH);
        }

        return true;
    }

    private void drawBackgroundAndEngine(GameEngine activeEngine, float delta) {
        gameBatch.setProjectionMatrix(camera.combined);
        gameBatch.begin();

        Map activeMap = activeEngine.getMap() != null ? activeEngine.getMap() : gameMap;

        boolean chapterBackgroundDrawn = false;

        if (activeEngine instanceof RegularGameEngine) {
            chapterBackgroundDrawn = drawChapterBackground(gameBatch, activeMap);
        }

        if (!chapterBackgroundDrawn) {
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
                float startX = offsetX;
                float startY = offsetY;
                gameBatch.draw(activeBackground, startX, startY, leftW, leftH);
                gameBatch.draw(activeBackgroundRight, startX + leftW, startY, rightW, rightH);
            } else if (activeBackground != null) {
                float finalW = VIRTUAL_WIDTH * scaleX;
                float finalH = VIRTUAL_HEIGHT * scaleY;
                float startX = offsetX;
                float startY = offsetY;
                gameBatch.draw(activeBackground, startX, startY, finalW, finalH);
            }
        }

        if (!(activeEngine instanceof com.PVZ.model.game.IZombieGameEngine)) {
            drawPreviewZombies(gameBatch);
        }
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
        if (timedWarLabel == null) return;
        if (!(activeEngine instanceof RegularGameEngine regularEngine)) return;
        if (!(regularEngine.getSpecialLevel() instanceof com.PVZ.model.game.chapter.sepecialLevel.TimedWarLevel timedWar)) return;

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
        if (!(activeEngine instanceof RegularGameEngine regularEngine)) return;
        if (!(regularEngine.getSpecialLevel() instanceof com.PVZ.model.game.chapter.sepecialLevel.DeadLineLevel deadline)) return;
        Map activeMap = regularEngine.getMap();
        if (activeMap == null) return;
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

    private void drawSeedBar(com.PVZ.model.game.SeedBarEngine seedEngine, GameEngine engineWithSeedBar) {
        SeedPacketBar seedBar = null;
        PlantType selected = null;
        if (engineWithSeedBar instanceof RegularGameEngine reg) {
            seedBar = reg.getSeedPacketBar();
            selected = reg.getSelectedPlantType();
        } else if (engineWithSeedBar instanceof com.PVZ.model.game.ZombotanyGameEngine z) {
            seedBar = z.getSeedPacketBar();
            selected = z.getSelectedPlantType();
        }
        if (seedBar == null) return;

        shapeDebug.begin(ShapeRenderer.ShapeType.Filled);
        seedBar.drawBackgrounds(shapeDebug, seedEngine, selected);
        shapeDebug.end();

        gameBatch.begin();
        seedBar.drawIconsAndLabels(gameBatch, hudFont, seedEngine);
        gameBatch.end();
    }

    private void drawGameOverOverlay(GameOverState state) {
        if (!state.isGameOver || gameOverAlpha <= 0) return;
        gameBatch.begin();
        gameOverFont.setColor(1, 1, 1, gameOverAlpha);
        String message = state.isWin ? "LEVEL COMPLETE!" : "GAME OVER";
        GlyphLayout layout = new GlyphLayout(gameOverFont, message);
        float x = VIRTUAL_WIDTH / 2f - layout.width / 2f;
        float y = VIRTUAL_HEIGHT / 2f + layout.height / 2f;
        gameOverFont.draw(gameBatch, message, x, y);
        if (state.isEndOfGame) {
            String hint = "Type 'show stats' or 'menu exit'";
            gameOverFont.setColor(1, 1, 0, gameOverAlpha);
            GlyphLayout hintLayout = new GlyphLayout(gameOverFont, hint);
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
        if (!AppStatus.tileDebugEnabled || activeMap == null) return;
        gameBatch.begin();
        for (int r = 0; r < activeMap.getRows(); r++) {
            for (int c = 0; c < activeMap.getCols(); c++) {
                com.PVZ.model.entity.Tile tile = activeMap.getTile(r, c);
                if (tile == null || tile.getType() == com.PVZ.model.enums.TileType.NORMAL) continue;
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
        if (isDisposed()) return;
        super.dispose();
        if (shapeDebug != null) shapeDebug.dispose();
        if (gameBatch != null) gameBatch.dispose();
        if (gameEngine != null) gameEngine.dispose();
        pluckModeActive = false;
        shovelButton = null;
        startWaveButton = null;
        startWaveButtonRoot = null;
        if (backgroundTexture != null) backgroundTexture.dispose();
        AppStatus.registerCameraShakeTrigger(null);
        System.out.println("[GameScreen] PVZ resources disposed cleanly.");
    }

    private static final float SHAKE_DURATION = 0.4f;
    private static final float SHAKE_MAGNITUDE = 14f;
    private float shakeTimer = 0f;
    private final Random random = new Random();

    public void activeCameraShake() {
        shakeTimer = SHAKE_DURATION;
    }

    private void applyCameraShake() {
        float shakeOffsetX = 0f;
        float shakeOffsetY = 0f;
        if (shakeTimer > 0f) {
            shakeTimer -= Gdx.graphics.getDeltaTime();
            float fade = Math.max(shakeTimer, 0f) / SHAKE_DURATION;
            shakeOffsetX = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
            shakeOffsetY = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
        }
        camera.zoom = cameraIntroZoom;
        camera.position.set(
            VIRTUAL_WIDTH / 2f + shakeOffsetX + cameraIntroOffsetX,
            VIRTUAL_HEIGHT / 2f + shakeOffsetY,
            0f
        );
        camera.update();
    }
}
