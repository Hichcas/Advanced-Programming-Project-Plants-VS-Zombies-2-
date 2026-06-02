package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class RegisterMenuController {

    public void show() {
        System.out.println("=== Register ===");
        System.out.println("register | back");
    }

    public Menu handleCommand(String command) {
        return switch (command) {
            case "register" -> Menu.MAIN;
            case "back" -> Menu.LOGIN;
            default -> {
                MenuView.showInvalidCommand();
                yield null;
            }
        };
    }
}
