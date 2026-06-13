package model.entity.plants.behavior;

import model.entity.zombies.base.Zombie;

import java.util.List;

public interface BehaviorContext {

    List<Zombie> getZombiesInLane(int lane);

    void spawnProjectile(Object projectile);

    void spawnSun(int amount);

    void damageArea(
            int lane,
            int row,
            int damage
    );

}