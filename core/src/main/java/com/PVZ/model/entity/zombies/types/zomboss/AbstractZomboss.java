package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
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
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onDestroy() {}

    @Override
    public void update(float delta, BattleController ctrl) {
        super.update(delta, ctrl);
        double hpRatio = hitpoints / maxHitpoints;
        if (totalPhases == 3 && currentPhase < totalPhases) {
            double threshold = phaseTransitionThreshold;
            if (currentPhase == 2) threshold = phaseTransitionThreshold * 0.5;
            if (hpRatio <= threshold) {
                currentPhase++;
                System.out.println("[" + alias + "] advanced to PHASE " + currentPhase + " (HP ratio=" + String.format("%.2f", hpRatio) + ")");
                onPhaseTransition(ctrl);
                abilityTimer = Math.max(abilityTimer, 1.0f);
            }
        }
        abilityTimer -= delta;
        if (abilityTimer <= 0) {
            useSpecialAbility(ctrl);
            abilityTimer = abilityCooldown;
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString()
            + "\nPHASE:" + currentPhase + "/" + totalPhases
            + " CD:" + String.format("%.1f", abilityTimer) + "s";
    }

    public abstract void onPhaseTransition(BattleController ctrl);
    public abstract void useSpecialAbility(BattleController ctrl);

    public int getCurrentPhase() { return currentPhase; }
    public int getTotalPhases() { return totalPhases; }
    public List<ZombossStage> getStages() { return stages; }
    public void addStage(ZombossStage stage) { stages.add(stage); }
    public double getMaxHitpoints() { return maxHitpoints; }
}
