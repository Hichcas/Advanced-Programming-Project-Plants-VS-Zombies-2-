package model.entity.zombies.types.zomboss;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechPirate extends AbstractZomboss {
    private boolean cannonBarrageAvailable;

    public ZombieZombossMechPirate() {
        super("ZombieZombossMechPirate", 10000, 500, 0.12, 5000, 10000, defaultScaledProps(),
              0.33, 3);
        this.cannonBarrageAvailable = true;
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
    public void onPhaseTransition() {}

    @Override
    public void useSpecialAbility() {}

    public boolean canCannonBarrage() { return cannonBarrageAvailable; }
}
