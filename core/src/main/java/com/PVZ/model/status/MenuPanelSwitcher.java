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
                PanelManager.getInstance().performPanelTransition(
                    new PlantSelectionPanel(AppStatus.currentChapterName, AppStatus.currentStageNumber));
                break;
            case I_ZOMBIE_SELECTION:
                PanelManager.getInstance().performPanelTransition(
                    new ZombieSelectionPanel(AppStatus.pendingIZombieLevelId));
                break;
            case MINIGAME_SELECTION:
                PanelManager.getInstance().performPanelTransition(new MinigameSelectionPanel());
                break;
            case IN_GAME:
                break;
            case NEWS:
                PanelManager.getInstance().performPanelTransition(new NewsPanel());
                break;
            case GREENHOUSE:
                PanelManager.getInstance().performPanelTransition(new GreenhousePanel());
                break;
            case SHOP:
                PanelManager.getInstance().performPanelTransition(new ShopPanel());
                break;
            case QUEST:
                PanelManager.getInstance().performPanelTransition(new QuestPanel());
                break;
            case COLLECTION:
                PanelManager.getInstance().performPanelTransition(new CollectionPanel());
                break;
            case LEADERBOARD:
                PanelManager.getInstance().performPanelTransition(new LeaderboardPanel());
                break;
            default:
                System.err.println("No UI panel mapped for menu: " + menuType);
                break;
        }
    }
}
