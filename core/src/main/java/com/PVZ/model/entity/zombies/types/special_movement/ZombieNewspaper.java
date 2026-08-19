package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieNewspaper extends AbstractSpecialMovementZombie {

    private boolean enraged = false;
    private float rageAnimTimer = 0f;
    private static final double NORMAL_SPEED = 0.18;
    private static final double ENRAGED_SPEED = 0.55;

    public ZombieNewspaper() {
        this("ZombieModernNewspaper");
    }

    public ZombieNewspaper(String alias) {
        super(alias != null ? alias : "ZombieModernNewspaper", 350, 100, NORMAL_SPEED, 300, 2000, defaultScaledProps());
        this.armor = new ZombieArmor(ZombieArmor.ArmorType.NEWSPAPER, 300, true, false, false);
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
    public void onMove(BattleController ctrl) {
    }

    @Override
    public void update(float delta, BattleController ctrl) {
        super.update(delta, ctrl);
        if (isDead() || isFrozen()) return;

        // Check if newspaper armor is destroyed -> Enrage
        if (!enraged && (armor == null || armor.isDestroyed())) {
            enraged = true;
            rageAnimTimer = 1.4f;
            speed = ENRAGED_SPEED;
            currentSpeed = ENRAGED_SPEED;
            eatDPS = 250;
            ZombieAnimation.trigger(this, "newspaper_defeat", 1.4);
            System.out.println("[ZombieNewspaper] Newspaper destroyed! Zombie entered RAGE mode!");
        }

        if (rageAnimTimer > 0) {
            rageAnimTimer -= delta;
        }
    }

    @Override
    protected void move(float delta, BattleController controller) {
        if (isFrozen()) return;
        if (rageAnimTimer > 0) return; // Pauses briefly during rage roar

        if (!enraged) {
            ZombieAnimation.trigger(this, "walk_newspaper", 1.0);
        } else {
            ZombieAnimation.trigger(this, "walk", 1.0);
        }
        super.move(delta, controller);
    }

    @Override
    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (isFrozen()) return;
        if (rageAnimTimer > 0) return;

        if (!enraged) {
            ZombieAnimation.trigger(this, "eat_newspaper", 1.0);
        } else {
            ZombieAnimation.trigger(this, "eat", 1.0);
        }

        attackCooldownTimer += delta;
        if (attackCooldownTimer >= 1.0f) {
            targetPlant.takeDamage((int) eatDPS, this, controller);
            attackCooldownTimer = 0;
        }
    }

    public boolean isEnraged() {
        return enraged;
    }
}
