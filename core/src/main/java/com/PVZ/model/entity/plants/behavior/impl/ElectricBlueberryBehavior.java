package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class ElectricBlueberryBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }
        double timer = asDouble(plant.getRuntimeState().getOrDefault("lightningTimer", 0.0),
            0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 5.0;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("lightningTimer", timer);
            return;
        }

        List<Zombie> living = new ArrayList<>();
        for (Zombie z : context.getAllZombies()) {
            if (z != null && !z.isDead()) {
                living.add(z);
            }
        }
        if (living.isEmpty()) {
            plant.putRuntimeState("lightningTimer", 0.0);
            return;
        }

        Zombie target;
        if (plant.getStats().hasFlag(PlantFlag.TARGET_PRIORITY_UP)) {
            int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
            Zombie closest = null;
            double bestX = Double.MAX_VALUE;
            for (Zombie z : living) {
                if ((int) z.getRow() == lane && z.getX() < bestX) {
                    bestX = z.getX();
                    closest = z;
                }
            }
            target = closest != null ? closest : living.get(0);
        } else {
            target = living.get(ThreadLocalRandom.current().nextInt(living.size()));
        }

        target.setDeathType(com.PVZ.model.enums.DeathType.ELECTRIC);
        spawnLightning(context, target);
        target.takeDamage(Double.MAX_VALUE);
        plant.putRuntimeState("lightningTimer", 0.0);
    }


    private void spawnLightning(BehaviorContext context, Zombie target) {
        Projectile bolt = new Projectile();
        bolt.setType(ProjectileType.LIGHTNING);
        bolt.setDamage(0);
        bolt.setPierce(0);
        bolt.initFreePosition((float) target.getX(), (float) target.getY(), 0f, 0f);
        bolt.putExtra("visualKey", "ELECTRIC_BLUEBERRY");
        bolt.putExtra("plantType", "ELECTRIC_BLUEBERRY");
        bolt.setFuse(0.55);
        context.spawnProjectile(bolt);
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

    private static double asDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
