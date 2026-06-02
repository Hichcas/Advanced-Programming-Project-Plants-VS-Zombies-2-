package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class LoginMenuController {

    public void show() {
        System.out.println("=== Login ===");
        System.out.println("login | register | exit");
    }

    public Menu handleCommand(String command) {
        return switch (command) {
            case "login" -> Menu.MAIN;
            case "register" -> Menu.REGISTER;
            case "exit" -> Menu.EXIT;
            default -> {
                MenuView.showInvalidCommand();
                yield null;
            }
        };
    }
}
