package com.PVZ.view.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import pvz.skin.PvzSkin;

public class FontManager {
    private static FontManager instance;

    private BitmapFont persianMenuFont;
    private BitmapFont persianTitleFont;

    private static final String PERSIAN_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS +
        "ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیيکآأإؤئ";

    private FontManager() {
        loadPersianFonts();
    }

    private void loadPersianFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Vazir.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.characters = PERSIAN_CHARS;

        parameter.size = 54;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        persianMenuFont = generator.generateFont(parameter);

        parameter.size = 72;
        persianTitleFont = generator.generateFont(parameter);

        generator.dispose();
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    // ==================== فونت‌های انگلیسی (از PvzSkin) ====================
    public BitmapFont getEnglishMenuFont() {
        // فونت بزرگ و خوانا با حاشیه، مناسب دکمه‌های منو
        return PvzSkin.get().getFont("FBUSV8C5EI_1_outline");
    }

    public BitmapFont getEnglishTitleFont() {
        // برای تیترها از همان فونت بزرگ استفاده می‌کنیم
        return PvzSkin.get().getFont("FBUSV8C5EI_1_outline");
    }

    public BitmapFont getEnglishTinyFont() {
        // فونت ریزتر برای توضیحات یا اطلاعات فرعی
        return PvzSkin.get().getFont("FBUSV8C6EI_3");
    }

    // ==================== فونت‌های فارسی (بدون تغییر) ====================
    public BitmapFont getPersianMenuFont() {
        return persianMenuFont;
    }

    public BitmapFont getPersianTitleFont() {
        return persianTitleFont;
    }

    // ==================== مدیریت منابع ====================
    public void dispose() {
        // فونت‌های انگلیسی متعلق به PvzSkin هستند و نباید اینجا dispose شوند.
        if (persianMenuFont != null) persianMenuFont.dispose();
        if (persianTitleFont != null) persianTitleFont.dispose();
    }
}
