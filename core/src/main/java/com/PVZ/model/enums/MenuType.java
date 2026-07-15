package com.PVZ.model.enums;

import com.PVZ.model.menus.*;

public enum MenuType {

    REGISTER(new RegisterMenu()),
    LOGIN(new LoginMenu()),
    MAIN(new MainMenu()),
    CHAPTER_AND_LEVEL_SELECTION(new ChapterAndLevelSelectionMenu()),
    PLANT_SELECTION(new PlantSelectionMenu()),
    SETTINGS(new SettingsMenu()),
    NEWS(new NewsMenu()),
    PROFILE(new ProfileMenu()),
    COLLECTION(new CollectionMenu()),
    GREENHOUSE(new GreenhouseMenu()),
    SHOP(new ShopMenu()),
    TRAVEL_LOG(new TravelLogMenu()),
    LEADERBOARD(new LeaderboardMenu()),
    VASEBREAKER(new VasebreakerMenu()),
    IN_GAME(new InGameMenu());

    private final Menu currentMenu;

    MenuType(Menu currentMenu) {
        this.currentMenu = currentMenu;
    }

    public Menu getCurrentMenu() {
        return currentMenu;
    }
}
