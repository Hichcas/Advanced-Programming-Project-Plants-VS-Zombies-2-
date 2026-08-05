package com.PVZ;

import com.PVZ.controller.AppController;
import com.PVZ.database.UserDatabase;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.model.user.User;
import com.PVZ.view.screen.MainMenuScreen;
import com.PVZ.view.input.CommandParser;
import com.PVZ.view.screen.manager.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.PVZ.util.GameInitialization;
import java.io.IOException;
public class PVZ extends Game {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        CursorManager.getInstance();
        try {
            GameInitialization.initialize();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load plant data JSON", e);
        }
        ScreenManager.getInstance().init(this);
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
//
//        ScreenManager.getInstance().startWithFadeIn(new GameScreen("maps/Frontyard.jpg", "music/Title Screen.mp3",
//                new RegularGameEngine(new GameStatus())));

        ScreenManager.getInstance().startWithFadeIn(new MainMenuScreen());
    }
    @Override
    public void render() {
        MusicManager.getInstance().update(Gdx.graphics.getDeltaTime());
        super.render();
        ScreenManager.getInstance().updateAndRender(Gdx.graphics.getDeltaTime());

        AppController.render();
        UserRegistry.saveAllDirtyUsers();
    }

    @Override
    public void dispose() {
        CursorManager.getInstance().dispose();
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
