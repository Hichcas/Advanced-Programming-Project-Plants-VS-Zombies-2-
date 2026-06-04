package model.user;

import model.greenhouse.GreenhouseState;

public class User {
    public Profile profile;
    public UserStats userStats;
    public ProgressState progressState;
    public CollectionState collectionState;
    public GreenhouseState greenhouseState;
    public AppStats appStats;

    public User() {
    }

    public static User createNewUser(String username, String passwordHash, String nickname,
            String email, String gender,
            String securityQuestion, String securityAnswerHash) {
        // ایجاد پروفایل با اطلاعات ثبت‌نام
        Profile profile = new Profile(username, passwordHash, nickname, email, gender,
                securityQuestion, securityAnswerHash);

        User user = new User();
        user.profile = profile;
        user.userStats = new UserStats(); // همه آمار صفر
        user.appStats = new AppStats(); // سختی ۳، صدا ۱۵
        user.collectionState = new CollectionState(); // مجموعه‌های خالی
        user.progressState = new ProgressState(); // همه مراحل صفر
        user.greenhouseState = new GreenhouseState(); // ردیف اول باز، بقیه قفل

        return user;
    }
}