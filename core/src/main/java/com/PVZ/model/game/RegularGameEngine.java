package com.PVZ.model.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RegularGameEngine extends GameEngine {

    public RegularGameEngine(GameStatus gameStatus) {
        super(gameStatus, new RegularInputProcessor());
        ((RegularInputProcessor)inputProcessor).setRegularGameEngine(this);
    }

    @Override
    public void update (float delta) {

    }

    public void draw (SpriteBatch batch) {

    }

    public void dispose () {

    }
}
