package com.PVZ.model.game.survival;

/**
 * ۵ الگوی امتیازگیری بازی امتیازی («میوپوینت»)، طبق سند فاز سه:
 * «شما باید ۵ الگوی امتیازگیری تعریف کنید که با استراتژی‌های مختلف
 * بتوان میزان میوپوینت متفاوتی به‌دست آورد».
 *
 * سه‌تای اول تقریب مستقیم سه مثالِ خودِ سند هستند؛ دوتای آخر برای
 * رسیدن به عدد ۵ و ایجاد تنوع استراتژی (تهاجمی جلوی خط دفاعی در برابر
 * دفاع محتاطانه، یا زنجیره‌ی ضربه‌ی پیوسته در برابر ضربات پراکنده)
 * اضافه شده‌اند.
 */
public enum MyoPointPattern {

    /** کشتن زامبی خیلی زود بعد از ورودش به زمین (سند: «کشتن سریع زامبی»). */
    QUICK_KILL("Quick Kill", 20),

    /**
     * چند زامبی در یک لاین (سطر) در بازه‌ی زمانی خیلی کوتاه از هم می‌میرند
     * - تقریبی از «کشتن چند زامبی با یک تیر» (مثلا یک نخود سوراخ‌کننده
     * که چند زامبی پشت سر هم را همزمان می‌زند).
     */
    SINGLE_SHOT_MULTI_KILL("Single-Shot Multi-Kill", 25),

    /**
     * دو زامبی در سطرهای مختلف تقریبا همزمان کشته می‌شوند - سند:
     * «کشتن همزمان زامبی‌ها» (بر خلاف الگوی بالا که مخصوص یک لاین است).
     */
    SIMULTANEOUS_KILL("Simultaneous Kill", 20),

    /** زنجیره‌ای از کشتارهای پیوسته بدون وقفه‌ی طولانی - هر چه زنجیره طولانی‌تر، امتیاز بیشتر. */
    COMBO_STREAK("Combo Streak", 5),

    /** کشتن زامبی درست وقتی نزدیک به خانه (ستون‌های آخر) رسیده - ریسک بالا، پاداش بالا. */
    CLUTCH_KILL("Clutch Kill", 40);

    private final String displayName;
    private final int basePoints;

    MyoPointPattern(String displayName, int basePoints) {
        this.displayName = displayName;
        this.basePoints = basePoints;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBasePoints() {
        return basePoints;
    }
}
