package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public class LobberBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Double timer = (Double) plant.getRuntimeState().getOrDefault("lobTimer", 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            plant.putRuntimeState("lobTimer", timer);
            return;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("lobTimer", timer);
            return;
        }

        timer = 0.0;

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            plant.putRuntimeState("lobTimer", timer);
            return;
        }

        int damage = plant.getStats().getDamage();
        int pierce = Math.max(1, plant.getStats().getPierce() == 0 ? 1 : plant.getStats().getPierce());

        int hits = 0;
        /*for (Zombie zombie : zombies) {
            zombie.takeDamage(damage);
            hits++;
            if (hits >= pierce) {
                break;
            }
        }*/

        plant.putRuntimeState("lobTimer", timer);
    }
}
