package com.PVZ.view.screen.ui;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.game.reaction.ReactionCatalog;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.function.Consumer;

/**
 * پنل انتخاب واکنش در حین بازی دونفره‌ی «من، زامبی» (فاز شبکه) - دقیقا مثل
 * سیستم ری‌اکشن کلش‌آف‌کلنز بین دو بازیکن: سه پیام متنی، سه ایموجی (با آیکون
 * گیاه چون اسکین گلیف ایموجی ندارد) و چند استیکر متحرک واقعی از pam_animations.json.
 *
 * فقط وقتی موتور بازی از نوع {@code IZombieMultiplayerGameEngine} باشد به
 * Stage اضافه می‌شود (نگاه کنید به GameScreen). با کلیک روی هر دکمه،
 * {@code onReactionSelected} صدا زده می‌شود که به {@code engine.sendReaction(id)}
 * وصل است.
 */
public final class ReactionPanel extends Table {

    public ReactionPanel(Consumer<String> onReactionSelected) {
        setFillParent(true);
        bottom().padBottom(90f);

        Table row = new Table();
        row.defaults().pad(4f);

        for (ReactionCatalog.Reaction r : ReactionCatalog.TEXT_MESSAGES) {
            row.add(buildTextButton(r, onReactionSelected)).size(150f, 46f);
        }
        for (ReactionCatalog.Reaction r : ReactionCatalog.EMOJIS) {
            row.add(buildEmojiButton(r, onReactionSelected)).size(56f, 56f);
        }
        for (ReactionCatalog.Reaction r : ReactionCatalog.STICKERS) {
            row.add(buildStickerButton(r, onReactionSelected)).size(118f, 46f);
        }

        add(row);
    }

    private TextButton buildTextButton(ReactionCatalog.Reaction reaction, Consumer<String> onSelected) {
        Skin skin = PvzSkin.get();
        Drawable up = safeDrawable(skin, "image_ui_generic_purplebutton_10");
        Drawable down = safeDrawable(skin, "image_ui_generic_purplebutton_down_10");

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = up;
        style.down = down != null ? down : up;
        style.font = com.PVZ.view.screen.manager.FontManager.getInstance().getEnglishMenuFont();

        TextButton button = new TextButton(reaction.label(), style);
        button.getLabel().setFontScale(0.55f);
        button.getLabel().setAlignment(Align.center);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSelected.accept(reaction.id());
            }
        });
        return button;
    }

    private ImageButton buildEmojiButton(ReactionCatalog.Reaction reaction, Consumer<String> onSelected) {
        Skin skin = PvzSkin.get();
        Drawable up = safeDrawable(skin, "image_ui_generic_greenbutton_10");
        Drawable down = safeDrawable(skin, "image_ui_generic_greenbutton_down_10");
        Drawable icon = loadIcon(reaction.plantIconType());

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = up;
        style.down = down != null ? down : up;
        style.imageUp = icon;

        ImageButton button = new ImageButton(style);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSelected.accept(reaction.id());
            }
        });
        return button;
    }

    /**
     * دکمه‌ی استیکر متحرک - از پوسته‌ی قهوه‌ای/طلایی برای متمایز شدن از پیام‌های
     * متنی (بنفش) و ایموجی‌ها (سبز) استفاده می‌کند، به‌علاوه‌ی گلیف ستاره تا حس
     * «چیز خاص و متحرک» را قبل از کلیک هم منتقل کند.
     */
    private TextButton buildStickerButton(ReactionCatalog.Reaction reaction, Consumer<String> onSelected) {
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
        style.font = com.PVZ.view.screen.manager.FontManager.getInstance().getEnglishMenuFont();
        style.fontColor = Color.GOLD;

        TextButton button = new TextButton("* " + reaction.label() + " *", style);
        button.getLabel().setFontScale(0.5f);
        button.getLabel().setAlignment(Align.center);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSelected.accept(reaction.id());
            }
        });
        return button;
    }

    private Drawable safeDrawable(Skin skin, String key) {
        try {
            return skin != null ? skin.getDrawable(key) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable loadIcon(String plantTypeName) {
        if (plantTypeName == null) return null;
        try {
            String path = PlantTexturePaths.getPath(plantTypeName);
            Texture tex = new Texture(Gdx.files.internal(path));
            return new TextureRegionDrawable(tex);
        } catch (Exception e) {
            return null;
        }
    }
}
