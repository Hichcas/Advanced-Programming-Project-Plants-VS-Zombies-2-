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

    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null || engine.getGame() == null) return false;

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

        BeghouledUpgrade upgrade = engine.getUpgradeAt(world.x, world.y);
        if (upgrade != null) {
            String result = engine.applyUpgrade(upgrade);
            System.out.println("[Beghouled] " + result);
            return true;
        }

        if (engine.getMap() == null) return false;

        Map map = engine.getMap();
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

        boolean clickedPlant = engine.getGame().isPlant(row, col);

        if (selectedRow < 0 || selectedCol < 0) {

            if (clickedPlant) {
                selectedRow = row;
                selectedCol = col;
                System.out.println("[Beghouled] selected (" + row + ", " + col + ")");
            }
            return true;
        }

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            return true;
        }

        boolean adjacent = Math.abs(selectedRow - row) + Math.abs(selectedCol - col) == 1;
        if (adjacent && clickedPlant) {
            String result = engine.trySwap(selectedRow, selectedCol, row, col);
            System.out.println("[Beghouled] " + result);
            clearSelection();
            return true;
        }

        if (clickedPlant) {
            selectedRow = row;
            selectedCol = col;
            System.out.println("[Beghouled] selected (" + row + ", " + col + ")");
        } else {
            clearSelection();
        }
        return true;
    }
}
