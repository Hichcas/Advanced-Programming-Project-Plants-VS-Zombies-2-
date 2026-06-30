package com.PVZ.model.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.manager.FontManager;

public class GameHud extends Group {

    private final BitmapFont font;

    // مقادیری که قرار است نمایش دهیم
    private int sunflowerCount = 0;
    private int zombieWavePercent = 0;

    // ارتفاع مجازی (ثابت در BaseScreen)
    private static final float VIRTUAL_HEIGHT = 1440f;

    public GameHud() {
        // ۱. گرفتن فونت از FontManager (دقیقاً مثل قدیم)
        this.font = FontManager.getInstance().getEnglishMenuFont();

        // ۲. اندازه گروه را برابر کل فضای مجازی می‌دهیم
        //    تا کل صفحه را پوشش دهد (برای رسم در مختصات دلخواه)
        setSize(2560, VIRTUAL_HEIGHT);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // خواندن مقادیر از وضعیت بازی
        sunflowerCount = AppStatus.getGameEngine().gameStatus.getSunflower();
        zombieWavePercent = AppStatus.getGameEngine().gameStatus.getRemainingZomieWaveInPercent();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // رسم خود گروه (اگر actor دیگری داشت، رسم می‌شد – ولی اینجا خالیست)
        super.draw(batch, parentAlpha);

        // تنظیم رنگ قلم
        font.setColor(Color.GOLD);
        // رسم متن اول: Sunflowers
        font.draw(batch, "Sunflowers: " + sunflowerCount, 20, VIRTUAL_HEIGHT - 50);

        font.setColor(Color.RED);
        // رسم متن دوم: Zombie Wave
        font.draw(batch, "Zombie Wave: " + zombieWavePercent + "%", 20, VIRTUAL_HEIGHT - 100);

        // بعد از رسم، رنگ را به سفید برگردانیم (اختیاری برای ایمنی)
        font.setColor(Color.WHITE);
    }

    // نیازی به dispose جداگانه نیست چون فونت را خود FontManager مدیریت می‌کند
}
