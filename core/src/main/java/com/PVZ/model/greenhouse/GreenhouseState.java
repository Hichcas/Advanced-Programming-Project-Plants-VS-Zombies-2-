package com.PVZ.model.greenhouse;


import com.PVZ.model.enums.PlantType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * وضعیت گلخانهٔ کاربر شامل ۲۰ گلدان در یک شبکهٔ ۴×۵.
 * ردیف اول (y=1) در ابتدا باز است، ردیف‌های ۲ تا ۴ قفل هستند و با خرید باز می‌شوند.
 */
public class GreenhouseState {

    public static class Pot {
        private boolean unlocked;
        private PlantType plantType;          // null if empty
        private long plantedTimeMillis;       // 0 if empty

        public Pot() {
            this.unlocked = false;
            this.plantType = null;
            this.plantedTimeMillis = 0;
        }

        public Pot(boolean unlocked) {
            this.unlocked = unlocked;
            this.plantType = null;
            this.plantedTimeMillis = 0;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public void setUnlocked(boolean unlocked) {
            this.unlocked = unlocked;
        }

        public PlantType getPlantType() {
            return plantType;
        }

        public void setPlantType(PlantType plantType) {
            this.plantType = plantType;
        }

        public long getPlantedTimeMillis() {
            return plantedTimeMillis;
        }

        public void setPlantedTimeMillis(long plantedTimeMillis) {
            this.plantedTimeMillis = plantedTimeMillis;
        }

        /** آیا گلدان خالی است (گیاهی در آن کاشته نشده) */
        @JsonIgnore
        public boolean isEmpty() {
            return plantType == null;
        }

        /** آیا گلدان باز و خالی است (آمادهٔ کاشت) */
        @JsonIgnore
        public boolean isReadyForPlanting() {
            return unlocked && isEmpty();
        }
    }

    // ردیف‌ها ۰ تا ۳ (y = 1..4)، ستون‌ها ۰ تا ۴ (x = 1..5)
    private Pot[][] pots;

    // ---------- سازنده ----------
    public GreenhouseState() {
        pots = new Pot[4][5];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                // ردیف اول (row 0) باز، بقیه قفل
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

    // ---------- دسترسی بر اساس مختصات بازی (۱-based) ----------
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

    /** باز کردن قفل یک گلدان (با خرید) */
    public void unlockPot(int x, int y) {
        getPot(x, y).setUnlocked(true);
    }

    /** کاشت یک گیاه در گلدان (باید باز و خالی باشد) */
    public void plantInPot(int x, int y, PlantType plantType, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (!pot.isReadyForPlanting()) {
            throw new IllegalStateException("Pot is not available for planting.");
        }
        pot.setPlantType(plantType);
        pot.setPlantedTimeMillis(currentTimeMillis);
    }

    /** برداشت گیاه (خالی کردن گلدان و برگرداندن نوع گیاه قبلی) */
    public PlantType collectFromPot(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) {
            throw new IllegalStateException("Pot is empty.");
        }
        PlantType harvested = pot.getPlantType();
        pot.setPlantType(null);
        pot.setPlantedTimeMillis(0);
        return harvested;
    }

    /** تسریع رشد (بلافاصله گیاه را آمادهٔ برداشت می‌کند – صرفاً plantedTimeMillis را صفر می‌کند) */
    public void accelerateGrowth(int x, int y) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) {
            throw new IllegalStateException("No plant to accelerate.");
        }
        pot.setPlantedTimeMillis(0); // ready instantly
    }

    /** بررسی آماده بودن گیاه برای برداشت بر اساس زمان فعلی و نوع گیاه */
    public boolean isPlantReady(int x, int y, long currentTimeMillis) {
        Pot pot = getPot(x, y);
        if (pot.isEmpty()) return false;
        long growthDurationMillis = getGrowthDurationMillis(pot.getPlantType());
        return (currentTimeMillis - pot.getPlantedTimeMillis()) >= growthDurationMillis;
    }

    /** زمان لازم برای رشد کامل یک گیاه (به میلی‌ثانیه) */
    private long getGrowthDurationMillis(PlantType plantType) {
        // marigold (= معمولی) 2 ساعت، سایر گیاهان 8 ساعت



        // TODO this method must be completed later
        // if (plantType == PlantType.MARIGOLD) { // فرض می‌کنیم MARIGOLD در PlantType تعریف شده باشد
        //     return 2L * 60 * 60 * 1000;
        // }


        return 8L * 60 * 60 * 1000;
    }
}
