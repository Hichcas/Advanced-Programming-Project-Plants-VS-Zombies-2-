package model.status;

import model.enums.MenuType;
import model.user.User;

import java.util.Scanner;

public final class AppStatus {

    private AppStatus() {
    }

    public static final Scanner scanner = new Scanner(System.in);
    public static MenuType currentMenuType = MenuType.REGISTER;
    public static User currentUser = null;
}