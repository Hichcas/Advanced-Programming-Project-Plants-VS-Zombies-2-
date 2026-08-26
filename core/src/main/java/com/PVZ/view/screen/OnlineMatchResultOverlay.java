package com.PVZ.view.screen;

import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

public class OnlineMatchResultOverlay extends Table {

    private final Label titleLabel;
    private final Label messageLabel;
    private boolean showing = false;

    public void showDraw(String reason) {
        titleLabel.setText("DRAW!");
        titleLabel.setColor(Color.GRAY);
        messageLabel.setText(reason != null ? reason : "The game ended in a draw.");
        showing = true;
        setVisible(true);
        setTouchable(Touchable.enabled);
        toFront();
    }

    public OnlineMatchResultOverlay(Runnable onExit) {
        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);

        Skin skin = PvzSkin.get();
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Table dialog = new Table();
        dialog.pad(30f);
        dialog.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        titleLabel = new Label("", new Label.LabelStyle(font, Color.GOLD));
        titleLabel.setFontScale(1.6f);
        titleLabel.setAlignment(Align.center);

        messageLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);
        messageLabel.setFontScale(0.9f);

        Drawable purpleUp = skin.getDrawable("image_ui_generic_purplebutton_10");
        Drawable purpleDown = skin.getDrawable("image_ui_generic_purplebutton_down_10");
        MenuButton exitButton = new MenuButton(purpleUp, "EXIT", font, purpleDown, null, null, () -> {
            hide();
            onExit.run();
        });
        exitButton.setSize(220f, 64f);

        dialog.add(titleLabel).padBottom(20f).row();
        dialog.add(messageLabel).width(500f).padBottom(30f).row();
        dialog.add(exitButton).size(220f, 64f);

        add(dialog);
    }

    public void showResult(boolean win, String reason) {
        titleLabel.setText(win ? "VICTORY!" : "DEFEAT!");
        titleLabel.setColor(win ? Color.GREEN : Color.RED);
        messageLabel.setText(reason != null ? reason : (win ? "You won the match!" : "You lost the match."));
        showing = true;
        setVisible(true);
        setTouchable(Touchable.enabled);
        toFront();
    }

    public void hide() {
        showing = false;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public boolean isShowing() {
        return showing;
    }
}
