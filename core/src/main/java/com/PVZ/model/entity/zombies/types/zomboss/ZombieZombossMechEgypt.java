package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZombieZombossMechEgypt extends AbstractZomboss {
    public ZombieZombossMechEgypt() {
        super("ZombieZombossMechEgypt", 10000, 500, 0.12, 5000, 10000, defaultScaledProps(),
              0.33, 3, 6.0);
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
        currentSpeed = speed * (1 + currentPhase * 0.2);
        abilityCooldown = Math.max(3.0, abilityCooldown - 1.0);
        System.out.println("[ZombossEgypt] Phase " + currentPhase + ": speed="
            + String.format("%.2f", currentSpeed) + " cooldown=" + String.format("%.1f", abilityCooldown) + "s");
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        List<Plant> plants = ctrl.getPlants();
        if (plants.isEmpty()) return;
        int damage = 800 * currentPhase;
        Set<Integer> targetRows = new HashSet<>();
        List<Integer> rowsWithPlants = new ArrayList<>();
        for (Plant p : plants) {
            int r = asInt(p.getRuntimeState("row"), 0);
            if (!rowsWithPlants.contains(r)) rowsWithPlants.add(r);
        }
        if (rowsWithPlants.isEmpty()) return;
        int numStomps = Math.min(currentPhase, rowsWithPlants.size());
        for (int i = 0; i < numStomps; i++) {
            int idx = (int) (Math.random() * rowsWithPlants.size());
            targetRows.add(rowsWithPlants.get(idx));
            rowsWithPlants.remove(idx);
            if (rowsWithPlants.isEmpty()) break;
        }
        int hitCount = 0;
        for (Plant p : plants) {
            if (targetRows.contains(asInt(p.getRuntimeState("row"), Integer.MIN_VALUE))) {
                p.takeDamage(damage);
                hitCount++;
            }
        }
        System.out.println("[ZombossEgypt] Pyramid Stomp x" + numStomps + " rows=" + targetRows
            + " damage=" + damage + " hit=" + hitCount + " plants");
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
