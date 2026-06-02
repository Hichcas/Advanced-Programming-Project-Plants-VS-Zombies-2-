package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class CollectionMenuController {

    public void show() {
        System.out.println("=== Collection ===");
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
