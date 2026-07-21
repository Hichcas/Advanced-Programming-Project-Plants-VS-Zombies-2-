package com.PVZ.model.user;

import com.PVZ.model.greenhouse.GreenhouseState;

public class User {
    public Profile profile;
    public UserStats userStats;
    public ProgressState progressState;
    public CollectionState collectionState;
    public GreenhouseState greenhouseState;
    public AppStats appStats;
    public NewsState newsState;
    public ShopDaily shopDaily;
    private boolean stayLoggedIn;
    public QuestState questState;

    public User() {
    }

    public static User createNewUser(String username, String passwordHash, String nickname,
                                     String email, String gender,
                                     String securityQuestion, String securityAnswerHash) {
        Profile profile = new Profile(username, passwordHash, nickname, email, gender,
                securityQuestion, securityAnswerHash);

        User user = new User();
        user.profile = profile;
        user.userStats = new UserStats();
        user.appStats = new AppStats();
        user.newsState = new NewsState();
        user.collectionState = new CollectionState();
        user.progressState = new ProgressState();
        user.greenhouseState = new GreenhouseState();
        user.stayLoggedIn = false;
        user.shopDaily = new ShopDaily();
        user.questState = new QuestState();

        return user;
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
    }

    public void setStayLoggedIn(boolean stayLoggedIn) {
        this.stayLoggedIn = stayLoggedIn;
    }
}
