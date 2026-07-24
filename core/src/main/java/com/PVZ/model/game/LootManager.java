package com.PVZ.model.game;

import com.PVZ.model.entity.LootDrop;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks coin/diamond/pot drops sitting on the lawn (doc page 28), the same way
 * {@link SunManager} tracks suns: spawned at a world position, drawn until collected or expired.
 */
public class LootManager {
    private final List<LootDrop> drops = new ArrayList<>();

    public List<LootDrop> getDrops() {
        return Collections.unmodifiableList(drops);
    }

    public LootDrop spawn(double x, double y, LootDrop.LootType type) {
        LootDrop drop = new LootDrop(x, y, type);
        drops.add(drop);
        return drop;
    }

    public void update(float delta) {
        for (LootDrop drop : drops) {
            drop.update(delta);
        }
        Iterator<LootDrop> iterator = drops.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isCollected()) {
                iterator.remove();
            }
        }
    }

    /** Returns the drop under the given point (world coordinates), or null if none. */
    public LootDrop collectAt(double x, double y) {
        Rectangle point = new Rectangle((float) x - 8f, (float) y - 8f, 16f, 16f);
        for (LootDrop drop : drops) {
            if (!drop.isCollected() && point.overlaps(drop.getHitbox())) {
                drop.collect();
                return drop;
            }
        }
        return null;
    }

    public void clear() {
        drops.clear();
    }
}
