package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public interface BehaviorContext {

    List<Zombie> getZombiesInLane(int lane);

    default int getRowCount() {
        return 5;
    }

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

    default void spawnSunAt(int row, int col, int amount, double fallSpeed) {
        spawnSunAt(row, col, amount);
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

    default void damageLane(int lane, int damage) {
        damageArea(lane, 0, damage);
    }

    default void meltIceInLane(int lane) {
    }

    default void damageSingleTarget(Object target, int damage) {
    }

    default void spawnBouncingProjectiles(int lane, int row, int count, int damagePerGrape, double lifespanSeconds) {
    }

}
