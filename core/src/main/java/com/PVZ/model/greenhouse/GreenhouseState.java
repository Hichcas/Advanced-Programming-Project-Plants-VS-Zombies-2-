package com.PVZ.model.greenhouse;

import com.PVZ.model.enums.PlantType;

/**
 * وضعیت گلخانهٔ کاربر شامل ۲۰ گلدان در یک شبکهٔ ۴×۵.
 * ردیف اول (y=1) در ابتدا باز است، ردیف‌های ۲ تا ۴ قفل هستند و با خرید باز می‌شوند.
 */
public class GreenhouseState {

    private Pot[][] pots;

    public GreenhouseState() {
        pots = new Pot[4][5];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                // ردیف اول باز، بقیه قفل
                pots[row][col] = new Pot(row == 0);
            }
        }
    }

    // ---------- Getter / Setter (برای سریالایز) ----------
    public Pot[][] getPots() {
        return pots;
    }

    public void setPots(Pot[][] pots) {
        this.pots = pots;
    }

    /**
     * دریافت اطلاعات یک گلدان با مختصات (x,y) که x از ۱ تا ۵ و y از ۱ تا ۴.
     * @throws IllegalArgumentException اگر مختصات خارج از محدوده باشد
     */
    public Pot getPot(int x, int y) {
        if (x < 1 || x > 5 || y < 1 || y > 4) {
            throw new IllegalArgumentException("Invalid greenhouse coordinates: (" + x + ", " + y + ")");
        }
        return pots[y - 1][x - 1];
    }

    /**
     * باز کردن گلدان‌ها به تعداد مشخص شده.
     * ترتیب باز شدن به صورت ردیف به ردیف (از چپ به راست) است.
     * گلدان‌هایی که از قبل باز شده‌اند نادیده گرفته می‌شوند.
     */
    public void unlockPots(int count) {
        if (count <= 0) return;

        int unlockedSoFar = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                if (!pots[row][col].isUnlocked()) {
                    pots[row][col].setUnlocked(true);
                    unlockedSoFar++;
                    if (unlockedSoFar == count) {
                        return;
                    }
                }
            }
        }
    }

    /** تعداد کل گلدان‌های قفل‌شده در گلخانه را برمی‌گرداند. */
    public int getNumberOfLockedPots() {
        return 20 - getNumberOfUnlockedPots();
    }

    /** بررسی می‌کند که آیا هنوز گلدان قفل‌شده‌ای باقی مانده است یا خیر. */
    public boolean hasLockedPots() {
        return getNumberOfLockedPots() > 0;
    }

    /** باز کردن قفل یک گلدان خاص */
    public void unlockPot(int x, int y) {
        getPot(x, y).setUnlocked(true);
    }

    /** کاشت گیاه آنلاک‌شده (گیاه معمولی بازی) */
    public void plantInPot(int x, int y, PlantType plantType, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting()) {
            throw new IllegalStateException("Pot is not available for planting.");
        }
        pot.setPlantType(plantType);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    /** کاشت گل معمولی (Marigold) */
    public void plantMarigold(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting()) {
            throw new IllegalStateException("Pot is not available for planting.");
        }
        pot.setPlantType(null);
        pot.setMarigold(true);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    /** برداشت گیاه (خالی کردن گلدان و برگرداندن نوع گیاه قبلی)
     *  برای Marigold مقدار null برمی‌گرداند. */
    public PlantType collectFromPot(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) {
            throw new IllegalStateException("Pot is empty.");
        }
        PlantType harvested = pot.getPlantType();
        pot.setPlantType(null);
        pot.setMarigold(false);
        pot.setPlantedTimeMillis(0);
        return harvested;   // برای marigold، null است
    }

    /** آیا گلدان مشخص شده Marigold دارد؟ */
    public boolean isMarigold(int x, int y) {
        return getPot(x, y).isMarigold();
    }

    /** تسریع رشد (بلافاصله گیاه را آمادهٔ برداشت می‌کند) */
    public void accelerateGrowth(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty() && !pot.isMarigold()) {
            throw new IllegalStateException("No plant to accelerate.");
        }
        pot.setPlantedTimeMillis(0);   // ready instantly
    }

    /** بررسی آماده بودن گیاه برای برداشت بر اساس زمان فعلی */
    public boolean isPlantReady(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return false;
        long duration = getGrowthDurationMillis(pot);
        return (currentTimeMillis - pot.getPlantedTimeMillis()) >= duration;
    }

    /** ساعت باقی‌مانده تا رشد کامل (به صورت اعشاری) */
    public double getRemainingHours(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return 0;
        long elapsed = currentTimeMillis - pot.getPlantedTimeMillis();
        long total = getGrowthDurationMillis(pot);
        if (elapsed >= total) return 0;
        return (total - elapsed) / (1000.0 * 3600.0);
    }

    /** زمان رشد بر اساس نوع گیاه موجود در گلدان (Marigold یا غیر آن) */
    public long getGrowthDurationMillis(Pot pot) {
        if (pot.isMarigold()) {
            return 2L * 60 * 60 * 1000;   // 2 ساعت
        }
        return 8L * 60 * 60 * 1000;       // 8 ساعت برای سایر گیاهان
    }

    /** تعداد گلدان‌های باز */
    public int getNumberOfUnlockedPots() {
        int c = 0;
        for (Pot[] row : pots) {
            for (Pot p : row) {
                if (p.isUnlocked()) c++;
            }
        }
        return c;
    }

    /**
     * متد کمکی قدیمی (برای جاهایی که PlantType بدون Pot نیاز است)
     * فعلاً همان ۸ ساعت را برمی‌گرداند.
     */
    public long getGrowthDurationMillis(PlantType plantType) {
        return 8L * 60 * 60 * 1000;
    }
}
