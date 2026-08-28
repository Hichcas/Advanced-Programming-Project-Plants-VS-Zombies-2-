package com.PVZ.model.game;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.BaseScreen;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHud extends Group {

    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String PLANTFOOD_PAM = "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM";

    private static final float PANEL_ICON_SIZE = 74f;
    private static final float PANEL_HEIGHT = 84f;
    private static final float PANEL_GAP = 14f;
    private static final float PANEL_TOP_MARGIN = 30f;
    private static final float PANEL_LEFT_MARGIN = 24f;

    private static final float WAVEBAR_HEIGHT = 30f;
    private static final float WAVEBAR_TOP_MARGIN = 18f;
    private static final float WAVEBAR_SIDE_MARGIN = 480f;

    private final BitmapFont font;
    private final BitmapFont bigFont;
    private float animTime = 0f;
    private int sunflowerCount = 0;
    private int plantFoodCount = 0;
    private int zombieWavePercent = 0;
    private boolean versusMode = false;
    private float matchTimeRemaining = -1f;
    private List<Float> waveMarkerRatios = List.of();
    private boolean conveyorBeltMode = false;
    private String beltLine = null;

    private static final float LOCKED_ICON_SIZE = 32f;
    private static final float LOCKED_ICON_GAP = 6f;
    private List<PlantType> lockedPlantsForHud = new ArrayList<>();
    private final Map<PlantType, Texture> lockedIconCache = new HashMap<>();

    private static NinePatch panelBackground;
    private static NinePatch waveBarTrack;
    private static NinePatch waveBarFill;

    public GameHud() {
        this.font = FontManager.getInstance().getEnglishMenuFont();
        this.bigFont = FontManager.getInstance().getEnglishMenuFont();
        setSize(BaseScreen.VIRTUAL_WIDTH, BaseScreen.VIRTUAL_HEIGHT);
        ensureBackgrounds();
    }

    private static void ensureBackgrounds() {
        if (panelBackground != null) {
            return;
        }
        panelBackground = buildNinePatch(0.06f, 0.05f, 0.03f, 0.72f, 0.85f, 0.65f, 0.25f, 1f, 10);
        waveBarTrack = buildNinePatch(0.08f, 0.08f, 0.08f, 0.8f, 0.4f, 0.4f, 0.4f, 1f, 6);
        waveBarFill = buildNinePatch(0.75f, 0.12f, 0.1f, 1f, 1f, 0.55f, 0.5f, 1f, 6);
    }

    private static NinePatch buildNinePatch(float r, float g, float b, float a,
                                             float borderR, float borderG, float borderB, float borderA,
                                             int border) {
        int size = border * 2 + 4;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        pixmap.setColor(borderR, borderG, borderB, borderA);
        pixmap.drawRectangle(0, 0, size, size);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new NinePatch(texture, border, border, border, border);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animTime += delta;

        GameEngine engine = AppStatus.getGameEngine();
        if (engine == null || engine.gameStatus == null) {
            return;
        }
        sunflowerCount = engine.gameStatus.getSunflower();
        zombieWavePercent = engine.gameStatus.getRemainingZombieWaveInPercent();

        if (engine instanceof IZombieLocalVersusEngine versusEngine) {
            versusMode = true;
            matchTimeRemaining = versusEngine.getMatchTimeRemaining();
        } else {
            versusMode = false;
            matchTimeRemaining = -1f;
        }

        if (engine instanceof RegularGameEngine regularEngine) {
            plantFoodCount = regularEngine.getPlantFoodManager().getPlantFoodCount();
            WaveManager waveManager = regularEngine.getWaveManager();
            waveMarkerRatios = waveManager != null ? waveManager.getWaveMarkerRatios() : List.of();
        } else {
            plantFoodCount = 0;
            waveMarkerRatios = List.of();
        }

        if (engine instanceof RegularGameEngine regularEngine && regularEngine.isConveyorBeltMode()) {
            conveyorBeltMode = true;
            // The vertical belt bar (left side) already shows the upcoming queue
            // visually now, so the old text line would just be a redundant
            // overlap - skip it.
            beltLine = null;
        } else {
            conveyorBeltMode = false;
            beltLine = null;
        }

        if (engine instanceof RegularGameEngine regularEngine && regularEngine.isLockedPlantsMode()) {
            lockedPlantsForHud = new ArrayList<>(regularEngine.getLockedPlantsForStage());
        } else {
            lockedPlantsForHud = List.of();
        }
    }

    private String formatBelt(List<PlantType> queue) {
        if (queue.isEmpty()) {
            return "(empty, waiting...)";
        }
        StringBuilder sb = new StringBuilder();
        for (PlantType type : queue) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.getDisplayName());
        }
        return sb.toString();
    }

    private Texture getLockedIcon(PlantType type) {
        if (lockedIconCache.containsKey(type)) {
            return lockedIconCache.get(type);
        }
        Texture texture = null;
        try {
            String path = PlantTexturePaths.getPath(type.name());
            if (Gdx.files.internal(path).exists()) {
                texture = new Texture(Gdx.files.internal(path));
            }
        } catch (RuntimeException ex) {
            texture = null;
        }
        lockedIconCache.put(type, texture);
        return texture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float top = BaseScreen.VIRTUAL_HEIGHT - PANEL_TOP_MARGIN;
        float sunPanelY;
        float sunPanelX;
        if (conveyorBeltMode) {
            // Conveyor belt levels get a vertical plant bar down the left side, so
            // push sun/plant-food down to the bottom-right instead of their usual
            // top-left spot - nothing to collide with there.
            float panelWidthGuess = PANEL_ICON_SIZE + 90f;
            sunPanelX = BaseScreen.VIRTUAL_WIDTH - panelWidthGuess - PANEL_LEFT_MARGIN;
            sunPanelY = PANEL_TOP_MARGIN + PANEL_HEIGHT + PANEL_GAP;
        } else {
            sunPanelY = top - PANEL_HEIGHT;
            sunPanelX = PANEL_LEFT_MARGIN;
        }

        drawCounterPanel(batch, parentAlpha, SUN_PAM, sunflowerCount, sunPanelX, sunPanelY, Color.GOLD);

        float plantFoodPanelY = conveyorBeltMode ? PANEL_TOP_MARGIN : sunPanelY - PANEL_HEIGHT - PANEL_GAP;
        drawCounterPanel(batch, parentAlpha, PLANTFOOD_PAM, plantFoodCount, sunPanelX, plantFoodPanelY, Color.LIME);

        if (versusMode) {
            drawMatchTimer(batch, parentAlpha, top);
        } else {
            drawWaveBar(batch, parentAlpha, top);
        }

        float nextLineY = top - PANEL_HEIGHT - PANEL_HEIGHT - PANEL_GAP * 2 - 30f;

        if (beltLine != null) {
            font.setColor(Color.CYAN);
            font.draw(batch, beltLine, sunPanelX, nextLineY);
            nextLineY -= 40f;
        }

        if (!lockedPlantsForHud.isEmpty()) {
            font.setColor(Color.ORANGE);
            font.draw(batch, "Locked:", sunPanelX, nextLineY);

            float iconX = sunPanelX;
            float iconY = nextLineY - LOCKED_ICON_SIZE - 6;
            for (PlantType type : lockedPlantsForHud) {
                Texture icon = getLockedIcon(type);
                if (icon != null) {
                    batch.setColor(1f, 1f, 1f, 0.85f);
                    batch.draw(icon, iconX, iconY, LOCKED_ICON_SIZE, LOCKED_ICON_SIZE);
                    batch.setColor(1f, 1f, 1f, 1f);
                }
                iconX += LOCKED_ICON_SIZE + LOCKED_ICON_GAP;
            }
        }
        font.setColor(Color.WHITE);
    }

    private void drawCounterPanel(Batch batch, float parentAlpha, String pamPath, int value,
                                   float x, float y, Color textColor) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        float panelWidth = PANEL_ICON_SIZE + 90f;
        panelBackground.draw(batch, x, y, panelWidth, PANEL_HEIGHT);

        float iconCx = x + PANEL_ICON_SIZE / 2f + 6f;
        float iconCy = y + PANEL_HEIGHT / 2f;
        boolean drew = false;
        if (batch instanceof SpriteBatch spriteBatch) {
            drew = EntityRenderer.getInstance().renderPam(spriteBatch, pamPath, animTime, iconCx, iconCy);
        }
        if (!drew) {
            batch.setColor(textColor.r, textColor.g, textColor.b, 0.85f * parentAlpha);
            batch.draw(getFallbackDot(), iconCx - PANEL_ICON_SIZE / 2.2f, iconCy - PANEL_ICON_SIZE / 2.2f,
                PANEL_ICON_SIZE * 0.9f, PANEL_ICON_SIZE * 0.9f);
        }

        bigFont.setColor(textColor);
        String text = String.valueOf(value);
        GlyphLayout layout = new GlyphLayout(bigFont, text);
        float textX = x + PANEL_ICON_SIZE + 14f;
        float textY = y + PANEL_HEIGHT / 2f + layout.height / 2f;
        bigFont.draw(batch, text, textX, textY);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawWaveBar(Batch batch, float parentAlpha, float top) {
        float barX = WAVEBAR_SIDE_MARGIN;
        float barWidth = BaseScreen.VIRTUAL_WIDTH - 2 * WAVEBAR_SIDE_MARGIN;
        float barY = top - WAVEBAR_TOP_MARGIN - WAVEBAR_HEIGHT;

        batch.setColor(1f, 1f, 1f, parentAlpha);
        Drawable trackDrawable = resolveMeterDrawable(false);
        if (trackDrawable != null) {
            trackDrawable.draw(batch, barX, barY, barWidth, WAVEBAR_HEIGHT);
        } else {
            waveBarTrack.draw(batch, barX, barY, barWidth, WAVEBAR_HEIGHT);
        }

        float fillRatio = Math.max(0f, Math.min(1f, zombieWavePercent / 100f));
        float fillWidth = Math.max(WAVEBAR_HEIGHT, barWidth * fillRatio);
        if (fillRatio > 0.01f) {
            Drawable fillDrawable = resolveMeterDrawable(true);
            if (fillDrawable != null) {
                fillDrawable.draw(batch, barX, barY, fillWidth, WAVEBAR_HEIGHT);
            } else {
                waveBarFill.draw(batch, barX, barY, fillWidth, WAVEBAR_HEIGHT);
            }
        }

        for (int i = 0; i < waveMarkerRatios.size(); i++) {
            float ratio = waveMarkerRatios.get(i);
            boolean isLastWave = i == waveMarkerRatios.size() - 1;
            float markerX = barX + barWidth * ratio;
            drawWaveFlag(batch, parentAlpha, markerX, barY, isLastWave);
        }

        font.setColor(Color.WHITE);
        String label = "Zombies: " + zombieWavePercent + "%";
        GlyphLayout layout = new GlyphLayout(font, label);
        font.draw(batch, label, barX + barWidth / 2f - layout.width / 2f, barY + WAVEBAR_HEIGHT / 2f + layout.height / 2f);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawMatchTimer(Batch batch, float parentAlpha, float top) {
        int totalSeconds = (int) Math.ceil(Math.max(0f, matchTimeRemaining));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);

        boolean urgent = matchTimeRemaining <= 10f;
        BitmapFont timerFont = bigFont;
        Color timerColor = urgent ? Color.SCARLET : Color.GOLD;

        float boxW = 220f;
        float boxH = WAVEBAR_HEIGHT + 18f;
        float boxX = BaseScreen.VIRTUAL_WIDTH / 2f - boxW / 2f;
        float boxY = 1270f;
        if (getFallbackSquare() != null) {
            batch.setColor(0f, 0f, 0f, 0.55f * parentAlpha);
            batch.draw(getFallbackSquare(), boxX, boxY, boxW, boxH);
            batch.setColor(timerColor.r, timerColor.g, timerColor.b, 0.9f * parentAlpha);
            batch.draw(getFallbackSquare(), boxX, boxY, boxW, 2f);
            batch.draw(getFallbackSquare(), boxX, boxY + boxH - 2f, boxW, 2f);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        timerFont.setColor(timerColor);
        GlyphLayout layout = new GlyphLayout(timerFont, timeText);
        float textX = BaseScreen.VIRTUAL_WIDTH / 2f - layout.width / 2f;
        float textY = boxY + boxH / 2f + layout.height / 2f;
        timerFont.draw(batch, timeText, textX, textY);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private static Drawable resolveMeterDrawable(boolean fill) {
        try {
            com.badlogic.gdx.scenes.scene2d.ui.Skin skin = PvzSkin.get();
            if (skin == null) {
                return null;
            }
            String name = fill ? "image_ui_hud_ingame_progress_meter_fill_10"
                                : "image_ui_hud_ingame_progress_meter_10";
            if (skin.has(name, Drawable.class)) {
                return skin.getDrawable(name);
            }
        } catch (Exception ignored) {
        }
        return null;
    }


    private void drawWaveFlag(Batch batch, float parentAlpha, float x, float barY, boolean isFinalWave) {
        float poleHeight = isFinalWave ? WAVEBAR_HEIGHT + 22f : WAVEBAR_HEIGHT + 10f;

        boolean drewPam = false;
        if (batch instanceof SpriteBatch spriteBatch) {
            String flagPam = resolveChapterFlagPam();
            if (flagPam != null) {
                float flagCx = x;
                float flagCy = barY + poleHeight * 0.5f;
                drewPam = EntityRenderer.getInstance().renderPam(spriteBatch, flagPam, animTime, flagCx, flagCy);
            }
        }

        if (drewPam) {
            return;
        }

        float poleWidth = 4f;
        Color color = isFinalWave ? Color.SCARLET : Color.WHITE;
        batch.setColor(color.r, color.g, color.b, parentAlpha);
        batch.draw(getFallbackSquare(), x - poleWidth / 2f, barY - 4f, poleWidth, poleHeight);

        float flagSize = isFinalWave ? 20f : 14f;
        batch.draw(getFallbackSquare(), x - poleWidth / 2f, barY - 4f + poleHeight - flagSize,
            flagSize, flagSize * 0.7f);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private static final Map<ChapterEnum, String> CHAPTER_FLAG_PAM = new HashMap<>();
    static {
        CHAPTER_FLAG_PAM.put(ChapterEnum.ANCIENT_EGYPT, "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_FLAG/ZOMBIE_EGYPT_FLAG.PAM");
        CHAPTER_FLAG_PAM.put(ChapterEnum.FROSTBITE_CAVES, "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_FLAG/ZOMBIE_ICEAGE_FLAG.PAM");
        CHAPTER_FLAG_PAM.put(ChapterEnum.BIG_WAVE_BEACH, "768/FULL/ZOMBIE/ZOMBIE_BEACH_FLAG/ZOMBIE_BEACH_FLAG.PAM");
        CHAPTER_FLAG_PAM.put(ChapterEnum.DARK_AGES, "768/FULL/ZOMBIE/ZOMBIE_DARK_FLAG/ZOMBIE_DARK_FLAG.PAM");
    }
    private static final String DEFAULT_FLAG_PAM = "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_FLAG/ZOMBIE_TUTORIAL_FLAG.PAM";

    private static String resolveChapterFlagPam() {
        ChapterEnum chapter;
        try {
            chapter = AppStatus.getCurrentChapterEnum();
        } catch (Exception e) {
            chapter = null;
        }
        String pam = chapter != null ? CHAPTER_FLAG_PAM.get(chapter) : null;
        return pam != null ? pam : DEFAULT_FLAG_PAM;
    }

    private static Texture fallbackDot;
    private static Texture fallbackSquare;

    private static Texture getFallbackDot() {
        if (fallbackDot == null) {
            int size = 32;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
            fallbackDot = new Texture(pixmap);
            pixmap.dispose();
        }
        return fallbackDot;
    }

    private static Texture getFallbackSquare() {
        if (fallbackSquare == null) {
            Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            fallbackSquare = new Texture(pixmap);
            pixmap.dispose();
        }
        return fallbackSquare;
    }
}
