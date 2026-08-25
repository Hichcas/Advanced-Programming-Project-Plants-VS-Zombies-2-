package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractZomboss extends Zombie {
    public static class ZombossStage {
        private double hitpoints;
        private String actions;
        private double idleTime;
        private double stunTime;

        public ZombossStage(double hitpoints, String actions, double idleTime, double stunTime) {
            this.hitpoints = hitpoints;
            this.actions = actions;
            this.idleTime = idleTime;
            this.stunTime = stunTime;
        }

        public double getHitpoints() { return hitpoints; }
        public String getActions() { return actions; }
        public double getIdleTime() { return idleTime; }
        public double getStunTime() { return stunTime; }
    }

    protected double maxHitpoints;
    protected double phaseTransitionThreshold;
    protected int currentPhase;
    protected int totalPhases;
    protected List<ZombossStage> stages;
    protected double abilityCooldown;
    protected double abilityTimer;
    protected boolean usePortalNext;
    protected int targetRow;
    protected float moveTimer;
    protected float stunTimer = 0f;

    public AbstractZomboss(String alias, double hitpoints, double eatDPS, double speed,
                           int wavePointCost, int weight, List<ScaledProperty> scaledProps,
                           double phaseTransitionThreshold, int totalPhases,
                           double abilityCooldown) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
        this.maxHitpoints = hitpoints;
        this.phaseTransitionThreshold = phaseTransitionThreshold;
        this.currentPhase = 1;
        this.totalPhases = totalPhases;
        this.stages = new ArrayList<>();
        this.abilityCooldown = abilityCooldown;
        this.abilityTimer = abilityCooldown;
        this.usePortalNext = false;
        this.targetRow = 1;
        this.moveTimer = 0f;
        this.moving = false;
        this.hitbox.setSize(220f, 240f);
    }

    @Override
    public void onSpawn() {
        this.hitbox.setSize(220f, 240f);
    }

    @Override
    public void onDestroy() {}

    public void triggerStun(float duration) {
        this.stunTimer = duration;
        ZombieAnimation.trigger(this, "stun_start", 1.5);
        com.PVZ.model.status.AppStatus.triggerCameraShake();
    }

    public boolean isStunned() {
        return stunTimer > 0f;
    }

    public float getStunTimer() {
        return stunTimer;
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

        if (isStunned()) {
            stunTimer -= delta;
            if (stunTimer <= 0f) {
                stunTimer = 0f;
                ZombieAnimation.trigger(this, "stun_end", 1.0);
            }
            onUpdate(delta, ctrl);
            return;
        }

        if (ctrl.getMap() != null) {
            // Occupies 2 rows (targetRow and targetRow + 1)
            targetRow = Math.max(0, Math.min(3, targetRow));
            com.PVZ.model.entity.Tile t = ctrl.getMap().getTile(targetRow, 8);
            if (t != null) {
                if (x == 0 || x < 500) {
                    x = t.getX();
                }
                float targetY = t.getY();
                if (Math.abs(y - targetY) > 2.0f) {
                    y += (targetY - y) * Math.min(1.0f, delta * 4.0f);
                } else {
                    y = targetY;
                }
                row = targetRow;
            }
        }
        hitbox.setPosition((float) x, (float) y);

        double hpRatio = hitpoints / maxHitpoints;
        if (totalPhases == 3 && currentPhase < totalPhases) {
            double threshold = phaseTransitionThreshold;
            if (currentPhase == 2) threshold = phaseTransitionThreshold * 0.5;
            if (hpRatio <= threshold) {
                currentPhase++;
                System.out.println("[" + alias + "] advanced to PHASE " + currentPhase + " (HP ratio=" + String.format(
                        "%.2f", hpRatio) + ")");
                triggerStun(4.0f);
                onPhaseTransition(ctrl);
                abilityTimer = Math.max(abilityTimer, 4.0f);
            }
        }

        abilityTimer -= delta;
        if (abilityTimer <= 0) {
            if (usePortalNext) {
                spawnZombieWave(ctrl);
                usePortalNext = false;
            } else {
                useSpecialAbility(ctrl);
                usePortalNext = true;
            }
            abilityTimer = Math.max(3.0, abilityCooldown - (currentPhase - 1) * 1.0);

            // Switch to a new 2-row span (0..3)
            int newRow = (int) (Math.random() * 4);
            if (newRow < targetRow) {
                ZombieAnimation.trigger(this, "walk_up", 1.5);
            } else if (newRow > targetRow) {
                ZombieAnimation.trigger(this, "walk_down", 1.5);
            }
            targetRow = newRow;
        }

        onUpdate(delta, ctrl);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString()
            + "\nPHASE:" + currentPhase + "/" + totalPhases
            + " CD:" + String.format("%.1f", abilityTimer) + "s";
    }

    public abstract void onPhaseTransition(BattleController ctrl);
    public abstract void useSpecialAbility(BattleController ctrl);
    public abstract void spawnZombieWave(BattleController ctrl);

    public int getCurrentPhase() { return currentPhase; }
    public int getTotalPhases() { return totalPhases; }
    public List<ZombossStage> getStages() { return stages; }
    public void addStage(ZombossStage stage) { stages.add(stage); }
    public double getMaxHitpoints() { return maxHitpoints; }
    public int getTargetRow() { return targetRow; }
    public void setTargetRow(int row) { this.targetRow = row; }
}
