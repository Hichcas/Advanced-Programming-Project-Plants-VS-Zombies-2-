package com.PVZ.screen;

import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameHud;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularInputProcessor;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.manager.MusicManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    private final GameEngine gameEngine;
    private final String mapPath;
    private final String musicPath;
    private Map gameMap;
    private ShapeRenderer shapeDebug;

    // * تکسچر پس‌زمینه مستقیماً اینجا مدیریت می‌شود
    private Texture backgroundTexture;

    @Override
    public void show () {
        // در سازنده یا show
        multiplexer.clear();
        multiplexer.addProcessor(gameEngine.inputProcessor); // اول این
        multiplexer.addProcessor(stage); // بعد stage
        super.show();
    }


    public GameScreen(String mapPath, String musicPath, GameEngine gameEngine) {
        super();
        this.gameBatch = new SpriteBatch();
        this.mapPath = mapPath;
        this.musicPath = musicPath;

        backgroundTexture = new Texture(mapPath);

        MusicManager.getInstance().playMusic(musicPath);
        this.gameEngine = gameEngine ;
        AppStatus.setGameEngine(gameEngine);

        gameHud = new GameHud();
        stage.addActor(gameHud);

        gameMap = new Map(550, 1240, 1600, 1170, 5, 9);  // 5 ردیف، 9 ستون
        shapeDebug = new ShapeRenderer();

        // اضافه کردن gameMap به engine (اگر نیاز است)
        gameEngine.setMap(gameMap);
    }

    @Override
    protected void renderScreen(float delta) {
        // ۱. به‌روزرسانی منطق بازی (زامبی‌ها، گیاهان و ...)
        gameEngine.render(Math.min(delta, 1 / 30f), gameBatch);

        // ۲. رسم پس‌زمینه ثابت
        gameBatch.setProjectionMatrix(camera.combined);
        gameBatch.begin();
        // * کل صفحه را با تکسچر پس‌زمینه پر می‌کنیم
        // * (ابعاد VIRTUAL_WIDTH/HEIGHT از BaseScreen برابر 2560x1440 است)
        gameBatch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH + 500, VIRTUAL_HEIGHT);
        gameBatch.end();
        shapeDebug.setProjectionMatrix(camera.combined);
        shapeDebug.begin(ShapeRenderer.ShapeType.Line);
        gameMap.renderBorders(shapeDebug);   // اینجا ۴۵ مستطیل سبز می‌کشد
        shapeDebug.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeDebug.dispose();
        if (gameBatch != null) gameBatch.dispose();
        if (gameEngine != null) gameEngine.dispose();
        // * آزادسازی تکسچر پس‌زمینه
        if (backgroundTexture != null) backgroundTexture.dispose();
        System.out.println("[GameScreen] PVZ resources disposed cleanly.");
    }
}
