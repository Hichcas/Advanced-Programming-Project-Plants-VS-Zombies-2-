package com.PVZ.model.game;

import com.PVZ.model.minigame.beghouled.BeghouledUpgrade;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;


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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!validateEngineAndCamera()) {
            return false;
        }

        Vector3 world = unproject(screenX, screenY);
        if (world == null) {
            return false;
        }

        if (handleUpgradeClick(world.x, world.y)) {
            return true;
        }

        TilePos pos = getTilePosition(world.x, world.y);
        if (pos == null) {
            return false;
        }

        boolean clickedPlant = engine.getGame().isPlant(pos.row, pos.col);
        return handleTileClick(pos.row, pos.col, clickedPlant);
    }

    private boolean validateEngineAndCamera() {
        if (engine == null || engine.getGame() == null) {
            return false;
        }
        return AppStatus.getCamera() != null;
    }

    private Vector3 unproject(int screenX, int screenY) {
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) {
            return null;
        }
        return camera.unproject(new Vector3(screenX, screenY, 0));
    }

    private TilePos getTilePosition(float worldX, float worldY) {
        if (engine.getMap() == null) {
            return null;
        }
        Map map = engine.getMap();
        int row = map.worldToRow(worldY);
        int col = map.worldToCol(worldX);
        if (!map.isWithinBounds(row, col)) {
            return null;
        }
        return new TilePos(row, col);
    }

    private boolean handleTileClick(int row, int col, boolean clickedPlant) {
        if (selectedRow < 0 || selectedCol < 0) {
            if (clickedPlant) {
                selectPlant(row, col);
            }
            return true;
        }

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            return true;
        }

        if (isAdjacent(selectedRow, selectedCol, row, col) && clickedPlant) {
            String result = engine.trySwap(selectedRow, selectedCol, row, col);
            System.out.println("[Beghouled] " + result);
            clearSelection();
            return true;
        }

        if (clickedPlant) {
            selectPlant(row, col);
        } else {
            clearSelection();
        }
        return true;
    }

    private void selectPlant(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        System.out.println("[Beghouled] selected (" + row + ", " + col + ")");
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    private boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }

    private boolean handleUpgradeClick(float worldX, float worldY) {
        BeghouledUpgrade upgrade = engine.getUpgradeAt(worldX, worldY);
        if (upgrade != null) {
            String result = engine.applyUpgrade(upgrade);
            System.out.println("[Beghouled] " + result);
            return true;
        }
        return false;
    }

    private static class TilePos {
        final int row;
        final int col;

        TilePos(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
