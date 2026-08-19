package com.PVZ.model.entity;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.renderer.EntityRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.EnumMap;
import java.util.Map;

public class LawnMower {
    private static final float SIZE = 140f;
    private static final double SPEED = 500;

    /**
     * هر فصل ماجراجویی توی بازی اصلی چمن‌زن مخصوص به خودش را دارد (طبق pam_list.txt کاربر:
     * MOWER_EGYPT برای مصر باستان، MOWER_ICEAGE برای غار یخ‌زده، MOWER_BEACH برای ساحل موج
     * بزرگ، MOWER_DARK برای قرون وسطا). این نگاشت مسیر PAM واقعی هر فصل را می‌دهد؛ اگر فصلی
     * این‌جا نبود (مثلاً مینی‌گیم‌ها که فصل مشخصی ندارند)، به چمن‌زن پیش‌فرض مصر برمی‌گردیم.
     */
    private static final Map<ChapterEnum, String> CHAPTER_MOWER_PAM = new EnumMap<>(ChapterEnum.class);
    static {
        CHAPTER_MOWER_PAM.put(ChapterEnum.ANCIENT_EGYPT, "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM");
        CHAPTER_MOWER_PAM.put(ChapterEnum.FROSTBITE_CAVES, "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM");
        CHAPTER_MOWER_PAM.put(ChapterEnum.BIG_WAVE_BEACH, "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM");
        CHAPTER_MOWER_PAM.put(ChapterEnum.DARK_AGES, "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM");
    }
    private static final String DEFAULT_MOWER_PAM = "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";

    private double x;
    private double y;
    private double triggerX;
    private double travelLimitX;
    private int row;
    private boolean triggered = false;
    private boolean used = false;
    private float animTime = 0f;
    private final Rectangle hitbox = new Rectangle();
    private final Rectangle parkedZone = new Rectangle();

    public void init(int row, double parkY, double triggerX, double travelLimitX) {
        this.row = row;
        this.y = parkY;
        this.triggerX = triggerX;
        this.travelLimitX = travelLimitX;
        this.x = triggerX;
        hitbox.set((float) x, (float) y, SIZE, SIZE);
        parkedZone.set((float) x, (float) y, SIZE, SIZE);
    }

    public Rectangle getParkedZone() {
        return parkedZone;
    }

    public double getFrontX() {
        return triggerX + SIZE;
    }

    public int getRow() {
        return row;
    }

    public double getTriggerX() {
        return triggerX;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public boolean isUsed() {
        return used;
    }

    public void trigger() {
        triggered = true;
    }

    public void advance(float delta) {
        animTime += delta;
        if (!triggered || used) {
            return;
        }
        x += SPEED * delta;
        hitbox.setPosition((float) x, (float) y);
        if (x > travelLimitX) {
            used = true;
        }
    }

    public void draw(SpriteBatch batch) {
        if (used) {
            return;
        }

        ChapterEnum chapter = null;
        if (AppStatus.getGameEngine() instanceof RegularGameEngine) {
            chapter = AppStatus.getCurrentChapterEnum();
        }

        String pamPath = CHAPTER_MOWER_PAM.getOrDefault(chapter, DEFAULT_MOWER_PAM);
        // "idle" وقتی پارک شده، "attack" وقتی فعال شده و در حال حرکت روی زامبی‌هاست — همان دو
        // کلیپی که توی pam_animations.json برای همه‌ی MOWER_* تعریف شده.
        String clip = triggered ? "attack" : "idle";

        boolean drew = EntityRenderer.getInstance()
            .renderPam(batch, pamPath, clip, animTime,
                (float) x + SIZE / 2f, (float) y + SIZE / 2f);

        if (!drew) {
            // اگر asset لود نشد (مثلاً پوشه‌ی asset ها کنار پروژه نیست)، حداقل یه بلوک قرمز
            // جایگزین نشون داده بشه تا هیچ سطری بدون چمن‌زن قابل‌مشاهده نمونه.
            batch.setColor(0.85f, 0.15f, 0.15f, 1f);
            batch.draw(getFallbackTexture(), (float) x, (float) y, SIZE, SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private static com.badlogic.gdx.graphics.Texture fallbackTexture;

    private static com.badlogic.gdx.graphics.Texture getFallbackTexture() {
        if (fallbackTexture == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap =
                new com.badlogic.gdx.graphics.Pixmap(4, 4, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            fallbackTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
            pixmap.dispose();
        }
        return fallbackTexture;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
