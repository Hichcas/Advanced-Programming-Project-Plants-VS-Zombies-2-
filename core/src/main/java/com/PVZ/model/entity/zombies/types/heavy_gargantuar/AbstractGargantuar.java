package com.PVZ.model.entity.zombies.types.heavy_gargantuar;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.BattleController;

import java.util.List;

public abstract class AbstractGargantuar extends Zombie {
    protected double smashDamage;
    protected double smashDuration;
    protected double throwImpDuration;
    protected double healthThresholdToThrowImp;
    protected int impTargetColumn;

    public AbstractGargantuar(String alias, double hitpoints, double eatDPS, double speed,
                              int wavePointCost, int weight, List<ScaledProperty> scaledProps,
                              double smashDamage, double smashDuration,
                              double throwImpDuration, double healthThresholdToThrowImp,
                              int impTargetColumn) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
        this.smashDamage = smashDamage;
        this.smashDuration = smashDuration;
        this.throwImpDuration = throwImpDuration;
        this.healthThresholdToThrowImp = healthThresholdToThrowImp;
        this.impTargetColumn = impTargetColumn;
    }

    @Override
    protected void applyCustomScaledProperty(String key, double scale) {
        if ("SmashDamage".equals(key)) smashDamage *= scale;
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onDestroy() {}

    public abstract void smash(BattleController ctrl, Plant target);
    public abstract void throwImp(BattleController ctrl);

    public double getSmashDamage() { return smashDamage; }
    public double getHealthThresholdToThrowImp() { return healthThresholdToThrowImp; }
}
