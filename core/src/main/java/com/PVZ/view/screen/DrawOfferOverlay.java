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

public class DrawOfferOverlay extends Table {

    public interface DrawOfferCallback {
        void onDecision(boolean accept);
    }

    private final DrawOfferCallback callback;
    private boolean showing = false;

    public DrawOfferOverlay(DrawOfferCallback callback) {
        this.callback = callback;

        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.disabled);
        setBackground(PvzSkin.get().getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Skin skin = PvzSkin.get();
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        Table dialog = new Table();
        dialog.pad(30f);
        dialog.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label title = new Label("DRAW OFFER", new Label.LabelStyle(font, Color.GOLD));
        title.setAlignment(Align.center);

        Label message = new Label("Your opponent offers a draw. Do you accept?",
            new Label.LabelStyle(font, Color.WHITE));
        message.setWrap(true);
        message.setAlignment(Align.center);

        Drawable greenUp = skin.getDrawable("image_ui_generic_greenbutton_10");
        Drawable greenDown = skin.getDrawable("image_ui_generic_greenbutton_down_10");
        Drawable purpleUp = skin.getDrawable("image_ui_generic_purplebutton_10");
        Drawable purpleDown = skin.getDrawable("image_ui_generic_purplebutton_down_10");

        MenuButton acceptBtn = new MenuButton(greenUp, "ACCEPT", font, greenDown, null, null,
            () -> { hide(); if (callback != null) callback.onDecision(true); });
        acceptBtn.setSize(160f, 60f);

        MenuButton declineBtn = new MenuButton(purpleUp, "DECLINE", font, purpleDown, null, null,
            () -> { hide(); if (callback != null) callback.onDecision(false); });
        declineBtn.setSize(160f, 60f);

        dialog.add(title).padBottom(15f).row();
        dialog.add(message).width(500f).padBottom(25f).row();
        Table buttons = new Table();
        buttons.add(acceptBtn).size(160f, 60f).padRight(30f);
        buttons.add(declineBtn).size(160f, 60f);
        dialog.add(buttons);

        add(dialog);
    }

    public void showOffer() {
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

    public boolean isShowing() { return showing; }
}
