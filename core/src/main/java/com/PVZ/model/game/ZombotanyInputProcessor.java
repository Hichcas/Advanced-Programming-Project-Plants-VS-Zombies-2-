package com.PVZ.model.game;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class ZombotanyInputProcessor extends InputAdapter {

    private ZombotanyGameEngine engine;

    public ZombotanyInputProcessor() {
    }

    public void setEngine(ZombotanyGameEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) {
            System.out.println("Camera not set in AppStatus!");
            return false;
        }
        if (engine == null) {
            return false;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        SeedPacketBar seedBar = engine.getSeedPacketBar();
        SeedPacket clickedPacket = seedBar == null ? null : seedBar.getPacketAt(worldX, worldY);
        if (clickedPacket != null) {
            PlantType type = clickedPacket.getPlantType();
            System.out.println("[Zombotany] seed packet clicked: " + type.getDisplayName());
            engine.selectPlant(type);
            return true;
        }

        Map map = engine.getMap();
        if (map == null) {
            return false;
        }
        int row = map.worldToRow(worldY);
        int col = map.worldToCol(worldX);
        if (!map.isWithinBounds(row, col)) {
            return false;
        }
        System.out.println("[Zombotany] clicked tile (col=" + col + ", row=" + row + ")");
        if (engine.getSelectedPlantType() != null) {
            String result = engine.plantSelectedAt(col, row);
            System.out.println("[Zombotany] " + result);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null || engine == null) {
            return false;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        engine.collectSunAtWorldPoint(worldCoords.x, worldCoords.y);
        return false;
    }
}
