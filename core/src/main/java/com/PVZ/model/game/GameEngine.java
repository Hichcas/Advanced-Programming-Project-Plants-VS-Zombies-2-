package com.PVZ.model.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameEngine {

    public final GameStatus gameStatus;

    GameEngine (GameStatus gameStatus) {this.gameStatus = gameStatus;}

    public void render(float delta, SpriteBatch batch) {
        this.update(delta);
    }
    public abstract void update(float delta);
    public abstract void draw(SpriteBatch batch);
    public abstract void dispose();
}
