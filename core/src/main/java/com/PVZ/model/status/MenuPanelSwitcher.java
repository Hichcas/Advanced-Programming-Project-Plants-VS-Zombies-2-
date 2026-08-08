package com.PVZ.model.status;

import com.PVZ.model.enums.MenuType;
import com.PVZ.view.screen.manager.PanelManager;
import com.PVZ.view.screen.panels.MainMenuPanel;
import com.PVZ.view.screen.panels.SettingsPanel;
import com.PVZ.view.screen.panels.RegisterPanel;

public class MenuPanelSwitcher {

    private static MenuType lastMenuType = null;

    private MenuPanelSwitcher() {}

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
            // اضافه‌کردن پنل‌های دیگر در آینده:
            // case LOGIN -> PanelManager.getInstance().performPanelTransition(new LoginPanel());
            // case PROFILE -> ...
            default:
                System.err.println("No UI panel mapped for menu: " + menuType);
                break;
        }
    }
}
