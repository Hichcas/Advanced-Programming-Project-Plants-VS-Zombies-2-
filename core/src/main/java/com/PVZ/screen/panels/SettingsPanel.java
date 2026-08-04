package com.PVZ.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.screen.manager.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.ui.MenuButton;
import com.PVZ.screen.ui.MenuSlider;
import com.PVZ.screen.ui.SliderBinding;
import com.PVZ.screen.ui.ToggleBinding;

public class SettingsPanel extends BasePanel {

    private Texture soundOnTex;
    private Texture soundOffTex;

    private MenuSlider musicSlider;
    private MenuSlider sfxSlider;
    private MenuSlider brightnessSlider;

    private MenuButton gameSpeedButton;
    private MenuButton difficultyButton;
    private MenuButton gridButton;
    private MenuButton debugButton;
    private MenuButton resetSoundsButton;
    private MenuButton backButton;

    private boolean toPauseMenu;

    // مقادیر ذخیره‌شده برای جلوگیری از فراخوانی تکراری منیجرها در act
    private int lastMusicVolume = -1;
    private int lastSfxVolume = -1;
    private int lastBrightness = -1;
    private boolean lastMusicMute = false;
    private boolean lastSfxMute = false;

    public SettingsPanel() {
        this(false);
    }

    public SettingsPanel(boolean toPauseMenu) {
        this.toPauseMenu = toPauseMenu;
        setFillParent(true);
        align(Align.center);

        // عنوان پنل (غیرفعال)
        MenuButton title = new MenuButton("Settings",
            FontManager.getInstance().getEnglishMenuFont(),
            () -> {});
        title.setDisabled(true);
        add(title).padBottom(20).row();

        // بارگذاری آیکون‌های صدا
        soundOnTex = new Texture(Gdx.files.internal("global/sound/sound_on.png"));
        soundOffTex = new Texture(Gdx.files.internal("global/sound/sound_off.png"));

        // اسلایدر حجم موسیقی (مستقیماً با MusicManager)
        musicSlider = new MenuSlider("Music Volume",
            FontManager.getInstance().getEnglishMenuFont(),
            null, null, null,
            new Texture("global/button_marker.png"),
            true,
            soundOnTex, soundOffTex,
            new SliderBinding() {
                @Override
                public int get() {
                    return (int) (MusicManager.getInstance().getVolume() * 100);
                }
                @Override
                public void set(int value) {
                    MusicManager.getInstance().setVolume(value / 100f);
                }
            },
            new ToggleBinding() {
                @Override
                public boolean get() {
                    return MusicManager.getInstance().getMute();
                }
                @Override
                public void set(boolean value) {
                    MusicManager.getInstance().setMuted(value);
                }
            });
        add(musicSlider).padBottom(20).row();

        // اسلایدر حجم افکت‌ها (مستقیماً با SoundManager)
        sfxSlider = new MenuSlider("SFX Volume",
            FontManager.getInstance().getEnglishMenuFont(),
            null, null, null,
            new Texture("global/button_marker.png"),
            true,
            soundOnTex, soundOffTex,
            new SliderBinding() {
                @Override
                public int get() {
                    return (int) (SoundManager.getInstance().getVolume() * 100);
                }
                @Override
                public void set(int value) {
                    SoundManager.getInstance().setVolume(value / 100f);
                }
            },
            new ToggleBinding() {
                @Override
                public boolean get() {
                    return SoundManager.getInstance().getMute();
                }
                @Override
                public void set(boolean value) {
                    SoundManager.getInstance().setMuted(value);
                }
            });
        add(sfxSlider).padBottom(20).row();

        // اسلایدر روشنایی (مستقیماً با BrightnessController)
        brightnessSlider = new MenuSlider("Brightness",
            FontManager.getInstance().getEnglishMenuFont(),
            null, null, null,
            new Texture("global/button_marker.png"),
            false, null, null,
            new SliderBinding() {
                @Override
                public int get() {
                    return (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f);
                }
                @Override
                public void set(int value) {
                    BrightnessController.getInstance().setBrightness((value / 50f) - 1f);
                }
            },
            null);
        add(brightnessSlider).padBottom(20).row();

        // دکمه چرخشی سرعت بازی (۱، ۲، ۳)
        gameSpeedButton = new MenuButton("Game Speed: " + AppStatus.getGameSpeed(),
            FontManager.getInstance().getEnglishMenuFont(),
            this::onGameSpeedChange);
        add(gameSpeedButton).padBottom(20).row();

        // دکمه تغییر سختی
        difficultyButton = new MenuButton("Difficulty: " + AppStatus.getDifficulty().name(),
            FontManager.getInstance().getEnglishMenuFont(),
            this::onDifficultyChange);
        add(difficultyButton).padBottom(20).row();

        // دکمه نمایش شبکه‌بندی
        gridButton = new MenuButton("Show Grid: " + (AppStatus.tileDebugEnabled ? "ON" : "OFF"),
            FontManager.getInstance().getEnglishMenuFont(),
            this::onGridToggle);
        add(gridButton).padBottom(20).row();

        // دکمه حالت دیباگ
        debugButton = new MenuButton("Debug Mode: " + (AppStatus.isDebugMode() ? "ON" : "OFF"),
            FontManager.getInstance().getEnglishMenuFont(),
            this::onDebugToggle);
        add(debugButton).padBottom(20).row();

        // دکمه ریست صداها
        resetSoundsButton = new MenuButton("Reset Sounds",
            FontManager.getInstance().getEnglishMenuFont(),
            this::onResetSounds);
        add(resetSoundsButton).padBottom(20).row();

        // دکمه بازگشت
        backButton = new MenuButton("Back",
            FontManager.getInstance().getEnglishMenuFont(),
            this::onBack);
        add(backButton).row();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refreshUI();
    }

    /**
     * مقادیر رابط کاربری را با وضعیت جاری سینگلتون‌ها هم‌گام می‌کند.
     * این متد در هر فریم صدا زده می‌شود تا تغییرات خارجی نیز اعمال شوند.
     */
    private void refreshUI() {
        // به‌روزرسانی متن دکمه‌ها
        gameSpeedButton.setText("Game Speed: " + AppStatus.getGameSpeed());
        difficultyButton.setText("Difficulty: " + AppStatus.getDifficulty().name());
        gridButton.setText("Show Grid: " + (AppStatus.tileDebugEnabled ? "ON" : "OFF"));
        debugButton.setText("Debug Mode: " + (AppStatus.isDebugMode() ? "ON" : "OFF"));

        // به‌روزرسانی بی‌صدا (silent) موقعیت اسلایدرها
        // فرض: MenuSlider دارای متد public void setValue(int value) است
        // که فقط ظاهر را تغییر می‌دهد و SliderBinding.set را صدا نمی‌زند.

        int musicVol = (int) (MusicManager.getInstance().getVolume() * 100);
        if (musicVol != lastMusicVolume) {
            musicSlider.setValue(musicVol);   // ← متد silent
            lastMusicVolume = musicVol;
        }

        int sfxVol = (int) (SoundManager.getInstance().getVolume() * 100);
        if (sfxVol != lastSfxVolume) {
            sfxSlider.setValue(sfxVol);
            lastSfxVolume = sfxVol;
        }

        int brightness = (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f);
        if (brightness != lastBrightness) {
            brightnessSlider.setValue(brightness);
            lastBrightness = brightness;
        }

        // همچنین وضعیت دکمه‌های mute را اگر در اسلایدر تغییری کرد،
        // می‌توان با متد متناظر خود اسلایدر به‌روز کرد (اگر وجود داشته باشد).
        // اما معمولاً وضعیت mute در اسلایدر با آیکون مدیریت می‌شود؛
        // در صورت نیاز، می‌توانید از متدهای اضافی MenuSlider برای به‌روزرسانی آیکون mute استفاده کنید.
    }

    private void onGameSpeedChange() {
        int current = AppStatus.getGameSpeed();
        int next = (current % 3) + 1;   // چرخش بین ۱-۲-۳
        AppStatus.setGameSpeed(next);
        // refreshUI() در act بعدی متن را به‌روز می‌کند
    }

    private void onDifficultyChange() {
        AppStatus.Difficulty current = AppStatus.getDifficulty();
        AppStatus.Difficulty next;
        switch (current) {
            case EASY:   next = AppStatus.Difficulty.NORMAL; break;
            case NORMAL: next = AppStatus.Difficulty.HARD;   break;
            default:     next = AppStatus.Difficulty.EASY;   break;
        }
        AppStatus.setDifficulty(next);
    }

    private void onGridToggle() {
        AppStatus.tileDebugEnabled = !AppStatus.tileDebugEnabled;
    }

    private void onDebugToggle() {
        boolean newVal = !AppStatus.isDebugMode();
        AppStatus.setDebugMode(newVal);
    }

    private void onResetSounds() {
        MusicManager.getInstance().setVolume(0.5f);
        SoundManager.getInstance().setVolume(0.5f);
        BrightnessController.getInstance().setBrightness(0f);
    }

    private void onBack() {
        PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    @Override
    public void dispose() {
        if (soundOnTex != null) soundOnTex.dispose();
        if (soundOffTex != null) soundOffTex.dispose();
        super.dispose();
    }
}
