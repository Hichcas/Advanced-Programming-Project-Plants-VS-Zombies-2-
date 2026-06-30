package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.Iterator;
import java.util.List;

public class BattleController {

    private static final float HOUSE_X = -50;

    public void update(float delta, ZombieEngine engine,
                       List<Plant> plants,
                       List<Projectile> projectiles,
                       List<Zombie> zombies) {

        // zombie ↔ plant
        for (Zombie z : zombies) {
            if (z.isDead()) continue;
            boolean eating = false;
            for (Plant p : plants) {
                if (z.getHitbox().overlaps(p.getHitbox())) {
                    engine.takeDamage(p, z.getEatDPS() * delta);
                    z.stopMoving();
                    eating = true;
                    break;
                }
            }
            if (!eating) z.startMoving();
        }

        // projectile ↔ zombie
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            boolean hit = false;
            for (Zombie z : zombies) {
                if (z.isDead()) continue;
                if (p.getHitbox().overlaps(z.getHitbox())) {
                    engine.takeDamage(z, p.getDamage());
                    projIt.remove();
                    hit = true;
                    break;
                }
            }
        }

        //zombie gets house
        for (Zombie z : zombies) {
            if (z.getX() <= HOUSE_X) {
                // game over
            }
        }

        // dead zombies
        Iterator<Zombie> zit = zombies.iterator();
        while (zit.hasNext()) {
            Zombie z = zit.next();
            if (z.isDead()) {
                z.onDestroy();
                zit.remove();
            }
        }
    }
}
