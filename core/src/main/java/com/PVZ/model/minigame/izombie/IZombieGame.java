package com.PVZ.model.minigame.izombie;

import com.PVZ.model.enums.ZombieType;

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
    private List<ZombieOption> roster;

    private final double sunProductionBase;
    private final double sunProductionGrowthPerTick;
    private final double sunProductionIntervalSeconds;
    private final double sunProductionCap;
    private double sunProductionTimer;
    private double[] sunProductionTimerByRow;
    private double currentSunRate;
    private final double plantDensity;
    private final boolean[] sunZombieAlive;
    private final String sunZombieAlias;
    private final Set<Integer> brainsEaten = new HashSet<>();

    private boolean won = false;
    private boolean lost = false;

    /**
     * Every walking, non-boss zombie the renderer already knows how to animate
     * reliably (matches com.PVZ.model.enums.ZombieType - Zomboss mechs excluded since
     * those are stage-boss set-pieces, not placeable roster units). Costs are a simple
     * tier heuristic (armor pieces / special abilities = pricier) so a freshly-
     * randomized roster still feels balanced without needing 60 hand-tuned numbers.
     */
    private static final ZombieOption[] RANDOM_POOL = {
        new ZombieOption("ZombieTutorialDefault", 50, "Basic Zombie"),
        new ZombieOption("ZombieTutorialArmor1Default", 75, "Conehead Zombie"),
        new ZombieOption("ZombieTutorialArmor2Default", 125, "Buckethead Zombie"),
        new ZombieOption("ZombieTutorialArmor4Default", 175, "Screen Door Zombie"),
        new ZombieOption("ZombieTutorialFlagDefault", 50, "Flag Zombie"),
        new ZombieOption("ZombieMummyDefault", 50, "Mummy Zombie"),
        new ZombieOption("ZombieMummyArmor1Default", 75, "Mummy Conehead"),
        new ZombieOption("ZombieMummyArmor2Default", 125, "Mummy Buckethead"),
        new ZombieOption("ZombieMummyArmor4Default", 175, "Mummy Screen Door"),
        new ZombieOption("ZombieIceageDefault", 50, "Iceage Zombie"),
        new ZombieOption("ZombieIceageArmor1Default", 75, "Iceage Conehead"),
        new ZombieOption("ZombieIceageArmor2Default", 125, "Iceage Buckethead"),
        new ZombieOption("ZombieIceageArmor3Default", 150, "Iceage Ice Block"),
        new ZombieOption("ZombieBeachDefault", 60, "Beach Zombie"),
        new ZombieOption("ZombieBeachArmor1Default", 90, "Beach Conehead"),
        new ZombieOption("ZombieBeachArmor2Default", 150, "Beach Buckethead"),
        new ZombieOption("ZombieDarkDefault", 60, "Dark Zombie"),
        new ZombieOption("ZombieDarkArmor1Default", 90, "Dark Conehead"),
        new ZombieOption("ZombieDarkArmor2Default", 150, "Dark Buckethead"),
        new ZombieOption("ZombieDarkArmor3Default", 190, "Dark Crowned"),
        new ZombieOption("ZombieDarkArmor4Default", 210, "Dark Screen Door"),
        new ZombieOption("ZombiePharaohDefault", 80, "Pharaoh Zombie"),
        new ZombieOption("ZombieCamelDefault", 175, "Camel Zombie"),
        new ZombieOption("ZombieTutorialImpDefault", 25, "Imp"),
        new ZombieOption("ZombieEgyptImpDefault", 25, "Egypt Imp"),
        new ZombieOption("ZombieIceageImpDefault", 25, "Iceage Imp"),
        new ZombieOption("ZombieBeachImpDefault", 25, "Beach Imp"),
        new ZombieOption("ZombieDarkImpDefault", 25, "Dark Imp"),
        new ZombieOption("ZombieDarkImpDragon", 45, "Imp Dragon"),
        new ZombieOption("ZombieIceAgeTroglobite", 140, "Troglobite"),
        new ZombieOption("ZombieIceAgeDodo", 200, "Dodo Rider"),
        new ZombieOption("ZombieWeaselHoarderDefault", 130, "Weasel Hoarder"),
        new ZombieOption("ZombieWeaselDefault", 70, "Weasel"),
        new ZombieOption("ZombieBeachSnorkel", 90, "Snorkel Zombie"),
        new ZombieOption("ZombieBeachSurfer", 140, "Surfer Zombie"),
        new ZombieOption("ZombieBeachFastSwimmer", 100, "Fast Swimmer"),
        new ZombieOption("ZombieBeachFisherman", 110, "Fisherman Zombie"),
        new ZombieOption("ZombieBeachOctopus", 160, "Octopus Zombie"),
        new ZombieOption("ZombieRaDefault", 180, "Ra Zombie"),
        new ZombieOption("ZombieExplorerDefault", 130, "Explorer Zombie"),
        new ZombieOption("ZombieTombRaiserDefault", 190, "Tomb Raiser"),
        new ZombieOption("ZombieIceAgeHunter", 160, "Iceage Hunter"),
        new ZombieOption("ZombieWizardDefault", 200, "Wizard Zombie"),
        new ZombieOption("ZombieDarkJugglerDefault", 170, "Dark Juggler"),
        new ZombieOption("ZombieDarkKing", 260, "Dark King"),
        new ZombieOption("ZombieModernAllStar", 150, "All-Star Zombie"),
        new ZombieOption("ZombieFootball", 220, "Football Zombie"),
        new ZombieOption("Zombie80sArcade", 170, "Arcade Zombie"),
        new ZombieOption("ZombieLostCityJane", 130, "Jane Zombie"),
        new ZombieOption("ZombieLostCityCrystalSkull", 240, "Crystal Skull Zombie"),
        new ZombieOption("ZombieProspector", 110, "Prospector Zombie"),
        new ZombieOption("ZombiePiano", 190, "Piano Zombie"),
        new ZombieOption("ZombieModernNewspaper", 100, "Newspaper Zombie"),
        new ZombieOption("ZombotanyPeashooterDefault", 120, "Zombotany Peashooter"),
        new ZombieOption("ZombotanyWallnutDefault", 130, "Zombotany Wall-nut"),
        new ZombieOption("ZombotanyJalapenoDefault", 150, "Zombotany Jalapeno"),
        new ZombieOption("ZombotanySquashDefault", 140, "Zombotany Squash"),
        new ZombieOption("ZombieGargantuarBasic", 400, "Gargantuar"),
        new ZombieOption("ZombieEgyptGargantuar", 420, "Egypt Gargantuar"),
        new ZombieOption("ZombieIceAgeGargantuar", 420, "Iceage Gargantuar"),
        new ZombieOption("ZombieBeachGargantuar", 420, "Beach Gargantuar"),
        new ZombieOption("ZombieDarkGargantuar", 440, "Dark Gargantuar"),
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
        // Each level ships its own hand-authored 5-zombie roster (see minigames.json ->
        // i_zombie -> levels[].zombieRoster). This is what the roster-selection screen
        // shows before the level starts. Only fall back to a random pool if a level is
        // missing its data entirely.
        this.roster = (level.getZombieRoster() != null && !level.getZombieRoster().isEmpty())
            ? new ArrayList<>(level.getZombieRoster())
            : randomRoster(5, new java.util.Random());
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
        // Stagger each row's first sun tick evenly across the interval so all five
        // rows don't thud down sun in lockstep every cycle - it fans out over time
        // instead of arriving as one big simultaneous pile.
        this.sunProductionTimerByRow = new double[rows];
        for (int r = 0; r < rows; r++) {
            sunProductionTimerByRow[r] = sunProductionIntervalSeconds * (r + 1) / (double) Math.max(1, rows);
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getRedLineCol() { return redLineCol; }

    public boolean isInDeployZone(int col) { return col > redLineCol && col < cols; }

    public int getSun() { return sun; }
    public void addSun(int amount) { sun = Math.max(0, sun + amount); }

    public List<ZombieOption> getRoster() { return roster; }

    public static final int MAX_SELECTED_ROSTER = 8;

    /**
     * Replaces the in-game packet list with the zombies the player actually picked
     * (at most 8), in pick order. Unknown aliases still become playable cards instead
     * of falling back to the whole catalog.
     */
    public void restrictRosterTo(java.util.Collection<String> selectedAliases) {
        if (selectedAliases == null || selectedAliases.isEmpty()) return;
        List<ZombieOption> next = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String alias : selectedAliases) {
            if (alias == null || alias.isBlank()) continue;
            String key = alias.toLowerCase();
            if (!seen.add(key)) continue;
            ZombieOption option = findOption(alias);
            next.add(option != null ? option : optionFromAlias(alias));
            if (next.size() >= MAX_SELECTED_ROSTER) break;
        }
        if (!next.isEmpty()) this.roster = next;
    }

    private static ZombieOption optionFromAlias(String alias) {
        ZombieType type = ZombieType.fromAlias(alias);
        return new ZombieOption(alias, costFor(type), displayNameFor(type));
    }

    private static int costFor(ZombieType type) {
        if (type == null) return 50;
        String name = type.name().toUpperCase();
        if (name.contains("GARGANTUAR") || name.contains("ZOMBOSS")) return 300;
        if (name.contains("BRICK") || name.contains("KNIGHT") ||
            name.contains("ARMOR2") || name.contains("CENTURION")) return 150;
        if (name.contains("BUCKET") || name.contains("BARREL") ||
            name.contains("JALAPENO") || name.contains("SQUASH")) return 125;
        if (name.contains("CONE") || name.contains("ARMOR1") ||
            name.contains("HELMET") || name.contains("FLAG")) return 75;
        if (name.contains("IMP")) return 25;
        if (name.contains("ZOMBOTANY")) return 100;
        return 50;
    }

    private static String displayNameFor(ZombieType type) {
        if (type == null) return "Zombie";
        String name = type.name().replace("ZOMBOTANY_", "Zombotany ").replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String part : name.split(" ")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

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

    /**
     * Advances the production timer/ramp and reports, per alive row, how much sun
     * that row's sun zombie generated this tick - without crediting it to the
     * player directly. The caller (IZombieGameEngine) is expected to drop an
     * actual collectible Sun near that row's zombie instead, matching how sun
     * works everywhere else in the game (tap to collect), rather than silently
     * incrementing the counter.
     */
    public java.util.Map<Integer, Integer> tickSunProductionPerRow(float delta) {
        // Rate still ramps up on the shared clock (matches sunProductionTimer/
        // currentSunRate used by the legacy tickSunProduction below), but each row
        // fires its own drop independently once its own staggered timer elapses.
        sunProductionTimer -= delta;
        if (sunProductionTimer <= 0) {
            sunProductionTimer += sunProductionIntervalSeconds;
            currentSunRate = Math.min(sunProductionCap, currentSunRate + sunProductionGrowthPerTick);
        }

        java.util.Map<Integer, Integer> perRow = new java.util.LinkedHashMap<>();
        int amount = (int) Math.round(currentSunRate);
        for (int r = 0; r < rows; r++) {
            if (!sunZombieAlive[r]) continue;
            sunProductionTimerByRow[r] -= delta;
            if (sunProductionTimerByRow[r] <= 0) {
                sunProductionTimerByRow[r] += sunProductionIntervalSeconds;
                perRow.put(r, amount);
            }
        }
        return perRow;
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
