package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Set;


public class ExplosiveBehavior implements PlantBehavior {

    private static final Set<String> CONTACT_TRIGGERED = Set.of(
            "potato_mine", "primal_potato_mine", "tangle_kelp", "iceberg_lettuce");

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed == null || !armed) {
            Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
            armTimer += deltaTime;

            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTimer >= Math.max(armTime, 0.0)) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }

            plant.putRuntimeState("armTimer", armTimer);
            if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE))) {
                return;
            }
        }

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();

        boolean requiresContact = key != null && CONTACT_TRIGGERED.contains(key);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (requiresContact && zombies.isEmpty()) {
            return;
        }

        if ("ice_shroom".equals(key)) {
            context.freezeAllZombies(Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            plant.takeDamage(plant.getCurrentHp());
            return;
        }

        int damage = plant.getStats().getExplodeDamage() > 0
                ? plant.getStats().getExplodeDamage()
                : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());

        context.damageArea(lane, row, damage);
        plant.takeDamage(plant.getCurrentHp());
    }
}
