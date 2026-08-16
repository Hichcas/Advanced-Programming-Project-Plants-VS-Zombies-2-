package com.PVZ.model.game.chapter.sepecialLevel;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.chapter.StageConfig;

import java.util.Random;

/**
 * "Dead Line" brain buster: every lane has its own vertical red line, placed at
 * a random column of that lane (around the anchor column configured in the
 * stage JSON). The level is lost the moment any zombie crosses its lane's
 * line. Winning works like a normal level: survive and clear all waves.
 */
public class DeadLineLevel implements SpecialLevel {

    private static final int DEFAULT_ANCHOR_COL = 5;
    private static final int SPREAD = 2;
    private static final int MIN_COL = 2;

    private final Random random = new Random();
    private int anchorCol = DEFAULT_ANCHOR_COL;
    private int[] deadlineCols = new int[0];
    private boolean crossed = false;

    @Override
    public void onGameStart(RegularGameEngine engine, Map map, StageConfig stage) {
        crossed = false;
        deadlineCols = new int[0];
        anchorCol = stage.getDeadlineCol() > 0 ? stage.getDeadlineCol() : DEFAULT_ANCHOR_COL;
        if (map != null) {
            assignRandomColumns(map.getRows(), map.getCols());
        }
        System.out.println("[DeadLine] Level started — every lane has its own red line at a random column!");
    }

    @Override
    public void onTick(RegularGameEngine engine, Map map) {
        if (crossed) {
            return;
        }
        ensureColumns(map);
        if (deadlineCols.length == 0 || map == null) {
            return;
        }
        for (Zombie zombie : engine.getZombieList()) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }
            int row = (int) zombie.getRow();
            if (row < 0 || row >= deadlineCols.length) {
                continue;
            }
            if (zombie.getX() <= getLineXForRow(map, row)) {
                crossed = true;
                System.out.println("[DeadLine] A zombie crossed the line of lane " + row
                    + " (x=" + zombie.getX() + ", line at column " + deadlineCols[row] + ") — you lose!");
                return;
            }
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
        return crossed;
    }

    @Override
    public String getName() {
        return "DEAD_LINE";
    }

    private void assignRandomColumns(int rows, int cols) {
        int min = Math.max(MIN_COL, anchorCol - SPREAD);
        int max = Math.min(cols - 2, anchorCol + SPREAD);
        deadlineCols = new int[rows];
        StringBuilder sb = new StringBuilder("[DeadLine] Lane lines -> ");
        for (int row = 0; row < rows; row++) {
            deadlineCols[row] = min + random.nextInt(max - min + 1);
            sb.append("lane ").append(row).append(": col ").append(deadlineCols[row]);
            if (row < rows - 1) sb.append(", ");
        }
        System.out.println(sb);
    }

    /**
     * Pixel X of a lane's deadline: the left edge of that lane's random column
     * (zombies may walk anywhere to the right of it but not past it).
     */
    public double getLineXForRow(Map map, int row) {
        ensureColumns(map);
        int col = row >= 0 && row < deadlineCols.length ? deadlineCols[row] : anchorCol;
        return map.getStartX() + col * map.getTileWidth();
    }

    public int getDeadlineColForRow(int row) {
        return row >= 0 && row < deadlineCols.length ? deadlineCols[row] : anchorCol;
    }

    private void ensureColumns(Map map) {
        if (deadlineCols.length == 0 && map != null) {
            assignRandomColumns(map.getRows(), map.getCols());
        }
    }
}
