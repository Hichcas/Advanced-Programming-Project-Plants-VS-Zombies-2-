package com.PVZ.model.game;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHud extends Group {

    private final BitmapFont font;
    private int sunflowerCount = 0;
    private int zombieWavePercent = 0;
    private String beltLine = null;

    private static final float LOCKED_ICON_SIZE = 32f;
    private static final float LOCKED_ICON_GAP = 6f;
    private List<PlantType> lockedPlantsForHud = new ArrayList<>();
    private final Map<PlantType, Texture> lockedIconCache = new HashMap<>();


    public GameHud() {
        this.font = FontManager.getInstance().getEnglishMenuFont();
        setSize(AppStatus.getQuality().width, AppStatus.getQuality().height);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameEngine engine = AppStatus.getGameEngine();
        if (engine == null || engine.gameStatus == null) {
            return;
        }
        sunflowerCount = engine.gameStatus.getSunflower();
        zombieWavePercent = engine.gameStatus.getRemainingZombieWaveInPercent();

        if (engine instanceof RegularGameEngine regularEngine && regularEngine.isConveyorBeltMode()) {
            beltLine = "Belt: " + formatBelt(regularEngine.getConveyorBeltQueue());
        } else {
            beltLine = null;
        }

        if (engine instanceof RegularGameEngine regularEngine && regularEngine.isLockedPlantsMode()) {
            lockedPlantsForHud = new ArrayList<>(regularEngine.getLockedPlantsForStage());
        } else {
            lockedPlantsForHud = List.of();
        }
    }

    private String formatBelt(List<PlantType> queue) {
        if (queue.isEmpty()) {
            return "(empty, waiting...)";
        }
        StringBuilder sb = new StringBuilder();
        for (PlantType type : queue) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.getDisplayName());
        }
        return sb.toString();
    }

    private Texture getLockedIcon(PlantType type) {
        if (lockedIconCache.containsKey(type)) {
            return lockedIconCache.get(type);
        }
        Texture texture = null;
        try {
            String path = PlantTexturePaths.getPath(type.name());
            if (Gdx.files.internal(path).exists()) {
                texture = new Texture(Gdx.files.internal(path));
            }
        } catch (RuntimeException ex) {
            texture = null;
        }
        lockedIconCache.put(type, texture);
        return texture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        font.setColor(Color.GOLD);
        font.draw(batch, "Sunflowers: " + sunflowerCount, 20, AppStatus.getQuality().height - 50);
        font.setColor(Color.RED);
        font.draw(batch, "Zombie Wave: " + zombieWavePercent + "%", 20, AppStatus.getQuality().height - 100);

        if (beltLine != null) {
            font.setColor(Color.CYAN);
            font.draw(batch, beltLine, 20, AppStatus.getQuality().height - 150);
        }

        if (!lockedPlantsForHud.isEmpty()) {
            font.setColor(Color.ORANGE);
            float labelY = AppStatus.getQuality().height - 150;
            font.draw(batch, "Locked:", 20, labelY);

            float iconX = 20;
            float iconY = labelY - LOCKED_ICON_SIZE - 6;
            for (PlantType type : lockedPlantsForHud) {
                Texture icon = getLockedIcon(type);
                if (icon != null) {
                    batch.setColor(1f, 1f, 1f, 0.85f);
                    batch.draw(icon, iconX, iconY, LOCKED_ICON_SIZE, LOCKED_ICON_SIZE);
                    batch.setColor(1f, 1f, 1f, 1f);
                }
                iconX += LOCKED_ICON_SIZE + LOCKED_ICON_GAP;
            }
        }
        font.setColor(Color.WHITE);
    }
}
