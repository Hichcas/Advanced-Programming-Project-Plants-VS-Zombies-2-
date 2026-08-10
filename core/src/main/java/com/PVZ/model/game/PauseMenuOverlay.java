package com.PVZ.model.game;

import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.SoundManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

        Table sliders = buildSliders(font);

        TextButton saveAndExit = buildButton("SAVE AND EXIT", "brown", font, () -> {
            hide();
            if (exitHandler != null) {
                exitHandler.onSaveAndExit();
            }
        });
        TextButton restart = buildButton("RESTART", "green", font, () -> {
            hide();
            if (restartHandler != null) {
                restartHandler.onRestart();
            }
        });
        TextButton resume = buildButton("RESUME", "purple", font, this::hide);

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

    private final Label missionLabelRef;

    public void setMissionText(String text) {
        missionLabelRef.setText(text == null ? "" : text);
    }

    private Table buildSliders(BitmapFont font) {
        Table table = new Table();

        Label musicLabel = new Label("Music", new Label.LabelStyle(font, Color.WHITE));
        Slider musicSlider = buildSlider();
        musicSlider.setValue(MusicManager.getInstance().getVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                MusicManager.getInstance().setVolume(musicSlider.getValue());
            }
        });

        Label soundLabel = new Label("Sound FX", new Label.LabelStyle(font, Color.WHITE));
        Slider soundSlider = buildSlider();
        soundSlider.setValue(SoundManager.getInstance().getVolume());
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.getInstance().setVolume(soundSlider.getValue());
            }
        });

        table.add(musicLabel).left().padRight(12f);
        table.add(musicSlider).width(420f).growX().row();
        table.add(soundLabel).left().padRight(12f).padTop(8f);
        table.add(soundSlider).width(420f).growX().padTop(8f).row();
        return table;
    }

    private Slider buildSlider() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("default-horizontal", Slider.SliderStyle.class)) {
                return new Slider(0f, 1f, 0.01f, false, skin, "default-horizontal");
            }
        } catch (Exception ignored) {
        }
        return new Slider(0f, 1f, 0.01f, false, new Slider.SliderStyle());
    }

    private TextButton buildButton(String text, String styleName, BitmapFont fallbackFont, Runnable action) {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has(styleName, TextButton.TextButtonStyle.class)) {
                TextButton button = new TextButton(text, skin, styleName);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        action.run();
                    }
                });
                return button;
            }
        } catch (Exception ignored) {
        }
        TextButton.TextButtonStyle fallbackStyle = new TextButton.TextButtonStyle();
        fallbackStyle.font = fallbackFont;
        fallbackStyle.fontColor = Color.WHITE;
        TextButton fallback = new TextButton(text, fallbackStyle);
        fallback.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return fallback;
    }

    private Drawable resolveDialogBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
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
        if (paused) {
            hide();
        } else {
            show();
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
