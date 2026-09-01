package com.PVZ.view.screen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.view.renderer.EntityRenderer;

/**
 * A single plant "card": background frame, live plant idle animation, cost text, a lock
 * overlay when the plant isn't selectable, and a highlighted border when selected.
 *
 * Reusable anywhere a plant needs to be shown as a pickable tile — the pre-game plant
 * selection panel, the collection screen, and the in-game selected-plants bar can all share
 * this one Actor instead of re-implementing plant-card rendering three times.
 */
public class PlantCardActor extends Actor {
    private static final String LOCK_ICON_PAM = "768/INITIAL/UI/CHOOSER/SLOT_LOCK_SMALL/SLOT_LOCK_SMALL.PAM";

    private static Texture frameTexture;
    private static Texture selectedFrameTexture;
    private static Texture lockOverlayTexture;

    private final PlantType type;
    private final BitmapFont font;
    private boolean locked;
    private boolean selected;
    private Runnable onClick;
    private float animTime = (float) (Math.random() * 2.0);

    public PlantCardActor(PlantType type, BitmapFont font) {
        this.type = type;
        this.font = font;
        setSize(90f, 110f);
        ensureFallbackTextures();
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (locked) {
                    return;
                }
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public PlantType getPlantType() {
        return type;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        animTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(selected ? selectedFrameTexture : frameTexture, x, y, w, h);

        // Plant art: the same idle PAM animation used on the lawn, centered in the card.
        float plantCx = x + w / 2f;
        float plantCy = y + h * 0.6f;
        EntityRenderer.getInstance().renderPlant((SpriteBatch) batch, type.name(), animTime, plantCx, plantCy);

        if (font != null) {
            int cost = com.PVZ.model.entity.plants.PlantLibrary.getEffectiveCost(type);
            font.setColor(1f, 1f, 1f, parentAlpha);
            font.draw(batch, String.valueOf(cost), x + 4f, y + 16f);
        }

        if (locked) {
            batch.setColor(0f, 0f, 0f, 0.55f * parentAlpha);
            batch.draw(frameTexture, x, y, w, h);
            batch.setColor(1f, 1f, 1f, parentAlpha);
            boolean drewLock = EntityRenderer.getInstance().renderPam((SpriteBatch) batch,
                LOCK_ICON_PAM, animTime, plantCx, y + h * 0.5f);
            if (!drewLock && lockOverlayTexture != null) {
                float lw = 28f;
                float lh = 28f;
                batch.draw(lockOverlayTexture, plantCx - lw / 2f, y + h * 0.5f - lh / 2f, lw, lh);
            }
        }

        batch.setColor(Color.WHITE);
    }

    private static void ensureFallbackTextures() {
        if (frameTexture != null) {
            return;
        }
        frameTexture = solidTexture(0.35f, 0.28f, 0.15f, 0.9f);
        selectedFrameTexture = solidTexture(0.55f, 0.85f, 0.25f, 1f);
        Pixmap lock = new Pixmap(28, 28, Pixmap.Format.RGBA8888);
        lock.setColor(1f, 1f, 1f, 0.9f);
        lock.fillRectangle(6, 0, 16, 14);
        lock.fillCircle(14, 16, 9);
        lock.setColor(0f, 0f, 0f, 1f);
        lock.fillCircle(14, 16, 5);
        lockOverlayTexture = new Texture(lock);
        lock.dispose();
    }

    private static Texture solidTexture(float r, float g, float b, float a) {
        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(r, g, b, a);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }
}
