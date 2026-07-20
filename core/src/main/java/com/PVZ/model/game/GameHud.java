package com.PVZ.model.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.manager.FontManager;

import java.util.List;

public class GameHud extends Group {

    private final BitmapFont font;

    // مقادیری که قرار است نمایش دهیم
    private int sunflowerCount = 0;
    private int zombieWavePercent = 0;
    private String beltLine = null;

    // ارتفاع مجازی (ثابت در BaseScreen)

    public GameHud() {
        // ۱. گرفتن فونت از FontManager (دقیقاً مثل قدیم)
        this.font = FontManager.getInstance().getEnglishMenuFont();

        // ۲. اندازه گروه را برابر کل فضای مجازی می‌دهیم
        //    تا کل صفحه را پوشش دهد (برای رسم در مختصات دلخواه)
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // رسم خود گروه (اگر actor دیگری داشت، رسم می‌شد – ولی اینجا خالیست)
        super.draw(batch, parentAlpha);

        // تنظیم رنگ قلم
        font.setColor(Color.GOLD);
        // رسم متن اول: Sunflowers
        font.draw(batch, "Sunflowers: " + sunflowerCount, 20, AppStatus.getQuality().height - 50);

        font.setColor(Color.RED);
        // رسم متن دوم: Zombie Wave
        font.draw(batch, "Zombie Wave: " + zombieWavePercent + "%", 20, AppStatus.getQuality().height - 100);

        if (beltLine != null) {
            font.setColor(Color.CYAN);
            // رسم متن سوم: محتوای نوار نقاله (مرحله ویژه Conveyor Belt)
            font.draw(batch, beltLine, 20, AppStatus.getQuality().height - 150);
        }

        // بعد از رسم، رنگ را به سفید برگردانیم (اختیاری برای ایمنی)
        font.setColor(Color.WHITE);
    }

    // نیازی به dispose جداگانه نیست چون فونت را خود FontManager مدیریت می‌کند
}
