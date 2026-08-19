package com.PVZ.model.entity.zombies.types.basic;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieDarkKing extends AbstractBasicZombie {
    private double buffRadius;
    private double speedBoost;
    private double damageBoost;
    private float crownCooldown;
    private static final float CROWN_INTERVAL = 3.0f;

    public ZombieDarkKing() {
        super("ZombieDarkKing", 1000, 100, 0.05, 1200, 5000, defaultScaledProps());
        this.buffRadius = 600.0;
        this.speedBoost = 1.3;
        this.damageBoost = 1.3;
        this.crownCooldown = CROWN_INTERVAL; // Ready immediately on spawn!
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public double getBuffRadius() { return buffRadius; }
    public double getSpeedBoost() { return speedBoost; }
    public double getDamageBoost() { return damageBoost; }

    @Override
    public void onUpdate(float delta, BattleController controller) {
        if (controller == null || isDead() || isFrozen()) return;

        crownCooldown += delta;

        // Speed buff to nearby zombies
        for (Zombie z : controller.getAllZombies()) {
            if (z == this || z.isDead()) continue;
            double dx = Math.abs(z.getX() - this.x);
            if (dx <= buffRadius && Math.abs(z.getRow() - this.row) <= 2) {
                z.setCurrentSpeed(Math.max(z.getCurrentSpeed(), z.getSpeed() * speedBoost));
            }
        }

        // Crowning (Knight Transformation) mechanic:
        if (crownCooldown >= CROWN_INTERVAL) {
            for (Zombie z : controller.getAllZombies()) {
                if (z == this || z.isDead()) continue;
                // Target basic / peasant zombie who doesn't yet have Knight Crown
                if (z.getArmor() == null || z.getArmor().isDestroyed() || z.getArmor().getType() != ZombieArmor.ArmorType.CROWN) {
                    double dx = Math.abs(z.getX() - this.x);
                    if (dx <= buffRadius && Math.abs(z.getRow() - this.row) <= 2) {
                        crownCooldown = 0f;
                        ZombieAnimation.trigger(this, "special", 3.2333);
                        z.setArmor(new ZombieArmor(ZombieArmor.ArmorType.CROWN, 1600, true, true, true));
                        z.setAlias("ZombieDarkArmor3Default");
                        com.PVZ.model.game.RegularGameEngine engine = com.PVZ.model.status.AppStatus.getGameEngine() instanceof com.PVZ.model.game.RegularGameEngine re ? re : null;
                        if (engine != null) {
                            engine.addTimedPamEffect("768/FULL/EFFECTS/DARK_WIZARD_LIGHTNINGBOLT/DARK_WIZARD_LIGHTNINGBOLT.PAM", "animation", 0.6, 1.0f, (float) z.getX() + 20f, (float) z.getY() + 50f);
                        }
                        System.out.println("[ZombieDarkKing] Crowned " + z.getAlias() + " into a Knight Zombie (+1600 Armor HP)!");
                        break;
                    }
                }
            }
        }
    }
}
