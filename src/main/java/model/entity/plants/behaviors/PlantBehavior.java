package model.entity.plants.behaviors;

import model.entity.plants.Plant;


public interface PlantBehavior {
    void tick(Plant plant, Object gameMap);
}