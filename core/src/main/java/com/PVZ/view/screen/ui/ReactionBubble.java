package com.PVZ.view.screen.ui;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.game.reaction.ReactionCatalog;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

/**
 * حباب کوچکی که وقتی خودمان یا حریف یک واکنش می‌فرستیم، بالای صفحه ظاهر
 * می‌شود، کمی می‌ماند و بعد محو می‌شود (fade out). برای استیکرهای متحرک،
 * انیمیشن واقعی PAM هم روی خود زمین بازی پخش می‌شود (نگاه کنید به
 * {@code IZombieMultiplayerGameEngine#playStickerIfAny}); این حباب فقط
 * یک برچسب سریع برای اطلاع از این‌که «چه واکنشی و از طرف کی» است.
 */
public final class ReactionBubble extends Table {

    private static final float VISIBLE_SECONDS = 2.2f;
    private static final float FADE_SECONDS = 0.6f;

    public ReactionBubble(ReactionCatalog.Reaction reaction, boolean mine) {
        pad(10f, 16f, 10f, 16f);

        try {
            var skin = PvzSkin.get();
            if (skin != null) {
                setBackground(skin.getDrawable("image_ui_generic_tooltip_bg"));
            }
        } catch (Exception ignored) {
            // پس‌زمینه‌ی اختیاری - در نبودش هم حباب کار می‌کند، فقط بی‌قاب است.
        }

        Label who = new Label(mine ? "You" : "Opponent", new Label.LabelStyle(
            FontManager.getInstance().getEnglishMenuFont(), mine ? Color.SKY : Color.SALMON));
        who.setFontScale(0.5f);
        who.setAlignment(Align.center);
        add(who).row();

        if (reaction.kind() == ReactionCatalog.Kind.EMOJI && reaction.plantIconType() != null) {
            try {
                Texture tex = new Texture(Gdx.files.internal(PlantTexturePaths.getPath(reaction.plantIconType())));
                Image icon = new Image(tex);
                add(icon).size(48f, 48f).padTop(2f);
            } catch (Exception e) {
                addTextLabel(reaction, false);
            }
        } else {
            addTextLabel(reaction, reaction.kind() == ReactionCatalog.Kind.STICKER);
        }

        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        addAction(Actions.sequence(
            Actions.delay(VISIBLE_SECONDS),
            Actions.fadeOut(FADE_SECONDS),
            Actions.removeActor()
        ));
    }

    private void addTextLabel(ReactionCatalog.Reaction reaction, boolean isSticker) {
        Label.LabelStyle textStyle = new Label.LabelStyle(
            FontManager.getInstance().getEnglishMenuFont(), isSticker ? Color.GOLD : Color.WHITE);
        String text = isSticker ? ("* " + reaction.label() + " *") : reaction.label();
        Label textLabel = new Label(text, textStyle);
        textLabel.setFontScale(0.75f);
        textLabel.setAlignment(Align.center);
        add(textLabel).padTop(2f);
    }
}
