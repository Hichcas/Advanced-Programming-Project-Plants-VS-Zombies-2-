package com.PVZ.model.game;

import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import pvz.skin.PvzSkin;

public class WinLoseOverlay extends Table {
    public interface ExitHandler {
        void onExit();
    }

    public interface RetryHandler {
        void onRetry();
    }

    private boolean showing = false;
    private final Label titleLabel;
    private final TextButton retryButton;

    public WinLoseOverlay(ExitHandler exitHandler, RetryHandler retryHandler) {
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        setBackground(new NinePatchDrawable(dimNinePatch()));

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        Table dialog = new Table();
        dialog.pad(30f);
        dialog.setBackground(resolveDialogBackground());

        titleLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        titleLabel.setFontScale(1.6f);
        dialog.add(titleLabel).padBottom(24f).colspan(2).row();

        TextButton exitButton = buildButton("EXIT", "brown", font, () -> {
            hide();
            if (exitHandler != null) {
                exitHandler.onExit();
            }
        });
        retryButton = buildButton("TRY AGAIN", "green", font, () -> {
            hide();
            if (retryHandler != null) {
                retryHandler.onRetry();
            }
        });

        dialog.add(exitButton).size(200f, 64f).padRight(16f);
        dialog.add(retryButton).size(200f, 64f);

        add(dialog);
    }

    public void showResult(boolean win) {
        titleLabel.setText(win ? "LEVEL COMPLETE!" : "GAME OVER");
        retryButton.setVisible(!win);
        showing = true;
        setVisible(true);
        setTouchable(Touchable.enabled);
    }

    public void hide() {
        showing = false;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public boolean isShowing() {
        return showing;
    }

    private TextButton buildButton(String text, String styleName, BitmapFont fallbackFont, Runnable action) {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has(styleName, TextButton.TextButtonStyle.class)) {
                TextButton button = new TextButton(text, skin, styleName);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        action.run();
                    }
                });
                return button;
            }
        } catch (Exception ignored) {
        }
        TextButton.TextButtonStyle fallbackStyle = new TextButton.TextButtonStyle();
        fallbackStyle.font = fallbackFont;
        fallbackStyle.fontColor = Color.WHITE;
        TextButton fallback = new TextButton(text, fallbackStyle);
        fallback.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return fallback;
    }

    private Drawable resolveDialogBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
        return new NinePatchDrawable(dimNinePatch());
    }

    private static com.badlogic.gdx.graphics.g2d.NinePatch dimNinePatch() {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.6f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.graphics.g2d.NinePatch(texture, 1, 1, 1, 1);
    }
}
