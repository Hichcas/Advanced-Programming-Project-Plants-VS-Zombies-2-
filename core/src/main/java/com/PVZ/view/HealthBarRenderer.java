package com.PVZ.view;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Draws a small health bar above an entity. Green for friendly units (plants),
 * red for enemies (zombies). Call it while a SpriteBatch is between begin()/end().
 */
public class HealthBarRenderer {
    private static Texture white;

    private static Texture tex() {
        if (white == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(1f, 1f, 1f, 1f);
            p.fill();
            white = new Texture(p);
            p.dispose();
        }
        return white;
    }

    public static void draw(SpriteBatch batch, float x, float y, float width, float ratio, boolean friendly) {
        float h = 5f;
        float clamped = Math.max(0f, Math.min(1f, ratio));
        // background
        batch.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        batch.draw(tex(), x, y, width, h);
        // fill
        if (friendly) {
            batch.setColor(0.2f, 0.9f, 0.2f, 1f);
        } else {
            batch.setColor(0.95f, 0.2f, 0.2f, 1f);
        }
        batch.draw(tex(), x, y, width * clamped, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
