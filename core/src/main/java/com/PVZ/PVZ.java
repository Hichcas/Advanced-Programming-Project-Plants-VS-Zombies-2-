package com.PVZ;

import com.PVZ.controller.AppController;
import com.PVZ.database.UserDatabase;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.screen.GameScreen;
import com.PVZ.screen.manager.BrightnessController;
import com.PVZ.screen.manager.FontManager;
import com.PVZ.screen.manager.MusicManager;
import com.PVZ.screen.manager.ScreenManager;
import com.PVZ.view.input.CommandParser;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PVZ extends Game {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
//          // here is what we used to have in previous Main.java file

        com.PVZ.screen.manager.ScreenManager.getInstance().init(this);
        AppStatus.setPVZ(this);
        CommandParser.start();
        AppStatus.currentMenuType = MenuType.REGISTER;
        AppStatus.setQuality(GraphicsQuality.Ultra_High);
//        batch = new SpriteBatch();
//        image = new Texture("libgdx.png");

        try {
            UserDatabase.init();
        } catch (Exception e) {
            System.err.println("UserDatabase init failed: " + e.getMessage());
        }

        ScreenManager.getInstance().startWithFadeIn(new GameScreen("libgdx.png", "music/Title Screen.mp3", new RegularGameEngine(new GameStatus())));
    }

    @Override
    public void render() {
        com.PVZ.screen.manager.MusicManager.getInstance().update(Gdx.graphics.getDeltaTime());
//        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
//        batch.begin();
//        batch.draw(image, 140, 210);
//        batch.end();
        super.render();
        com.PVZ.screen.manager.ScreenManager.getInstance().updateAndRender(Gdx.graphics.getDeltaTime());

//        System.out.println("the Start is triggered");
        AppController.render();
        UserRegistry.saveAllDirtyUsers();
    }

    @Override
    public void dispose() {
        UserRegistry.clear();
        MusicManager.getInstance().dispose();
        FontManager.getInstance().dispose();
        ScreenManager.getInstance().dispose();
        BrightnessController.getInstance().dispose();
        CommandParser.end();
//        batch.dispose();
//        image.dispose();
    }

    public void updateGraphics(GraphicsQuality quality) {
        Gdx.graphics.setWindowedMode(quality.width, quality.height);
        Gdx.graphics.setVSync(quality.vSync);
        System.out
            .println("Graphics updated to: " + quality.name() + " (" + quality.width + "x" + quality.height + ")");
    }
}
