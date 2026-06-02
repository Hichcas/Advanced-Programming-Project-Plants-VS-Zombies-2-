package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class GameMenuController {

    public void show() {
        System.out.println("=== Game ===");
        System.out.println("start | back");
    }

    public Menu handleCommand(String command) {
        return switch (command) {
            case "start" -> null;
            case "back" -> Menu.MAIN;
            default -> {
                MenuView.showInvalidCommand();
                yield null;
            }
        };
    }
}
