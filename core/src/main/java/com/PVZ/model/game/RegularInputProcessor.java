package com.PVZ.model.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;

public class RegularInputProcessor extends InputAdapter {
    private RegularGameEngine regularGameEngine;

    public RegularInputProcessor() {
    }

    public void setRegularGameEngine(RegularGameEngine regularGameEngine) {
        this.regularGameEngine = regularGameEngine;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) {
            System.out.println("Camera not set in AppStatus!");
            return false;
        }
        if (regularGameEngine == null) {
            return false;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        // 1) a click on the seed-packet bar selects (or deselects) that plant type.
        SeedPacketBar seedBar = regularGameEngine.getSeedPacketBar();
        SeedPacket clickedPacket = seedBar == null ? null : seedBar.getPacketAt(worldX, worldY);
        if (clickedPacket != null) {
            PlantType type = clickedPacket.getPlantType();
            System.out.println("Seed packet clicked: " + type.getDisplayName());
            regularGameEngine.selectPlant(type);
            return true;
        }

        // 2) otherwise, a click on the grid either plants the currently selected seed
        //    or (if nothing is selected) just reports which tile was clicked, as before.
        if (regularGameEngine.getMap() == null) {
            return false;
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = regularGameEngine.getMap().getTile(row, col);
                if (tile != null && tile.contains(worldX, worldY)) {
                    System.out.println("Clicked on Tile (" + col + ", " + row + ")");

                    if (regularGameEngine.getSelectedPlantType() != null) {
                        // plantPlant/plantSelectedAt use the design doc's 1-based (x, y)
                        // convention, so we convert from the 0-based grid indices here.
                        String result = regularGameEngine.plantSelectedAt(col , row );
                        System.out.println(result);
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
