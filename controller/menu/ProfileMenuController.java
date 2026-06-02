package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class ProfileMenuController {

    public void show() {
        System.out.println("=== Profile ===");
        System.out.println("back");
    }

    public Menu handleCommand(String command) {
        return switch (command) {
            case "back" -> Menu.MAIN;
            default -> {
                MenuView.showInvalidCommand();
                yield null;
            }
        };
    }
}
