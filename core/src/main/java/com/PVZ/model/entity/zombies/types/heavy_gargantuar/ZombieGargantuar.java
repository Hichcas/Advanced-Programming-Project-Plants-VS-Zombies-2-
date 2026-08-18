package com.PVZ.model.entity.zombies.types.heavy_gargantuar;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
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
            1500, 1.7667, 0.9667, 0.5, 2);
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
        ZombieAnimation.tick(this, delta);

        if (isDying()) {
            animStateTime += delta;
            if (!ZombieAnimation.isActive(this)) {
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
        ZombieAnimation.trigger(this, "smash", 1.7667);
    }

    @Override
    public void throwImp(BattleController ctrl) {
        ZombieAnimation.trigger(this, "fire", 0.9667);
        String impAlias = (theme == Theme.EGYPT) ? "ZombieEgyptImpDefault" : "ZombieTutorialImpDefault";
        ZombieImp.Theme impTheme = (theme == Theme.EGYPT) ? ZombieImp.Theme.EGYPT : ZombieImp.Theme.BASIC;
        ZombieImp imp = new ZombieImp(impAlias, impTheme);

        int currentTileCol = ctrl.getTileColumn((float) x);
        int targetCol = Math.max(0, currentTileCol - 3);
        float targetX = (float) x - 300f;
        if (ctrl.getMap() != null) {
            com.PVZ.model.entity.Tile t = ctrl.getMap().getTile((int) row, targetCol);
            if (t != null) {
                targetX = t.getX();
            }
        }

        imp.initPosition(targetX, y, row);
        imp.setCol(targetCol);
        ZombieAnimation.trigger(imp, "land", 1.0);
        ctrl.addZombie(imp);
        System.out.println(alias + " threw an Imp (" + impAlias + ") to column " + targetCol + "!");
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nSMC:" + (smashing ? "Y" : "N") + " IMP:" + (impThrown ? "Y" : "N");
    }

    public Theme getTheme() { return theme; }
    public boolean isImpThrown() { return impThrown; }
}
