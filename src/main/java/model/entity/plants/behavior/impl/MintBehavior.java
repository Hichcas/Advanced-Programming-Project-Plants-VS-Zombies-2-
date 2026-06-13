package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.BehaviorContext;
import model.entity.plants.behavior.PlantBehavior;

public class MintBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Boolean triggered = (Boolean) plant.getRuntimeState().getOrDefault("mintTriggered", Boolean.FALSE);
        if (triggered != null && triggered) {
            return;
        }

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);

        int duration = plant.getStats().getDurationSeconds() > 0
                ? (int) Math.ceil(plant.getStats().getDurationSeconds())
                : 1;

        plant.putRuntimeState("mintTriggered", Boolean.TRUE);
        plant.putRuntimeState("mintDuration", duration);

        context.damageArea(lane, row, Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage()));
    }
}