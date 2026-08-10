package com.PVZ.model.game;

import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.SoundManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.view.screen.ui.MenuSlider;
import com.PVZ.view.screen.ui.SliderBinding;
import com.PVZ.view.screen.ui.ToggleBinding;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

public class PauseMenuOverlay extends Table {
    public interface ExitHandler {
        void onSaveAndExit();
    }
    public interface RestartHandler {
        void onRestart();
    }

    private boolean paused = false;
    private final Label missionLabelRef;

    public PauseMenuOverlay(ExitHandler exitHandler, RestartHandler restartHandler) {
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        setBackground(new NinePatchDrawable(dimNinePatch()));

        Table dialog = new Table();
        dialog.pad(30f);
        dialog.setBackground(resolveDialogBackground());

        Label title = new Label("GAME PAUSED", new Label.LabelStyle(font, Color.WHITE));
        title.setFontScale(1.6f);

        Label missionLabel = new Label("", new Label.LabelStyle(font, Color.LIGHT_GRAY));
        missionLabel.setWrap(true);
        missionLabel.setAlignment(Align.center);
        this.missionLabelRef = missionLabel;

        // اسلایدرهای سفارشی
        Table sliders = buildCustomSliders(font);

        // دکمه‌ها با MenuButton (مانند قبل)
        Drawable brownUp   = PvzSkin.get().getDrawable("image_ui_generic_brownbutton_10");
        Drawable brownDown = PvzSkin.get().getDrawable("image_ui_generic_brownbutton_down_10");
        Drawable greenUp   = PvzSkin.get().getDrawable("image_ui_generic_greenbutton_10");
        Drawable greenDown = PvzSkin.get().getDrawable("image_ui_generic_greenbutton_down_10");
        Drawable purpleUp   = PvzSkin.get().getDrawable("image_ui_generic_purplebutton_10");
        Drawable purpleDown = PvzSkin.get().getDrawable("image_ui_generic_purplebutton_down_10");

        MenuButton saveAndExit = new MenuButton(brownUp, "SAVE AND EXIT", font,
            brownDown, null, null, () -> {
            hide();
            if (exitHandler != null) exitHandler.onSaveAndExit();
        });
        saveAndExit.setSize(260f, 64f);

        MenuButton restart = new MenuButton(greenUp, "RESTART", font,
            greenDown, null, null, () -> {
            hide();
            if (restartHandler != null) restartHandler.onRestart();
        });
        restart.setSize(200f, 64f);

        MenuButton resume = new MenuButton(purpleUp, "RESUME", font,
            purpleDown, null, null, this::hide);
        resume.setSize(200f, 64f);

        Table buttonRow = new Table();
        buttonRow.add(saveAndExit).size(260f, 64f).padRight(16f);
        buttonRow.add(restart).size(200f, 64f).padRight(16f);
        buttonRow.add(resume).size(200f, 64f);

        dialog.add(title).padBottom(18f).row();
        dialog.add(missionLabel).width(700f).padBottom(14f).row();
        dialog.add(sliders).width(700f).padBottom(20f).row();
        dialog.add(buttonRow).row();

        add(dialog);
    }

    public void setMissionText(String text) {
        missionLabelRef.setText(text == null ? "" : text);
    }

    // ---------- اسلایدرهای سفارشی ----------
    private Table buildCustomSliders(BitmapFont font) {
        Table table = new Table();
        Skin skin = PvzSkin.get();

        // Drawable‌های اسلایدر از PvzSkin
        Drawable track = skin.getDrawable("image_ui_almanac_plants_plant_fuelbar_10");
        Drawable fill  = skin.getDrawable("image_ui_almanac_general_fuelbar_fill_10");
        Drawable knob  = skin.getDrawable("image_ui_generic_navdot");

        // اسلایدر موسیقی
        Label musicLabel = new Label("Music", new Label.LabelStyle(font, Color.WHITE));
        MenuSlider musicSlider = new MenuSlider(
            "", font,                // بدون برچسب (خودمان جداگانه داریم)
            track, fill, knob,
            null,                    // markerTexture (null = بدون مارکر)
            false, null, null,       // بدون آیکون قطع/وصل
            new SliderBinding() {
                @Override
                public int get() {
                    return (int)(MusicManager.getInstance().getVolume() * 100);
                }
                @Override
                public void set(int value) {
                    MusicManager.getInstance().setVolume(value / 100f);
                }
            },
            null                     // ToggleBinding null
        );
        musicSlider.setSize(420f, 40f);  // ارتفاع کمتر از پیش‌فرض

        // اسلایدر افکت‌ها
        Label soundLabel = new Label("Sound FX", new Label.LabelStyle(font, Color.WHITE));
        MenuSlider soundSlider = new MenuSlider(
            "", font,
            track, fill, knob,
            null,
            false, null, null,
            new SliderBinding() {
                @Override
                public int get() {
                    return (int)(SoundManager.getInstance().getVolume() * 100);
                }
                @Override
                public void set(int value) {
                    SoundManager.getInstance().setVolume(value / 100f);
                }
            },
            null
        );
        soundSlider.setSize(420f, 40f);

        // چیدمان مشابه قبل
        table.add(musicLabel).left().padRight(12f);
        table.add(musicSlider).size(420f, 40f).growX().row();
        table.add(soundLabel).left().padRight(12f).padTop(8f);
        table.add(soundSlider).size(420f, 40f).growX().padTop(8f).row();

        return table;
    }

    // ---------- باقی متدها بدون تغییر ----------
    private Drawable resolveDialogBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {}
        return new NinePatchDrawable(dimNinePatch());
    }

    private static com.badlogic.gdx.graphics.g2d.NinePatch dimNinePatch() {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.55f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.graphics.g2d.NinePatch(texture, 1, 1, 1, 1);
    }

    public void show() {
        paused = true;
        setVisible(true);
        setTouchable(Touchable.enabled);
    }

    public void hide() {
        paused = false;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public void toggle() {
        if (paused) hide(); else show();
    }

    public boolean isPaused() {
        return paused;
    }
}
