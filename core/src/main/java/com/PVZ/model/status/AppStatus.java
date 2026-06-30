package com.PVZ.model.status;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.user.User;

import java.util.Scanner;

public final class AppStatus {

    private AppStatus() {
    }

    public static final Scanner scanner = new Scanner(System.in);
    public static MenuType currentMenuType = MenuType.REGISTER;
    public static User currentUser = null;
}
