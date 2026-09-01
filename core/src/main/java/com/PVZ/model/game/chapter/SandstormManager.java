package com.PVZ.model.game.chapter;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.view.renderer.EntityRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SandstormManager {
    public enum State { INTRO, LOOP, OUTRO, DONE }

    public static class Sandstorm {
        private final int row;
        private final int targetCol;
        private final float targetX;
        private float currentX;
        private final float y;
        private final String zombieAlias;
        private State state = State.INTRO;
        private float stateTime = 0f;
        private final float speed;

        public Sandstorm(int row, int targetCol, float startX, float targetX,
                         float y, String zombieAlias, float speed) {
            this.row = row;
            this.targetCol = targetCol;
            this.currentX = startX;
            this.targetX = targetX;
            this.y = y;
            this.zombieAlias = zombieAlias;
            this.speed = speed;
        }

        public void update(float delta, RegularGameEngine engine) {
            stateTime += delta;
            switch (state) {
                case INTRO:
                    if (stateTime >= 0.33f) {
                        state = State.LOOP;
                        stateTime = 0f;
                    }
                    break;
                case LOOP:
                    currentX -= speed * delta;
                    if (currentX <= targetX) {
                        currentX = targetX;
                        state = State.OUTRO;
                        stateTime = 0f;
                    }
                    break;
                case OUTRO:
                    if (stateTime >= 0.33f) {
                        state = State.DONE;
                        if (engine != null && engine.getZombieEngine() != null) {
                            engine.getZombieEngine().spawnZombie(zombieAlias, row, targetCol);
                        }
                    }
                    break;
                case DONE:
                    break;
            }
        }

        public void draw(SpriteBatch batch) {
            if (state == State.DONE || batch == null) return;
            String clip = switch (state) {
                case INTRO -> "intro";
                case LOOP -> "loop";
                case OUTRO -> "outro";
                default -> "loop";
            };

            // 1. Rear layer behind zombie
            EntityRenderer.getInstance().renderPam(batch,
                "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM",
                clip, stateTime, currentX, y, 1.0f);

            // 2. Front layer in front of zombie
            EntityRenderer.getInstance().renderPam(batch,
                "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM",
                clip, stateTime, currentX, y, 1.0f);
        }

        public boolean isDone() {
            return state == State.DONE;
        }

        public int getRow() { return row; }
        public int getTargetCol() { return targetCol; }
        public float getCurrentX() { return currentX; }
        public State getState() { return state; }
        public String getZombieAlias() { return zombieAlias; }
    }

    private final List<Sandstorm> activeSandstorms = new ArrayList<>();
    private final Random random = new Random();

    public void triggerSandstorm(RegularGameEngine engine, int count) {
        if (engine == null || engine.getMap() == null) return;
        com.PVZ.model.status.AppStatus.showAnnouncement("SANDSTORM!");
        String[] egyptZombies = {
            "ZombieMummyDefault",
            "ZombieMummyArmor1Default",
            "ZombieMummyArmor2Default",
            "ZombieRaDefault"
        };

        for (int i = 0; i < count; i++) {
            int row = random.nextInt(5);
            int targetCol = 3 + random.nextInt(3); // Column 3, 4, or 5
            float startX = 950f + i * 40f;
            float[] center = engine.getPlantWorldCenter(row, targetCol);
            float targetX = center[0];
            float y = center[1];
            String zAlias = egyptZombies[random.nextInt(egyptZombies.length)];
            float speed = 250f + random.nextFloat() * 80f;

            activeSandstorms.add(new Sandstorm(row, targetCol, startX, targetX, y, zAlias, speed));
        }
    }

    public void update(float delta, RegularGameEngine engine) {
        Iterator<Sandstorm> it = activeSandstorms.iterator();
        while (it.hasNext()) {
            Sandstorm s = it.next();
            s.update(delta, engine);
            if (s.isDone()) {
                it.remove();
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (Sandstorm s : activeSandstorms) {
            s.draw(batch);
        }
    }

    public List<Sandstorm> getActiveSandstorms() {
        return activeSandstorms;
    }

    public void clear() {
        activeSandstorms.clear();
    }
}
