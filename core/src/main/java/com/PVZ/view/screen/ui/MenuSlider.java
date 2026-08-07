package com.PVZ.view.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.PVZ.view.screen.manager.CursorManager;
import com.PVZ.view.screen.manager.SoundManager;

public class MenuSlider extends Actor {

    // ----- منابع بصری (انعطاف‌پذیر) -----
    private final Texture trackTexture;
    private final TextureRegion trackRegion;
    private final Drawable trackDrawable;

    private final Texture fillTexture;
    private final TextureRegion fillRegion;
    private final Drawable fillDrawable;

    private final Texture knobTexture;
    private final TextureRegion knobRegion;
    private final Drawable knobDrawable;

    // مارکر تزئینی
    private final Texture markerTexture;

    // آیکون قطع/وصل صدا
    private final boolean hasIcon;
    private final Texture iconOnTexture;
    private final Texture iconOffTexture;
    private final Rectangle iconBounds = new Rectangle();   // حالا درجا ساخته می‌شود
    private final float iconSize = 72f;
    private final ToggleBinding toggleBinding;
    private int preMuteValue = 50;

    // مقدار فعلی
    private int value;
    private String valueText = "";
    private final BitmapFont font;
    private final GlyphLayout textLayout = new GlyphLayout();
    private final GlyphLayout labelLayout = new GlyphLayout();

    // برچسب
    private final String labelText;

    // حالت هاور
    private boolean isHovered = false;
    private float hoverAlpha = 0.5f;
    private static final float TARGET_HOVER_ALPHA = 1.0f;
    private static final float FADE_SPEED = 5f;

    // صداها
    private static Sound hoverSound;
    private static Sound clickSound;

    // ابعاد فیزیکی
    private float trackHeight = 20f;
    private float knobWidth = 30f;
    private float knobHeight = 40f;
    private final float padding = 15f;

    private final SliderBinding binding;

    // -------------------- سازنده‌ها --------------------

    /** سازندهٔ اصلی با Texture (سازگاری با گذشته) */
    public MenuSlider(String label, BitmapFont font,
                      Texture trackTexture, Texture fillTexture,
                      Texture knobTexture, Texture markerTexture,
                      boolean hasIcon, Texture iconOnTexture, Texture iconOffTexture,
                      SliderBinding binding, ToggleBinding toggleBinding) {
        this.labelText = label;
        this.font = font;
        this.trackTexture = trackTexture;
        this.fillTexture = fillTexture;
        this.knobTexture = knobTexture;
        this.markerTexture = markerTexture;
        this.hasIcon = hasIcon;
        this.iconOnTexture = iconOnTexture;
        this.iconOffTexture = iconOffTexture;
        this.binding = binding;
        this.toggleBinding = toggleBinding;

        // این فیلدها در این سازنده استفاده نمی‌شوند
        this.trackRegion = null;
        this.fillRegion = null;
        this.knobRegion = null;
        this.trackDrawable = null;
        this.fillDrawable = null;
        this.knobDrawable = null;

        initCommon();
    }

    /** سازنده با Drawable (برای PvzSkin) */
    public MenuSlider(String label, BitmapFont font,
                      Drawable track, Drawable fill, Drawable knob,
                      Texture markerTexture,
                      boolean hasIcon, Texture iconOnTexture, Texture iconOffTexture,
                      SliderBinding binding, ToggleBinding toggleBinding) {
        this.labelText = label;
        this.font = font;
        this.trackDrawable = track;
        this.fillDrawable = fill;
        this.knobDrawable = knob;
        this.markerTexture = markerTexture;
        this.hasIcon = hasIcon;
        this.iconOnTexture = iconOnTexture;
        this.iconOffTexture = iconOffTexture;
        this.binding = binding;
        this.toggleBinding = toggleBinding;

        this.trackTexture = null;
        this.fillTexture = null;
        this.knobTexture = null;
        this.trackRegion = null;
        this.fillRegion = null;
        this.knobRegion = null;

        initCommon();
    }

    /** سازنده با TextureRegion (برای TextureBank) */
    public MenuSlider(String label, BitmapFont font,
                      TextureRegion track, TextureRegion fill, TextureRegion knob,
                      Texture markerTexture,
                      boolean hasIcon, Texture iconOnTexture, Texture iconOffTexture,
                      SliderBinding binding, ToggleBinding toggleBinding) {
        this.labelText = label;
        this.font = font;
        this.trackRegion = track;
        this.fillRegion = fill;
        this.knobRegion = knob;
        this.markerTexture = markerTexture;
        this.hasIcon = hasIcon;
        this.iconOnTexture = iconOnTexture;
        this.iconOffTexture = iconOffTexture;
        this.binding = binding;
        this.toggleBinding = toggleBinding;

        this.trackTexture = null;
        this.fillTexture = null;
        this.knobTexture = null;
        this.trackDrawable = null;
        this.fillDrawable = null;
        this.knobDrawable = null;

        initCommon();
    }

    private void initCommon() {
        // تنظیم اندازهٔ اولیه بر اساس بزرگترین المان
        float maxTrackH = 20f, maxKnobW = 30f, maxKnobH = 40f;
        if (trackDrawable != null) {
            maxTrackH = Math.max(maxTrackH, trackDrawable.getMinHeight());
        } else if (trackRegion != null) {
            maxTrackH = Math.max(maxTrackH, trackRegion.getRegionHeight());
        } else if (trackTexture != null) {
            maxTrackH = Math.max(maxTrackH, trackTexture.getHeight());
        }
        if (knobDrawable != null) {
            maxKnobW = Math.max(maxKnobW, knobDrawable.getMinWidth());
            maxKnobH = Math.max(maxKnobH, knobDrawable.getMinHeight());
        } else if (knobRegion != null) {
            maxKnobW = Math.max(maxKnobW, knobRegion.getRegionWidth());
            maxKnobH = Math.max(maxKnobH, knobRegion.getRegionHeight());
        } else if (knobTexture != null) {
            maxKnobW = Math.max(maxKnobW, knobTexture.getWidth());
            maxKnobH = Math.max(maxKnobH, knobTexture.getHeight());
        }
        this.trackHeight = maxTrackH;
        this.knobWidth = maxKnobW;
        this.knobHeight = maxKnobH;

        // ارتفاع کلی = ارتفاع آیکون یا knob + فضای برچسب
        float totalHeight = Math.max(hasIcon ? iconSize : 0, knobHeight) + 30f;
        setSize(450, totalHeight);

        if (labelText != null && !labelText.isEmpty()) {
            labelLayout.setText(font, labelText);
        }

        if (binding != null) {
            setValue(binding.get());
            if (value > 0) preMuteValue = value;
        }

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
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
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 0) {
                    SoundManager.getInstance().playSound(getClickSound());
                    if (hasIcon && iconBounds.contains(x, y)) {
                        if (toggleBinding != null) {
                            boolean currentState = toggleBinding.get();
                            toggleBinding.set(!currentState);
                            if (!currentState) {
                                if (value > 0) preMuteValue = value;
                                setValue(0);
                            } else {
                                setValue(preMuteValue > 0 ? preMuteValue : 50);
                            }
                        }
                    } else {
                        updateValueFromMouse(x);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!(hasIcon && iconBounds.contains(x, y))) {
                    updateValueFromMouse(x);
                }
            }
        });
    }

    private void updateValueFromMouse(float mouseX) {
        float sliderLeftOffset = hasIcon ? (iconSize + 15f) : 0f;
        float usableWidth = getWidth() - padding * 2f - sliderLeftOffset;
        float knobSpace = knobWidth / 2f;
        float minX = padding + sliderLeftOffset + knobSpace;
        float maxX = padding + sliderLeftOffset + usableWidth - knobSpace;
        float clampedX = MathUtils.clamp(mouseX, minX, maxX);
        float percent = (clampedX - minX) / (maxX - minX);
        int newValue = MathUtils.round(percent * 100f);
        setValue(newValue);

        if (hasIcon && toggleBinding != null) {
            if (newValue == 0) {
                toggleBinding.set(true);
            } else {
                toggleBinding.set(false);
                preMuteValue = newValue;
            }
        }
    }

    public void setValue(int newValue) {
        this.value = MathUtils.clamp(newValue, 0, 100);
        this.valueText = String.valueOf(value);
        if (textLayout != null && font != null) {
            textLayout.setText(font, valueText);
        }
        if (binding != null) {
            binding.set(value);
        }
    }

    public int getValue() {
        return value;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float targetAlpha = isHovered ? TARGET_HOVER_ALPHA : 0.5f;
        hoverAlpha += (targetAlpha - hoverAlpha) * FADE_SPEED * delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float trackCenterY = getY() + 40f;
        float sliderLeftOffset = hasIcon ? (iconSize + 15f) : 0f;

        // برچسب
        if (labelText != null && !labelText.isEmpty() && labelLayout != null) {
            font.setColor(1f, 1f, 1f, hoverAlpha);
            float labelX = getX() + (getWidth() - labelLayout.width) / 2f;
            float labelY = trackCenterY + trackHeight + 20f + labelLayout.height;
            font.draw(batch, labelText, labelX, labelY);
        }

        // آیکون قطع/وصل
        if (hasIcon) {
            iconBounds.set(padding, 40f - iconSize / 2f, iconSize, iconSize);
            boolean isMuted = toggleBinding != null ? toggleBinding.get() : (value == 0);
            Texture currentIcon = isMuted ? iconOffTexture : iconOnTexture;
            if (currentIcon != null) {
                batch.setColor(1f, 1f, 1f, hoverAlpha);
                batch.draw(currentIcon, getX() + iconBounds.x, getY() + iconBounds.y,
                    iconBounds.width, iconBounds.height);
            }
        }

        float startX = getX() + padding + sliderLeftOffset;
        float trackW = getWidth() - padding * 2f - sliderLeftOffset;
        float fillWidth = trackW * (value / 100f);

        // رسم track
        drawElement(batch, trackDrawable, trackRegion, trackTexture,
            startX, trackCenterY - trackHeight / 2f, trackW, trackHeight, hoverAlpha);
        // رسم fill
        drawElement(batch, fillDrawable, fillRegion, fillTexture,
            startX, trackCenterY - trackHeight / 2f, fillWidth, trackHeight, hoverAlpha);

        // رسم knob
        float knobX = startX + fillWidth - knobWidth / 2f;
        float knobY = trackCenterY - knobHeight / 2f;
        drawElement(batch, knobDrawable, knobRegion, knobTexture,
            knobX, knobY, knobWidth, knobHeight, hoverAlpha);

        // عدد مقدار
        font.setColor(1f, 1f, 1f, hoverAlpha);
        float textX = getX() + getWidth() + 25f;
        float textY = trackCenterY + textLayout.height / 2f;
        font.draw(batch, valueText, textX, textY);

        // مارکرهای هاور
        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            float markerW = 40f, markerH = 30f;
            float markerY = trackCenterY - markerH / 2f;
            batch.draw(markerTexture, getX() - markerW - 5f, markerY, markerW, markerH);
            batch.draw(markerTexture, textX + textLayout.width + 10f, markerY, markerW, markerH,
                0, 0, markerTexture.getWidth(), markerTexture.getHeight(), true, false);
        }
        batch.setColor(Color.WHITE);
    }

    /** رسم یک المان با اولویت Drawable > TextureRegion > Texture */
    private void drawElement(Batch batch, Drawable d, TextureRegion r, Texture t,
                             float x, float y, float w, float h, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        if (d != null) {
            d.draw(batch, x, y, w, h);
        } else if (r != null) {
            batch.draw(r, x, y, w, h);
        } else if (t != null) {
            batch.draw(t, x, y, w, h);
        } else {
            drawDummy(batch, x, y, w, h);
        }
    }

    private static Texture dummyTexture;
    private static void drawDummy(Batch batch, float x, float y, float w, float h) {
        if (dummyTexture == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dummyTexture = new Texture(pix);
            pix.dispose();
        }
        batch.draw(dummyTexture, x, y, w, h);
    }

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
        if (dummyTexture != null) { dummyTexture.dispose(); dummyTexture = null; }
    }
}
