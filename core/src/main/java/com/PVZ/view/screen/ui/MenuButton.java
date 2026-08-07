package com.PVZ.view.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.PVZ.view.screen.manager.CursorManager;
import com.PVZ.view.screen.manager.SoundManager;

public class MenuButton extends Actor {
    // ----- انواع پس‌زمینه -----
    private Texture backgroundTexture;
    private TextureRegion backgroundRegion;
    private Drawable backgroundDrawable;

    private Texture hoverTexture;
    private TextureRegion hoverRegion;
    private Drawable hoverDrawable;

    private Texture disabledTexture;
    private TextureRegion disabledRegion;
    private Drawable disabledDrawable;

    // ----- متن و فونت -----
    private String text;
    private BitmapFont font;
    private final GlyphLayout textLayout = new GlyphLayout();
    private float textOffsetX = 0f;
    private float textOffsetY = 0f;

    // ----- نشانگرهای تزئینی (مارکر) -----
    private Texture markerTexture;
    private float markerWidth = 70f;
    private float markerHeight = 44f;
    private float markerPadding = 35f;

    // ----- عملکرد -----
    private Runnable clickAction;

    // ----- حالت‌ها -----
    private boolean isHovered = false;
    private float currentAlpha = 0.5f;
    private final float TARGET_HOVER_ALPHA = 1.0f;
    private final float FADE_SPEED = 5f;
    private boolean disabled = false;

    // ----- صداهای مشترک -----
    private static Sound hoverSound;
    private static Sound clickSound;

    // ==================== سازنده‌ها ====================

    /** سازنده‌ی ساده (فقط متن) */
    public MenuButton(String text, BitmapFont font, Runnable clickAction) {
        this((Texture) null, text, font, null, null, null, clickAction);
    }

    /** سازنده با Texture (پس‌زمینه عادی) */
    public MenuButton(Texture background, String text, BitmapFont font, Runnable clickAction) {
        this(background, text, font, null, null, null, clickAction);
    }

    /** سازنده کامل Texture (عادی، هاور، غیرفعال، مارکر) */
    public MenuButton(Texture background, String text, BitmapFont font,
                      Texture hoverBackground, Texture disabledBackground,
                      Texture markerTexture, Runnable clickAction) {
        this.backgroundTexture = background;
        this.hoverTexture = hoverBackground;
        this.disabledTexture = disabledBackground;
        this.markerTexture = markerTexture;
        this.text = text;
        this.font = font;
        this.clickAction = clickAction;
        commonInit();
    }

    /** سازنده با TextureRegion (برای استفاده مستقیم از Atlas) */
    public MenuButton(TextureRegion backgroundRegion, String text, BitmapFont font,
                      TextureRegion hoverRegion, TextureRegion disabledRegion,
                      Texture markerTexture, Runnable clickAction) {
        this.backgroundRegion = backgroundRegion;
        this.hoverRegion = hoverRegion;
        this.disabledRegion = disabledRegion;
        this.markerTexture = markerTexture;
        this.text = text;
        this.font = font;
        this.clickAction = clickAction;
        commonInit();
    }

    /** سازنده با Drawable (برای PvzSkin) */
    public MenuButton(Drawable background, String text, BitmapFont font,
                      Drawable hoverBackground, Drawable disabledBackground,
                      Texture markerTexture, Runnable clickAction) {
        this.backgroundDrawable = background;
        this.hoverDrawable = hoverBackground;
        this.disabledDrawable = disabledBackground;
        this.markerTexture = markerTexture;
        this.text = text;
        this.font = font;
        this.clickAction = clickAction;
        commonInit();
    }

    /** سازنده‌ی کمکی Drawable فقط با background و hover */
    public MenuButton(Drawable background, Drawable hover, String text, BitmapFont font,
                      Texture markerTexture, Runnable clickAction) {
        this(background, text, font, hover, null, markerTexture, clickAction);
    }

    private void commonInit() {
        if (text != null && !text.isEmpty()) {
            textLayout.setText(font, text);
        }
        // تعیین اندازه اولیه بر اساس پس‌زمینه
        float w = 0, h = 0;
        if (backgroundTexture != null) {
            w = backgroundTexture.getWidth();
            h = backgroundTexture.getHeight();
        } else if (backgroundRegion != null) {
            w = backgroundRegion.getRegionWidth();
            h = backgroundRegion.getRegionHeight();
        } else if (backgroundDrawable != null) {
            w = backgroundDrawable.getMinWidth();
            h = backgroundDrawable.getMinHeight();
        } else {
            // حالت کاملاً متنی
            w = textLayout.width + 150f;
            h = 50f;
        }
        setSize(w, h);
        addListener(createClickListener());
    }

    // ==================== Listener ====================
    private ClickListener createClickListener() {
        return new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (disabled) return;
                if (pointer == -1 && !isHovered) {
                    SoundManager.getInstance().playSound(getHoverSound());
                }
                isHovered = true;
                CursorManager.getInstance().setPointerMode(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                isHovered = false;
                CursorManager.getInstance().setPointerMode(false);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (disabled) return;
                SoundManager.getInstance().playSound(getClickSound());
                if (clickAction != null) clickAction.run();
            }
        };
    }

    // ==================== تنظیمات ====================
    public void setTextOffset(float x, float y) {
        this.textOffsetX = x;
        this.textOffsetY = y;
    }

    /** تغییر متن دکمه و به‌روزرسانی اندازه (در صورت نبود پس‌زمینه) */
    public void setText(String newText) {
        this.text = newText;
        if (text == null || text.isEmpty()) {
            textLayout.reset();
        } else {
            textLayout.setText(font, text);
        }
        // اگر پس‌زمینه‌ای وجود نداشته باشد، اندازه را بر اساس متن به‌روز می‌کنیم
        if (backgroundTexture == null && backgroundRegion == null && backgroundDrawable == null) {
            float dynamicWidth = textLayout.width + 150f;
            setSize(dynamicWidth, getHeight());
        }
    }

    /** پهنای متن فعلی */
    public float getTextWidth() {
        return textLayout.width;
    }

    /** ارتفاع متن فعلی */
    public float getTextHeight() {
        return textLayout.height;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
    }

    // ==================== act ====================
    @Override
    public void act(float delta) {
        super.act(delta);
        if (disabled) {
            currentAlpha = 0.5f;
            return;
        }
        float target = isHovered ? TARGET_HOVER_ALPHA : 0.5f;
        currentAlpha += (target - currentAlpha) * FADE_SPEED * delta;
    }

    // ==================== draw ====================
    @Override
    public void draw(Batch batch, float parentAlpha) {
        // تعیین پس‌زمینه مناسب
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();
        float alpha = currentAlpha * parentAlpha;

        // اولویت: Drawable > TextureRegion > Texture
        // حالت غیرفعال
        if (disabled) {
            if (disabledDrawable != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                disabledDrawable.draw(batch, x, y, w, h);
            } else if (disabledRegion != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(disabledRegion, x, y, w, h);
            } else if (disabledTexture != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(disabledTexture, x, y, w, h);
            } else {
                drawFallback(batch, x, y, w, h, alpha);
            }
        }
        // حالت هاور
        else if (isHovered) {
            if (hoverDrawable != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                hoverDrawable.draw(batch, x, y, w, h);
            } else if (hoverRegion != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(hoverRegion, x, y, w, h);
            } else if (hoverTexture != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(hoverTexture, x, y, w, h);
            } else {
                drawFallback(batch, x, y, w, h, alpha);
            }
        }
        // حالت عادی
        else {
            if (backgroundDrawable != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                backgroundDrawable.draw(batch, x, y, w, h);
            } else if (backgroundRegion != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(backgroundRegion, x, y, w, h);
            } else if (backgroundTexture != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(backgroundTexture, x, y, w, h);
            } else {
                drawFallback(batch, x, y, w, h, alpha);
            }
        }

        // رسم متن
        if (text != null && !text.isEmpty() && font != null) {
            font.setColor(1f, 1f, 1f, alpha);
            float textX = x + (w - textLayout.width) / 2f + textOffsetX;
            float textY = y + (h + textLayout.height) / 2f + textOffsetY;
            font.draw(batch, text, textX, textY);
        }

        // نشانگرهای تزئینی هنگام هاور
        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            float markY = y + (h - markerHeight) / 2f;

            // مارکر چپ
            float leftMarkX = x - markerWidth - markerPadding;
            batch.draw(markerTexture, leftMarkX, markY, markerWidth, markerHeight);

            // مارکر راست
            float rightMarkX = x + w + markerPadding;
            batch.draw(markerTexture,
                rightMarkX, markY,
                markerWidth, markerHeight,
                0, 0, markerTexture.getWidth(), markerTexture.getHeight(),
                true, false);
        }

        batch.setColor(Color.WHITE);
    }

    /** در صورت نبود هیچ پس‌زمینه‌ای، یک مستطیل نیمه‌شفاف رسم می‌کند (اختیاری) */
    private void drawFallback(Batch batch, float x, float y, float w, float h, float alpha) {
        // در این نسخه هیچ کاری نمی‌کنیم (می‌توانید یک مستطیل ساده رسم کنید)
    }

    // ==================== غیرفعال‌سازی ====================
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if (disabled) isHovered = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    // ==================== صداها ====================
    private static Sound getHoverSound() {
        if (hoverSound == null) {
            hoverSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomSelection.mp3"));
        }
        return hoverSound;
    }

    private static Sound getClickSound() {
        if (clickSound == null) {
            clickSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomClicked.mp3"));
        }
        return clickSound;
    }

    public static void disposeStatic() {
        if (hoverSound != null) { hoverSound.dispose(); hoverSound = null; }
        if (clickSound != null) { clickSound.dispose(); clickSound = null; }
    }
}
