package util;

import model.enums.Menu;
import model.status.AppStatus;

public class GameInitialization {
    public static void initialize () {
        //TODO
        /**
         * other initializations must be done in here
         */

        AppStatus.currentMenu = Menu.LOGIN; // must be changed later
    }
}
