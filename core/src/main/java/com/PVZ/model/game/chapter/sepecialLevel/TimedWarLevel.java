package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

/**
 * "Timed War" brain buster: a timer runs at the top of the screen. The player
 * must reach the configured goal(s) before the timer expires:
 * <ul>
 *   <li>kill {@code timedWarZombieKills} zombies (e.g. 12 zombies in Q seconds), and/or</li>
 *   <li>produce (collect) {@code timedWarSunTarget} sun.</li>
 * </ul>
 * Reaching the goal(s) in time wins the level immediately; if the timer runs
 * out first, the level is lost. Progress (remaining time / remaining kills or
 * sun) is shown live via the HUD in GameScreen.
 */
public class TimedWarLevel implements SpecialLevel {

    private static final double TICK_SECONDS = 0.1;
    private static final double DEFAULT_SECONDS = 120;
    private static final int DEFAULT_KILL_GOAL = 12;

    private double timeLimitSeconds = DEFAULT_SECONDS;
    private int targetKills = DEFAULT_KILL_GOAL;
    private int targetSun = 0;
    private long tickCount = 0;
    private boolean won = false;
    private boolean lost = false;

    @Override
    public void onGameStart(RegularGameEngine engine, Map map, StageConfig stage) {
        won = false;
        lost = false;
        tickCount = 0;
        timeLimitSeconds = stage.getTimedWarSeconds() > 0 ? stage.getTimedWarSeconds() : DEFAULT_SECONDS;
        targetKills = stage.getTimedWarZombieKills();
        targetSun = stage.getTimedWarSunTarget();
        if (targetKills <= 0 && targetSun <= 0) {
            targetKills = DEFAULT_KILL_GOAL;
        }
        StringBuilder goal = new StringBuilder("[TimedWar] Level started — in "
            + formatSeconds(timeLimitSeconds) + ": ");
        if (targetKills > 0) {
            goal.append("kill ").append(targetKills).append(" zombies");
        }
        if (targetKills > 0 && targetSun > 0) {
            goal.append(" AND ");
        }
        if (targetSun > 0) {
            goal.append("produce ").append(targetSun).append(" sun");
        }
        System.out.println(goal.append("!"));
    }

    @Override
    public void onTick(RegularGameEngine engine, Map map) {
        if (won || lost) {
            return;
        }
        tickCount++;
        if (goalsReached(engine)) {
            won = true;
            System.out.println("[TimedWar] Goal reached with "
                + formatSeconds(getRemainingSeconds()) + " left — you win!");
            return;
        }
        if (getElapsedSeconds() >= timeLimitSeconds) {
            lost = true;
            System.out.println("[TimedWar] Time ran out — you lose! (kills: "
                + engine.totalZombieKills + "/" + targetKills + ", sun: "
                + engine.getSunManager().getTotalCollectedSun() + "/" + targetSun + ")");
        }
    }

    @Override
    public void onPlantDestroyed(int row, int col, RegularGameEngine engine) {
    }

    @Override
    public boolean disableFallingSun() {
        return false;
    }

    @Override
    public boolean isLossConditionMet() {
        return lost;
    }

    @Override
    public boolean isWinConditionMet() {
        return won;
    }

    @Override
    public String getName() {
        return "TIMED_WAR";
    }

    private boolean goalsReached(RegularGameEngine engine) {
        if (targetKills <= 0 && targetSun <= 0) {
            return false;
        }
        if (targetKills > 0 && engine.totalZombieKills < targetKills) {
            return false;
        }
        return targetSun <= 0 || engine.getSunManager().getTotalCollectedSun() >= targetSun;
    }

    // ---------- live status for the HUD ----------

    public double getElapsedSeconds() {
        return tickCount * TICK_SECONDS;
    }

    public double getRemainingSeconds() {
        return Math.max(0, timeLimitSeconds - getElapsedSeconds());
    }

    public double getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public boolean hasKillGoal() {
        return targetKills > 0;
    }

    public boolean hasSunGoal() {
        return targetSun > 0;
    }

    public int getKillsRemaining(RegularGameEngine engine) {
        return Math.max(0, targetKills - engine.totalZombieKills);
    }

    public int getSunRemaining(RegularGameEngine engine) {
        return Math.max(0, targetSun - engine.getSunManager().getTotalCollectedSun());
    }

    public int getTargetKills() {
        return targetKills;
    }

    public int getTargetSun() {
        return targetSun;
    }

    private static String formatSeconds(double seconds) {
        return seconds == Math.floor(seconds)
            ? String.valueOf((long) seconds) + "s"
            : String.format("%.1fs", seconds);
    }
}
