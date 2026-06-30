package com.PVZ.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Projectile {
    public void update(float delta) { /*positionX += speed * delta;*/ }
    public void draw(SpriteBatch batch) { }
}
