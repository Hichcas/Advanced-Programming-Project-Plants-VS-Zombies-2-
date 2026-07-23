package com.PVZ.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.PVZ.PVZ;
import com.PVZ.screen.BaseScreen;
import java.util.function.Supplier; // 🌟 اضافه شدن ابزار ساخت تأخیری

public class ScreenManager {
    private static ScreenManager instance;
    private PVZ game;
    private Supplier<BaseScreen> pendingScreenSupplier;
    private final SpriteBatch batch;
    private final Texture blackOverlay;
    private enum TransitionState { NONE, FADE_OUT, FADE_IN }
    private TransitionState state = TransitionState.NONE;
    private float blackScreenAlpha = 0f;
    private float currentDuration = 2.5f;
    private String activeMessage;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private ScreenManager() {
        batch = new SpriteBatch();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackOverlay = new Texture(pixmap);
        pixmap.dispose();
    }

    public static ScreenManager getInstance() {
        if (instance == null) instance = new ScreenManager();
        return instance;
    }

    public void init(PVZ game) {
        this.game = game;
    }

    public void startWithFadeIn(BaseScreen firstScreen) {
        this.game.setScreen(firstScreen);
        this.currentDuration = 3.0f;
        this.state = TransitionState.FADE_IN;
        this.blackScreenAlpha = 1.0f;
    }

    public void performTransition(Supplier<BaseScreen> screenSupplier) {
        performTransition(screenSupplier, null);
    }
    public void performTransition(Supplier<BaseScreen> screenSupplier, String message) {
        if (state != TransitionState.NONE) return;

        this.pendingScreenSupplier = screenSupplier;
        this.currentDuration = 1.5f;
        this.state = TransitionState.FADE_OUT;
        this.blackScreenAlpha = 0f;
        this.activeMessage = message;
    }

    public void updateAndRender(float delta) {
        if (state == TransitionState.NONE) return;

        if (state == TransitionState.FADE_OUT) {
            blackScreenAlpha += delta / currentDuration;
            if (blackScreenAlpha >= 1.0f) {
                blackScreenAlpha = 1.0f;

                Screen currentScreen = game.getScreen();
                if (currentScreen != null) {
                    currentScreen.dispose();
                }

                if (pendingScreenSupplier != null) {
                    BaseScreen nextScreen = pendingScreenSupplier.get();
                    game.setScreen(nextScreen);
                    pendingScreenSupplier = null;
                }

                state = TransitionState.FADE_IN;
            }
        } else if (state == TransitionState.FADE_IN) {
            blackScreenAlpha -= delta / currentDuration;
            if (blackScreenAlpha <= 0.0f) {
                blackScreenAlpha = 0.0f;
                state = TransitionState.NONE;
                activeMessage = null;
            }
        }

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.setColor(1f, 1f, 1f, blackScreenAlpha);
        batch.draw(blackOverlay, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);

        if (activeMessage != null) {
            BitmapFont font = FontManager.getInstance().getEnglishTitleFont();
            float textAlpha = Math.min(1f, blackScreenAlpha * 1.4f);
            font.setColor(1f, 0.15f, 0.15f, textAlpha);
            glyphLayout.setText(font, activeMessage);
            font.draw(batch, glyphLayout,
                (Gdx.graphics.getWidth() - glyphLayout.width) / 2f,
                (Gdx.graphics.getHeight() + glyphLayout.height) / 2f);
            font.setColor(Color.WHITE);
        }
        batch.end();
    }

    public void dispose() {
        if (blackOverlay != null) blackOverlay.dispose();
        if (batch != null) batch.dispose();
    }
}
