package model.entity.zombies.types.zomboss;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.Zombie;

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

    protected double phaseTransitionThreshold;
    protected int currentPhase;
    protected int totalPhases;
    protected List<ZombossStage> stages;

    public AbstractZomboss(String alias, double hitpoints, double eatDPS, double speed,
                           int wavePointCost, int weight, List<ScaledProperty> scaledProps,
                           double phaseTransitionThreshold, int totalPhases) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
        this.phaseTransitionThreshold = phaseTransitionThreshold;
        this.currentPhase = 1;
        this.totalPhases = totalPhases;
        this.stages = new ArrayList<>();
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onDestroy() {}

    public abstract void onPhaseTransition();
    public abstract void useSpecialAbility();

    public int getCurrentPhase() { return currentPhase; }
    public int getTotalPhases() { return totalPhases; }
    public List<ZombossStage> getStages() { return stages; }
    public void addStage(ZombossStage stage) { stages.add(stage); }
}
