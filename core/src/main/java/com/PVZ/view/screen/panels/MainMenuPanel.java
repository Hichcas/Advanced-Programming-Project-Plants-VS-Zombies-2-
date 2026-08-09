package com.PVZ.view.screen.panels;

import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.PanelManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.ui.MenuButton;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

public class MainMenuPanel extends BasePanel {

    // ---------- ثابت‌های قابل تنظیم ----------
    private static final float BUTTON_HEIGHT = 80f;
    private static final float HORIZONTAL_PADDING = 40f;

    private static final float CONTENT_SCALE_X = 1.3f;
    private static final float CONTENT_SCALE_Y = 1.3f;
    private static final float CONTENT_Y = 625f;

    private static final float SETTINGS_SIZE = 120f;
    private static final float SETTINGS_RIGHT_MARGIN = 50f;
    private static final float SETTINGS_BOTTOM_MARGIN = 100f;

    private static final float BUTTONS_EXTRA_DOWN = 450f;

    // Stage روی ویوپورت مجازی ثابت (VIRTUAL_WIDTH x VIRTUAL_HEIGHT در BaseScreen) کار می‌کند،
    // نه اندازه‌ی واقعی پنجره. برای سنتر شدن دقیق روی هر رزولوشنی باید از همین اعداد استفاده کنیم،
    // نه Gdx.graphics.getWidth/Height که پیکسل واقعی صفحه است و با فضای مختصات Stage یکی نیست.
    private static final float VIRTUAL_WIDTH = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VIRTUAL_HEIGHT = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;

    public MainMenuPanel() {
        setFillParent(true);

        // ---------- تصاویر اصلی ----------
        Texture logoTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        Texture contentTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_MAINMENU_CONTENT_OFFLINE");

        float logoX = (VIRTUAL_WIDTH - logoTexture.getWidth()) / 2f;
        float logoY = VIRTUAL_HEIGHT - logoTexture.getHeight() - 50f - 300f;
        addArt(logoTexture, logoX, logoY, logoTexture.getWidth(), logoTexture.getHeight());

        float contentW = contentTexture.getWidth() * CONTENT_SCALE_X;
        float contentH = contentTexture.getHeight() * CONTENT_SCALE_Y;
        float contentX = (VIRTUAL_WIDTH - contentW) / 2f;
        float contentY = CONTENT_Y;
        addArt(contentTexture, contentX, contentY, contentW, contentH);

        // ---------- دکمه‌های متنی ----------
        Texture greenUpTex = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        Texture greenDownTex = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        Texture markerTex = new Texture(Gdx.files.internal("global/button_marker.png"));
        BitmapFont buttonFont = PvzSkin.get().getFont("FBUSV8C5EI_1_outline");

        float startY = contentY + contentH - 150f - BUTTONS_EXTRA_DOWN;
        float centerX = VIRTUAL_WIDTH / 2f;

        addButton("START GAME", this::onStartGame, greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY);
        addButton("QUIT GAME", this::onQuit, greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY - 90);

        // ---------- دکمهٔ تنظیمات ----------
        Texture settingsNormal = safeTextureFromRegion("IMAGE_UI_HUD_SETTINGSBUTTON_BUTTONS_HUD_SETTINGS_NORMAL");
        Texture settingsSelected = safeTextureFromRegion("IMAGE_UI_HUD_SETTINGSBUTTON_BUTTONS_HUD_SETTINGS_SELECTED");

        MenuButton settingsBtn = new MenuButton(
            settingsNormal, null, null,
            settingsSelected, null, null,
            this::onSettings
        );
        settingsBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float settingsX = VIRTUAL_WIDTH - SETTINGS_SIZE - SETTINGS_RIGHT_MARGIN;
        float settingsY = SETTINGS_BOTTOM_MARGIN;
        settingsBtn.setPosition(settingsX, settingsY);
        addActor(settingsBtn);
// ---------- دکمهٔ پروفایل (سمت چپ تنظیمات) ----------
        Texture profileTex = createProfileButtonTexture(); // تصویر ترکیبی

        MenuButton profileBtn = new MenuButton(
            profileTex, null, null,       // عادی
            profileTex, profileTex, null, // هاور و غیرفعال هم همان تصویر
            this::onProfile
        );
        profileBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float gap = 20f;
        float profileX = settingsX - SETTINGS_SIZE - gap;
        profileBtn.setPosition(profileX, settingsY);
        addActor(profileBtn);
    }

    /**
     * ساخت تکسچر ترکیبی برای دکمهٔ پروفایل:
     * پس‌زمینه = IMAGE_UI_MAINMENU_BTN_BKGD
     * آیکون   = IMAGE_UI_MAINMENU_MM_PLAYERICON
     */
    private Texture createProfileButtonTexture() {
        TextureBank bank = getTextureBank(); // همان نمونهٔ سینگلتون EntityRenderer

        TextureRegion bgRegion = bank.region("IMAGE_UI_MAINMENU_BTN_BKGD");
        TextureRegion iconRegion = bank.region("IMAGE_UI_MAINMENU_MM_PLAYERICON");

        if (bgRegion == null || iconRegion == null) {
            // fallback
            return createDummyTexture((int) SETTINGS_SIZE, (int) SETTINGS_SIZE);
        }

        // استخراج Pixmap از نواحی
        Texture bgTex = bgRegion.getTexture();
        if (!bgTex.getTextureData().isPrepared()) bgTex.getTextureData().prepare();
        Pixmap bgPix = bgTex.getTextureData().consumePixmap();
        Pixmap bgSub = new Pixmap(bgRegion.getRegionWidth(), bgRegion.getRegionHeight(), bgPix.getFormat());
        bgSub.drawPixmap(bgPix, 0, 0, bgRegion.getRegionX(), bgRegion.getRegionY(),
            bgRegion.getRegionWidth(), bgRegion.getRegionHeight());

        Texture iconTex = iconRegion.getTexture();
        if (!iconTex.getTextureData().isPrepared()) iconTex.getTextureData().prepare();
        Pixmap iconPix = iconTex.getTextureData().consumePixmap();
        Pixmap iconSub = new Pixmap(iconRegion.getRegionWidth(), iconRegion.getRegionHeight(), iconPix.getFormat());
        iconSub.drawPixmap(iconPix, 0, 0, iconRegion.getRegionX(), iconRegion.getRegionY(),
            iconRegion.getRegionWidth(), iconRegion.getRegionHeight());

        int w = (int) SETTINGS_SIZE;
        int h = (int) SETTINGS_SIZE;
        Pixmap finalPix = new Pixmap(w, h, bgSub.getFormat());

        // رسم پس‌زمینه
        finalPix.drawPixmap(bgSub, 0, 0, bgSub.getWidth(), bgSub.getHeight(), 0, 0, w, h);

        // رسم آیکون در وسط (حدود ۶۰٪ اندازهٔ دکمه)
        int iconSize = (int) (w * 0.6f);
        int iconX = (w - iconSize) / 2;
        int iconY = (h - iconSize) / 2;
        finalPix.drawPixmap(iconSub, 0, 0, iconSub.getWidth(), iconSub.getHeight(),
            iconX, iconY, iconSize, iconSize);

        Texture finalTex = new Texture(finalPix);
        bgSub.dispose();
        iconSub.dispose();
        finalPix.dispose();
        if (bgPix != null) bgPix.dispose();
        if (iconPix != null) iconPix.dispose();

        return finalTex;
    }

    private Texture createDummyTexture(int w, int h) {
        Pixmap pix = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    private void addButton(String text, Runnable action,
                           Texture upTex, Texture downTex,
                           BitmapFont font, Texture marker,
                           float centerX, float y) {
        MenuButton btn = new MenuButton(upTex, text, font, downTex, null, marker, action);
        float textWidth = btn.getTextWidth();
        float desiredWidth = textWidth + HORIZONTAL_PADDING * 2;
        btn.setSize(desiredWidth, BUTTON_HEIGHT);
        btn.setPosition(centerX - desiredWidth / 2f, y);
        addActor(btn);
    }

    // ---------- رویدادها ----------
    private void onStartGame() {
//        ScreenManager.getInstance().performTransition(() -> new GameScreen(
//            "maps/Frontyard.jpg",
//            "music/Title Screen.mp3",
//            new RegularGameEngine(new GameStatus())
//        ));
//        AppStatus.setCurrentMenuType(MenuType.CHAPTER_AND_LEVEL_SELECTION);


//        PanelManager.getInstance().performPanelTransition(new PlantSelectionPanel("Egypt", 1));

        AppStatus.setCurrentMenuType(MenuType.CHAPTER_AND_LEVEL_SELECTION);

    }

    private void onSettings() {
        AppStatus.setCurrentMenuType(MenuType.SETTINGS);
    }

    private void onProfile() {
        AppStatus.setCurrentMenuType(MenuType.PROFILE);
    }

    private void onQuit() {
        Gdx.app.exit();
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
