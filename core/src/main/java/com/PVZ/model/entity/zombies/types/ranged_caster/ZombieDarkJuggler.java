package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieDarkJuggler extends AbstractRangedCasterZombie {

    private float spinDuration = 0f;
    private static final float SPIN_TIME_PER_HIT = 1.2f;

    public ZombieDarkJuggler() {
        super("ZombieDarkJuggler", 420, 100, 0.185, 700, 3500, defaultScaledProps(),
              120, 180, 2.0, 3);
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
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        ZombieAnimation.tick(this, delta);
        if (isDying()) {
            animStateTime += delta;
            if (!ZombieAnimation.isActive(this)) {
                finishDeath(ctrl);}return;}
        if (!isFrozen()) {animStateTime += delta;}
        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            startDeath(ctrl);return;}
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);return;}
        if (spinDuration > 0) {
            spinDuration -= delta;
            moving = false;
            ZombieAnimation.trigger(this, "spin", 1.2);
            hitbox.setPosition((float) x, (float) y);
            if (ctrl != null) {onUpdate(delta, ctrl);}return;}
        if (ctrl == null) {return;}
        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;
        Plant plantInFront = ctrl.getPlantAt((int) row, tileCol);
        if (plantInFront != null && !plantInFront.isDead()) {
            moving = false;
            attack(plantInFront, delta, ctrl);
        } else {moving = true;move(delta, ctrl);}
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    @Override
    public void shoot(BattleController controller, Plant target) {
        // Jester does not shoot projectiles; he only reflects incoming projectiles!
    }

    @Override
    public void onHit(Plant target) {}

    public boolean isSpinning() {
        return spinDuration > 0;
    }

    public boolean reflectProjectile() {
        if (isDead() || isFrozen()) return false;
        // Trigger / refresh spin state whenever a projectile reaches him
        this.spinDuration = SPIN_TIME_PER_HIT;
        ZombieAnimation.trigger(this, "spin", 1.2);
        return true;
    }

    public boolean isReflecting() {
        return spinDuration > 0;
    }
}
