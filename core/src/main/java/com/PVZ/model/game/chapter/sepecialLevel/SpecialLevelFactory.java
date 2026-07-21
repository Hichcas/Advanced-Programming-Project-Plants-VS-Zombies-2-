package com.PVZ.model.game.chapter.sepecialLevel;

public final class SpecialLevelFactory {

    private SpecialLevelFactory() {
    }

    public static SpecialLevel create(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        switch (type.trim().toUpperCase()) {
            case "NIGHT_OPS":
                return new NightOpsLevel();
            case "LOVE_YOUR_PLANTS":
                return new LoveYourPlantsLevel();
            default:
                return null;
        }
    }
}
