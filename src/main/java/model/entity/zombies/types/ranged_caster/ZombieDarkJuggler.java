package model.entity.zombies.types.ranged_caster;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieDarkJuggler extends AbstractRangedCasterZombie {
    private boolean isReflecting;
    private int reflectableProjectiles;

    public ZombieDarkJuggler() {
        super("ZombieDarkJuggler", 420, 100, 0.185, 700, 3500, defaultScaledProps(),
              120, 180, 2.0, 3);
        this.isReflecting = false;
        this.reflectableProjectiles = 5;
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
    public void shoot() {}

    @Override
    public void onHit() {}

    public boolean isReflecting() { return isReflecting; }
    public void startReflecting() { this.isReflecting = true; }
    public void stopReflecting() { this.isReflecting = false; }
    public int getReflectableProjectiles() { return reflectableProjectiles; }
    public boolean reflectProjectile() {
        if (isReflecting && reflectableProjectiles > 0) {
            reflectableProjectiles--;
            return true;
        }
        return false;
    }
}
