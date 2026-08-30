package com.PVZ.model.enums;

import com.PVZ.model.menus.*;

public enum MenuType {

    REGISTER(new RegisterMenu()),
    LOGIN(new LoginMenu()),
    MAIN(new MainMenu()),
    CHAPTER_AND_LEVEL_SELECTION(new ChapterAndLevelSelectionMenu()),
    PLANT_SELECTION(new PlantSelectionMenu()),
    SETTINGS(new SettingsMenu()),
    NETWORK(new NetworkMenu()),
    ONLINE_LEADERBOARD(new OnlineLeaderboardMenu()),
    NEWS(new NewsMenu()),
    PROFILE(new ProfileMenu()),
    COLLECTION(new CollectionMenu()),
    GREENHOUSE(new GreenhouseMenu()),
    SHOP(new ShopMenu()),
    TRAVEL_LOG(new TravelLogMenu()),
    LEADERBOARD(new LeaderboardMenu()),
    VASEBREAKER(new VasebreakerMenu()),
    MINIGAME_SELECTION(new MinigameSelectionMenu()),
    IN_GAME(new InGameMenu()),
    QUEST(new QuestMenu()),
    WALLNUT_BOWLING(new WallnutBowlingMenu()),
    I_ZOMBIE(new IZombieMenu()),
    I_ZOMBIE_SELECTION(new IZombieMenu()),
    BEGHOULED(new BeghouledMenu()),
    ZOMBOTANY(new ZombotanyMenu()),
    END_OF_GAME(new EndOfGameMenu());


    private final Menu currentMenu;

    MenuType(Menu currentMenu) {
        this.currentMenu = currentMenu;
    }

    public Menu getCurrentMenu() {
        return currentMenu;
    }
}
