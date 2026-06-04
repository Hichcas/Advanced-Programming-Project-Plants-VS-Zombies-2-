package view.input;

import model.enums.MenuType;
import model.status.AppStatus;

/**
 * this file will be deleted after graphic
 * input command parser package will all be deleted after graphic
 */

public class CommandParser {
        public static InputDTO getNext() {
        String command = AppStatus.scanner.nextLine();
        MenuType MenuType = AppStatus.currentMenuType;
        return MenuType.currentMenu.parseNextCommand(command);
    }
}
