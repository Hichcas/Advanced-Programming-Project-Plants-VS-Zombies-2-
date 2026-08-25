package com.PVZ.view.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.PVZ.view.screen.manager.FontManager;
import pvz.skin.PvzSkin;

public class CountdownOverlay extends Table {

    private final Label numberLabel;
    private final long startAt;
    private final Runnable onComplete;
    private boolean completed = false;

    public CountdownOverlay(long startAt, Runnable onComplete) {
        this.startAt = startAt;
        this.onComplete = onComplete;

        setFillParent(true);
        setTouchable(Touchable.enabled);

        Skin skin = PvzSkin.get();
        Drawable bg = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        if (bg != null) setBackground(bg);
        pad(30f);

        BitmapFont font = FontManager.getInstance().getEnglishTitleFont();
        numberLabel = new Label("3", new Label.LabelStyle(font, Color.GOLD));
        numberLabel.setFontScale(3.0f);
        numberLabel.setAlignment(Align.center);
        add(numberLabel).expand().center();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        long remainingMs = startAt - System.currentTimeMillis();
        if (remainingMs <= 0) {
            if (!completed) {
                completed = true;
                remove();
                if (onComplete != null) onComplete.run();
            }
            return;
        }
        int seconds = (int) Math.ceil(remainingMs / 1000.0);
        numberLabel.setText(seconds == 0 ? "GO!" : String.valueOf(seconds));
    }

    public static void show(Stage stage, long startAt, Runnable onComplete) {
        CountdownOverlay overlay = new CountdownOverlay(startAt, onComplete);
        stage.addActor(overlay);
        overlay.toFront();
    }
}
