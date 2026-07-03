package com.PVZ.model.game;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.PVZ.model.entity.Tile;
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
        // دریافت دوربین از کلاس AppStatus
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) {
            System.out.println("Camera not set in AppStatus!");
            return false;
        }

        // تبدیل مختصات صفحه به فضای دنیای مجازی
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        // بررسی تمام خانه‌های شبکه ۵×۹
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                if (regularGameEngine == null || regularGameEngine.getMap() == null) continue;
                Tile tile = regularGameEngine.getMap().getTile(row, col);
                if (tile != null && tile.contains(worldX, worldY)) {
                    System.out.println("Clicked on Tile (" + row + ", " + col + ")");
                    // اگر خواستی کلیک مصرف شود، return true;
                    // فعلاً false می‌گذاریم تا Stage هم کلیک را ببیند
                    return false;
                }
            }
        }
        // کلیک روی هیچ خانه‌ای نبود
        return false;
    }
}
