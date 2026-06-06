package model.entity.zombies.types.heavy_gargantuar;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.Zombie;

import java.util.ArrayList;
import java.util.List;

public class ZombieImp extends Zombie {
    public enum Theme { BASIC, EGYPT, ICEAGE, BEACH, DARK }

    private Theme theme;

    public ZombieImp(String alias, Theme theme) {
        super(alias, 190, 100, 0.22, 100, 1000, defaultScaledProps());
        this.theme = theme;
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
    public void onSpawn() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onDestroy() {}

    public Theme getTheme() { return theme; }
}
