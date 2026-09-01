package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.view.screen.manager.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.view.screen.ui.MenuSlider;
import com.PVZ.view.screen.ui.SliderBinding;
import com.PVZ.view.screen.ui.ToggleBinding;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SettingsPanel extends BasePanel {

    // اندازه‌های دکمه‌های معمولی
    private static final float BUTTON_HEIGHT = 80f;
    private static final float HORIZONTAL_PADDING = 40f;

    // اندازه‌های عنوان (خیلی بزرگ‌تر)
    private static final float TITLE_HEIGHT = 120f;
    private static final float TITLE_HORIZONTAL_PADDING = 60f;

    private MenuSlider musicSlider;
    private MenuSlider sfxSlider;
    private MenuSlider brightnessSlider;

    private MenuButton gameSpeedButton;
    private MenuButton difficultyButton;
    private MenuButton gridButton;
    private MenuButton debugButton;
    private MenuButton resetSoundsButton;
    private MenuButton backButton;

    private boolean toPauseMenu;

    public SettingsPanel() {
        this(false);
    }

    public SettingsPanel(boolean toPauseMenu) {
        this.toPauseMenu = toPauseMenu;
        setFillParent(true);
        align(Align.center);

        // ---------- منابع مشترک ----------
        Texture purpleUpTex   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDownTex = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture markerTex     = new Texture(Gdx.files.internal("global/button_marker.png"));
        BitmapFont buttonFont = PvzSkin.get().getFont("FBUSV8C5EI_1_outline");

        Texture soundOnTex  = new Texture(Gdx.files.internal("global/sound/sound_on.png"));
        Texture soundOffTex = new Texture(Gdx.files.internal("global/sound/sound_off.png"));

        Drawable track = PvzSkin.get().getDrawable("image_ui_almanac_plants_plant_fuelbar_10");
        Drawable fill  = PvzSkin.get().getDrawable("image_ui_almanac_general_fuelbar_fill_10");
        Drawable knob  = PvzSkin.get().getDrawable("image_ui_generic_navdot");

        // ---------- عنوان بزرگ ----------
        MenuButton title = createTitleButton("SETTINGS", purpleUpTex, purpleDownTex, buttonFont, markerTex);
        title.setDisabled(true);
        add(title).padBottom(50f).row();

        // ---------- اسلایدرها (بزرگ‌تر) ----------
        musicSlider = new MenuSlider("Music Volume",
            PvzSkin.get().getFont("FBUSV8C5EI_2"),
            track, fill, knob,
            markerTex,
            true, soundOnTex, soundOffTex,
            new SliderBinding() {
                @Override public int get() { return (int) (MusicManager.getInstance().getVolume() * 100); }
                @Override public void set(int value) { MusicManager.getInstance().setVolume(value / 100f); }
            },
            new ToggleBinding() {
                @Override public boolean get() { return MusicManager.getInstance().getMute(); }
                @Override public void set(boolean value) { MusicManager.getInstance().setMuted(value); }
            });
        musicSlider.setSize(600f, 130f);
        add(musicSlider).padBottom(15).row();

        sfxSlider = new MenuSlider("SFX Volume",
            PvzSkin.get().getFont("FBUSV8C5EI_2"),
            track, fill, knob,
            markerTex,
            true, soundOnTex, soundOffTex,
            new SliderBinding() {
                @Override public int get() { return (int) (SoundManager.getInstance().getVolume() * 100); }
                @Override public void set(int value) { SoundManager.getInstance().setVolume(value / 100f); }
            },
            new ToggleBinding() {
                @Override public boolean get() { return SoundManager.getInstance().getMute(); }
                @Override public void set(boolean value) { SoundManager.getInstance().setMuted(value); }
            });
        sfxSlider.setSize(600f, 130f);
        add(sfxSlider).padBottom(15).row();

        brightnessSlider = new MenuSlider("Brightness",
            PvzSkin.get().getFont("FBUSV8C5EI_2"),
            track, fill, knob,
            markerTex,
            false, null, null,
            new SliderBinding() {
                @Override public int get() {
                    return (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f); }
                @Override public void set(int value) {
                    BrightnessController.getInstance().setBrightness((value / 50f) - 1f); }
            },
            null);
        brightnessSlider.setSize(600f, 130f);
        add(brightnessSlider).padBottom(30).row();

        // ---------- دکمه‌های معمولی (همان‌ها) ----------
        gameSpeedButton = createButton("Game Speed: " + AppStatus.getGameSpeed(),
            this::onGameSpeedChange, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(gameSpeedButton).padBottom(15).row();

        difficultyButton = createButton("Difficulty: " + AppStatus.getDifficulty().name(),
            this::onDifficultyChange, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(difficultyButton).padBottom(15).row();

        gridButton = createButton("Show Grid: " + (AppStatus.tileDebugEnabled ? "ON" : "OFF"),
            this::onGridToggle, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(gridButton).padBottom(15).row();

        debugButton = createButton("Debug Mode: " + (AppStatus.isDebugMode() ? "ON" : "OFF"),
            this::onDebugToggle, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(debugButton).padBottom(15).row();

        resetSoundsButton = createButton("Reset Sounds",
            this::onResetSounds, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(resetSoundsButton).padBottom(15).row();

        backButton = createButton("Back",
            this::onBack, purpleUpTex, purpleDownTex, buttonFont, markerTex);
        add(backButton).row();
    }

    /** دکمه‌ای با اندازهٔ بزرگ‌تر برای عنوان */
    private MenuButton createTitleButton(String text,
                                         Texture upTex, Texture downTex,
                                         BitmapFont font, Texture marker) {
        MenuButton btn = new MenuButton(upTex, text, font, downTex, null, marker, () -> {});
        float textWidth = btn.getTextWidth();
        float desiredWidth = textWidth + TITLE_HORIZONTAL_PADDING * 2;
        btn.setSize(desiredWidth, TITLE_HEIGHT);
        return btn;
    }

    /** دکمه‌ای با اندازهٔ معمولی */
    private MenuButton createButton(String text, Runnable action,
                                    Texture upTex, Texture downTex,
                                    BitmapFont font, Texture marker) {
        MenuButton btn = new MenuButton(upTex, text, font, downTex, null, marker, action);
        float textWidth = btn.getTextWidth();
        float desiredWidth = textWidth + HORIZONTAL_PADDING * 2;
        btn.setSize(desiredWidth, BUTTON_HEIGHT);
        return btn;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refreshUI();
    }

    private void refreshUI() {
        gameSpeedButton.setText("Game Speed: " + AppStatus.getGameSpeed());
        difficultyButton.setText("Difficulty: " + AppStatus.getDifficulty().name());
        gridButton.setText("Show Grid: " + (AppStatus.tileDebugEnabled ? "ON" : "OFF"));
        debugButton.setText("Debug Mode: " + (AppStatus.isDebugMode() ? "ON" : "OFF"));

        int musicVol = (int) (MusicManager.getInstance().getVolume() * 100);
        if (musicVol != musicSlider.getValue()) musicSlider.setValue(musicVol);

        int sfxVol = (int) (SoundManager.getInstance().getVolume() * 100);
        if (sfxVol != sfxSlider.getValue()) sfxSlider.setValue(sfxVol);

        int brightness = (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f);
        if (brightness != brightnessSlider.getValue()) brightnessSlider.setValue(brightness);
    }

    private void onGameSpeedChange() {
        int current = AppStatus.getGameSpeed();
        int next = (current % 3) + 1;
        AppStatus.setGameSpeed(next);
    }

    private void onDifficultyChange() {
        AppStatus.Difficulty current = AppStatus.getDifficulty();
        AppStatus.Difficulty next;
        switch (current) {
            case EASY:   next = AppStatus.Difficulty.NORMAL; break;
            case NORMAL: next = AppStatus.Difficulty.HARD;   break;
            default:     next = AppStatus.Difficulty.EASY;   break;
        }
        AppStatus.setDifficulty(next);
    }

    private void onGridToggle() { AppStatus.tileDebugEnabled = !AppStatus.tileDebugEnabled; }
    private void onDebugToggle() { AppStatus.setDebugMode(!AppStatus.isDebugMode()); }

    private void onResetSounds() {
        MusicManager.getInstance().setVolume(0.5f);
        SoundManager.getInstance().setVolume(0.5f);
        BrightnessController.getInstance().setBrightness(0f);
    }

    private void onBack() {
        PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
