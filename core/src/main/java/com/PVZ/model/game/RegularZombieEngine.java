package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class RegularZombieEngine implements ZombieEngine {

    private final List<Zombie> zombies = new ArrayList<>();
    private Map map;

    public void bindMap(Map map) {
        this.map = map;

        // if (zombies.isEmpty()) {
        // spawnZombie("ZombieTutorialDefault", 2, 2000);
        // }
    }

    public void update(float delta) {
    }

    public void draw(SpriteBatch batch) {
        batch.begin();
        List<Zombie> sorted = new ArrayList<>(zombies);
        // Sort zombies:
        // 1. Primary: row ascending (0 -> 1 -> 2 -> 3 -> 4) so back lanes (top) are drawn before front lanes (bottom).
        // 2. Secondary: Y descending (higher Y / further back drawn first).
        // 3. Tertiary: X descending (zombies further back / to the right drawn first).
        sorted.sort((a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            int rowComp = Double.compare(a.getRow(), b.getRow());
            if (rowComp != 0) return rowComp;
            int yComp = Double.compare(b.getY(), a.getY());
            if (yComp != 0) return yComp;
            return Double.compare(b.getX(), a.getX());
        });
        for (Zombie z : sorted) {
            if (z != null && (!z.isDead() || com.PVZ.model.entity.zombies.base.ZombieAnimation.isActive(z))) {
                z.draw(batch);
            }
        }
        batch.end();
    }

    public void dispose() {
        for (Zombie z : zombies) {
            z.onDestroy();
        }
        zombies.clear();
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    @Override
    public void kill(Object entity) {
        if (entity instanceof Zombie z) {
            z.setHitpoints(0);
            if (z.getArmor() != null) {
                z.setArmor(null);
            }
            z.onDestroy();
            zombies.remove(z);
        }
    }

    @Override
    public void takeDamage(Object entity, double amount) {
        if (entity instanceof Zombie z) {
            z.takeDamage(amount);
        } else if (entity instanceof Plant p) {
            p.takeDamage((int) amount);
        }
    }

    @Override
    public Plant getPlantAt(int row, int col) {
        return map != null ? map.getPlantAt(row, col) : null;
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        List<Zombie> laneZombies = new ArrayList<>();
        for (Zombie z : zombies) {
            if ((int) z.getRow() == lane) {
                laneZombies.add(z);
            }
        }
        return laneZombies;
    }

    @Override
    public int getSunCount() {
        return 0;
    }

    @Override
    public void addSun(int amount) {
    }

    @Override
    public void spawnProjectile(Projectile p) {
    }

    @Override
    public Zombie spawnZombie(String alias, int row, int col) {
        if (map == null || !map.isWithinBounds(row, col))
            return null;
        Zombie zombie = ZombieType.fromAlias(alias).create();
        if (com.PVZ.model.status.AppStatus.currentUser != null
            && com.PVZ.model.status.AppStatus.currentUser.appStats != null) {
            zombie.applyDifficultyScaling(
                com.PVZ.model.status.AppStatus.currentUser.appStats.getDifficultyLevel());
        }
        Tile tile = map.getTile(row, col);
        float y = tile.getY() + (tile.getHeight() - 70f) / 2f;
        float x;
        if (col >= map.getCols() - 1) {
            x = map.getStartX() + map.getTotalWidth() + 120f + (float)(Math.random() * 80f);
        } else {
            x = tile.getX() + tile.getWidth() / 2f;
        }
        zombie.initPosition(x, y, row);
        zombie.setGlowing(Math.random() < 0.05);
        zombie.setCol(col);
        zombie.onSpawn();
        zombies.add(zombie);
        if (AppStatus.currentUser != null && AppStatus.currentUser.collectionState != null) {
            ZombieType type = ZombieType.fromAlias(alias);
            AppStatus.currentUser.collectionState.seeZombie(type);
            UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
        }
        return zombie;
    }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) {
            map.removePlant(row, col);
        }
    }

    public void spawnZombieInLane(String alias, int row) {

    }

    @Override
    public int getTileColumn(float worldX) {
        return map.worldToCol(worldX);
    }
}
