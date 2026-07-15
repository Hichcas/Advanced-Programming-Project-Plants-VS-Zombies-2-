package com.PVZ.model.game;

import com.PVZ.model.minigame.vasebreaker.Vase;
import com.PVZ.model.minigame.vasebreaker.VasebreakerGame;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class VasebreakerInputProcessor extends InputAdapter {

    private VasebreakerGameEngine engine;

    public void setEngine(VasebreakerGameEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null) return false;

        Map map = engine.getMap();
        VasebreakerGame game = engine.getGame();
        if (map == null || game == null) return false;

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

        Vase vase = game.getVase(row, col);
        String result;
        if (vase != null && !vase.isBroken()) {
            result = game.breakVaseAt(row, col);
        } else {
            boolean planted = game.plantSeedAt(row, col);
            result = planted ? "plant is planted mobarak kheilia" : "there is no seeds in this place";
        }
        System.out.println("[Vasebreaker] " + result);
        return true;
    }
}
