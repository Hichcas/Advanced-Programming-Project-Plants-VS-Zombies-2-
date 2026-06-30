package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public class ShooterBehavior implements PlantBehavior {

    @Override
    public void onUpdate(
            PlantInstance plant,
            BehaviorContext context,
            double deltaTime
    ) {

        Double attackTimer =
                (Double) plant.getRuntimeState()
                        .getOrDefault("attackTimer", 0.0);

        attackTimer += deltaTime;

        double cooldown =
                plant.getStats()
                        .getActionIntervalSeconds();

        if (attackTimer < cooldown) {

            plant.putRuntimeState(
                    "attackTimer",
                    attackTimer
            );

            return;
        }

        attackTimer = 0.0;

        Integer lane =
                (Integer) plant.getRuntimeState()
                        .getOrDefault("lane", 0);

        List<Zombie> zombies =
                context.getZombiesInLane(lane);

        if (zombies.isEmpty()) {
            return;
        }

        int damage =
                plant.getStats()
                        .getDamage();

        int projectileCount =
                plant.getStats()
                        .getIntExtra(
                                "projectileCount",
                                1
                        );

        for (int i = 0; i < projectileCount; i++) {

            Object projectile =
                    ProjectileFactory.createProjectile(
                            plant,
                            damage
                    );

            context.spawnProjectile(
                    projectile
            );
        }

        plant.putRuntimeState(
                "attackTimer",
                attackTimer
        );
    }
}
