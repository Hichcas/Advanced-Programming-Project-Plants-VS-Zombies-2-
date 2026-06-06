package model.entity.zombies.types.basic;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieCamel extends AbstractBasicZombie {
    private CamelSegment frontSegment;
    private CamelSegment middleSegment;
    private CamelSegment rearSegment;

    public ZombieCamel() {
        super("ZombieCamelDefault", 600, 100, 0.185, 600, 3500, defaultScaledProps());
        frontSegment = new CamelSegment(200);
        middleSegment = new CamelSegment(200);
        rearSegment = new CamelSegment(200);
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public CamelSegment getFrontSegment() { return frontSegment; }
    public CamelSegment getMiddleSegment() { return middleSegment; }
    public CamelSegment getRearSegment() { return rearSegment; }

    @Override
    public double getEffectiveHitpoints() {
        return frontSegment.getHealth() + middleSegment.getHealth() + rearSegment.getHealth();
    }

    public static class CamelSegment {
        private double health;

        public CamelSegment(double health) { this.health = health; }

        public double getHealth() { return health; }
        public void takeDamage(double dmg) { health = Math.max(0, health - dmg); }
        public boolean isDestroyed() { return health <= 0; }
    }
}
