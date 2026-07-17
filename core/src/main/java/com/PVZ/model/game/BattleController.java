package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.Iterator;
import java.util.List;

public class BattleController implements BehaviorContext {

    private final List<Zombie> zombies;
    private final List<Plant> plants;
    private final List<Projectile> projectiles;
    private final List<ZombieProjectile> zombieProjectiles = new java.util.ArrayList<>();
    private final GameStatus gameStatus;
    private Map map;

    public BattleController(List<Zombie> zombies, List<Plant> plants,
                            List<Projectile> projectiles, GameStatus gameStatus) {
        this.zombies = zombies;
        this.plants = plants;
        this.projectiles = projectiles;
        this.gameStatus = gameStatus;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void update(float delta) {
        for (int i = zombies.size() - 1; i >= 0; i--) {
            zombies.get(i).update(delta, this);
        }

        // zombie projectiles
        java.util.Iterator<ZombieProjectile> zpIt = zombieProjectiles.iterator();
        while (zpIt.hasNext()) {
            ZombieProjectile zp = zpIt.next();
            zp.update(delta);
            if (zp.isDestroyed()) { zpIt.remove(); continue; }

            int zpCol = getTileColumn(zp.getX());
            Plant p = getPlantAt(zp.getRow(), zpCol);
            if (p != null && !p.isDead()) {
                p.takeDamage(zp.getDamage());
                zp.getOwner().onProjectileHit(p);
                zp.destroy();
                zpIt.remove();
            }
        }

        // plant projectiles
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            for (Zombie z : zombies) {
                if (z.isDead()) continue;
                if (p.getHitbox().overlaps(z.getHitbox())) {
                    z.takeDamage((int) p.getDamage(), resolveDamageType(p));
                    if (Boolean.TRUE.equals(p.getExtra("stunOnHit"))) {
                        z.freeze(1.5f); // Kernel-pult's "butter" shot: brief stun on contact
                    }
                    projIt.remove();
                    break;
                }
            }
        }

        Iterator<Plant> pit = plants.iterator();
        while (pit.hasNext()) {
            Plant p = pit.next();
            if (p.isDead()) {
                int r = asInt(p.getRuntimeState("row"), 0);
                int c = asInt(p.getRuntimeState("col"), 0);
                if (map != null) map.removePlant(r, c);
                pit.remove();
            }
        }
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        java.util.List<Zombie> out = new java.util.ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie != null && !zombie.isDead() && (int) zombie.getRow() == lane) {
                out.add(zombie);
            }
        }
        return out;
    }

    @Override
    public List<Zombie> getAllZombies() {
        return new java.util.ArrayList<>(zombies);
    }

    @Override
    public Plant getPlantAt(int row, int col) {
        return map != null ? map.getPlantAt(row, col) : null;
    }

    @Override
    public List<Plant> getAllPlants() {
        return new java.util.ArrayList<>(plants);
    }

    public List<Plant> getPlants() {
        return plants;
    }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) {
            map.removePlant(row, col);
        }
        plants.removeIf(p -> p != null && !p.isDead()
                && asInt(p.getRuntimeState("row"), Integer.MIN_VALUE) == row
                && asInt(p.getRuntimeState("col"), Integer.MIN_VALUE) == col);
    }

    @Override
    public void spawnProjectile(Object projectile) {
        if (projectile instanceof Projectile p) {
            placeProjectileOnMap(p);
            projectiles.add(p);
        }
    }

    private void placeProjectileOnMap(Projectile p) {
        if (p == null || p.isWorldPositioned()) {
            return;
        }
        int row = p.getRow();
        float tileWidth = 177f;
        float tileHeight = 234f;
        float startX = 550f;
        float startY = 1240f;
        if (map != null) {
            tileWidth = map.getTileWidth();
            tileHeight = map.getTileHeight();
            startX = map.getStartX();
            startY = map.getStartY();
        }

        int col = 0;
        Object colState = p.getExtra("originCol");
        if (colState instanceof Number number) {
            col = number.intValue();
        }

        float worldX = startX + col * tileWidth + tileWidth * 0.5f;
        float worldY = startY - (row + 1) * tileHeight + tileHeight * 0.35f;

        // ~1.5 tiles per second feels like the classic PVZ pea speed.
        float speedPxPerSec = tileWidth * 1.5f;
        if (p.getType() == ProjectileType.LOB) {
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
        if (p.getType() == ProjectileType.LOB && verticalSpeed == 0.0) {
            p.initArcPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec));
            return;
        }

        p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
    }

    @Override
    public void spawnSun(int amount) {
        addSun(amount);
    }

    @Override
    public void addSun(int amount) {
        if (gameStatus == null || amount == 0) {
            return;
        }
        gameStatus.setSunflower(Math.max(0, gameStatus.getSunflower() + amount));
    }

    @Override
    public void damageArea(int lane, int row, int damage) {
        if (damage <= 0) {
            return;
        }
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.takeDamage(damage);
            }
        }
    }

    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }


    public void triggerGameOver() {
        if (gameStatus == null || gameStatus.isGameOver()) {
            return;
        }
        System.out.println("GAME OVER — zombie reached the house!");
        gameStatus.setGameOver(true);
        AppStatus.returnToMainMenu();
    }

    public void removeZombie(Zombie zombie) {
        zombies.remove(zombie);
    }

    private DamageType resolveDamageType(Projectile p) {
        if (p.getType() == ProjectileType.ICE_PEA) return DamageType.ICE;
        Object dt = p.getExtra("damageType");
        if (dt instanceof DamageType) return (DamageType) dt;
        if (p.getType() == ProjectileType.FIRE_PEA) return DamageType.NORMAL;
        return DamageType.NORMAL;
    }

    public Zombie findZombieAt(int col, int row) {
        for (Zombie z : zombies) {
            if ((int)z.getRow() == row && getTileColumn((float)z.getX()) == col)
                return z;
        }
        return null;
    }

    public void addZombieProjectile(ZombieProjectile p) {
        zombieProjectiles.add(p);
    }

    public void drawProjectiles(SpriteBatch batch) {
        for (ZombieProjectile zp : zombieProjectiles) {
            zp.draw(batch);
        }
    }

    public void addZombie(Zombie z) {
        zombies.add(z);
    }

    public TileType getTileTypeAt(int row, int col) {
        Tile tile = map.getTile(row, col);
        return tile != null ? tile.getType() : TileType.NORMAL;
    }

    public void setTileTypeAt(int row, int col, TileType type) {
        Tile tile = map.getTile(row, col);
        if (tile != null) tile.setType(type);
    }

    public void dispose() {
        zombieProjectiles.clear();
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
