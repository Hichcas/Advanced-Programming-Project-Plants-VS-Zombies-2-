package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.BehaviorContext;
import model.entity.plants.behavior.PlantBehavior;

public class SunProducerBehavior implements PlantBehavior {

    @Override
    public void onUpdate(
            PlantInstance plant,
            BehaviorContext context,
            double deltaTime
    ) {

        Double timer =
                (Double) plant.getRuntimeState()
                        .getOrDefault("sunTimer", 0.0);

        timer += deltaTime;

        double productionTime =
                plant.getStats().getProductionTimeSeconds();

        if (productionTime <= 0) {
            return;
        }

        if (timer >= productionTime) {

            timer = 0.0;

            int amount =
                    plant.getStats().getSunAmount();

            context.spawnSun(amount);
        }

        plant.putRuntimeState(
                "sunTimer",
                timer
        );
    }
}