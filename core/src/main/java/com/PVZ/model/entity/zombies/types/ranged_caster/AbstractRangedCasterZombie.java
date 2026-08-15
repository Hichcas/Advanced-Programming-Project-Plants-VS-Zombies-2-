package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.BattleController;

import java.util.List;

public abstract class AbstractRangedCasterZombie extends Zombie {
    protected double projectileDamage;
    protected double projectileSpeed;
    protected double attackCooldown;
    protected int attackRange;
    protected float rangedCooldown = 0;

    public AbstractRangedCasterZombie(String alias, double hitpoints, double eatDPS, double speed,
                                      int wavePointCost, int weight, List<ScaledProperty> scaledProps,
                                      double projectileDamage, double projectileSpeed,
                                      double attackCooldown, int attackRange) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        this.attackCooldown = attackCooldown;
        this.attackRange = attackRange;
    }

    @Override
    protected void applyCustomScaledProperty(String key, double scale) {
        if ("ProjectileDamage".equals(key)) projectileDamage *= scale;
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onDestroy() {}

    @Override
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        com.PVZ.model.entity.zombies.base.ZombieAnimation.tick(this, delta);

        if (isDying()) {
            animStateTime += delta;
            if (!com.PVZ.model.entity.zombies.base.ZombieAnimation.isActive(this)) {
                finishDeath(ctrl);
            }
            return;
        }

        if (!isFrozen()) {
            animStateTime += delta;
        }

        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            startDeath(ctrl);
            return;
        }
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        if (ctrl == null) {
            return;
        }
        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;

        Plant plantInFront = ctrl.getPlantAt((int) row, tileCol);
        Plant rangedTarget = findNearestPlantInRange(ctrl);

        if (plantInFront != null && !plantInFront.isDead()) {
            moving = false;
            attack(plantInFront, delta, ctrl);
        } else if (rangedTarget != null) {
            moving = false;
            rangedCooldown += delta;
            if (rangedCooldown >= attackCooldown) {
                shoot(ctrl, rangedTarget);
                rangedCooldown = 0;
            }
        } else {
            moving = true;
            move(delta, ctrl);
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    private Plant findNearestPlantInRange(BattleController ctrl) {
        int startCol = (int) col - 1;
        int endCol = Math.max(0, (int) col - attackRange);
        for (int c = startCol; c >= endCol; c--) {
            Plant p = ctrl.getPlantAt((int) row, c);
            if (p != null && !p.isDead()) return p;
        }
        return null;
    }

    @Override
    public void onProjectileHit(Plant target) {
        onHit(target);
    }

    public abstract void shoot(BattleController controller, Plant target);
    public abstract void onHit(Plant target);

    public double getProjectileDamage() { return projectileDamage; }
    public double getAttackCooldown() { return attackCooldown; }
    public int getAttackRange() { return attackRange; }

    @Override
    public String getStatusString() {
        String armorStr = (armor != null && !armor.isDestroyed())
            ? String.format(" Armor=%s(%.0f)", armor.getType(), armor.getHealth())
            : "";
        String actionStr = moving ? "" : " SHOOTING";
        return String.format("<%s> x=%.1f row=%d col=%d HP=%.1f%s speed=%.3f%s",
            alias, x, (int)row, (int)col, hitpoints, armorStr, currentSpeed, actionStr);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nRNG:" + attackRange + " CD:" + String.format("%.1f", rangedCooldown);
    }
}
