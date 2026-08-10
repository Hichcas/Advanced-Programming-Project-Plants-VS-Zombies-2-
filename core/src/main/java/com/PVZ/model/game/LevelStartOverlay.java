package com.PVZ.model.game;

import com.PVZ.model.game.chapter.StageConfig;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.ui.MenuButton;
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

import java.util.ArrayList;
import java.util.List;

public class LevelStartOverlay extends Table {
    public interface ContinueHandler {
        void onContinue();
    }

    private boolean showing = false;

    public LevelStartOverlay(StageConfig config, ContinueHandler continueHandler) {
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        setBackground(new NinePatchDrawable(dimNinePatch()));

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        Table dialog = new Table();
        dialog.pad(30f);
        dialog.setBackground(resolveDialogBackground());

        Label title = new Label("Level Objectives", new Label.LabelStyle(font, Color.WHITE));
        title.setFontScale(1.4f);
        dialog.add(title).padBottom(18f).row();

        for (String objective : buildObjectives(config)) {
            Label line = new Label("- " + objective, new Label.LabelStyle(font, Color.LIGHT_GRAY));
            line.setWrap(true);
            line.setAlignment(Align.left);
            dialog.add(line).width(620f).left().padBottom(8f).row();
        }

        // --- دکمه CONTINUE با MenuButton سفارشی ---
        Drawable purpleUp = PvzSkin.get().getDrawable("image_ui_generic_purplebutton_10");
        Drawable purpleDown = PvzSkin.get().getDrawable("image_ui_generic_purplebutton_down_10");
        MenuButton continueButton = new MenuButton(purpleUp, "CONTINUE", font,
            purpleDown, null, null, () -> {
            hide();
            if (continueHandler != null) {
                continueHandler.onContinue();
            }
        });
        continueButton.setSize(220f, 60f);
        dialog.add(continueButton).size(220f, 60f).padTop(16f).row();

        add(dialog);
    }

    private static List<String> buildObjectives(StageConfig config) {
        List<String> objectives = new ArrayList<>();
        if (config == null) {
            objectives.add("Don't let the zombies reach your house!");
            return objectives;
        }

        String type = config.getType() == null ? "" : config.getType().toUpperCase();
        if (type.contains("DEADLINE")) {
            objectives.add("Don't let the zombies cross the marked line!");
        } else {
            objectives.add("Don't let the zombies reach your house!");
        }

        if (config.getMaxPlantDeaths() > 0) {
            objectives.add("Lose no more than " + config.getMaxPlantDeaths() + " plants!");
        }

        if (config.isDisableFallingSun()) {
            objectives.add("No sun will fall from the sky this level!");
        }

        if (config.getLockedPlants() != null && !config.getLockedPlants().isEmpty()) {
            objectives.add("These plants are locked for this level: "
                + String.join(", ", config.getLockedPlants()));
        }

        if (config.getSpecialLevel() != null && !config.getSpecialLevel().isBlank()) {
            objectives.add("Special level: " + config.getSpecialLevel());
        }

        return objectives;
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
        pixmap.setColor(0f, 0f, 0f, 0.6f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.graphics.g2d.NinePatch(texture, 1, 1, 1, 1);
    }

    public void show() {
        showing = true;
        setVisible(true);
        setTouchable(Touchable.enabled);
    }

    public void hide() {
        showing = false;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public boolean isShowing() {
        return showing;
    }
}
