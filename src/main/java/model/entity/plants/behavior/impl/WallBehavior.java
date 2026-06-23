package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.BehaviorContext;
import model.entity.plants.behavior.PlantBehavior;
import model.entity.zombies.base.Zombie;

import java.util.List;

public class WallBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);

        if (zombies.isEmpty()) {
            return;
        }

        /*for (Zombie zombie : zombies) {
            if (zombie != null && zombie.isInMeleeRange()) {
                zombie.setBlockedByWall(true);
                return;
            }
        }*/
    }
}