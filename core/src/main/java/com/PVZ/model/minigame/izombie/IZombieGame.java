package com.PVZ.model.minigame.izombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class IZombieGame {

    private final int rows;
    private final int cols;
    private final int redLineCol;

    private int sun;
    private final List<ZombieOption> roster;

    private final double sunProductionBase;
    private final double sunProductionGrowthPerTick;
    private final double sunProductionIntervalSeconds;
    private final double sunProductionCap;
    private double sunProductionTimer;
    private double currentSunRate;
    private final double plantDensity;
    private final boolean[] sunZombieAlive;
    private final String sunZombieAlias;
    private final Set<Integer> brainsEaten = new HashSet<>();

    private boolean won = false;
    private boolean lost = false;

    /**
     * Every walking, non-boss, non-gargantuar zombie the renderer already knows how
     * to animate reliably (matches com.PVZ.model.enums.ZombieType). Costs are a
     * simple tier heuristic (armor pieces = pricier) so a freshly-randomized roster
     * still feels balanced without needing 40 hand-tuned numbers.
     */
    private static final ZombieOption[] RANDOM_POOL = {
        new ZombieOption("ZombieTutorialDefault", 50, "Basic Zombie"),
        new ZombieOption("ZombieTutorialArmor1Default", 75, "Conehead Zombie"),
        new ZombieOption("ZombieTutorialArmor2Default", 125, "Buckethead Zombie"),
        new ZombieOption("ZombieMummyDefault", 50, "Mummy Zombie"),
        new ZombieOption("ZombieMummyArmor1Default", 75, "Mummy Conehead"),
        new ZombieOption("ZombieMummyArmor2Default", 125, "Mummy Buckethead"),
        new ZombieOption("ZombieIceageDefault", 50, "Iceage Zombie"),
        new ZombieOption("ZombieIceageArmor1Default", 75, "Iceage Conehead"),
        new ZombieOption("ZombieIceageArmor2Default", 125, "Iceage Buckethead"),
        new ZombieOption("ZombieBeachDefault", 60, "Beach Zombie"),
        new ZombieOption("ZombieBeachArmor1Default", 90, "Beach Conehead"),
        new ZombieOption("ZombieBeachArmor2Default", 150, "Beach Buckethead"),
        new ZombieOption("ZombieDarkDefault", 60, "Dark Zombie"),
        new ZombieOption("ZombieDarkArmor1Default", 90, "Dark Conehead"),
        new ZombieOption("ZombieDarkArmor2Default", 150, "Dark Buckethead"),
        new ZombieOption("ZombiePharaohDefault", 80, "Pharaoh Zombie"),
        new ZombieOption("ZombieCamelDefault", 175, "Camel Zombie"),
        new ZombieOption("ZombieTutorialImpDefault", 25, "Imp"),
        new ZombieOption("ZombieEgyptImpDefault", 25, "Egypt Imp"),
        new ZombieOption("ZombieIceageImpDefault", 25, "Iceage Imp"),
        new ZombieOption("ZombieBeachImpDefault", 25, "Beach Imp"),
        new ZombieOption("ZombieDarkImpDefault", 25, "Dark Imp"),
    };

    /** Picks a fresh, distinct-alias, randomly-ordered roster of `count` options every call. */
    public static List<ZombieOption> randomRoster(int count, java.util.Random random) {
        List<ZombieOption> pool = new ArrayList<>(List.of(RANDOM_POOL));
        Collections.shuffle(pool, random != null ? random : new java.util.Random());
        int size = Math.max(1, Math.min(count, pool.size()));
        return new ArrayList<>(pool.subList(0, size));
    }

    public IZombieGame(IZombieLevelDefinition level) {
        this.rows = level.getRows();
        this.cols = level.getCols();
        this.redLineCol = Math.max(0, Math.min(level.getRedLineCol(), cols - 1));
        this.sun = level.getStartingSun();
        // Roster is always freshly randomized per playthrough (not the level's
        // hand-authored JSON list), per design - same economy/board otherwise.
        int rosterSize = level.getZombieRoster() != null && !level.getZombieRoster().isEmpty()
            ? level.getZombieRoster().size() : 5;
        this.roster = randomRoster(rosterSize, new java.util.Random());
        this.sunZombieAlias = level.getSunZombieAlias();
        this.sunProductionBase = level.getSunProductionBase();
        this.sunProductionGrowthPerTick = level.getSunProductionGrowthPerTick();
        this.sunProductionIntervalSeconds = level.getSunProductionIntervalSeconds();
        this.sunProductionCap = level.getSunProductionCap();
        this.currentSunRate = sunProductionBase;
        this.sunProductionTimer = sunProductionIntervalSeconds;
        this.plantDensity = level.getPlantDensity();
        this.sunZombieAlive = new boolean[rows];
        java.util.Arrays.fill(sunZombieAlive, true);
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getRedLineCol() { return redLineCol; }

    public boolean isInDeployZone(int col) { return col > redLineCol && col < cols; }

    public int getSun() { return sun; }
    public void addSun(int amount) { sun = Math.max(0, sun + amount); }

    public List<ZombieOption> getRoster() { return roster; }

    public ZombieOption findOption(String alias) {
        for (ZombieOption o : roster) {
            if (o.getAlias().equalsIgnoreCase(alias)) return o;
        }
        return null;
    }

    public boolean trySpend(ZombieOption option) {
        if (option == null || sun < option.getCost()) return false;
        sun -= option.getCost();
        return true;
    }

    public String getSunZombieAlias() { return sunZombieAlias; }

    public boolean isSunZombieAlive(int row) {
        return row >= 0 && row < rows && sunZombieAlive[row];
    }

    public void markSunZombieDead(int row) {
        if (row >= 0 && row < rows) sunZombieAlive[row] = false;
    }

    public int tickSunProduction(float delta) {
        sunProductionTimer -= delta;
        if (sunProductionTimer > 0) return 0;
        sunProductionTimer = sunProductionIntervalSeconds;
        currentSunRate = Math.min(sunProductionCap, currentSunRate + sunProductionGrowthPerTick);

        int produced = 0;
        for (int r = 0; r < rows; r++) {
            if (sunZombieAlive[r]) {
                produced += (int) Math.round(currentSunRate);
            }
        }
        if (produced > 0) addSun(produced);
        return produced;
    }

    public double getCurrentSunRate() { return currentSunRate; }

    public double getPlantDensity() { return plantDensity; }

    public boolean isBrainEaten(int row) { return brainsEaten.contains(row); }

    public boolean eatBrain(int row) {
        if (row < 0 || row >= rows || brainsEaten.contains(row)) return false;
        brainsEaten.add(row);
        if (brainsEaten.size() >= rows) {
            won = true;
        }
        return true;
    }

    public int getBrainsRemaining() { return rows - brainsEaten.size(); }

    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public boolean isFinished() { return won || lost; }
    public void markLost() { lost = true; }
    public void markWon() { won = true; }
}
