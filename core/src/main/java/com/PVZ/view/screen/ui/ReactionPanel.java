package com.PVZ.view.screen.ui;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.game.reaction.ReactionCatalog;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.function.Consumer;

/** Collapsed Clash-like reaction button with an animated reaction picker. */
public final class ReactionPanel extends Table {
    private final Consumer<String> onReactionSelected;
    private Table picker;
    private boolean open;

    public ReactionPanel(Consumer<String> onReactionSelected) {
        this.onReactionSelected = onReactionSelected;
        setFillParent(true);
        bottom().padBottom(92f);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);

        Table root = new Table();
        TextButton trigger = buildTrigger();
        root.add(trigger).size(185f, 60f);
        add(root);

        picker = buildPicker();
        picker.setVisible(false);
        picker.setOrigin(Align.bottom);
        picker.setScale(0.85f);
        picker.getColor().a = 0f;
        picker.pack();
        addActor(picker);
    }

    @Override
    public void layout() {
        super.layout();
        // Keep the picker centered above the (now bottom-center) trigger button.
        float x = (getWidth() - picker.getWidth()) / 2f;
        float y = 92f + 60f + 12f;
        picker.setPosition(x, y);
    }

    private TextButton buildTrigger() {
        Skin skin = PvzSkin.get();
        Drawable up = safeDrawable(skin, "image_ui_generic_brownbutton_10");
        Drawable down = safeDrawable(skin, "image_ui_generic_brownbutton_down_10");
        if (up == null) {
            up = safeDrawable(skin, "image_ui_generic_purplebutton_10");
            down = safeDrawable(skin, "image_ui_generic_purplebutton_down_10");
        }
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = up;
        style.down = down != null ? down : up;
        style.font = FontManager.getInstance().getEnglishMenuFont();
        style.fontColor = Color.WHITE;

        TextButton button = new TextButton("REACTIONS  +", style);
        button.getLabel().setFontScale(0.58f);
        button.getLabel().setAlignment(Align.center);
        button.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { togglePicker(); }
        });
        return button;
    }

    private Table buildPicker() {
        Skin skin = PvzSkin.get();
        Table outer = new Table();
        Drawable bg = safeDrawable(skin, "image_ui_generic_tooltip_bg");
        if (bg != null) outer.setBackground(bg);
        outer.pad(14f);

        Label title = new Label("PICK A REACTION", new Label.LabelStyle(
                FontManager.getInstance().getEnglishMenuFont(), Color.GOLD));
        title.setFontScale(0.58f);
        title.setAlignment(Align.center);
        outer.add(title).colspan(4).center().padBottom(8f).row();

        int col = 0;
        for (ReactionCatalog.Reaction reaction : ReactionCatalog.ALL) {
            outer.add(buildReactionButton(reaction)).size(150f, 54f).pad(3f);
            col++;
            if (col == 4) {
                outer.row();
                col = 0;
            }
        }
        if (col != 0) outer.row();

        TextButton close = new TextButton("CLOSE", plainStyle(skin));
        close.getLabel().setFontScale(0.45f);
        close.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { closePicker(); }
        });
        outer.add(close).colspan(4).size(110f, 40f).padTop(7f).center();
        return outer;
    }

    private TextButton buildReactionButton(ReactionCatalog.Reaction reaction) {
        Skin skin = PvzSkin.get();
        Drawable up;
        Drawable down;
        if (reaction.kind() == ReactionCatalog.Kind.EMOJI) {
            up = safeDrawable(skin, "image_ui_generic_greenbutton_10");
            down = safeDrawable(skin, "image_ui_generic_greenbutton_down_10");
        } else if (reaction.kind() == ReactionCatalog.Kind.STICKER) {
            up = safeDrawable(skin, "image_ui_generic_brownbutton_10");
            down = safeDrawable(skin, "image_ui_generic_brownbutton_down_10");
        } else {
            up = safeDrawable(skin, "image_ui_generic_purplebutton_10");
            down = safeDrawable(skin, "image_ui_generic_purplebutton_down_10");
        }
        if (up == null) up = safeDrawable(skin, "image_ui_generic_purplebutton_10");

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = up;
        style.down = down != null ? down : up;
        style.font = FontManager.getInstance().getEnglishMenuFont();
        style.fontColor = reaction.kind() == ReactionCatalog.Kind.STICKER ? Color.GOLD : Color.WHITE;

        String prefix = reaction.kind() == ReactionCatalog.Kind.STICKER ? "★ " :
                reaction.kind() == ReactionCatalog.Kind.EMOJI ? "✦ " : "";
        TextButton button = new TextButton(prefix + reaction.label(), style);
        button.getLabel().setFontScale(0.48f);
        button.getLabel().setWrap(true);
        button.getLabel().setAlignment(Align.center);
        button.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                onReactionSelected.accept(reaction.id());
                closePicker();
            }
        });
        return button;
    }

    private TextButton.TextButtonStyle plainStyle(Skin skin) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        Drawable up = safeDrawable(skin, "image_ui_generic_purplebutton_10");
        Drawable down = safeDrawable(skin, "image_ui_generic_purplebutton_down_10");
        style.up = up;
        style.down = down != null ? down : up;
        style.font = FontManager.getInstance().getEnglishMenuFont();
        return style;
    }

    private void togglePicker() { if (open) closePicker(); else openPicker(); }

    private void openPicker() {
        open = true;
        picker.setVisible(true);
        picker.clearActions();
        picker.addAction(Actions.parallel(
                Actions.fadeIn(0.16f),
                Actions.scaleTo(1f, 1f, 0.18f)
        ));
    }

    private void closePicker() {
        if (!open) return;
        open = false;
        picker.clearActions();
        picker.addAction(Actions.sequence(
                Actions.parallel(Actions.fadeOut(0.12f), Actions.scaleTo(0.92f, 0.92f, 0.12f)),
                Actions.hide()
        ));
    }

    private Drawable safeDrawable(Skin skin, String key) {
        try { return skin != null ? skin.getDrawable(key) : null; }
        catch (Exception e) { return null; }
    }

    @SuppressWarnings("unused")
    private Drawable loadIcon(String plantTypeName) {
        if (plantTypeName == null) return null;
        try {
            Texture tex = new Texture(Gdx.files.internal(PlantTexturePaths.getPath(plantTypeName)));
            return new TextureRegionDrawable(tex);
        } catch (Exception e) { return null; }
    }
}
