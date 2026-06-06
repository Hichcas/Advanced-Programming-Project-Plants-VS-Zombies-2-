package model.entity.zombies.types.ranged_caster;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.Zombie;

import java.util.List;

public abstract class AbstractRangedCasterZombie extends Zombie {
    protected double projectileDamage;
    protected double projectileSpeed;
    protected double attackCooldown;
    protected int attackRange;

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
    public void onSpawn() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onDestroy() {}

    public abstract void shoot();
    public abstract void onHit();

    public double getProjectileDamage() { return projectileDamage; }
    public double getAttackCooldown() { return attackCooldown; }
    public int getAttackRange() { return attackRange; }
}
