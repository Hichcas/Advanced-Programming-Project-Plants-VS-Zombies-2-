package com.PVZ.model.game.survival;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * موتور امتیازدهیِ بازی امتیازی («میوپوینت»)، پیاده‌سازی‌کننده‌ی ۵
 * الگوی {@link MyoPointPattern}. یک نمونه از این کلاس روی هر
 * {@code RegularGameEngine} که در حالت {@code survivalScoreMode} است
 * زندگی می‌کند.
 *
 * نحوه‌ی اتصال:
 * <ul>
 *   <li>{@link SurvivalHandler} هر بار زامبی جدیدی spawn می‌کند،
 *       {@link #registerSpawn} را صدا می‌زند تا لحظه‌ی تولد ثبت شود
 *       (برای محاسبه‌ی Quick Kill لازم است).</li>
 *   <li>{@code BattleController.notifyZombieKilled} هر بار زامبی‌ای
 *       واقعا کشته می‌شود (دقیقا یک‌بار، همان محل که quest هم از آن
 *       استفاده می‌کند)، {@link #recordKill} را صدا می‌زند.</li>
 * </ul>
 */
public class MyoPointScorer {

    private static final double QUICK_KILL_WINDOW_SECONDS = 3.0;
    private static final double BURST_WINDOW_SECONDS = 0.15;
    private static final double COMBO_WINDOW_SECONDS = 2.5;
    private static final int COMBO_MAX_STACKS = 10;
    private static final int CLUTCH_COLUMN_THRESHOLD = 1; // ستون ۰ یا ۱ یعنی خیلی نزدیک خانه

    private static final String CAT_PAM_PATH = "768/DEV/UI/MYOPOINT/MYOPOINT_CAT/MYOPOINT_CAT.PAM";
    // TODO(art): وقتی اسکین/انیمیشن گربه (میو) آماده شد، یک entry متناظر با این path
    // و یک clip به نام "meow" به resources/data/pam_animations.json اضافه کنید.
    // تا اون موقع renderPam این مسیر را پیدا نمی‌کند و بی‌صدا نادیده گرفته می‌شود (کرش نمی‌کند)،
    // ولی متن اعلان (AppStatus.showAnnouncement) همین الان هم کار می‌کند.
    private static final String CAT_PAM_CLIP = "meow";

    private record KillRecord(double time, int row, int col, float x, float y) {
    }

    private final Map<Zombie, Double> spawnTimestamps = new IdentityHashMap<>();
    private final Deque<KillRecord> recentKills = new ArrayDeque<>();

    private int totalScore = 0;
    private int comboCount = 0;
    private double lastKillTime = Double.NEGATIVE_INFINITY;

    public void registerSpawn(Zombie zombie, double survivalTimeSeconds) {
        spawnTimestamps.put(zombie, survivalTimeSeconds);
    }

    /**
     * باید دقیقا یک‌بار به ازای هر زامبیِ واقعا کشته‌شده صدا زده شود.
     *
     * @param engine              موتور بازی (برای addTimedPamEffect لازم است).
     * @param zombie              زامبی کشته‌شده.
     * @param row                 سطر (لاین) محل مرگ.
     * @param col                 ستون محل مرگ (۰ = نزدیک‌ترین به خانه).
     * @param worldX              مختصات X صفحه برای نمایش انیمیشن/اعلان.
     * @param worldY              مختصات Y صفحه برای نمایش انیمیشن/اعلان.
     * @param survivalTimeSeconds زمان سپری‌شده از شروع بازی امتیازی (ثانیه).
     */
    public void recordKill(RegularGameEngine engine, Zombie zombie, int row, int col,
                            float worldX, float worldY, double survivalTimeSeconds) {

        pruneOldKills(survivalTimeSeconds);

        int gained = 10; // امتیاز پایه‌ی هر کشتن
        StringBuilder breakdown = new StringBuilder("+10 Kill");

        // ----- Quick Kill -----
        Double spawnTime = spawnTimestamps.remove(zombie);
        if (spawnTime != null && (survivalTimeSeconds - spawnTime) <= QUICK_KILL_WINDOW_SECONDS) {
            gained += MyoPointPattern.QUICK_KILL.getBasePoints();
            breakdown.append(", +").append(MyoPointPattern.QUICK_KILL.getBasePoints())
                    .append(' ').append(MyoPointPattern.QUICK_KILL.getDisplayName());
        }

        // ----- Single-Shot Multi-Kill (همان سطر) در برابر Simultaneous Kill (سطر متفاوت) -----
        boolean sameRowBurst = false;
        boolean crossRowBurst = false;
        for (KillRecord prev : recentKills) {
            if (survivalTimeSeconds - prev.time() > BURST_WINDOW_SECONDS) continue;
            if (prev.row() == row) {
                sameRowBurst = true;
            } else {
                crossRowBurst = true;
            }
        }
        if (sameRowBurst) {
            gained += MyoPointPattern.SINGLE_SHOT_MULTI_KILL.getBasePoints();
            breakdown.append(", +").append(MyoPointPattern.SINGLE_SHOT_MULTI_KILL.getBasePoints())
                    .append(' ').append(MyoPointPattern.SINGLE_SHOT_MULTI_KILL.getDisplayName());
        } else if (crossRowBurst) {
            gained += MyoPointPattern.SIMULTANEOUS_KILL.getBasePoints();
            breakdown.append(", +").append(MyoPointPattern.SIMULTANEOUS_KILL.getBasePoints())
                    .append(' ').append(MyoPointPattern.SIMULTANEOUS_KILL.getDisplayName());
        }

        // ----- Combo Streak -----
        if (survivalTimeSeconds - lastKillTime <= COMBO_WINDOW_SECONDS) {
            comboCount = Math.min(comboCount + 1, COMBO_MAX_STACKS + 1);
        } else {
            comboCount = 1;
        }
        lastKillTime = survivalTimeSeconds;
        if (comboCount >= 2) {
            int comboBonus = (comboCount - 1) * MyoPointPattern.COMBO_STREAK.getBasePoints();
            gained += comboBonus;
            breakdown.append(", +").append(comboBonus)
                    .append(' ').append(MyoPointPattern.COMBO_STREAK.getDisplayName())
                    .append(" x").append(comboCount - 1);
        }

        // ----- Clutch Kill -----
        if (col <= CLUTCH_COLUMN_THRESHOLD) {
            gained += MyoPointPattern.CLUTCH_KILL.getBasePoints();
            breakdown.append(", +").append(MyoPointPattern.CLUTCH_KILL.getBasePoints())
                    .append(' ').append(MyoPointPattern.CLUTCH_KILL.getDisplayName());
        }

        totalScore += gained;
        recentKills.addLast(new KillRecord(survivalTimeSeconds, row, col, worldX, worldY));

        notifyPlayer(engine, gained, breakdown.toString(), worldX, worldY);
    }

    private void pruneOldKills(double now) {
        // بازه‌ی نگه‌داری کوتاه است (فقط برای تشخیص burst لازم است)، پس صف را کوچک نگه می‌داریم.
        while (!recentKills.isEmpty() && now - recentKills.peekFirst().time() > COMBO_WINDOW_SECONDS) {
            recentKills.pollFirst();
        }
    }

    private void notifyPlayer(RegularGameEngine engine, int gained, String breakdown, float x, float y) {
        // ۱) اعلان متنی فوری - همین الان با هر امکانات فعلی کار می‌کند.
        AppStatus.showAnnouncement("MyoPoint! " + breakdown + "  (Total: +" + gained + ")");
        // ۲) انیمیشن گرافیکی خاص گربه («میو») - از همان مکانیزم TimedPamEffect که برای
        //    افکت‌های موقتی روی زمین (مثل یخ‌زدگی) استفاده می‌شود. اگر asset گربه هنوز
        //    اضافه نشده باشد، renderPam بی‌خطر شکست می‌خورد و فقط لاگ می‌کند - بازی کرش نمی‌کند.
        if (engine != null) {
            engine.addTimedPamEffect(CAT_PAM_PATH, CAT_PAM_CLIP, 1.2, 1.0f, x, y);
        }
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getComboCount() {
        return Math.max(0, comboCount - 1);
    }
}
