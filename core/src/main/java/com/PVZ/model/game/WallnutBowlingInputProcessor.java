package com.PVZ.model.game;

import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class WallnutBowlingInputProcessor extends InputAdapter {

    private WallnutBowlingGameEngine engine;

    public void setEngine(WallnutBowlingGameEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null) return false;

        Map map = engine.getMap();
        if (map == null || engine.getGame() == null) return false;

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

        boolean onCooldown = engine.getGame().getCooldownRemaining() > 0.0;
        boolean launched = !onCooldown && engine.launchHeldNut(row, col);
        if (launched) {
            engine.getGame().drawNextNut();
        }
        String reason = launched ? "nut launched at row " + row + " col " + col
                : onCooldown ? "still reloading, wait a bit"
                : "can't launch there (must be left of the red line)";
        System.out.println("[WallnutBowling] " + reason);
        return true;
    }
}
