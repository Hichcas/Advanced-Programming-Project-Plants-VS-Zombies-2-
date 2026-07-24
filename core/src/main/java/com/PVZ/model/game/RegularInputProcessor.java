package com.PVZ.model.game;

import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;


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

        if (handleSeedPacketClick(worldX, worldY)) {
            return true;
        }

        if (handleGridClick(worldX, worldY)) {
            return true;
        }

        if (handleZombieClick(worldX, worldY)) {
            return true;
        }

        return false;
    }


    private boolean handleSeedPacketClick(float worldX, float worldY) {
        SeedPacketBar seedBar = regularGameEngine.getSeedPacketBar();
        SeedPacket clickedPacket = seedBar == null ? null : seedBar.getPacketAt(worldX, worldY);
        if (clickedPacket != null) {
            PlantType type = clickedPacket.getPlantType();
            System.out.println("Seed packet clicked: " + type.getDisplayName());
            regularGameEngine.selectPlant(type);
            return true;
        }
        return false;
    }

    private boolean handleGridClick(float worldX, float worldY) {
        if (regularGameEngine.getMap() == null) {
            return false;
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = regularGameEngine.getMap().getTile(row, col);
                if (tile != null && tile.contains(worldX, worldY)) {
                    System.out.println("Clicked on Tile (" + col + ", " + row + ")");

                    if (regularGameEngine.getSelectedPlantType() != null) {
                        String result = regularGameEngine.plantSelectedAt(col, row);
                        System.out.println(result);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleZombieClick(float worldX, float worldY) {
        for (Zombie z : regularGameEngine.getAllZombies()) {
            if (z != null && !z.isDead()) {
                float zx = (float) z.getX();
                float zy = (float) z.getY();
                if (worldX >= zx && worldX <= zx + 100 && worldY >= zy && worldY <= zy + 120) {
                    System.out.println("=== Zombie clicked: " + z.getAlias() + " ===");
                    System.out.println(z.getDebugString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null || regularGameEngine == null) {
            return false;
        }
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        regularGameEngine.collectSunAtWorldPoint(worldCoords.x, worldCoords.y);
        regularGameEngine.collectLootAtWorldPoint(worldCoords.x, worldCoords.y);
        return false;
    }
}
