package com.PVZ.view.screen.panels;

import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.controller.menuControllers.NewsMenuController;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

public class MainMenuPanel extends BasePanel {

    // ================== حالت منو (آفلاین / آنلاین) ==================
    private enum MenuMode {
        OFFLINE,
        ONLINE
    }

    private MenuMode currentMode = MenuMode.OFFLINE;

    // ================== تصاویر پس‌زمینه‌ی میانی ==================
    private Texture offlineContentTexture;
    private Texture onlineContentTexture;
    private Image contentImage;

    // ================== دکمه‌ی شروع ==================
    private MenuButton startButton;

    // ================== ثابت‌ها ==================
    private static final float BUTTON_HEIGHT = 80f;
    private static final float HORIZONTAL_PADDING = 40f;

    private static final float CONTENT_SCALE_X = 1.3f;
    private static final float CONTENT_SCALE_Y = 1.3f;
    private static final float CONTENT_Y = 625f;

    private static final float SETTINGS_SIZE = 120f;
    private static final float SETTINGS_RIGHT_MARGIN = 50f;
    private static final float SETTINGS_BOTTOM_MARGIN = 100f;

    private static final float BUTTONS_EXTRA_DOWN = 450f;

    private static final float VIRTUAL_WIDTH = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VIRTUAL_HEIGHT = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;

    private NewsBadge newsBadge;

    public MainMenuPanel() {
        setFillParent(true);
        setTouchable(Touchable.enabled);   // برای دریافت Swipe

        // ---------- آهنگ منو ----------
        MusicManager.getInstance().playMusic("music/TitleScreen.mp3");

        // ---------- لوگو ----------
        Texture logoTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        float logoX = (VIRTUAL_WIDTH - logoTexture.getWidth()) / 2f;
        float logoY = VIRTUAL_HEIGHT - logoTexture.getHeight() - 50f - 300f;
        addArt(logoTexture, logoX, logoY, logoTexture.getWidth(), logoTexture.getHeight());

        // ---------- دو تصویر محتوای مرکزی (آفلاین و آنلاین) ----------
        offlineContentTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_MAINMENU_CONTENT_OFFLINE");
        onlineContentTexture = safeTextureFromRegion("IMAGE_UI_MAINMENU_MAINMENU_CONTENT_DOWNLOADING");

        float contentW = offlineContentTexture.getWidth() * CONTENT_SCALE_X;
        float contentH = offlineContentTexture.getHeight() * CONTENT_SCALE_Y;
        float contentX = (VIRTUAL_WIDTH - contentW) / 2f;
        float contentY = CONTENT_Y;

        contentImage = new Image(new TextureRegionDrawable(offlineContentTexture));
        contentImage.setBounds(contentX, contentY, contentW, contentH);
        addActor(contentImage);

        // ---------- دکمه‌های متنی ----------
        Texture greenUpTex = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        Texture greenDownTex = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        Texture markerTex = new Texture(Gdx.files.internal("global/button_marker.png"));
        BitmapFont buttonFont = PvzSkin.get().getFont("FBUSV8C5EI_1_outline");

        float startY = contentY + contentH - 150f - BUTTONS_EXTRA_DOWN;
        float centerX = VIRTUAL_WIDTH / 2f;

        // دکمه‌ی شروع (متن و اکشن آن بسته به حالت تغییر می‌کند)
        startButton = addButton("START GAME", this::startOfflineGame,
            greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY);
        addButton("QUIT GAME", this::onQuit,
            greenUpTex, greenDownTex, buttonFont, markerTex, centerX, startY - 90);

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

        // ---------- دکمهٔ پروفایل ----------
        Texture profileTex = createProfileButtonTexture();
        MenuButton profileBtn = new MenuButton(
            profileTex, null, null,
            profileTex, profileTex, null,
            this::onProfile
        );
        profileBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float gap = 20f;
        float profileX = settingsX - SETTINGS_SIZE - gap;
        profileBtn.setPosition(profileX, settingsY);
        addActor(profileBtn);

        // ---------- دکمهٔ News ----------
        Texture newsNormal = safeTextureFromRegion("IMAGE_UI_HUD_NEWSBUTTON_BUTTONS_HUD_NEWS_NORMAL");
        Texture newsSelected = safeTextureFromRegion("IMAGE_UI_HUD_NEWSBUTTON_BUTTONS_HUD_NEWS_SELECTED");

        MenuButton newsBtn = new MenuButton(
            newsNormal, null, null,
            newsSelected, null, null,
            this::onNews
        );
        newsBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float newsX = SETTINGS_RIGHT_MARGIN;
        float newsY = SETTINGS_BOTTOM_MARGIN;
        newsBtn.setPosition(newsX, newsY);
        addActor(newsBtn);

        newsBadge = new NewsBadge();
        newsBadge.setSize(24f, 24f);
        newsBadge.setPosition(newsX + SETTINGS_SIZE - newsBadge.getWidth() / 2f,
            newsY + SETTINGS_SIZE - newsBadge.getHeight() / 2f);
        addActor(newsBadge);

        // ---------- دکمهٔ Leaderboard ----------
        Texture leaderboardNormal = safeTextureFromRegion("IMAGE_UI_GAMECENTER_ANDROID_LEADERBOARD");
        Texture leaderboardSelected = safeTextureFromRegion("IMAGE_UI_GAMECENTER_ANDROID_LEADERBOARD_SELECT");

        MenuButton leaderboardBtn = new MenuButton(
            leaderboardNormal, null, null,
            leaderboardSelected, null, null,
            this::onLeaderboard
        );
        leaderboardBtn.setSize(SETTINGS_SIZE, SETTINGS_SIZE);
        float leaderboardX = newsX + SETTINGS_SIZE + gap;
        leaderboardBtn.setPosition(leaderboardX, settingsY);
        addActor(leaderboardBtn);

        // ---------- Swipe برای جابه‌جایی آفلاین / آنلاین ----------
        addSwipeListener();

        // حالت اولیه را اعمال کن
        applyMode(MenuMode.OFFLINE);
    }

    // ======================== Swipe ========================
    private final Vector2 touchStart = new Vector2();
    private final Vector2 touchEnd = new Vector2();
    private boolean swiping = false;

    private void addSwipeListener() {
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touchStart.set(x, y);
                swiping = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!swiping) return;
                swiping = false;
                touchEnd.set(x, y);

                float dx = touchEnd.x - touchStart.x;
                float dy = touchEnd.y - touchStart.y;

                // جابه‌جایی افقی به‌اندازه‌ی کافی و افقی‌تر از عمودی
                if (Math.abs(dx) > 80 && Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) {
                        // کشیدن به راست -> حالت آفلاین
                        applyMode(MenuMode.OFFLINE);
                    } else {
                        // کشیدن به چپ -> حالت آنلاین
                        applyMode(MenuMode.ONLINE);
                    }
                }
            }
        });
    }

    // ======================== تغییر حالت ========================
    private void applyMode(MenuMode mode) {
        currentMode = mode;

        // تغییر تصویر مرکزی
        if (contentImage != null) {
            TextureRegionDrawable drawable = new TextureRegionDrawable(
                mode == MenuMode.OFFLINE ? offlineContentTexture : onlineContentTexture
            );
            contentImage.setDrawable(drawable);
        }

        // تغییر متن و اکشن دکمه‌ی شروع
        if (startButton != null) {
            if (mode == MenuMode.OFFLINE) {
                startButton.setText("START GAME");
                startButton.setClickAction(this::startOfflineGame);
            } else {
                startButton.setText("PLAY ONLINE");
                startButton.setClickAction(this::startOnlineGame);
            }
        }
    }

    // ======================== اکشن‌ها ========================
    private void startOfflineGame() {
        AppStatus.setCurrentMenuType(MenuType.CHAPTER_AND_LEVEL_SELECTION);
    }

    private void startOnlineGame() {
        // TODO: بعداً پنل بازی آنلاین واقعی را جایگزین کنید
        AppStatus.setCurrentMenuType(MenuType.NETWORK); // موقتاً
        System.out.println("Online game panel is not implemented yet. (Placeholder)");
    }

    private void onSettings() {
        AppStatus.setCurrentMenuType(MenuType.SETTINGS);
    }

    private void onProfile() {
        AppStatus.setCurrentMenuType(MenuType.PROFILE);
    }

    private void onNews() {
        AppStatus.setCurrentMenuType(MenuType.NEWS);
    }

    private void onLeaderboard() {
        AppStatus.setCurrentMenuType(MenuType.LEADERBOARD);
    }

    private void onQuit() {
        Gdx.app.exit();
    }

    // ======================== متدهای کمکی ========================
    private Texture createProfileButtonTexture() {
        TextureBank bank = getTextureBank();
        TextureRegion bgRegion = bank.region("IMAGE_UI_MAINMENU_BTN_BKGD");
        TextureRegion iconRegion = bank.region("IMAGE_UI_MAINMENU_MM_PLAYERICON");
        if (bgRegion == null || iconRegion == null) {
            return createDummyTexture((int) SETTINGS_SIZE, (int) SETTINGS_SIZE);
        }
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

        int w = (int) SETTINGS_SIZE, h = (int) SETTINGS_SIZE;
        Pixmap finalPix = new Pixmap(w, h, bgSub.getFormat());
        finalPix.drawPixmap(bgSub, 0, 0, bgSub.getWidth(), bgSub.getHeight(), 0, 0, w, h);
        int iconSize = (int) (w * 0.6f);
        int iconX = (w - iconSize) / 2, iconY = (h - iconSize) / 2;
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

    private MenuButton addButton(String text, Runnable action,
                                 Texture upTex, Texture downTex,
                                 BitmapFont font, Texture marker,
                                 float centerX, float y) {
        MenuButton btn = new MenuButton(upTex, text, font, downTex, null, marker, action);
        float textWidth = btn.getTextWidth();
        float desiredWidth = textWidth + HORIZONTAL_PADDING * 2;
        btn.setSize(desiredWidth, BUTTON_HEIGHT);
        btn.setPosition(centerX - desiredWidth / 2f, y);
        addActor(btn);
        return btn;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (newsBadge != null) {
            newsBadge.updateVisibility();
        }
    }

    @Override
    public void dispose() {
        // تصاویری که جداگانه ساخته‌ایم را آزاد کن
        if (offlineContentTexture != null) offlineContentTexture.dispose();
        if (onlineContentTexture != null) onlineContentTexture.dispose();
        super.dispose();
    }

    // ======================== نشان‌گر قرمز News ========================
    private static class NewsBadge extends Image {
        private static final Texture dotTexture;

        static {
            int size = 24;
            Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pix.setColor(Color.RED);
            pix.fillCircle(size / 2, size / 2, size / 2 - 1);
            dotTexture = new Texture(pix);
            pix.dispose();
        }

        public NewsBadge() {
            super(new TextureRegionDrawable(new TextureRegion(dotTexture)));
            setVisible(false);
        }

        public void updateVisibility() {
            setVisible(NewsMenuController.hasUnreadNews());
        }
    }
}
