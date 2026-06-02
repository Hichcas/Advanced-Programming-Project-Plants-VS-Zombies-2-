package pvz.controller.menu;

import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class MainMenuController {

    public void show() {
        System.out.println("=== Main Menu ===");
        System.out.println("game | profile | collection | greenhouse | shop | quest | leaderboard | settings | logout | exit");
    }

    public Menu handleCommand(String command) {
        return switch (command) {
            case "game" -> Menu.GAME;
            case "profile" -> Menu.PROFILE;
            case "collection" -> Menu.COLLECTION;
            case "greenhouse" -> Menu.GREENHOUSE;
            case "shop" -> Menu.SHOP;
            case "quest" -> Menu.QUEST;
            case "leaderboard" -> Menu.LEADERBOARD;
            case "settings" -> Menu.SETTINGS;
            case "logout" -> Menu.LOGIN;
            case "exit" -> Menu.EXIT;
            default -> {
                MenuView.showInvalidCommand();
                yield null;
            }
        };
    }
}
