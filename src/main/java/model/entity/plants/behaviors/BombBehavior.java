package model.entity.plants.behaviors;

import model.entity.plants.Plant;

public class BombBehavior implements PlantBehavior {
    @Override
    public void tick(Plant plant, Object gameMap) {
        System.out.println("💥 " + plant.getName() + " EXPLODED! Dealing AOE Damage: " + plant.getAoeDamage());
        plant.takeDamage(plant.getMaxHp());
    }
}