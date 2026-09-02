package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameEngine {
    public final InputAdapter inputProcessor;
    public final GameStatus gameStatus;
    public Map map;

    GameEngine(GameStatus gameStatus, InputAdapter inputProcessor) {
        this.gameStatus = gameStatus;
        this.inputProcessor = inputProcessor;
    }

    public void render(float delta, SpriteBatch batch) {
        this.update(delta);
        this.draw(batch);
    }

    public abstract void update(float delta);

    public abstract void draw(SpriteBatch batch);

    public abstract void dispose();

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    /** Parked lawnmower for this row, or null when this mode has none. */
    public LawnMower getLawnMower(int row) {
        return null;
    }

    public Texture getBackgroundOverride() {
        return null;
    }

    public Texture getBackgroundOverrideRight() {
        return null;
    }
}
