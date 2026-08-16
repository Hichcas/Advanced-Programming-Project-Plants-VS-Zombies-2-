package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeHunter extends AbstractRangedCasterZombie {

    public ZombieIceAgeHunter() {
        super("ZombieIceAgeHunter", 380, 100, 0.185, 600, 3000, defaultScaledProps(),
              100, 300, 3.5, 4);
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("ProjectileDamage", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        return list;
    }

    @Override
    public void shoot(BattleController controller, Plant target) {
        if (target != null && !target.isDead()) {
            ZombieAnimation.trigger(this, "throw", 2.1);
            controller.addZombieProjectile(new ZombieProjectile(
                (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
            System.out.println("[HunterZombie] Threw snowball at plant in row " + (int) row);
        }
    }

    @Override
    public void onHit(Plant target) {
        if (target != null) {
            int freezeLevel = asInt(target.getRuntimeState().get("freezeLevel"), 0);
            freezeLevel = Math.min(3, freezeLevel + 1);
            target.putRuntimeState("freezeLevel", freezeLevel);
            if (freezeLevel >= 3) {
                target.putRuntimeState("iceHp", 600);
            }
            System.out.println("[HunterZombie] Plant at row " + (int) row + " freeze level increased to " + freezeLevel);
        }
    }

    @Override
    public void onProjectileHit(Plant target) {
        onHit(target);
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
