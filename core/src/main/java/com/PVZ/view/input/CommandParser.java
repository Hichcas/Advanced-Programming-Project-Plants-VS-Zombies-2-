package com.PVZ.view.input;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;

/**
 * this file will be deleted after graphic
 * input command parser package will all be deleted after graphic
 */

public class CommandParser {
        public static InputDTO getNext() {
        String command = AppStatus.scanner.nextLine();
        MenuType MenuType = AppStatus.currentMenuType;
        return MenuType.getCurrentMenu().parseNextCommand(command);
    }
}
