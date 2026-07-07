package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechDark extends AbstractZomboss {
    public ZombieZombossMechDark() {
        super("ZombieZombossMechDark", 12000, 600, 0.1, 6000, 12000, defaultScaledProps(),
              0.33, 3, 7.0);
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
        currentSpeed = speed * (1 + currentPhase * 0.3);
        abilityCooldown = Math.max(4.0, abilityCooldown - 1.0);
        System.out.println("[ZombossDark] Phase " + currentPhase + ": speed="
            + String.format("%.2f", currentSpeed) + " cooldown=" + String.format("%.1f", abilityCooldown) + "s");
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        List<Plant> plants = ctrl.getPlants();
        if (plants.isEmpty()) return;
        int numCurses = currentPhase;
        int damage = 1500 * currentPhase;
        List<Plant> targets = new ArrayList<>(plants);
        int hitCount = 0;
        for (int i = 0; i < numCurses && !targets.isEmpty(); i++) {
            int idx = (int) (Math.random() * targets.size());
            Plant p = targets.get(idx);
            p.takeDamage(damage);
            hitCount++;
            targets.remove(idx);
        }
        System.out.println("[ZombossDark] Dark Magic x" + numCurses
            + " damage=" + damage + " hit=" + hitCount + " plants");
    }
}
