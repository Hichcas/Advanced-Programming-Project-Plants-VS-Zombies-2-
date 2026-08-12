package com.PVZ.model.greenhouse;

import com.PVZ.model.enums.PlantType;

public class GreenhouseState {

    private Pot[][] pots;

    public GreenhouseState() {
        // تغییر به ۳ سطر و ۴ ستون
        pots = new Pot[3][4];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                pots[row][col] = new Pot(row == 0);
            }
        }
    }

    public Pot[][] getPots() {
        return pots;
    }

    public void setPots(Pot[][] pots) {
        this.pots = pots;
    }

    public Pot getPot(int x, int y) {
        if (x < 1 || x > 4 || y < 1 || y > 3) {
            throw new IllegalArgumentException("Invalid greenhouse coordinates: (" + x + ", " + y + ")");
        }
        return pots[y - 1][x - 1];
    }

    public void unlockPots(int count) {
        if (count <= 0) return;
        int unlockedSoFar = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (!pots[row][col].isUnlocked()) {
                    pots[row][col].setUnlocked(true);
                    unlockedSoFar++;
                    if (unlockedSoFar == count) return;
                }
            }
        }
    }

    public int getNumberOfLockedPots() {
        return 12 - getNumberOfUnlockedPots();
    }

    public boolean hasLockedPots() {
        return getNumberOfLockedPots() > 0;
    }

    public void unlockPot(int x, int y) {
        getPot(x, y).setUnlocked(true);
    }

    public void plantInPot(int x, int y, PlantType plantType, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting()) {
            throw new IllegalStateException("Pot is not available for planting.");
        }
        pot.setPlantType(plantType);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    public void plantMarigold(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting()) {
            throw new IllegalStateException("Pot is not available for planting.");
        }
        pot.setPlantType(null);
        pot.setMarigold(true);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    public PlantType collectFromPot(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) {
            throw new IllegalStateException("Pot is empty.");
        }
        PlantType harvested = pot.getPlantType();
        pot.setPlantType(null);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(0);
        return harvested;
    }

    public boolean isMarigold(int x, int y) {
        return getPot(x, y).isMarigold();
    }

    public void accelerateGrowth(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty() && !pot.isMarigold()) {
            throw new IllegalStateException("No plant to accelerate.");
        }
        pot.setPlantedTimeMillis(0);
    }

    public boolean isPlantReady(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return false;
        long duration = getGrowthDurationMillis(pot);
        return (currentTimeMillis - pot.getPlantedTimeMillis()) >= duration;
    }

    public double getRemainingHours(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return 0;
        long elapsed = currentTimeMillis - pot.getPlantedTimeMillis();
        long total = getGrowthDurationMillis(pot);
        if (elapsed >= total) return 0;
        return (total - elapsed) / (1000.0 * 3600.0);
    }

    public long getGrowthDurationMillis(Pot pot) {
        if (pot.isMarigold()) {
            return 2L * 60 * 60 * 1000;
        }
        return 8L * 60 * 60 * 1000;
    }

    public int getNumberOfUnlockedPots() {
        int c = 0;
        for (Pot[] row : pots) {
            for (Pot p : row) {
                if (p.isUnlocked()) c++;
            }
        }
        return c;
    }

    public long getGrowthDurationMillis(PlantType plantType) {
        return 8L * 60 * 60 * 1000;
    }
}
