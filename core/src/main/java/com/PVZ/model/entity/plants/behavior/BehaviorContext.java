package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public interface BehaviorContext {

    List<Zombie> getZombiesInLane(int lane);

    default List<Zombie> getAllZombies() {
        return List.of();
    }

    default Plant getPlantAt(int row, int col) {
        return null;
    }

    default List<Plant> getAllPlants() {
        return List.of();
    }

    default void removePlant(int row, int col) {
    }

    void spawnProjectile(Object projectile);

    void spawnSun(int amount);

    default void addSun(int amount) {
    }

    default void spawnSunAt(int row, int col, int amount) {
        spawnSun(amount);
    }

    void damageArea(int lane, int row, int damage);

    default void freezeZombiesInLane(int lane, double seconds) {
    }

    default void freezeAllZombies(double seconds) {
    }

    default void disarmZombiesInLane(int lane) {
    }

    default void moveZombiesFromLane(int sourceLane, int targetLane) {
    }

    default void pullAdjacentZombiesToLane(int lane) {
    }

    default void killRandomZombies(int count) {
    }

    default void killClosestZombieInLane(int lane) {
    }

    default void hypnotizeZombiesInLane(int lane, double seconds) {
    }

    default void healPlantAt(int row, int col, int amount) {
    }

    default void fortifyPlantAt(int row, int col, int amount) {
    }

    default void consumePlantFood(PlantInstance plant) {
    }


    /**
     * Damages all zombies in the specified lane (used by Jalapeno lane-clear).
     */
    default void damageLane(int lane, int damage) {
        // Default: delegate to area damage at each column
        damageArea(lane, 0, damage);
    }

    /**
     * Melts all ice/freeze effects in the specified lane (Jalapeno melts ice).
     */
    default void meltIceInLane(int lane) {
        // Default no-op; game engine overrides to remove ice tiles / freeze states
    }

    /**
     * Deals damage to a single zombie target (used by Squash single-target crush).
     */
    default void damageSingleTarget(Object target, int damage) {
        // Default: no-op; game engine overrides with actual zombie damage logic
    }

    /**
     * Spawns bouncing grape projectiles after Grapeshot explosion.
     */
    default void spawnBouncingProjectiles(int lane, int row, int count, int damagePerGrape, double lifespanSeconds) {
        // Default: no-op; game engine overrides to create bouncing projectile entities
    }

}
