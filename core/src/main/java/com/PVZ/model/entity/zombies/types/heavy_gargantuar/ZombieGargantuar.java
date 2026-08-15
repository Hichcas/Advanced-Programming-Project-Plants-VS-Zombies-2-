package com.PVZ.model.entity.zombies.types.heavy_gargantuar;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieGargantuar extends AbstractGargantuar {
    public enum Theme { BASIC, EGYPT, ICEAGE, BEACH, DARK }

    private Theme theme;
    private boolean impThrown;
    private boolean smashing;
    private float smashTimer;
    private double maxHitpoints;

    public ZombieGargantuar(String alias, Theme theme) {
        super(alias, 3600, 0, 0.24, 1500, 3000, defaultScaledProps(),
            1500, 2.0, 1.0, 0.5, 2);
        this.theme = theme;
        this.impThrown = false;
        this.smashing = false;
        this.smashTimer = 0;
        this.maxHitpoints = hitpoints;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("SmashDamage", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        return list;
    }

    @Override
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            die(ctrl);
            return;
        }
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }

        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;

        if (!impThrown && hitpoints <= maxHitpoints * healthThresholdToThrowImp) {
            throwImp(ctrl);
            impThrown = true;
        }

        if (smashing) {
            smashTimer += delta;
            if (smashTimer >= smashDuration) {
                smashing = false;
                smashTimer = 0;
            }
            moving = false;
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }

        Plant plant = ctrl.getPlantAt((int) row, tileCol);
        if (plant != null && !plant.isDead()) {
            moving = false;
            smash(ctrl, plant);
        } else {
            moving = true;
            move(delta, ctrl);
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    @Override
    public void smash(BattleController ctrl, Plant target) {
        target.takeDamage((int) smashDamage);
        smashing = true;
        smashTimer = 0;
        com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "smash", 1.7667);
    }

    @Override
    public void throwImp(BattleController ctrl) {
        ZombieImp imp = new ZombieImp("ZombieTutorialImpDefault", ZombieImp.Theme.BASIC);
        double impCol = Math.min(col + impTargetColumn, 8);
        imp.initPosition(x - 100, y, row);
        imp.setCol(impCol);
        ctrl.addZombie(imp);
        System.out.println(alias + " threw an Imp!");
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nSMC:" + (smashing ? "Y" : "N") + " IMP:" + (impThrown ? "Y" : "N");
    }

    public Theme getTheme() { return theme; }
}
