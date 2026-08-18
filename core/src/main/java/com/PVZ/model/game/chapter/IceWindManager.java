package com.PVZ.model.game.chapter;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.view.renderer.EntityRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IceWindManager {

    public static class IceWindGust {
        private final int[] rows;
        private float currentX = 950f;
        private float stateTime = 0f;
        private final float duration = 2.5667f;
        private final float speed = 400f;
        private boolean applied = false;

        public IceWindGust(int[] rows) {
            this.rows = rows;
        }

        public void update(float delta, RegularGameEngine engine) {
            stateTime += delta;
            currentX -= speed * delta;
        }

        public boolean isDone() {
            return stateTime >= duration || currentX < -150f;
        }

        public void draw(SpriteBatch batch, RegularGameEngine engine) {
            if (isDone() || engine == null || batch == null) return;
            for (int r : rows) {
                float[] center = engine.getPlantWorldCenter(r, 4);
                EntityRenderer.getInstance().renderPam(batch,
                    "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM",
                    "animation", stateTime, currentX, center[1], 1.0f);
            }
        }

        public int[] getRows() { return rows; }
        public float getStateTime() { return stateTime; }
    }

    private final List<IceWindGust> activeGusts = new ArrayList<>();

    public void triggerIceWind(RegularGameEngine engine, int[] rows) {
        if (engine == null) return;
        com.PVZ.model.status.AppStatus.showAnnouncement("FREEZING WIND!");
        activeGusts.add(new IceWindGust(rows));
    }

    public void update(float delta, RegularGameEngine engine) {
        Iterator<IceWindGust> it = activeGusts.iterator();
        while (it.hasNext()) {
            IceWindGust g = it.next();
            g.update(delta, engine);
            if (g.isDone()) {
                it.remove();
            }
        }
    }

    public void draw(SpriteBatch batch, RegularGameEngine engine) {
        for (IceWindGust g : activeGusts) {
            g.draw(batch, engine);
        }
    }

    public List<IceWindGust> getActiveGusts() {
        return activeGusts;
    }

    public void clear() {
        activeGusts.clear();
    }
}
