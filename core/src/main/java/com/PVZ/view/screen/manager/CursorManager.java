package com.PVZ.view.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CursorManager {
    private static CursorManager instance;

    // Cursor icons are capped to this size (px) so a large HUD button texture doesn't
    // become an oversized hardware cursor. Most OSes also refuse cursors above ~32-64px.
    private static final int MAX_CURSOR_SIZE = 48;

    // Store native OS-managed hardware cursor objects
    private Cursor normalCursor;
    private Cursor pointerCursor;

    // Built lazily from the existing UI spritesheet (skin) the first time each mode is
    // used, then cached and reused for the rest of the session.
    private Cursor shovelCursor;
    private Cursor foodCursor;
    private boolean shovelCursorBuildAttempted = false;
    private boolean foodCursorBuildAttempted = false;

    private CursorManager() {
        // 1. Load the PNG images into temporary Pixmaps
        Pixmap normalPixmap = new Pixmap(Gdx.files.internal("global/cursor/custom_cursor.png"));
        Pixmap pointerPixmap = new Pixmap(Gdx.files.internal("global/cursor/custom_pointer.png"));

        // 2. Create native hardware cursors. (0,0) is the hotspot (the exact click point at top-left)
        normalCursor = Gdx.graphics.newCursor(normalPixmap, 40, 20);
        pointerCursor = Gdx.graphics.newCursor(pointerPixmap, 50, 50);

        // 3. Clean up the pixmaps from RAM immediately to prevent memory leaks
        normalPixmap.dispose();
        pointerPixmap.dispose();

        // 4. Apply the default game cursor right away globally
        setPointerMode(false);
    }

    public static CursorManager getInstance() {
        if (instance == null) {
            instance = new CursorManager();
        }
        return instance;
    }

    /**
     * Switches the global OS hardware cursor between normal arrow and hand pointer.
     * @param isPointer true for button hover hand, false for default arrow
     */
    public void setPointerMode(boolean isPointer) {
        if (isPointer && pointerCursor != null) {
            Gdx.graphics.setCursor(pointerCursor);
        } else if (normalCursor != null) {
            Gdx.graphics.setCursor(normalCursor);
        }
    }

    /**
     * Switches the cursor to the shovel icon (used while shovel/pluck mode is active).
     * Built on demand from the "ingame_shovel" region of the existing UI skin spritesheet
     * so we don't need a dedicated cursor asset. Falls back to the normal arrow if the
     * skin isn't ready yet or the region can't be turned into a cursor for any reason.
     */
    public void setShovelMode() {
        if (!shovelCursorBuildAttempted) {
            shovelCursorBuildAttempted = true;
            shovelCursor = buildCursorFromSkinDrawable("ingame_shovel");
        }
        if (shovelCursor != null) {
            Gdx.graphics.setCursor(shovelCursor);
        } else {
            setPointerMode(false);
        }
    }

    /**
     * Switches the cursor to the plant-food icon (used while plant-food targeting is
     * active, i.e. after the plant-food button was clicked and before a tile is chosen).
     * Built on demand from the "plantfood" region of the existing UI skin spritesheet.
     */
    public void setFoodMode() {
        if (!foodCursorBuildAttempted) {
            foodCursorBuildAttempted = true;
            foodCursor = buildCursorFromSkinDrawable("plantfood");
        }
        if (foodCursor != null) {
            Gdx.graphics.setCursor(foodCursor);
        } else {
            setPointerMode(false);
        }
    }

    /**
     * Crops the icon out of the given ImageButton style's spritesheet region and turns it
     * into a hardware cursor. The style itself (e.g. "ingame_shovel") isn't a Drawable -
     * it's an ImageButtonStyle whose *imageUp* (foreground icon) or, failing that, *up*
     * (background) field is. Returns null (caller falls back to the normal arrow) if the
     * skin, the style, or the underlying pixmap data isn't available.
     */
    private Cursor buildCursorFromSkinDrawable(String styleName) {
        try {
            Skin skin = pvz.skin.PvzSkin.get();
            if (skin == null || !skin.has(styleName, com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle.class)) {
                return null;
            }
            com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle style =
                skin.get(styleName, com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle.class);
            Drawable source = style.imageUp != null ? style.imageUp : style.up;
            if (!(source instanceof TextureRegionDrawable)) {
                return null;
            }
            TextureRegion region = ((TextureRegionDrawable) source).getRegion();
            Texture texture = region.getTexture();
            TextureData data = texture.getTextureData();
            if (!data.isPrepared()) {
                data.prepare();
            }
            Pixmap sheet = data.consumePixmap();

            int rw = region.getRegionWidth();
            int rh = region.getRegionHeight();
            if (rw <= 0 || rh <= 0) {
                if (data.disposePixmap()) sheet.dispose();
                return null;
            }

            Pixmap cropped = new Pixmap(rw, rh, sheet.getFormat());
            cropped.setBlending(Pixmap.Blending.None);
            cropped.drawPixmap(sheet, 0, 0, region.getRegionX(), region.getRegionY(), rw, rh);
            if (data.disposePixmap()) {
                sheet.dispose();
            }

            Pixmap cursorPixmap = cropped;
            if (rw > MAX_CURSOR_SIZE || rh > MAX_CURSOR_SIZE) {
                float scale = Math.min((float) MAX_CURSOR_SIZE / rw, (float) MAX_CURSOR_SIZE / rh);
                int nw = Math.max(1, Math.round(rw * scale));
                int nh = Math.max(1, Math.round(rh * scale));
                Pixmap scaled = new Pixmap(nw, nh, cropped.getFormat());
                scaled.setFilter(Pixmap.Filter.BiLinear);
                scaled.drawPixmap(cropped, 0, 0, rw, rh, 0, 0, nw, nh);
                cropped.dispose();
                cursorPixmap = scaled;
            }

            // Hotspot at the icon's center: neither icon needs a precise "tip" pixel since
            // the click is resolved from the actual mouse position, not the cursor bitmap.
            int hotspotX = cursorPixmap.getWidth() / 2;
            int hotspotY = cursorPixmap.getHeight() / 2;
            Cursor cursor = Gdx.graphics.newCursor(cursorPixmap, hotspotX, hotspotY);
            cursorPixmap.dispose();
            return cursor;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Clears allocated hardware cursor resources from GPU memory on game exit.
     */
    public void dispose() {
        if (normalCursor != null) {
            normalCursor.dispose();
        }
        if (pointerCursor != null) {
            pointerCursor.dispose();
        }
        if (shovelCursor != null) {
            shovelCursor.dispose();
        }
        if (foodCursor != null) {
            foodCursor.dispose();
        }
    }
}
