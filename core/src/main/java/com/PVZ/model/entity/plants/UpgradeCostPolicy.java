package com.PVZ.model.entity.plants;

/**
 * Central source for the seed-packet cost of each upgrade tier.
 *
 * The workbook defines which upgrade is unlocked at levels 2/3/4,
 * but it does not contain numeric seed-packet costs. The gameplay rule
 * requires the cost to increase after every upgrade, so the UI and controller
 * use the same 10 / 20 / 30 progression for levels 2 / 3 / 4.
 */
public final class UpgradeCostPolicy {
    private UpgradeCostPolicy() {
    }

    public static int requiredSeedPacketsForDisplayLevel(int displayLevel) {
        if (displayLevel <= 1) return 0;
        return (displayLevel - 1) * 10;
    }

    public static int currentUpgradeRequirement(int currentDisplayLevel, int maxDisplayLevel) {
        int nextDisplayLevel = currentDisplayLevel + 1;
        if (nextDisplayLevel > maxDisplayLevel) return 0;
        return requiredSeedPacketsForDisplayLevel(nextDisplayLevel);
    }

    public static int seedsPerShopPack() {
        return 10;
    }

    public static int diamondsPerShopPack() {
        return 5;
    }
}
