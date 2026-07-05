package com.PVZ.model.game;

import com.PVZ.model.entity.Sun;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SunManager {
    private final List<Sun> suns = new ArrayList<>();

    public List<Sun> getSuns() {
        return Collections.unmodifiableList(suns);
    }

    public Sun spawn(double x, double y, int amount) {
        Sun sun = new Sun(x, y, amount);
        suns.add(sun);
        System.out.println("New sun is dropping at position (" + Math.round(x) + ", " + Math.round(y) + ")");
        return sun;
    }

    public Sun spawnFalling(double x, double y, int amount, double groundY) {
        Sun sun = spawn(x, y, amount);
        sun.configureFalling(180.0, groundY);
        return sun;
    }

    public void update(float delta) {
        for (Sun sun : suns) {
            sun.update(delta);
            if (sun.isFalling() && sun.hasReachedGround() && !sun.isGroundNotified()) {
                System.out.println("Sun reached the ground at position (" + Math.round(sun.getX()) + ", " + Math.round(sun.getY()) + ")");
                sun.setGroundNotified(true);
            }
        }

        Iterator<Sun> iterator = suns.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            if (sun.isCollected()) {
                iterator.remove();
            }
        }
    }

    public int collectAt(Rectangle area) {
        if (area == null) {
            return 0;
        }

        int collectedAmount = 0;
        for (Sun sun : suns) {
            if (!sun.isCollected() && area.overlaps(sun.getHitbox())) {
                sun.collect();
                collectedAmount += sun.getAmount();
            }
        }
        return collectedAmount;
    }

    public int collectAt(double x, double y) {
        Rectangle area = new Rectangle((float) x, (float) y, 50, 50);
        return collectAt(area);
    }

    public void clear() {
        suns.clear();
    }
}
