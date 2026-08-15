package com.PVZ.view.screen;

import com.PVZ.view.screen.panels.*;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.PanelManager;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainMenuScreen extends BaseScreen {
    private Texture backgroundTexture;
    private final SpriteBatch batch;

    private Texture blurParticleTexture;
    private final ArrayList<VoidParticle> particles;
    private final int PARTICLE_COUNT = 50;
    private final float MAX_HEIGHT_ZONE = 1200f;

    private boolean particlesEnabled = true;
    private static final String DEFAULT_BACKGROUND = "MainMenu/MainMenu_BackGround2.png";

    private EconomyHud economyHud;

    public MainMenuScreen() {
        super();
        this.batch = (SpriteBatch) stage.getBatch();

        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture(Gdx.files.internal(DEFAULT_BACKGROUND));
        createBlurryParticleTexture();

        MusicManager.getInstance().playMusic("music/TitleScreen.mp3");

        particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new VoidParticle());
            particles.get(i).y = MathUtils.random(0f, MAX_HEIGHT_ZONE);
            if (particles.get(i).y > 850f) {
                particles.get(i).triggerFadeState();
            }
        }

        PanelManager.getInstance().initialize(stage);
        com.PVZ.view.screen.panels.CheatPanel.attachToggleButton(stage, null);

        // داک ناوبری سراسری: کالکشن، مینی‌گیم، گلخانه، کوئست
        addGlobalNavigationDock();

        switch (AppStatus.currentMenuType) {
            case MAIN -> PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
            case CHAPTER_AND_LEVEL_SELECTION -> PanelManager.getInstance().performPanelTransition(new ChapterSelectPanel());
            case MINIGAME_SELECTION -> PanelManager.getInstance().performPanelTransition(new MinigameSelectionPanel());
            case LOGIN -> PanelManager.getInstance().performPanelTransition(new LoginPanel());
            case REGISTER -> PanelManager.getInstance().performPanelTransition(new RegisterPanel());
            default -> PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
        }

        // ====================== HUD اقتصادی (بالا راست) ======================
        economyHud = new EconomyHud();
        stage.addActor(economyHud);
        economyHud.toFront();
        PanelManager.getInstance().addPersistentOverlay(economyHud);
    }

    private void addGlobalNavigationDock() {
        Skin skin = PvzSkin.get();

        ImageButton.ImageButtonStyle almanacStyle =
            skin.get("almanac", ImageButton.ImageButtonStyle.class);
        ImageButton.ImageButtonStyle minigamesStyle =
            skin.get("hud_minigames", ImageButton.ImageButtonStyle.class);
        ImageButton.ImageButtonStyle zgStyle =
            skin.get("hud_zg", ImageButton.ImageButtonStyle.class);
        ImageButton.ImageButtonStyle questsStyle =
            skin.get("hud_quests", ImageButton.ImageButtonStyle.class);

        Table navDock = new Table();
        navDock.setFillParent(true);
        navDock.top().left().padTop(20f).padLeft(50f);
        navDock.setTouchable(Touchable.childrenOnly);

        float btnSize = 120f;
        float spacing = 20f;

        MenuButton collectionBtn = new MenuButton(
            almanacStyle.imageUp, null, null,
            almanacStyle.imageDown, null, null,
            () -> AppStatus.setCurrentMenuType(MenuType.COLLECTION)
        );
        collectionBtn.setSize(btnSize, btnSize);

        MenuButton minigamesBtn = new MenuButton(
            minigamesStyle.imageUp, null, null,
            minigamesStyle.imageDown, null, null,
            () -> AppStatus.setCurrentMenuType(MenuType.MINIGAME_SELECTION)
        );
        minigamesBtn.setSize(btnSize, btnSize);

        MenuButton greenhouseBtn = new MenuButton(
            zgStyle.imageUp, null, null,
            zgStyle.imageDown, null, null,
            () -> AppStatus.setCurrentMenuType(MenuType.GREENHOUSE)
        );
        greenhouseBtn.setSize(btnSize, btnSize);

        MenuButton questsBtn = new MenuButton(
            questsStyle.imageUp, null, null,
            questsStyle.imageDown, null, null,
            () -> AppStatus.setCurrentMenuType(MenuType.QUEST)
        );
        questsBtn.setSize(btnSize, btnSize);

        navDock.add(collectionBtn).size(btnSize).padRight(spacing);
        navDock.add(minigamesBtn).size(btnSize).padRight(spacing);
        navDock.add(greenhouseBtn).size(btnSize).padRight(spacing);
        navDock.add(questsBtn).size(btnSize);

        stage.addActor(navDock);
        PanelManager.getInstance().addPersistentOverlay(navDock);
    }

    /** تغییر پس‌زمینه و غیرفعال/فعال‌سازی ذرات */
    public void switchToBackground(String backgroundPath, boolean disableParticles) {
        if (backgroundTexture != null) backgroundTexture.dispose();
        try {
            backgroundTexture = new Texture(Gdx.files.internal(backgroundPath));
        } catch (Exception e) {
            System.err.println("MainMenuScreen: failed to load background: " + backgroundPath);
            backgroundTexture = new Texture(Gdx.files.internal(DEFAULT_BACKGROUND));
        }
        this.particlesEnabled = !disableParticles;
    }

    /** بازگشت به حالت اولیه (پس‌زمینه اصلی + ذرات فعال) */
    public void restoreDefaultBackground() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        backgroundTexture = new Texture(Gdx.files.internal(DEFAULT_BACKGROUND));
        this.particlesEnabled = true;
    }

    private void createBlurryParticleTexture() {
        int textureSize = 64;
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);
        float center = textureSize / 2f;
        float maxRadius = textureSize / 2f;

        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                float dx = center - x;
                float dy = center - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < maxRadius) {
                    float alphaFactor = 1.0f - (distance / maxRadius);
                    alphaFactor = alphaFactor * alphaFactor;
                    pixmap.setColor(new Color(0f, 0f, 0f, alphaFactor * 0.8f));
                    pixmap.drawPixel(x, y);
                }
            }
        }
        blurParticleTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    protected void renderScreen(float delta) {
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 2560, 1440);

        if (particlesEnabled) {
            for (VoidParticle p : particles) {
                p.update(delta);
                batch.setColor(1f, 1f, 1f, p.alpha);
                batch.draw(blurParticleTexture, p.x, p.y, p.size, p.size);
            }
            batch.setColor(Color.WHITE);
        }

        batch.end();
    }

    @Override
    public void hide() {
        dispose();
    }

    private class VoidParticle {
        float x, y;
        float currentSpeedY;
        float initialSpeedY;
        float size;
        float alpha;
        boolean isFading;
        float fadeProgress;
        private final float FADE_DURATION = 2.0f;

        public VoidParticle() {
            resetPosition();
        }

        public void resetPosition() {
            this.x = MathUtils.random(0f, 2560f);
            this.y = MathUtils.random(-40f, 10f);
            this.initialSpeedY = MathUtils.random(90f, 170f);
            this.currentSpeedY = this.initialSpeedY;
            this.size = MathUtils.random(12f, 42f);
            this.alpha = 1.0f;
            this.isFading = false;
            this.fadeProgress = 0f;
        }

        public void triggerFadeState() {
            this.isFading = true;
            this.fadeProgress = MathUtils.random(0f, 0.6f);
        }

        public void update(float delta) {
            y += currentSpeedY * delta;
            if (!isFading && y >= 850f) isFading = true;
            if (isFading) {
                fadeProgress += delta / FADE_DURATION;
                if (fadeProgress > 1.0f) fadeProgress = 1.0f;
                alpha = 1.0f - fadeProgress;
                currentSpeedY = MathUtils.lerp(initialSpeedY, initialSpeedY * 0.25f, fadeProgress);
            }
            if (alpha <= 0.001f || currentSpeedY <= 0.1f || y >= MAX_HEIGHT_ZONE) {
                resetPosition();
            }
        }
    }

    @Override
    public void dispose() {
        PanelManager.getInstance().dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (blurParticleTexture != null) blurParticleTexture.dispose();
        super.dispose();
    }

    // ====================== HUD اقتصادی ======================
    private static class EconomyHud extends Table {
        private final float MARGIN_RIGHT = 30f;
        private final float MARGIN_TOP = 110f;

        private final CurrencyButton coinButton;
        private final CurrencyButton gemButton;
        private final MenuButton storeButton;

        EconomyHud() {
            Skin skin = PvzSkin.get();
            BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

            TextureBank bank = EntityRenderer.getInstance().getTextures();
            TextureRegion coinRegion = bank.region("IMAGE_UI_GENERIC_BUTTONS_COIN_BUY_NORMAL");
            TextureRegion gemRegion = bank.region("IMAGE_UI_GENERIC_BUTTONS_PREMIUM_NORMAL");
            TextureRegion storeRegion = bank.region("IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_STORE_NORMAL");

            Drawable coinUp = createNinePatchDrawable(coinRegion, 35, 20, 0, 0);
            Drawable gemUp = createNinePatchDrawable(gemRegion, 35, 20, 0, 0);
            Drawable storeUp = new TextureRegionDrawable(storeRegion);

            coinButton = new CurrencyButton(coinUp, font);
            gemButton = new CurrencyButton(gemUp, font);

            storeButton = new MenuButton(
                storeUp, "", font,
                storeUp, storeUp, null,
                this::openShop
            );
            storeButton.setSize(90f, 70f);

            // چیدمان: فروشگاه، الماس، سکه (از راست به چپ)
            this.add(storeButton).size(90f, 70f).padRight(12f);
            this.add(gemButton).padRight(12f);
            this.add(coinButton);

            pack(); // محاسبه اولیه ابعاد
        }

        private void openShop() {
            ShopPanel shopPanel = new ShopPanel();
            shopPanel.setFillParent(true);
            getStage().addActor(shopPanel);
            shopPanel.toFront();
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            this.toFront();

            User user = AppStatus.currentUser;
            if (user != null && user.userStats != null) {
                coinButton.setValue(user.userStats.getCoins());
                gemButton.setValue(user.userStats.getDiamonds());
            } else {
                coinButton.setValue(0);
                gemButton.setValue(0);
            }

            pack(); // به‌روزرسانی عرض بر اساس اعداد جدید
            setPosition(
                VIRTUAL_WIDTH - getWidth() - MARGIN_RIGHT,
                VIRTUAL_HEIGHT - MARGIN_TOP
            );
        }

        private static NinePatchDrawable createNinePatchDrawable(TextureRegion region,
                                                                 int left, int right,
                                                                 int top, int bottom) {
            if (region == null) {
                return null;
            }
            NinePatch patch = new NinePatch(region, left, right, top, bottom);
            return new NinePatchDrawable(patch);
        }
    }

    /** دکمه‌ای که عرضش بر اساس عدد تغییر می‌کند و پس‌زمینه‌اش NinePatch است */
    private static class CurrencyButton extends MenuButton {
        private final NumberFormat numberFormat;

        CurrencyButton(Drawable background, BitmapFont font) {
            super(background, "", font, background, background, null, null);
            this.numberFormat = NumberFormat.getNumberInstance(Locale.US);
            setSize(200f, 70f);
        }

        void setValue(int value) {
            String text = numberFormat.format(value);
            setText(text);

            float desiredWidth = Math.max(200f, getTextWidth() + 50f);
            setSize(desiredWidth, 70f);
        }
    }
}
