package com.PVZ;

import com.PVZ.controller.AppController;
import com.PVZ.database.UserDatabase;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.model.user.User;
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
import com.PVZ.util.GameInitialization;
import java.io.IOException;
public class PVZ extends Game {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        try {
            GameInitialization.initialize();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load plant data JSON", e);
        }
        com.PVZ.screen.manager.ScreenManager.getInstance().init(this);
        AppStatus.setPVZ(this);
        CommandParser.start();
        AppStatus.setQuality(GraphicsQuality.Ultra_High);

        try {
            UserDatabase.init();
        } catch (Exception e) {
            System.err.println("UserDatabase init failed: " + e.getMessage());
        }

        ChapterLibrary.load();

        UserRegistry.loadAllFromDatabase();
        User autoUser = null;
        for (User u : UserRegistry.allUsers()) {
            if (u.isStayLoggedIn()) {
                autoUser = u;
                break;
            }
        }
        if (autoUser != null) {
            AppStatus.currentUser = autoUser;
            AppStatus.currentMenuType = MenuType.MAIN;
        } else {
            AppStatus.currentMenuType = MenuType.REGISTER;
        }

        ScreenManager.getInstance().startWithFadeIn(new GameScreen("maps/Frontyard.jpg", "music/Title Screen.mp3",
                new RegularGameEngine(new GameStatus())));
    }
    @Override
    public void render() {
        com.PVZ.screen.manager.MusicManager.getInstance().update(Gdx.graphics.getDeltaTime());
        super.render();
        com.PVZ.screen.manager.ScreenManager.getInstance().updateAndRender(Gdx.graphics.getDeltaTime());

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
    }

    public void updateGraphics(GraphicsQuality quality) {
        Gdx.graphics.setWindowedMode(quality.width, quality.height);
        Gdx.graphics.setVSync(quality.vSync);
        System.out
            .println("Graphics updated to: " + quality.name() + " (" + quality.width + "x" + quality.height + ")");
    }
}
