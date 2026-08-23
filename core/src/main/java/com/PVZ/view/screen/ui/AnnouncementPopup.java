package com.PVZ.view.screen.ui;

import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

/**
 * پاپ‌آپ اعلان‌های داخل بازی - جایگزین نسخه‌ی قبلی که فقط یک متن قرمز
 * وسط صفحه بدون هیچ پس‌زمینه‌ای بود.
 *
 * سبک این کلاس دقیقا از {@link UpgradeNotificationPopup} کپی شده (همون
 * اسکین، همون فونت‌ها، همون الگوی fade-in/delay/fade-out با Scene2D
 * Actions) تا تمام پاپ‌آپ‌های بازی ظاهر یکدست داشته باشند.
 *
 * برخلاف UpgradeNotificationPopup که فقط یک پاپ‌آپ در لحظه نشان می‌دهد،
 * این کلاس چند اعلان هم‌زمان را روی هم «استک» می‌کند - چیزی که برای
 * بازی امتیازی لازم است، چون یک کشتن می‌تواند چند بونوس هم‌زمان بدهد
 * (Quick Kill + Combo + Clutch در یک لحظه) و همه باید دیده شوند، نه
 * این‌که یکی جای دیگری را عوض کند.
 */
public final class AnnouncementPopup {

    private static final float FADE_SECONDS = 0.2f;
    private static final float SHOW_SECONDS = 2.2f;
    private static final float SLOT_HEIGHT = 58f;
    private static final float TOP_MARGIN = 90f;

    /** پاپ‌آپ‌های در حال نمایش، از بالا به پایین - برای محاسبه‌ی جایگاه پاپ‌آپ بعدی. */
    private static final List<Table> activeToasts = new ArrayList<>();

    private AnnouncementPopup() {
    }

    public static void show(Stage stage, String text) {
        show(stage, text, Color.WHITE);
    }

    /**
     * @param accentColor رنگ متن اصلی - برای MyoPoint می‌توان طلایی/بنفش داد
     *                    تا از اعلان‌های عادی (سفید) متمایز باشد.
     */
    public static void show(Stage stage, String text, Color accentColor) {
        if (stage == null || text == null || text.isBlank()) return;

        Skin skin = PvzSkin.get();
        FontManager fonts = FontManager.getInstance();

        Table toast = new Table();
        toast.setTouchable(Touchable.disabled);
        toast.setBackground(getToastBackground(skin));
        toast.pad(10f, 24f, 10f, 24f);
        toast.getColor().a = 0f;

        Label label = new Label(text, new Label.LabelStyle(fonts.getEnglishMenuFont(), accentColor));
        label.setFontScale(0.72f);
        label.setAlignment(Align.center);
        toast.add(label);
        toast.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        float x = (worldWidth - toast.getWidth()) * 0.5f;
        float y = stage.getViewport().getWorldHeight() - TOP_MARGIN - activeToasts.size() * SLOT_HEIGHT;
        toast.setPosition(Math.max(12f, x), Math.max(12f, y));

        // یک حرکت کوچک ورودی (کمی از بالا سر می‌خورد پایین + fade) که خیلی شیک‌تر
        // از ظاهر شدن ناگهانی است، بدون این‌که مزاحم دیدِ زمین بازی بشود.
        toast.moveBy(0f, 14f);
        stage.addActor(toast);

        activeToasts.add(toast);
        toast.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(FADE_SECONDS),
                Actions.moveBy(0f, -14f, FADE_SECONDS)
            ),
            Actions.delay(SHOW_SECONDS),
            Actions.fadeOut(FADE_SECONDS),
            Actions.run(() -> {
                activeToasts.remove(toast);
                toast.remove();
            })
        ));
    }

    /** برای صفحاتی که کاملا از نو ساخته می‌شوند (مثلا رفتن به منو) - جلوی پاپ‌آپ‌های یتیم را می‌گیرد. */
    public static void clearAll() {
        for (Table toast : new ArrayList<>(activeToasts)) {
            toast.remove();
        }
        activeToasts.clear();
    }

    private static Drawable getToastBackground(Skin skin) {
        Drawable background = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        if (background == null) {
            background = skin.getDrawable("image_ui_cards_almanac_plant_card_10");
        }
        return background;
    }
}
