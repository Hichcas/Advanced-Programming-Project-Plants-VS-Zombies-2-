package com.PVZ.model.minigame.beghouled;

import com.PVZ.model.enums.PlantType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeghouledGame {

    public static final int SUN_PER_UNIT = 50;

    private final int rows;
    private final int cols;

    private final PlantType[][] board;
    private final boolean[][] crater;

    private final PlantType[] plantTypes;
    private final List<BeghouledUpgrade> upgrades;
    private final List<String> zombieAliases;
    private final double zombieSpawnIntervalSeconds;

    private int sun;
    private int matchesMade;
    private final int targetMatches;

    private boolean won = false;
    private boolean lost = false;

    private final Random random = new Random();

    public BeghouledGame(BeghouledLevelDefinition level) {
        this.rows = Math.max(1, level.getRows());
        this.cols = Math.max(1, level.getCols());
        this.board = new PlantType[rows][cols];
        this.crater = new boolean[rows][cols];
        this.sun = level.getStartingSun();
        this.targetMatches = Math.max(1, level.getTargetMatches());
        this.zombieSpawnIntervalSeconds = level.getZombieSpawnIntervalSeconds();
        this.plantTypes = resolvePlantTypes(level.getPlantTypes());
        this.upgrades = resolveUpgrades(level.getUpgrades());
        this.zombieAliases = resolveZombieAliases(level.getZombieAliases());
    }

    private static final PlantType[] DEFAULT_PLANT_TYPES = {
            PlantType.PEASHOOTER, PlantType.WALL_NUT, PlantType.PUFF_SHROOM,
            PlantType.CABBAGE_PULT, PlantType.SUNFLOWER
    };

    private static PlantType[] resolvePlantTypes(List<String> names) {
        if (names == null || names.isEmpty()) {
            return DEFAULT_PLANT_TYPES.clone();
        }
        List<PlantType> resolved = new ArrayList<>();
        for (String name : names) {
            PlantType type = safeValueOf(name);
            if (type != null) resolved.add(type);
        }
        if (resolved.isEmpty()) return DEFAULT_PLANT_TYPES.clone();
        return resolved.toArray(new PlantType[0]);
    }

    private static PlantType safeValueOf(String name) {
        if (name == null) return null;
        try {
            return PlantType.valueOf(name.trim());
        } catch (IllegalArgumentException ex) {
            System.err.println("[Beghouled] unknown PlantType in level config: " + name);
            return null;
        }
    }

    private static List<BeghouledUpgrade> resolveUpgrades(List<BeghouledUpgrade> configured) {
        if (configured != null && !configured.isEmpty()) {
            return new ArrayList<>(configured);
        }

        List<BeghouledUpgrade> defaults = new ArrayList<>();
        defaults.add(new BeghouledUpgrade(PlantType.PEASHOOTER, PlantType.REPEATER, 500));
        defaults.add(new BeghouledUpgrade(PlantType.REPEATER, PlantType.THREEPEATER, 1500));
        defaults.add(new BeghouledUpgrade(PlantType.WALL_NUT, PlantType.TALL_NUT, 500));
        defaults.add(new BeghouledUpgrade(PlantType.PUFF_SHROOM, PlantType.FUME_SHROOM, 250));
        defaults.add(new BeghouledUpgrade(PlantType.CABBAGE_PULT, PlantType.MELON_PULT, 1000));
        defaults.add(new BeghouledUpgrade(PlantType.MELON_PULT, PlantType.WINTER_MELON, 750));
        return defaults;
    }

    private static List<String> resolveZombieAliases(List<String> configured) {
        if (configured != null && !configured.isEmpty()) {
            return new ArrayList<>(configured);
        }
        List<String> defaults = new ArrayList<>();
        defaults.add("ZombieTutorialDefault");
        defaults.add("ZombieTutorialArmor1Default");
        defaults.add("ZombieMummyDefault");
        defaults.add("ZombieIceageDefault");
        defaults.add("ZombieBeachDefault");
        return defaults;
    }

    public void initializeBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!crater[r][c]) {
                    board[r][c] = randomPlantType();
                }
            }
        }

        if (!isAnyMovePossible()) {
            resetBoard();
        }
    }

    private PlantType randomPlantType() {
        return plantTypes[random.nextInt(plantTypes.length)];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public PlantType getPlantTypeAt(int r, int c) {
        if (!isInBounds(r, c)) return null;
        return board[r][c];
    }

    public boolean isCrater(int r, int c) {
        return isInBounds(r, c) && crater[r][c];
    }

    public boolean isPlant(int r, int c) {
        return isInBounds(r, c) && board[r][c] != null && !crater[r][c];
    }

    public int getSun() { return sun; }
    public void addSun(int amount) { sun = Math.max(0, sun + amount); }

    public int getMatchesMade() { return matchesMade; }
    public int getTargetMatches() { return targetMatches; }
    public int getMatchesRemaining() { return Math.max(0, targetMatches - matchesMade); }

    public PlantType[] getPlantTypes() { return plantTypes.clone(); }
    public List<BeghouledUpgrade> getUpgrades() { return upgrades; }
    public List<String> getZombieAliases() { return zombieAliases; }
    public double getZombieSpawnIntervalSeconds() { return zombieSpawnIntervalSeconds; }

    public String randomZombieAlias() {
        return zombieAliases.get(random.nextInt(zombieAliases.size()));
    }

    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public boolean isFinished() { return won || lost; }
    public void markWon() { won = true; }
    public void markLost() { lost = true; }

    private static boolean adjacent(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }

    public boolean canSwap(int r1, int c1, int r2, int c2) {
        if (!isPlant(r1, c1) || !isPlant(r2, c2)) return false;
        if (!adjacent(r1, c1, r2, c2)) return false;

        swapCells(r1, c1, r2, c2);
        boolean createsMatch = !findRuns().isEmpty();
        swapCells(r1, c1, r2, c2);
        return createsMatch;
    }

    public String swap(int r1, int c1, int r2, int c2) {
        if (!isPlant(r1, c1) || !isPlant(r2, c2)) {
            return "Both cells must hold a plant.";
        }
        if (!adjacent(r1, c1, r2, c2)) {
            return "You can only swap two adjacent plants.";
        }
        if (!canSwap(r1, c1, r2, c2)) {
            return "That swap does not make a match of 3+.";
        }
        swapCells(r1, c1, r2, c2);
        int before = matchesMade;
        int sunGained = resolveMatches();
        int made = matchesMade - before;
        return "Matched " + made + " group(s), +" + sunGained + " sun. Sun: " + sun
                + " | Matches: " + matchesMade + "/" + targetMatches;
    }

    private void swapCells(int r1, int c1, int r2, int c2) {
        PlantType tmp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = tmp;
    }

    private static final class Run {
        final int size;
        final List<int[]> cells;
        Run(int size, List<int[]> cells) {
            this.size = size;
            this.cells = cells;
        }
    }

    private List<Run> findRuns() {
        List<Run> runs = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            int c = 0;
            while (c < cols) {
                PlantType t = board[r][c];
                if (t == null || crater[r][c]) { c++; continue; }
                int start = c;
                while (c + 1 < cols && board[r][c + 1] == t && !crater[r][c + 1]) c++;
                int len = c - start + 1;
                if (len >= 3) {
                    List<int[]> cells = new ArrayList<>();
                    for (int k = start; k <= c; k++) cells.add(new int[]{r, k});
                    runs.add(new Run(len, cells));
                }
                c++;
            }
        }

        for (int c = 0; c < cols; c++) {
            int r = 0;
            while (r < rows) {
                PlantType t = board[r][c];
                if (t == null || crater[r][c]) { r++; continue; }
                int start = r;
                while (r + 1 < rows && board[r + 1][c] == t && !crater[r + 1][c]) r++;
                int len = r - start + 1;
                if (len >= 3) {
                    List<int[]> cells = new ArrayList<>();
                    for (int k = start; k <= r; k++) cells.add(new int[]{k, c});
                    runs.add(new Run(len, cells));
                }
                r++;
            }
        }

        return runs;
    }

    public int resolveMatches() {
        int totalSunGained = 0;
        int chainIndex = 0;

        while (true) {
            List<Run> runs = findRuns();
            if (runs.isEmpty()) break;

            boolean cascade = chainIndex > 0;

            boolean[][] toClear = new boolean[rows][cols];
            for (Run run : runs) {
                int sunUnits = (run.size - 2) + (cascade ? 1 : 0);
                int gained = sunUnits * SUN_PER_UNIT;
                sun += gained;
                totalSunGained += gained;
                matchesMade++;
                for (int[] cell : run.cells) {
                    toClear[cell[0]][cell[1]] = true;
                }
            }

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (toClear[r][c]) board[r][c] = null;
                }
            }

            applyGravity();
            refill();
            chainIndex++;
        }

        return totalSunGained;
    }

    private void applyGravity() {
        for (int c = 0; c < cols; c++) {
            int segBottom = rows - 1;
            while (segBottom >= 0) {
                if (crater[segBottom][c]) { segBottom--; continue; }
                int bottom = segBottom;
                int top = bottom;
                while (top - 1 >= 0 && !crater[top - 1][c]) top--;

                List<PlantType> stacked = new ArrayList<>();
                for (int r = top; r <= bottom; r++) {
                    if (board[r][c] != null) stacked.add(board[r][c]);
                }
                for (int r = top; r <= bottom; r++) board[r][c] = null;
                int write = bottom;
                for (int i = stacked.size() - 1; i >= 0; i--) {
                    board[write][c] = stacked.get(i);
                    write--;
                }

                segBottom = top - 1;
            }
        }
    }

    private void refill() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!crater[r][c] && board[r][c] == null) {
                    board[r][c] = randomPlantType();
                }
            }
        }
    }

    public boolean isAnyMovePossible() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!isPlant(r, c)) continue;
                if (c + 1 < cols && canSwap(r, c, r, c + 1)) return true;
                if (r + 1 < rows && canSwap(r, c, r + 1, c)) return true;
            }
        }
        return false;
    }

    public void resetBoard() {
        for (int attempt = 0; attempt < 50; attempt++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (!crater[r][c]) board[r][c] = randomPlantType();
                }
            }
            if (isAnyMovePossible()) return;
        }

    }

    public void makeCrater(int r, int c) {
        if (!isInBounds(r, c)) return;
        crater[r][c] = true;
        board[r][c] = null;
    }

    public String tryUpgrade(PlantType from, PlantType to, int cost) {
        if (from == null || to == null) return "Unknown upgrade.";
        if (sun < cost) {
            return "Not enough sun for " + from.getDisplayName() + " -> "
                    + to.getDisplayName() + " (needs " + cost + ", have " + sun + ").";
        }
        int converted = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == from) {
                    board[r][c] = to;
                    converted++;
                }
            }
        }
        sun -= cost;
        if (converted == 0) {
            sun += cost;
            return "No " + from.getDisplayName() + " on the board to upgrade.";
        }
        return "Upgraded " + converted + " " + from.getDisplayName() + " into "
                + to.getDisplayName() + ". Sun: " + sun;
    }

    public String tryUpgrade(BeghouledUpgrade upgrade) {
        if (upgrade == null) return "Unknown upgrade.";
        return tryUpgrade(upgrade.getFrom(), upgrade.getTo(), upgrade.getCost());
    }

    public String cellTag(int r, int c) {
        if (isCrater(r, c)) return "XX";
        PlantType t = getPlantTypeAt(r, c);
        if (t == null) return "..";
        return switch (t) {
            case PEASHOOTER -> "Pe";
            case REPEATER -> "Re";
            case THREEPEATER -> "3p";
            case WALL_NUT -> "Wn";
            case TALL_NUT -> "Tn";
            case PUFF_SHROOM -> "Pu";
            case FUME_SHROOM -> "Fu";
            case CABBAGE_PULT -> "Ca";
            case MELON_PULT -> "Me";
            case WINTER_MELON -> "Wi";
            case SUNFLOWER -> "Su";
            default -> t.name().substring(0, Math.min(2, t.name().length()));
        };
    }
}
