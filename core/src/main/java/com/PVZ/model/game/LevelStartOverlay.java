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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

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

        // متن توضیحات بعضی مراحل (مثل «بازی امتیازی») از بقیه بلندتر است. برای اینکه
        // دکمه‌ی CONTINUE هیچ‌وقت خارج از صفحه یا زیر متن گم نشود، لیست توضیحات را
        // داخل یک ناحیه‌ی اسکرول‌شونده با ارتفاع محدود می‌گذاریم؛ خود دکمه همیشه
        // بیرون از این ناحیه و کاملا قابل‌کلیک باقی می‌ماند.
        Table objectivesTable = new Table();
        for (String objective : buildObjectives(config)) {
            Label line = new Label("- " + objective, new Label.LabelStyle(font, Color.LIGHT_GRAY));
            line.setFontScale(0.55f);
            line.setWrap(true);
            line.setAlignment(Align.left);
            objectivesTable.add(line).width(620f).left().padBottom(8f).row();
        }
        ScrollPane objectivesScroll = new ScrollPane(objectivesTable);
        objectivesScroll.setScrollingDisabled(true, false);
        objectivesScroll.setFadeScrollBars(false);
        dialog.add(objectivesScroll).width(650f).maxHeight(560f).padBottom(6f).row();

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

        add(dialog).maxHeight(1300f);
    }

    private static List<String> buildObjectives(StageConfig config) {
        return com.PVZ.model.game.chapter.StageRules.describe(config);
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
