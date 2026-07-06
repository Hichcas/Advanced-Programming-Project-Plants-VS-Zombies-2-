package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
public interface ZombieEngine {
        void kill(Object entity);
        void takeDamage(Object entity, double amount);
        Plant getPlantAt(int row, int col);
        List<Zombie> getZombiesInLane(int lane);
        int getSunCount();
        void addSun(int amount);
        void spawnProjectile(Projectile p);
    void spawnZombie(String alias, int row, int col);
    void removePlant(int row, int col);
    int getTileColumn(float worldX);
}
