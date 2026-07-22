package com.PVZ.model.game;

import com.PVZ.model.minigame.beghouled.BeghouledUpgrade;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Input processor for the Beghouled minigame handling touch/click events.
 * Refactored to comply with Checkstyle (method length ≤ 50 lines).
 */
public class BeghouledInputProcessor extends InputAdapter {

    private BeghouledGameEngine engine;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public void setEngine(BeghouledGameEngine engine) {
        this.engine = engine;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null || engine.getGame() == null) {
            return false;
        }

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) {
            return false;
        }

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

        // Check for upgrade button click
        if (handleUpgradeClick(world.x, world.y)) {
            return true;
        }

        // Check map bounds and get row/col
        if (engine.getMap() == null) {
            return false;
        }

        Map map = engine.getMap();
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) {
            return false;
        }

        boolean clickedPlant = engine.getGame().isPlant(row, col);

        // No selection yet
        if (selectedRow < 0 || selectedCol < 0) {
            handleNoSelection(row, col, clickedPlant);
            return true;
        }

        // Click on same tile -> deselect
        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            return true;
        }

        // Attempt swap if adjacent and both have plants
        if (isAdjacent(selectedRow, selectedCol, row, col) && clickedPlant) {
            String result = engine.trySwap(selectedRow, selectedCol, row, col);
            System.out.println("[Beghouled] " + result);
            clearSelection();
            return true;
        }

        // Click on another plant -> move selection, or clear if empty
        if (clickedPlant) {
            selectedRow = row;
            selectedCol = col;
            System.out.println("[Beghouled] selected (" + row + ", " + col + ")");
        } else {
            clearSelection();
        }
        return true;
    }

    // ---------- Helper methods ----------

    private boolean handleUpgradeClick(float worldX, float worldY) {
        BeghouledUpgrade upgrade = engine.getUpgradeAt(worldX, worldY);
        if (upgrade != null) {
            String result = engine.applyUpgrade(upgrade);
            System.out.println("[Beghouled] " + result);
            return true;
        }
        return false;
    }

    private void handleNoSelection(int row, int col, boolean clickedPlant) {
        if (clickedPlant) {
            selectedRow = row;
            selectedCol = col;
            System.out.println("[Beghouled] selected (" + row + ", " + col + ")");
        }
    }

    private boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }
}
