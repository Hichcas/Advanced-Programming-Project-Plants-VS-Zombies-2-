package com.PVZ.model.game;

import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CombatHandler {

    // ---------- ZombieEngine methods ----------
    public static List<Zombie> getZombiesInLane(RegularGameEngine engine, int lane) {
        if (lane < 0) return List.of();
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : engine.getZombieList()) {
            if (zombie != null && (int) zombie.getRow() == lane && !zombie.isDead()) {
                result.add(zombie);
            }
        }
        return result;
    }

    public static Zombie spawnZombie(RegularGameEngine engine, String alias, int row, int x) {
        if (engine.zombieEngine != null) return engine.zombieEngine.spawnZombie(alias, row, x);
        return null;
    }

    // ---------- Projectile ----------
    public static void spawnProjectile(RegularGameEngine engine, Object projectile) {
        if (projectile instanceof Projectile p) {
            placeProjectileOnMap(engine, p);
            engine.projectiles.add(p);
        }
    }

    public static void spawnProjectile(RegularGameEngine engine, Projectile p) {
        if (p != null) {
            placeProjectileOnMap(engine, p);
            engine.projectiles.add(p);
        }
    }

    private static void placeProjectileOnMap(RegularGameEngine engine, Projectile p) {
        if (p.isWorldPositioned()) return;
        int row = p.getRow();
        float tileWidth = 177f, tileHeight = 234f, startX = 550f, startY = 1240f;
        if (engine.map != null) {
            tileWidth = engine.map.getTileWidth();
            tileHeight = engine.map.getTileHeight();
            startX = engine.map.getStartX();
            startY = engine.map.getStartY();
        }
        int col = 0;
        Object colState = p.getExtra("originCol");
        if (colState instanceof Number number) col = number.intValue();

        float worldX = startX + col * tileWidth + tileWidth * 0.5f;
        float worldY = startY - (row + 1) * tileHeight + tileHeight * 0.35f;
        float speedPxPerSec = tileWidth * 1.5f;
        if (p.getType() == com.PVZ.model.entity.plants.behavior.impl.ProjectileType.LOB) {
            speedPxPerSec = tileWidth * 0.9f;
        }
        float speedMultiplier = (float) Math.max(0.1, Math.abs(p.getSpeed()));
        speedPxPerSec *= speedMultiplier;

        double horizontalSign = p.getSpeed() < 0 ? -1.0 : 1.0;
        double verticalSpeed = 0.0;
        Object targetLaneState = p.getExtra("targetLane");
        if (targetLaneState instanceof Number number) {
            int targetLane = number.intValue();
            if (targetLane != row) {
                verticalSpeed = Math.signum(targetLane - row) * speedPxPerSec;
            }
        }
        p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
    }

    // ---------- BehaviorContext damage methods ----------
    public static void damageArea(RegularGameEngine engine, int lane, int row, int damage) {
        if (damage <= 0) return;
        for (Zombie zombie : getZombiesInLane(engine, lane)) {
            if (zombie != null) zombie.takeDamage(damage);
        }
    }

    public static void freezeZombiesInLane(RegularGameEngine engine, int lane, double seconds) {
        for (Zombie zombie : getZombiesInLane(engine, lane)) {
            if (zombie != null) zombie.freeze((float) seconds);
        }
    }

    public static void freezeAllZombies(RegularGameEngine engine, double seconds) {
        for (Zombie zombie : engine.getZombieList()) {
            if (zombie != null && !zombie.isDead()) zombie.freeze((float) seconds);
        }
    }

    public static void disarmZombiesInLane(RegularGameEngine engine, int lane) {
        for (Zombie zombie : getZombiesInLane(engine, lane)) {
            if (zombie != null) zombie.setArmor(null);
        }
    }

    public static void moveZombiesFromLane(RegularGameEngine engine, int sourceLane, int targetLane) {
        int clampedTarget = Math.max(0, Math.min(5 - 1, targetLane));
        for (Zombie zombie : getZombiesInLane(engine, sourceLane)) {
            if (zombie != null) {
                zombie.setRow(clampedTarget);
                zombie.setY(clampedTarget * 100.0);
            }
        }
    }

    public static void pullAdjacentZombiesToLane(RegularGameEngine engine, int lane) {
        moveZombiesFromLane(engine, lane - 1, lane);
        moveZombiesFromLane(engine, lane + 1, lane);
    }

    public static void killRandomZombies(RegularGameEngine engine, int count) {
        List<Zombie> alive = new ArrayList<>();
        for (Zombie z : engine.getZombieList()) {
            if (z != null && !z.isDead()) alive.add(z);
        }
        Collections.shuffle(alive, engine.random);
        for (int i = 0; i < Math.min(count, alive.size()); i++) {
            alive.get(i).takeDamage(Double.MAX_VALUE);
        }
    }

    public static void killClosestZombieInLane(RegularGameEngine engine, int lane) {
        List<Zombie> laneZombies = new ArrayList<>(getZombiesInLane(engine, lane));
        if (laneZombies.isEmpty()) return;
        laneZombies.sort(Comparator.comparingDouble(Zombie::getX));
        laneZombies.get(0).takeDamage(Double.MAX_VALUE);
    }

    public static void hypnotizeZombiesInLane(RegularGameEngine engine, int lane, double seconds) {
        for (Zombie zombie : getZombiesInLane(engine, lane)) {
            if (zombie != null) zombie.hypnotize((float) seconds);
        }
    }

    public static String zombiesInfoText(RegularGameEngine engine) {
        StringBuilder sb = new StringBuilder();
        for (Zombie zombie : engine.getZombieList()) {
            if (zombie != null && !zombie.isDead()) sb.append(zombie.getStatusString()).append('\n');
        }
        return sb.length() == 0 ? "No zombies." : sb.toString().trim();
    }
}
