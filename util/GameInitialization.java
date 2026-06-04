package util;

import model.enums.MenuType;
import model.status.AppStatus;

public class GameInitialization {
    public static void initialize () {
        //TODO
        /**
         * other initializations must be done in here
         */

        AppStatus.currentMenuType = MenuType.LOGIN; // must be changed later
    }
}
