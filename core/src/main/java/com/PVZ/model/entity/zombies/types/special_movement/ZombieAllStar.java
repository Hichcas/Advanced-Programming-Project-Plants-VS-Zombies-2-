package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieAllStar extends AbstractSpecialMovementZombie {

    private boolean tackled = false;
    private boolean kicking = false;
    private float kickTimer = 0f;
    private static final double CHARGE_SPEED = 0.60;
    private static final double NORMAL_SPEED = 0.22;

    public ZombieAllStar() {
        this("ZombieModernAllStar");
    }

    public ZombieAllStar(String alias) {
        super(alias != null ? alias : "ZombieModernAllStar", 700, 100, CHARGE_SPEED, 300, 2000, defaultScaledProps());
        this.armor = new ZombieArmor(ZombieArmor.ArmorType.FOOTBALL_HELMET, 1400, true, false, true);
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
    public void update(float delta, BattleController ctrl) {
        if (kicking) {
            kickTimer += delta;
            if (kickTimer >= 1.6f) {
                kicking = false;
            }
        }
        super.update(delta, ctrl);
    }

    @Override
    protected void move(float delta, BattleController controller) {
        if (isFrozen() || kicking) return;
        if (!tackled) {
            ZombieAnimation.trigger(this, "run", 0.6667);
        } else {
            ZombieAnimation.trigger(this, "walk", 1.0);
        }
        super.move(delta, controller);
    }

    @Override
    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (isFrozen()) return;
        if (hypnotized) return;

        // When Football / All-Star Zombie first reaches a plant while charging:
        if (!tackled) {
            if (!kicking) {
                kicking = true;
                kickTimer = 0f;
                ZombieAnimation.trigger(this, "kick", 1.6);
                System.out.println("[ZombieAllStar] Football Zombie kicked plant: " + (targetPlant.getDefinition() != null ? targetPlant.getDefinition().getName() : "plant"));
            }
            if (kickTimer >= 0.5f) {
                targetPlant.takeDamage(1800, this, controller);
                tackled = true;
                speed = NORMAL_SPEED;
                currentSpeed = NORMAL_SPEED;
            }
            return;
        }

        // If still in kicking animation recovery, wait until kick finishes
        if (kicking) {
            return;
        }

        // After initial kick/tackle, perform regular eating attack
        ZombieAnimation.trigger(this, "eat", 1.0);
        attackCooldownTimer += delta;
        if (attackCooldownTimer >= 1.0f) {
            targetPlant.takeDamage((int) eatDPS, this, controller);
            attackCooldownTimer = 0;
        }
    }

    @Override
    public void onMove(BattleController ctrl) {
    }

    public boolean isTackled() {
        return tackled;
    }

    public boolean isKicking() {
        return kicking;
    }
}
