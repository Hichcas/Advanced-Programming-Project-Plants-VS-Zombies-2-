package com.PVZ.model.greenhouse;

import com.PVZ.model.enums.PlantType;

public class GreenhouseState {

    private Pot[][] pots;

    public GreenhouseState() {
        pots = new Pot[4][5];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                pots[row][col] = new Pot(row == 0);   // ردیف اول باز
            }
        }
    }

    public Pot[][] getPots() { return pots; }
    public void setPots(Pot[][] pots) { this.pots = pots; }

    public Pot getPot(int x, int y) {
        if (x < 1 || x > 5 || y < 1 || y > 4)
            throw new IllegalArgumentException("Invalid greenhouse coordinates: (" + x + ", " + y + ")");
        return pots[y - 1][x - 1];
    }

    public void unlockPots(int count) { /* بدون تغییر */ }

    public int getNumberOfLockedPots() {
        return 20 - getNumberOfUnlockedPots();
    }

    public boolean hasLockedPots() {
        return getNumberOfLockedPots() > 0;
    }

    public void unlockPot(int x, int y) {
        getPot(x, y).setUnlocked(true);
    }

    /** کاشت گیاه آنلاک‌شده */
    public void plantInPot(int x, int y, PlantType plantType, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting())
            throw new IllegalStateException("Pot is not available for planting.");
        pot.setPlantType(plantType);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    /** کاشت گل معمولی (marigold) */
    public void plantMarigold(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting())
            throw new IllegalStateException("Pot is not available for planting.");
        pot.setPlantType(null);
        pot.setMarigold(true);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    /** برداشت گیاه (خالی کردن گلدان و برگرداندن نوع گیاه قبلی) */
    public PlantType collectFromPot(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty())
            throw new IllegalStateException("Pot is empty.");
        PlantType harvested = pot.getPlantType();
        pot.setPlantType(null);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(0);
        return harvested;   // برای marigold، null برمی‌گردد
    }

    /** آیا گلدان marigold دارد؟ */
    public boolean isMarigold(int x, int y) {
        return getPot(x, y).isMarigold();
    }

    /** تسریع رشد */
    public void accelerateGrowth(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty() && !pot.isMarigold())
            throw new IllegalStateException("No plant to accelerate.");
        pot.setPlantedTimeMillis(0);
    }

    /** بررسی آماده بودن برای برداشت */
    public boolean isPlantReady(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return false;
        long duration = getGrowthDurationMillis(pot);
        return (currentTimeMillis - pot.getPlantedTimeMillis()) >= duration;
    }

    /** ساعت باقی‌مانده (اعشاری) */
    public double getRemainingHours(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return 0;
        long elapsed = currentTimeMillis - pot.getPlantedTimeMillis();
        long total = getGrowthDurationMillis(pot);
        if (elapsed >= total) return 0;
        return (total - elapsed) / (1000.0 * 3600.0);
    }

    /** زمان رشد بر اساس نوع واقعی گیاه (pot) */
    public long getGrowthDurationMillis(Pot pot) {
        if (pot.isMarigold()) return 2L * 60 * 60 * 1000;   // 2h
        return 8L * 60 * 60 * 1000;                         // 8h
    }

    public int getNumberOfUnlockedPots() {
        int c = 0;
        for (Pot[] row : pots)
            for (Pot p : row)
                if (p.isUnlocked()) c++;
        return c;
    }

    // متد قدیمی (برای جاهایی که PlantType نیاز است) می‌تواند بماند، ولی استفاده نشود
    public long getGrowthDurationMillis(PlantType plantType) {
        return 8L * 60 * 60 * 1000;
    }
}
