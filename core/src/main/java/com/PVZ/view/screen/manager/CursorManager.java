package com.PVZ.view.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ScreenUtils;

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
     * Built on demand from the "ingame_shovel" button style of the existing UI skin so we
     * don't need a dedicated cursor asset. Falls back to the normal arrow if the skin
     * isn't ready yet or the icon can't be turned into a cursor for any reason.
     */
    public void setShovelMode() {
        if (!shovelCursorBuildAttempted) {
            shovelCursorBuildAttempted = true;
            shovelCursor = buildCursorFromButtonStyle("ingame_shovel");
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
     * Built on demand from the "plantfood" button style of the existing UI skin.
     */
    public void setFoodMode() {
        if (!foodCursorBuildAttempted) {
            foodCursorBuildAttempted = true;
            foodCursor = buildCursorFromButtonStyle("plantfood");
        }
        if (foodCursor != null) {
            Gdx.graphics.setCursor(foodCursor);
        } else {
            setPointerMode(false);
        }
    }

    /**
     * Turns the icon of the given ImageButton style (e.g. "ingame_shovel") into a hardware
     * cursor. The style name isn't a Drawable itself - it's an ImageButtonStyle whose
     * *imageUp* (foreground icon) or, failing that, *up* (background) field is the actual
     * drawable. Rather than trying to read the packed atlas texture's pixels directly
     * (which fails silently for compressed/managed texture data - this was the bug: the
     * previous attempt used TextureData.consumePixmap() on the atlas page and always threw,
     * so it silently fell back to the arrow cursor every time), this renders the drawable
     * into an offscreen FrameBuffer and reads that back as a Pixmap. That path works for
     * literally any Drawable regardless of how its texture was loaded/compressed, because
     * it goes through the normal GPU draw call instead of touching the texture's raw data.
     */
    private Cursor buildCursorFromButtonStyle(String styleName) {
        try {
            Skin skin = pvz.skin.PvzSkin.get();
            if (skin == null || !skin.has(styleName, ImageButton.ImageButtonStyle.class)) {
                return null;
            }
            ImageButton.ImageButtonStyle style = skin.get(styleName, ImageButton.ImageButtonStyle.class);
            Drawable source = style.imageUp != null ? style.imageUp : style.up;
            if (source == null) {
                return null;
            }

            float rawW = source.getMinWidth() > 0 ? source.getMinWidth() : 64f;
            float rawH = source.getMinHeight() > 0 ? source.getMinHeight() : 64f;
            float scale = Math.min(1f, MAX_CURSOR_SIZE / Math.max(rawW, rawH));
            int cw = Math.max(1, Math.round(rawW * scale));
            int ch = Math.max(1, Math.round(rawH * scale));

            // LWJGL3 hardware cursors require the backing pixmap's width/height to each be
            // a power-of-two (this was the actual crash: e.g. 45px isn't), so render into a
            // POT-sized canvas and center the (non-POT) icon inside it with transparent padding.
            int potW = nextPowerOfTwo(cw);
            int potH = nextPowerOfTwo(ch);
            int offsetX = (potW - cw) / 2;
            int offsetY = (potH - ch) / 2;

            FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, potW, potH, false);
            SpriteBatch batch = new SpriteBatch();
            Matrix4 projection = new Matrix4().setToOrtho2D(0, 0, potW, potH);

            fbo.begin();
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.setProjectionMatrix(projection);
            batch.begin();
            source.draw(batch, offsetX, offsetY, cw, ch);
            batch.end();
            Pixmap rendered = ScreenUtils.getFrameBufferPixmap(0, 0, potW, potH);
            fbo.end();

            batch.dispose();
            fbo.dispose();

            // FrameBuffer reads back bottom-to-top, so the pixmap is upside down; flip it.
            Pixmap upright = flipVertically(rendered);
            rendered.dispose();

            // Hotspot stays at the center of the actual icon content, not the padded canvas.
            int hotspotX = offsetX + cw / 2;
            int hotspotY = offsetY + ch / 2;
            Cursor cursor = Gdx.graphics.newCursor(upright, hotspotX, hotspotY);
            upright.dispose();
            return cursor;
        } catch (Exception ex) {
            Gdx.app.error("CursorManager", "Failed to build cursor for style '" + styleName + "'", ex);
            return null;
        }
    }

    private static int nextPowerOfTwo(int value) {
        int p = 1;
        while (p < value) {
            p <<= 1;
        }
        return p;
    }

    private static Pixmap flipVertically(Pixmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Pixmap out = new Pixmap(w, h, src.getFormat());
        for (int y = 0; y < h; y++) {
            out.drawPixmap(src, 0, y, 0, h - 1 - y, w, 1);
        }
        return out;
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
