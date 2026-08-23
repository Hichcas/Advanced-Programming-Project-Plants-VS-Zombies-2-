package com.PVZ.model.game.survival;

import com.PVZ.model.game.Wave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * تولید موج‌های زامبی برای بازی امتیازی («استیج ۴، فصل ۴ - دارک ایجز»).
 *
 * برخلاف مراحل عادی که موج‌هایشان از {@code chapters.json} خوانده
 * می‌شود، این مرحله موج‌هایش را در لحظه‌ی ورود، به‌صورت رویه‌ای (procedural)
 * می‌سازد - طبق سند: «این بازی در هر روز برای همه‌ی کاربران با الگوریتم
 * یکسانی زامبی تولید می‌کند». چون از {@link DailySeedProvider} استفاده
 * می‌شود، تمام کاربرانی که همان روز (UTC) وارد این مرحله شوند، دقیقا
 * همان توالی زامبی‌ها را می‌گیرند؛ نتیجه هم کاملا با بقیه‌ی pipeline
 * (Wave/WaveManager/RegularGameEngine) سازگار است - هیچ موتور جدیدی
 * لازم نبود، فقط منبع تولید موج‌ها عوض شده.
 */
public final class SurvivalHandler {

    private SurvivalHandler() {
    }

    private static final int WAVE_COUNT = 6;
    private static final float FIRST_WAVE_START_DELAY = 8f;
    private static final float NEXT_WAVE_START_DELAY = 14f;

    /**
     * روستر زامبی‌های فصل «دارک ایجز» (همان‌هایی که در chapters.json برای
     * این فصل استفاده شده‌اند)، تفکیک‌شده به دو دسته‌ی معمولی و ویژه/نخبه
     * تا موج‌های آخر سخت‌تر و متنوع‌تر باشند.
     */
    private static final String[] COMMON_ZOMBIES = {
        "ZombieDarkDefault",
        "ZombieDarkArmor1Default",
        "ZombieCamelDefault",
        "ZombieDarkImpDefault",
    };
    private static final String[] ELITE_ZOMBIES = {
        "ZombieDarkArmor2Default",
        "ZombieDarkArmor3Default",
        "ZombieDarkJugglerDefault",
        "ZombieWizardDefault",
        "ZombiePharaohDefault",
    };
    private static final String BOSS_ZOMBIE = "ZombieDarkGargantuar";

    /** موج‌های امروز را می‌سازد. هر کاربری که امروز صدایش بزند، همین خروجی دقیق را می‌گیرد. */
    public static List<Wave> generateDailyWaves() {
        Random random = DailySeedProvider.newTodayRandom();
        List<Wave> waves = new ArrayList<>();

        for (int waveIndex = 0; waveIndex < WAVE_COUNT; waveIndex++) {
            waves.add(buildWave(waveIndex, random));
        }
        return waves;
    }

    private static Wave buildWave(int waveIndex, Random random) {
        // هر چه موج جلوتر برود: تعداد زامبی بیشتر، سرعت ورود (spawnDelay کمتر) بیشتر،
        // و احتمال حضور زامبی‌های نخبه/باس بالاتر می‌رود - یک منحنی سختی کلاسیک survival.
        int baseCount = 4 + waveIndex * 2;
        int variedCount = baseCount + random.nextInt(3); // کمی تصادفی‌سازی بدون خارج شدن از کنترل
        float spawnDelay = Math.max(0.8f, 2.4f - waveIndex * 0.25f);
        boolean isFinalWave = waveIndex == WAVE_COUNT - 1;

        List<Wave.WaveEntry> entries = new ArrayList<>();
        for (int i = 0; i < variedCount; i++) {
            entries.add(new Wave.WaveEntry(pickZombieAlias(waveIndex, random), 1, spawnDelay));
        }
        if (isFinalWave) {
            // موج آخر یک گارگانتوار به‌عنوان چاشنی نهایی دارد - نقطه‌ی اوج منحنی سختی.
            entries.add(new Wave.WaveEntry(BOSS_ZOMBIE, 1, spawnDelay));
        }

        float startDelay = waveIndex == 0 ? FIRST_WAVE_START_DELAY : NEXT_WAVE_START_DELAY;
        return new Wave(entries, startDelay);
    }

    private static String pickZombieAlias(int waveIndex, Random random) {
        // موج‌های اول تقریبا فقط معمولی‌اند؛ هرچه جلوتر برویم شانس زامبی نخبه بیشتر می‌شود.
        double eliteChance = Math.min(0.55, 0.08 * waveIndex);
        if (random.nextDouble() < eliteChance) {
            return ELITE_ZOMBIES[random.nextInt(ELITE_ZOMBIES.length)];
        }
        return COMMON_ZOMBIES[random.nextInt(COMMON_ZOMBIES.length)];
    }
}
