package com.PVZ.model.game;

import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class IZombieInputProcessor extends InputAdapter {

    private IZombieGameEngine engine;
    private String selectedAlias;

    public void setEngine(IZombieGameEngine engine) {
        this.engine = engine;
    }

    public void selectZombie(String alias) {
        this.selectedAlias = (selectedAlias != null && selectedAlias.equalsIgnoreCase(alias)) ? null : alias;
    }

    public String getSelectedAlias() { return selectedAlias; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null || engine.getGame() == null) return false;

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

        var packet = engine.getZombiePacketBar().getPacketAt(world.x, world.y);
        if (packet != null) {
            selectZombie(packet.getOption().getAlias());
            System.out.println("[IZombie] selected " + packet.getOption().getDisplayName());
            return true;
        }

        if (engine.getMap() == null || selectedAlias == null) return false;

        Map map = engine.getMap();
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

        String result = engine.deployZombie(selectedAlias, row, col);
        System.out.println("[IZombie] " + result);
        return true;
    }

    // Sun is collected by sweeping the mouse over it (matches the classic PvZ feel
    // more closely than click-to-collect), not by clicking - mouseMoved fires
    // continuously while the button is up, and touchDragged covers the touchscreen/
    // button-held-down case, so both routes call the same collection point.
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        tryCollectSunAt(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        tryCollectSunAt(screenX, screenY);
        return false;
    }

    private void tryCollectSunAt(int screenX, int screenY) {
        if (engine == null || engine.getGame() == null) return;
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return;
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int collected = engine.collectSunAtWorldPoint(world.x, world.y);
        if (collected > 0) {
            System.out.println("[IZombie] collected " + collected + " sun");
        }
    }
}
