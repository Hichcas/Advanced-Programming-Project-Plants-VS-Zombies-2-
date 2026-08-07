package com.PVZ.view.screen.panels;

import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.PanelManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.ui.MenuButton;
import pvz.skin.PvzSkin;

public class MainMenuPanel extends BasePanel {

    // ---------- ثابت‌های قابل تنظیم (مثل CSS) ----------
    private static final float BUTTON_HEIGHT = 80f;
    private static final float HORIZONTAL_PADDING = 40f;

    // تنظیمات قاب آفلاین
    private static final float CONTENT_SCALE_X = 1.3f;   // ضریب بزرگ‌نمایی عرض (۱.۰ = اندازه اصلی)
    private static final float CONTENT_SCALE_Y = 1.3f;   // ضریب بزرگ‌نمایی ارتفاع
    private static final float CONTENT_Y = 625f;         // موقعیت عمودی قاب (پایین صفحه)

    // تنظیمات دکمهٔ تنظیمات
    private static final float SETTINGS_SIZE = 120f;     // اندازهٔ آیکون (عرض و ارتفاع یکسان)
    private static final float SETTINGS_RIGHT_MARGIN = 50f; // فاصله از لبهٔ راست
    private static final float SETTINGS_BOTTOM_MARGIN = 100f; // فاصله از لبهٔ پایین

    // جابه‌جایی اضافی دکمه‌های متنی (نسبت به مکان پیش‌فرض)
    private static final float BUTTONS_EXTRA_DOWN = 450f; // این مقدار را کم/زیاد کنید تا دکمه‌ها جابه‌جا شوند

    // ------------------------------------------------------

    public MainMenuPanel() {
        setFillParent(true);

        // ---------- تصاویر اصلی ----------
        Texture logoTexture   = safeTextureFromRegion("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        Texture contentTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_MAINMENU_CONTENT_OFFLINE");

        // لوگو با 300 پیکسل پایین‌تر از لبهٔ بالا
        float logoX = (Gdx.graphics.getWidth() - logoTexture.getWidth()) / 2f;
        float logoY = Gdx.graphics.getHeight() - logoTexture.getHeight() - 50f - 300f;
        addArt(logoTexture, logoX, logoY, logoTexture.getWidth(), logoTexture.getHeight());

        // قاب آفلاین: اندازه با ضریب‌های تنظیم‌شده
        float contentW = contentTexture.getWidth() * CONTENT_SCALE_X;
        float contentH = contentTexture.getHeight() * CONTENT_SCALE_Y;
        float contentX = (Gdx.graphics.getWidth() - contentW) / 2f;
        float contentY = CONTENT_Y;   // قابل تنظیم
        addArt(contentTexture, contentX, contentY, contentW, contentH);

        // ---------- دکمه‌های متنی (سبز) ----------
        Texture greenUpTex   = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        Texture greenDownTex = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        Texture markerTex    = new Texture(Gdx.files.internal("global/button_marker.png"));
        BitmapFont buttonFont = PvzSkin.get().getFont("FBUSV8C5EI_1_outline");

        // موقعیت شروع دکمه‌ها (بالای قاب) منهای مقدار جابه‌جایی
        float startY = contentY + contentH - 150f - BUTTONS_EXTRA_DOWN;
        float centerX = Gdx.graphics.getWidth() / 2f;

        addButton("START GAME", this::onStartGame, greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY);
        addButton("QUIT GAME",  this::onQuit,      greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY - 90);

        // ---------- دکمهٔ تنظیمات (چرخ‌دنده) ----------
        Texture settingsNormal   = safeTextureFromRegion("IMAGE_UI_HUD_SETTINGSBUTTON_BUTTONS_HUD_SETTINGS_NORMAL");
        Texture settingsSelected = safeTextureFromRegion("IMAGE_UI_HUD_SETTINGSBUTTON_BUTTONS_HUD_SETTINGS_SELECTED");

        MenuButton settingsBtn = new MenuButton(
            settingsNormal,       // حالت عادی
            null,                 // بدون متن
            null,                 // فونت
            settingsSelected,     // حالت هاور
            null,                 // غیرفعال
            null,                 // بدون مارکر
            this::onSettings
        );
        settingsBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float btnX = Gdx.graphics.getWidth() - settingsBtn.getWidth() - SETTINGS_RIGHT_MARGIN;
        float btnY = SETTINGS_BOTTOM_MARGIN;
        settingsBtn.setPosition(btnX, btnY);
        addActor(settingsBtn);
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

    // رویدادهای کلیک
    private void onStartGame() {
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            "maps/Frontyard.jpg",
            "music/Title Screen.mp3",
            new RegularGameEngine(new GameStatus())
        ));
    }

    private void onSettings() {
        AppStatus.setCurrentMenuType(MenuType.SETTINGS);
        PanelManager.getInstance().performPanelTransition(new SettingsPanel());
    }

    private void onQuit() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
