package model.entity.plants.behaviors;

import model.entity.plants.Plant;
import java.util.Random;

public class SunProducerBehavior implements PlantBehavior {
    private final Random random = new Random();

    @Override
    public void tick(Plant plant, Object gameMap) {
        plant.incrementTicks();

        int requiredTicks = (int) (plant.getActionIntervalSeconds() * 20);

        if (plant.getTicksSinceLastAction() >= requiredTicks) {
            plant.resetActionTicks();

            if (plant.isDoubleSunChance() && random.nextBoolean()) {
                System.out.println("☀️☀️ " + plant.getName() + " produced DOUBLE suns due to flag!");
            } else {
                System.out.println("☀️ " + plant.getName() + " produced a sun.");
            }
        }
    }
}