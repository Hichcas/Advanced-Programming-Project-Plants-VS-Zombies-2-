package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.BehaviorContext;
import model.entity.plants.behavior.PlantBehavior;
import model.entity.zombies.base.Zombie;

import java.util.List;

public class ExplosiveBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed == null || !armed) {
            Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
            armTimer += deltaTime;

            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTime > 0 && armTimer >= armTime) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }

            plant.putRuntimeState("armTimer", armTimer);
            return;
        }

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);

        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        int damage = plant.getStats().getExplodeDamage() > 0
                ? plant.getStats().getExplodeDamage()
                : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());

        context.damageArea(lane, row, damage);
        plant.takeDamage(plant.getCurrentHp());
    }
}