package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;

/**
 * "تغییر خواص تیر؛ تبدیل تیر عبوری به آتشی (دمیج دو برابر و ذوب یخ)" — torchwood
 * doesn't shoot anything itself. While it's alive, any plant directly in front of or
 * behind it in the same lane gets its shots turned into fire shots (ShooterBehavior
 * already checks the "fireAttack" stat extra and switches to FIRE_PEA + doubles
 * damage — the same flag fire_peashooter and the fire_burst plant food use), and any
 * frozen neighbor tile is melted a bit faster (handled generically wherever ice
 * effects tick, via the "meltBoost" extra).
 */
public class TorchwoodBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);

        applyAura(context, row, col - 1);
        applyAura(context, row, col + 1);
    }

    private void applyAura(BehaviorContext context, int row, int col) {
        Plant neighbor = context.getPlantAt(row, col);
        if (neighbor == null || neighbor.isDead()) {
            return;
        }
        neighbor.getStats().putExtra("fireAttack", Boolean.TRUE);
        neighbor.getStats().putExtra("meltBoost", Boolean.TRUE);
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
}
