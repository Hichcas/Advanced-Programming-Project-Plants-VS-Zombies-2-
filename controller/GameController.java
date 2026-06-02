package pvz.controller;

import java.util.Scanner;
import pvz.controller.menu.CollectionMenuController;
import pvz.controller.menu.GameMenuController;
import pvz.controller.menu.GreenhouseMenuController;
import pvz.controller.menu.LeaderboardMenuController;
import pvz.controller.menu.LoginMenuController;
import pvz.controller.menu.MainMenuController;
import pvz.controller.menu.ProfileMenuController;
import pvz.controller.menu.QuestMenuController;
import pvz.controller.menu.RegisterMenuController;
import pvz.controller.menu.SettingsMenuController;
import pvz.controller.menu.ShopMenuController;
import pvz.model.enums.Menu;
import pvz.view.MenuView;

public class GameController {

    private Menu currentMenu = Menu.LOGIN;
    private final Scanner scanner = new Scanner(System.in);

    private final LoginMenuController loginMenuController = new LoginMenuController();
    private final RegisterMenuController registerMenuController = new RegisterMenuController();
    private final MainMenuController mainMenuController = new MainMenuController();
    private final GameMenuController gameMenuController = new GameMenuController();
    private final SettingsMenuController settingsMenuController = new SettingsMenuController();
    private final ProfileMenuController profileMenuController = new ProfileMenuController();
    private final CollectionMenuController collectionMenuController = new CollectionMenuController();
    private final GreenhouseMenuController greenhouseMenuController = new GreenhouseMenuController();
    private final ShopMenuController shopMenuController = new ShopMenuController();
    private final QuestMenuController questMenuController = new QuestMenuController();
    private final LeaderboardMenuController leaderboardMenuController = new LeaderboardMenuController();

    public void start() {
        while (currentMenu != Menu.EXIT) {
            showCurrentMenu();
            String input = scanner.nextLine().trim();
            Menu next = dispatchCommand(input);
            if (next != null) {
                currentMenu = next;
            }
        }
    }

    private void showCurrentMenu() {
        switch (currentMenu) {
            case LOGIN -> loginMenuController.show();
            case REGISTER -> registerMenuController.show();
            case MAIN -> mainMenuController.show();
            case GAME -> gameMenuController.show();
            case SETTINGS -> settingsMenuController.show();
            case PROFILE -> profileMenuController.show();
            case COLLECTION -> collectionMenuController.show();
            case GREENHOUSE -> greenhouseMenuController.show();
            case SHOP -> shopMenuController.show();
            case QUEST -> questMenuController.show();
            case LEADERBOARD -> leaderboardMenuController.show();
            case EXIT -> { }
        }
    }

    private Menu dispatchCommand(String input) {
        return switch (currentMenu) {
            case LOGIN -> loginMenuController.handleCommand(input);
            case REGISTER -> registerMenuController.handleCommand(input);
            case MAIN -> mainMenuController.handleCommand(input);
            case GAME -> gameMenuController.handleCommand(input);
            case SETTINGS -> settingsMenuController.handleCommand(input);
            case PROFILE -> profileMenuController.handleCommand(input);
            case COLLECTION -> collectionMenuController.handleCommand(input);
            case GREENHOUSE -> greenhouseMenuController.handleCommand(input);
            case SHOP -> shopMenuController.handleCommand(input);
            case QUEST -> questMenuController.handleCommand(input);
            case LEADERBOARD -> leaderboardMenuController.handleCommand(input);
            case EXIT -> Menu.EXIT;
        };
    }
}
