package com.PVZ.model.minigame.vasebreaker;

public class Vase {

    private final int row;
    private final int col;
    private final VaseType type;
    private boolean broken;
    private VaseOutcome outcome;

    public Vase(int row, int col, VaseType type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.broken = false;
        this.outcome = null;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public VaseType getType() { return type; }
    public boolean isBroken() { return broken; }
    public VaseOutcome getOutcome() { return outcome; }

    public VaseOutcome breakVase(VaseOutcome resolvedOutcome) {
        if (broken) return VaseOutcome.EMPTY;
        broken = true;
        outcome = resolvedOutcome;
        return outcome;
    }

    public String texturePath() {
        if (broken) return VasebreakerTexturePaths.BROKEN;
        return switch (type) {
            case NORMAL -> VasebreakerTexturePaths.NORMAL;
            case PLANT -> VasebreakerTexturePaths.PLANT;
            case GARGANTUAR -> VasebreakerTexturePaths.GARGANTUAR;
        };
    }
}
