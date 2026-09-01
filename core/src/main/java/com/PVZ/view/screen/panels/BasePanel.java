package com.PVZ.view.screen.panels;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.PVZ.view.renderer.EntityRenderer;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;

public abstract class BasePanel extends Table {

    public static class Art {
        public Texture texture;
        public float x, y, width, height;

        public Art(Texture texture, float x, float y, float width, float height) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private final ArrayList<Art> arts = new ArrayList<>();

    protected void addArt(Texture texture, float x, float y, float width, float height) {
        arts.add(new Art(texture, x, y, width, height));
    }

    // ======================== متدهای کمکی (جدید) ========================

    /** دسترسی به TextureBank مرکزی از طریق EntityRenderer */
    protected static TextureBank getTextureBank() {
        return EntityRenderer.getInstance().getTextures();
    }

    /**
     * تبدیل TextureRegion به Texture مستقل (کپی پیکسلی).
     * اگر region نال باشد، null برمی‌گرداند (برای استفاده در متد امن).
     */
    protected static Texture textureFromRegion(TextureRegion region) {
        if (region == null) return null;
        Texture tex = region.getTexture();
        if (!tex.getTextureData().isPrepared()) {
            tex.getTextureData().prepare();
        }
        Pixmap full = tex.getTextureData().consumePixmap();
        Pixmap sub = new Pixmap(region.getRegionWidth(), region.getRegionHeight(), full.getFormat());
        sub.drawPixmap(full, 0, 0, region.getRegionX(), region.getRegionY(),
            region.getRegionWidth(), region.getRegionHeight());
        Texture newTex = new Texture(sub);
        sub.dispose();
        full.dispose();
        return newTex;
    }

    /**
     * دریافت امن یک تکسچر با شناسه. اگر شناسه اشتباه باشد، خطا چاپ کرده و یک تکسچر ۱×۱ شفاف برمی‌گرداند.
     */
    protected static Texture safeTextureFromRegion(String resourceId) {
        TextureBank bank = getTextureBank();
        if (bank == null) {
            System.err.println("TextureBank is null! EntityRenderer not initialized?");
            return createDummyTexture();
        }
        TextureRegion region = bank.region(resourceId);
        if (region == null) {
            System.err.println("WARNING: TextureBank region is null for ID '"
                + resourceId + "'. Check resource ID or asset path.");
            return createDummyTexture();
        }
        return textureFromRegion(region);
    }

    /** افزودن Art مستقیماً از TextureRegion (تبدیل خودکار به Texture) */
    protected void addArt(TextureRegion region, float x, float y, float width, float height) {
        addArt(textureFromRegion(region), x, y, width, height);
    }

    /** چاپ فهرست همه شناسه‌های تصویر موجود در TextureBank برای دیباگ */
    protected static void printAvailableImageIds() {
        TextureBank bank = getTextureBank();
        if (bank != null && bank.getResourceIndex() != null) {
            System.out.println("=== Available image IDs in TextureBank ===");
            for (String id : bank.getResourceIndex().imageIds()) {
                System.out.println(id);
            }
        } else {
            System.out.println("TextureBank or ResourceIndex not available.");
        }
    }

    private static Texture createDummyTexture() {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();
        Texture dummy = new Texture(pix);
        pix.dispose();
        return dummy;
    }

    // ===================== پایان متدهای جدید =====================

    public void dispose() {
        for (Art art : arts) {
            if (art.texture != null) {
                art.texture.dispose();
            }
        }
        arts.clear();
        super.clear();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (Art art : arts) {
            if (art.texture != null) {
                batch.setColor(1f, 1f, 1f, parentAlpha);
                batch.draw(art.texture, art.x, art.y, art.width, art.height);
            }
        }
        super.draw(batch, parentAlpha);
    }
}
