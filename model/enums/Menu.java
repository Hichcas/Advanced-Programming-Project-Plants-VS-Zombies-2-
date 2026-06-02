package pvz.model.enums;

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

public enum Menu {

    public interface Controller {
        void run();
    }

    LOGIN(new LoginMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    REGISTER(new RegisterMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    MAIN(new MainMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    GAME(new GameMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    SETTINGS(new SettingsMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    PROFILE(new ProfileMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    COLLECTION(new CollectionMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    GREENHOUSE(new GreenhouseMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    SHOP(new ShopMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    QUEST(new QuestMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    LEADERBOARD(new LeaderboardMenuController() implements Controller {
        @Override
        public void run() {
            show();
        }
    }),
    EXIT(() -> { });

    private final Controller controller;

    Menu(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    public void execute() {
        controller.run();
    }
}
