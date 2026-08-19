package com.PVZ.view.screen;

import com.PVZ.view.screen.manager.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.entity.PamAnimationCatalog;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class NpcDialogueOverlay extends Actor {

    private static final String NPC_PAM = "768/FULL/NPC/SUNFLOWER/SUNFLOWER.PAM";

    private static final float NPC_DRAW_SIZE = 260f;
    private static final float NPC_NATIVE_CANVAS = 390f;

    private static final float ENTER_DURATION = 1.2f;
    private static final float SHOUT_DURATION = 1.8f;
    private float exitDuration = 1.0f; // مقدار پیش‌فرض، بعداً از کاتالوگ دقیق می‌شود
    private static final float CHARS_PER_SECOND = 28f;

    private static final String SHOUT_LINE_TEXT = "TA, GIVE ME A SCORE!";

    private enum State {
        HIDDEN, ENTER, TALK, WAIT, SHOUT, EXIT
    }

    private State state = State.HIDDEN;
    private List<String> lines = new ArrayList<>();
    private int lineIndex = -1;

    private float stateTimer = 0f;
    private float animTime = 0f;
    private float typeTimer = 0f;
    private int visibleChars = 0;

    private boolean showing = false;

    private final BitmapFont font;
    private final Drawable bubbleBackground;
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();

    private final java.util.Map<String, String> resolvedClips = new java.util.HashMap<>();
    private final java.util.Map<String, Float> resolvedDurations = new java.util.HashMap<>();

    public NpcDialogueOverlay() {
        setSize(BaseScreen.VIRTUAL_WIDTH, BaseScreen.VIRTUAL_HEIGHT);
        setVisible(false);
        setTouchable(Touchable.enabled);

        font = FontManager.getInstance().getEnglishMenuFont();
        bubbleBackground = resolveBubbleBackground();
        pixel = createPixel();

        preloadClipNames();

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (showing) {
                    advanceDialogue();
                    return true;
                }
                return false;
            }
        });
    }

    private void preloadClipNames() {
        String[] desired = {"enter", "talk", "idle", "shout", "exit"};
        for (String stateName : desired) {
            String exact = PamAnimationCatalog.resolveClip(NPC_PAM, stateName);
            if (exact != null) {
                resolvedClips.put(stateName, exact);
                Double duration = PamAnimationCatalog.clipDuration(NPC_PAM, exact);
                if (duration != null) {
                    resolvedDurations.put(stateName, duration.floatValue());
                }
            } else {
                System.err.println("[NPC] Clip not resolved for state: " + stateName);
            }
        }

        // تنظیم دقیق مدت زمان خروج برای جلوگیری از پخش دوباره انیمیشن
        Float exitDur = resolvedDurations.get("exit");
        if (exitDur != null && exitDur > 0f) {
            exitDuration = exitDur;
        }

        System.out.println("[NPC] Resolved clips: " + resolvedClips);
        System.out.println("[NPC] Exit duration: " + exitDuration);
    }

    public void showDialogue() {
        lines = resolveDialogueLines();
        lineIndex = 0;
        visibleChars = 0;
        typeTimer = 0f;
        stateTimer = 0f;
        animTime = 0f;
        showing = true;
        setVisible(true);
        toFront();

        state = State.ENTER;
        System.out.println("[NPC] Dialogue started, state=ENTER, lines=" + lines.size());
    }

    public boolean isShowing() {
        return showing;
    }

    private List<String> resolveDialogueLines() {
        List<String> result = new ArrayList<>();

        StageConfig sc = null;
        if (AppStatus.currentChapterName != null) {
            try {
                sc = ChapterLibrary.getStageConfig(
                    AppStatus.currentChapterName,
                    AppStatus.currentStageNumber
                );
            } catch (Exception ignored) {
            }
        }

        if (sc != null) {
            appendStageLines(sc, result);
        } else {
            appendMinigameLines(AppStatus.currentMenuType, result);
        }

        result.add("Defend! Don't let the zombies cross the line!");
        result.add(SHOUT_LINE_TEXT);
        return result;
    }

    private void appendStageLines(StageConfig sc, List<String> result) {
        String special = sc.getSpecialLevel();
        String type = sc.getType();

        if (special != null && !special.isBlank()) {
            switch (special.toUpperCase().replace('-', '_')) {
                case "NIGHT_OPS" -> result.add(
                    "Night Ops! No sun will fall from the sky. Use sun producers wisely.");
                case "LOVE_YOUR_PLANTS" -> result.add(
                    "Love Your Plants! Do not lose more than " + sc.getMaxPlantDeaths() + " plants.");
                case "TIMED_WAR" -> result.add(
                    "Timed War! Survive until the timer runs out.");
                case "DEAD_LINE", "DEADLINE" -> result.add(
                    "Deadline! Do not let the zombies cross the red line!");
                case "CONVEYOR_BELT" -> result.add(
                    "Conveyor Belt! Seeds arrive automatically. Use what the belt gives you.");
                case "LOCKED_PLANTS" -> result.add(
                    "Locked Plants! Some plants are unavailable in this level.");
                default -> result.add(
                    "Special level: " + special.replace('_', ' ') + ". Complete the objective!");
            }
            return;
        }

        if (type != null && !type.isBlank()) {
            String upper = type.toUpperCase();
            if (upper.contains("CONVEYOR_BELT")) {
                result.add("Conveyor Belt! Seeds arrive automatically. Use what the belt gives you.");
                return;
            }
            if (upper.contains("LOCKED_PLANTS")) {
                result.add("Locked Plants! Some plants are unavailable in this level.");
                return;
            }
        }

        ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
        if (chapter != null) {
            switch (chapter) {
                case ANCIENT_EGYPT -> result.add(
                    "Ancient Egypt! Beware of mummies, tombstones, and Ra stealing your sun.");
                case FROSTBITE_CAVES -> result.add(
                    "Frostbite Caves! Plants can freeze here. Fire plants can melt the ice.");
                case BIG_WAVE_BEACH -> result.add(
                    "Big Wave Beach! The tide rises. Use Lily Pads and watch for snorkel zombies.");
                case DARK_AGES -> result.add(
                    "Dark Ages! Necromancy tiles can summon zombies. Destroy them quickly!");
            }
        } else {
            result.add("Defend your house from the zombie attack!");
        }
    }

    private void appendMinigameLines(MenuType menuType, List<String> result) {
        if (menuType == null) {
            result.add("Welcome to the minigame! Follow the objective shown on screen.");
            return;
        }

        switch (menuType) {
            case VASEBREAKER -> result.add(
                "Vasebreaker! Break the vases and reveal plants or zombies.");
            case WALLNUT_BOWLING -> result.add(
                "Wallnut Bowling! Launch nuts before the red line and clear the lane.");
            case I_ZOMBIE -> result.add(
                "I, Zombie! Deploy zombies to reach the brains. Spend sun wisely.");
            case BEGHOULED -> result.add(
                "Beghouled! Swap plants to make matches and upgrade your defenses.");
            case ZOMBOTANY -> result.add(
                "Zombotany! The zombies are half-plant. Place plants and survive the waves.");
            default -> result.add(
                "Welcome to the minigame! Follow the objective shown on screen.");
        }
    }

    private void beginLine() {
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            state = State.EXIT;
            stateTimer = 0f;
            animTime = 0f;
            return;
        }

        visibleChars = 0;
        typeTimer = 0f;
        stateTimer = 0f;
        animTime = 0f;

        if (lines.get(lineIndex).equals(SHOUT_LINE_TEXT)) {
            state = State.SHOUT;
            SoundManager.getInstance().playSFX("ui/SFX/haaaa.wav");
            // متن شات از ابتدا کامل نمایش داده می‌شود
            visibleChars = lines.get(lineIndex).length();
        } else {
            state = State.TALK;
        }
        System.out.println("[NPC] beginLine index=" + lineIndex + " state=" + state);
    }

    private void advanceDialogue() {
        if (!showing) {
            return;
        }

        if (state == State.TALK) {
            String line = lines.get(lineIndex);
            if (visibleChars < line.length()) {
                visibleChars = line.length();
                state = State.WAIT;
                stateTimer = 0f;
            }
            return;
        }

        if (state == State.WAIT) {
            lineIndex++;
            if (lineIndex >= lines.size()) {
                state = State.EXIT;
                stateTimer = 0f;
                animTime = 0f;
            } else {
                beginLine();
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!showing) {
            return;
        }

        animTime += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            advanceDialogue();
        }

        switch (state) {
            case ENTER -> {
                stateTimer += delta;
                if (stateTimer >= ENTER_DURATION) {
                    if (lines.isEmpty()) {
                        state = State.EXIT;
                        stateTimer = 0f;
                        animTime = 0f;
                    } else {
                        lineIndex = 0;
                        beginLine();
                    }
                }
            }
            case TALK -> {
                typeTimer += delta;
                visibleChars = Math.min(lines.get(lineIndex).length(),
                    (int) (typeTimer * CHARS_PER_SECOND));

                if (visibleChars >= lines.get(lineIndex).length()) {
                    state = State.WAIT;
                    stateTimer = 0f;
                    System.out.println("[NPC] Typing complete, WAIT");
                }
            }
            case WAIT -> {
                // منتظر کلیک / Enter
            }
            case SHOUT -> {
                stateTimer += delta;
                if (stateTimer >= SHOUT_DURATION) {
                    state = State.EXIT;
                    stateTimer = 0f;
                    animTime = 0f;
                }
            }
            case EXIT -> {
                stateTimer += delta;
                // جلوگیری از پرش دوباره انیمیشن خروج
                animTime = Math.min(animTime, exitDuration);
                if (stateTimer >= exitDuration) {
                    showing = false;
                    setVisible(false);
                    state = State.HIDDEN;
                    System.out.println("[NPC] Overlay hidden");
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!showing) {
            return;
        }

        super.draw(batch, parentAlpha);

        float npcX = 250f;
        float npcY = 200f;
        drawNpc((SpriteBatch) batch, npcX, npcY);

        if (state == State.TALK || state == State.WAIT || state == State.SHOUT) {
            drawBubble((SpriteBatch) batch, parentAlpha);
        }

        batch.setColor(Color.WHITE);
    }

    private void drawNpc(SpriteBatch batch, float x, float y) {
        String desiredClip = getDesiredClipForState();
        String actualClip = resolvedClips.getOrDefault(desiredClip, desiredClip);

        // در حالت خروج، مطمئن شویم که زمان انیمیشن از مدت کلیپ بیشتر نشود
        float currentAnimTime = animTime;
        if (state == State.EXIT) {
            currentAnimTime = Math.min(animTime, exitDuration);
        }

        float scale = NPC_DRAW_SIZE / NPC_NATIVE_CANVAS;
        float cx = x + NPC_DRAW_SIZE / 2f;
        float cy = y + NPC_DRAW_SIZE / 2f;

        Matrix4 old = batch.getTransformMatrix().cpy();
        Matrix4 scaled = old.cpy()
            .translate(cx, cy, 0f)
            .scale(scale, scale, 1f)
            .translate(-cx, -cy, 0f);
        batch.setTransformMatrix(scaled);

        EntityRenderer.getInstance().renderPam(
            batch,
            NPC_PAM,
            actualClip,
            currentAnimTime,
            cx,
            cy
        );

        batch.setTransformMatrix(old);
    }

    private String getDesiredClipForState() {
        return switch (state) {
            case ENTER -> "enter";
            case TALK -> "talk";
            case WAIT -> "idle";
            case SHOUT -> "shout";
            case EXIT -> "exit";
            default -> "idle";
        };
    }

    private void drawBubble(SpriteBatch batch, float parentAlpha) {
        String fullLine = lines.get(lineIndex);
        String shown = fullLine.substring(0, Math.min(visibleChars, fullLine.length()));

        float bubbleX = 430f;
        float bubbleY = 410f;
        float bubbleW = 1200f;
        float pad = 40f;

        layout.setText(font, fullLine, Color.WHITE, bubbleW - pad * 2f, Align.left, true);
        float bubbleH = Math.max(130f, layout.height + pad * 2f);

        batch.setColor(1f, 1f, 1f, 0.95f * parentAlpha);
        if (bubbleBackground != null) {
            bubbleBackground.draw(batch, bubbleX, bubbleY, bubbleW, bubbleH);
        } else {
            batch.setColor(0f, 0f, 0f, 0.75f * parentAlpha);
            batch.draw(pixel, bubbleX, bubbleY, bubbleW, bubbleH);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, shown, bubbleX + pad, bubbleY + bubbleH - pad,
            bubbleW - pad * 2f, Align.left, true);
        font.setColor(Color.WHITE);
    }

    private Drawable resolveBubbleBackground() {
        try {
            if (PvzSkin.get() != null
                && PvzSkin.get().has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return PvzSkin.get().getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Texture createPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
