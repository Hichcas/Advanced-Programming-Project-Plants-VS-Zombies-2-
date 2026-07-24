package com.PVZ.model.game;

import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CombatHandler {

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
        if (p.getType() == com.PVZ.model.entity.plants.behavior.impl.ProjectileType.LOB) {

            p.initArcPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec));
        } else {
            p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
        }
    }

    public static void damageArea(RegularGameEngine engine, int lane, int row, int damage) {
        if (damage <= 0) return;
        int[] lanes = {row - 1, row, row + 1};
        for (int r : lanes) {
            if (r < 0) continue;
            for (Zombie zombie : getZombiesInLane(engine, r)) {
                if (zombie != null) {
                    double colDist = Math.abs(zombie.getX() - (engine.map != null
                        ? engine.map.getStartX() + lane * engine.map.getTileWidth() : 0));
                    if (colDist < (engine.map != null
                        ? engine.map.getTileWidth() * 1.6 : 280.0)) {
                        zombie.takeDamage(damage);
                    }
                }
            }
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
        int clampedTarget = Math.max(0, Math.min(engine.map != null ? engine.map.getRows() - 1 : 4, targetLane));
        for (Zombie zombie : getZombiesInLane(engine, sourceLane)) {
            if (zombie != null) {
                zombie.setRow(clampedTarget);
                // Use the map's actual tile height to compute the correct world Y-coordinate
                // (previously hardcoded target*100.0, which was wrong for the 234px tile grid).
                if (engine.map != null) {
                    float tileHeight = engine.map.getTileHeight();
                    float startY = engine.map.getStartY();
                    double newY = startY - (clampedTarget + 1) * tileHeight + tileHeight * 0.35;
                    zombie.setY(newY);
                } else {
                    zombie.setY(clampedTarget * 100.0);
                }
            }
        }
    }

    public static void pullAdjacentZombiesToLane(RegularGameEngine engine, int lane) {
        moveZombiesFromLane(engine, lane - 1, lane);
        moveZombiesFromLane(engine, lane + 1, lane);
    }

    public static void spawnBouncingProjectiles(RegularGameEngine engine, int lane, int row,
                                                int count, int damagePerGrape, double lifespanSeconds) {
        if (engine.map == null) return;
        float tileW = engine.map.getTileWidth();
        float tileH = engine.map.getTileHeight();
        float centreX = engine.map.getStartX() + lane * tileW + tileW * 0.5f;
        float centreY = engine.map.getStartY() - (row + 1) * tileH + tileH * 0.5f;
        float minX = engine.map.getStartX();
        float maxX = minX + 9 * tileW;
        float minY = engine.map.getStartY() - 5 * tileH;
        float maxY = engine.map.getStartY();
        double fuseSec = Math.max(lifespanSeconds, 4.0);
        double speed = tileW * 0.28;
        for (int i = 0; i < Math.max(1, Math.min(count, 20)); i++) {
            double angle = 2 * Math.PI * i / count + (engine.random.nextDouble() - 0.5) * 0.4;
            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed * 0.6);
            Projectile grape = new Projectile();
            grape.setType(ProjectileType.GRAPE);
            grape.setDamage(Math.max(1, damagePerGrape));
            grape.setPierce(0);
            grape.initFreePosition(centreX, centreY, vx, vy);
            grape.setBouncing(true);
            grape.setFuse(fuseSec);
            grape.setBounds(minX, maxX, minY, maxY);
            grape.setSpeed(0);
            engine.projectiles.add(grape);
        }
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

    public static void damageSingleTarget(RegularGameEngine engine, Object target, int damage) {
        if (target instanceof Zombie z && z != null && !z.isDead()) {
            z.takeDamage(damage);
        }
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
