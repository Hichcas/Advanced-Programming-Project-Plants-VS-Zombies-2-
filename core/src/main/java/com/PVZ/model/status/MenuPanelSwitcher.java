package com.PVZ.model.status;

import com.PVZ.model.enums.MenuType;
import com.PVZ.view.screen.manager.PanelManager;
import com.PVZ.view.screen.panels.*;

public class MenuPanelSwitcher {

    private static MenuType lastMenuType = null;

    private MenuPanelSwitcher() {
    }

    /**
     * باید هر فریم (مثلاً در PVZ.render()) فراخوانی شود.
     * اگر نوع منوی فعلی با قبلی متفاوت باشد، پنل گرافیکی آن را جایگزین می‌کند.
     */
    public static void update() {
        MenuType current = AppStatus.currentMenuType;
        if (current == lastMenuType) return;

        lastMenuType = current;
        performSwitch(current);
    }

    private static void performSwitch(MenuType menuType) {
        if (menuType == null) return;

        switch (menuType) {
            case MAIN:
                PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
                break;
            case SETTINGS:
                PanelManager.getInstance().performPanelTransition(new SettingsPanel());
                break;
            case REGISTER:
                PanelManager.getInstance().performPanelTransition(new RegisterPanel());
                break;
            case LOGIN:
                PanelManager.getInstance().performPanelTransition(new LoginPanel());
                break;
            case PROFILE:
                PanelManager.getInstance().performPanelTransition(new ProfilePanel());
                break;
            case CHAPTER_AND_LEVEL_SELECTION:
                PanelManager.getInstance().performPanelTransition(new ChapterSelectPanel());
                break;
            case PLANT_SELECTION:
                // این کیس قبلاً اصلاً وجود نداشت: کنترلر (ChapterAndLevelSelectionMenuController
                // .enterChapter) به‌درستی AppStatus.currentMenuType را روی PLANT_SELECTION
                // می‌گذاشت، ولی چون این‌جا هیچ پنلی برایش map نشده بود، سوییچر می‌رفت توی
                // default و فقط یک پیام خطا لاگ می‌کرد — یعنی بعد از انتخاب فصل/مرحله هیچ پنل
                // انتخاب گیاهی واقعاً روی صفحه نمی‌آمد. مقادیر فصل/مرحله را از همان
                // AppStatus.currentChapterName/currentStageNumber که enterChapter ست کرده
                // می‌خوانیم چون این متد فقط MenuType می‌گیرد.
                PanelManager.getInstance().performPanelTransition(
                    new PlantSelectionPanel(AppStatus.currentChapterName, AppStatus.currentStageNumber));
                break;
            case MINIGAME_SELECTION:
                PanelManager.getInstance().performPanelTransition(new MinigameSelectionPanel());
                break;
            case IN_GAME:
                // ورود به بازی از طریق ScreenManager.performTransition (که خودِ GameScreen را
                // می‌سازد) انجام می‌شود، نه از طریق پنل‌های این کلاس؛ پس این‌جا کاری لازم نیست.
                // فقط برای اینکه در کنسول به‌اشتباه به‌عنوان «پنل نگاشت‌نشده» لاگ نشود این کیس
                // را صریح خالی می‌گذاریم.
                break;
            default:
                System.err.println("No UI panel mapped for menu: " + menuType);
                break;
        }
    }
}
