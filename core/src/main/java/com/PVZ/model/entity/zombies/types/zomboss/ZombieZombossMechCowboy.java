package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechCowboy extends AbstractZomboss {
    public ZombieZombossMechCowboy() {
        super("ZombieZombossMechCowboy", 10000, 500, 0.12, 5000, 10000, defaultScaledProps(),
              0.33, 3, 5.0);
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    @Override
    public void onPhaseTransition(BattleController ctrl) {
        currentSpeed = speed * (1 + currentPhase * 0.25);
        abilityCooldown = Math.max(3.0, abilityCooldown - 0.8);
        System.out.println("[ZombossCowboy] Phase " + currentPhase + ": speed="
            + String.format("%.2f", currentSpeed) + " cooldown=" + String.format("%.1f", abilityCooldown) + "s");
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        List<Plant> plants = ctrl.getPlants();
        if (plants.isEmpty()) return;
        int numLassos = currentPhase;
        int damage = 1000 * currentPhase;
        List<Plant> targets = new ArrayList<>(plants);
        int hitCount = 0;
        for (int i = 0; i < numLassos && !targets.isEmpty(); i++) {
            int idx = (int) (Math.random() * targets.size());
            Plant p = targets.get(idx);
            p.takeDamage(damage);
            hitCount++;
            targets.remove(idx);
        }
        System.out.println("[ZombossCowboy] Lasso x" + numLassos
            + " damage=" + damage + " hit=" + hitCount + " plants");
    }

    @Override
    public void spawnZombieWave(BattleController ctrl) {
        System.out.println("[ZombossCowboy] Portal Wave spawned cowboy zombies!");
    }
}
