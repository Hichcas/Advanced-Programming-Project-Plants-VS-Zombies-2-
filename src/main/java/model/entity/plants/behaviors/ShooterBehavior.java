package model.entity.plants.behaviors;

import model.entity.plants.Plant;

public class ShooterBehavior implements PlantBehavior {
    @Override
    public void tick(Plant plant, Object gameMap) {
        plant.incrementTicks();
        int requiredTicks = (int) (plant.getActionIntervalSeconds() * 20);

        if (plant.getTicksSinceLastAction() >= requiredTicks) {
            plant.resetActionTicks();

            System.out.println("🎯 " + plant.getName() + " shot a projectile! Damage: " + plant.getDamage() + " | Range: " + plant.getRange());
        }
    }
}