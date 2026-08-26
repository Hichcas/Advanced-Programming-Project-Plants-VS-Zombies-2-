package com.PVZ.view.screen.ui;

import com.PVZ.model.game.reaction.ReactionCatalog;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

/** Short-lived, animated multiplayer reaction bubble. */
public final class ReactionBubble extends Table {
    private static final float LIFE = 2.25f;

    public ReactionBubble(ReactionCatalog.Reaction reaction, boolean mine, String senderName) {

        try {
            if (PvzSkin.get() != null) setBackground(PvzSkin.get().getDrawable("image_ui_generic_tooltip_bg"));
        } catch (Exception ignored) {}

        Color accent = mine ? Color.SKY : Color.SALMON;
        String who = mine ? "YOU" : (senderName == null || senderName.isBlank() ? "OPPONENT" : senderName.toUpperCase());
        Label whoLabel = new Label(who, new Label.LabelStyle(FontManager.getInstance().getEnglishMenuFont(), accent));
        whoLabel.setFontScale(0.40f);
        whoLabel.setAlignment(Align.center);
        add(whoLabel).colspan(1).center().row();

        String label = switch (reaction.kind()) {
            case EMOJI -> "✦ " + reaction.label() + " ✦";
            case STICKER -> "★ " + reaction.label() + " ★";
            case TEXT -> reaction.label();
        };
        Label reactionLabel = new Label(label, new Label.LabelStyle(
                FontManager.getInstance().getEnglishMenuFont(), Color.WHITE));
        reactionLabel.setFontScale(0.66f);
        reactionLabel.setAlignment(Align.center);
        reactionLabel.setWrap(true);
        add(reactionLabel).width(320f).center();

        setTouchable(Touchable.disabled);
        getColor().a = 0f;
        setTransform(true);
        setOrigin(Align.center);
        addAction(Actions.sequence(
                Actions.parallel(Actions.fadeIn(0.12f), Actions.scaleTo(1.06f, 1.06f, 0.12f)),
                Actions.scaleTo(1f, 1f, 0.12f),
                Actions.delay(LIFE),
                Actions.parallel(Actions.fadeOut(0.28f), Actions.scaleTo(0.94f, 0.94f, 0.28f)),
                Actions.removeActor()
        ));
    }
}
