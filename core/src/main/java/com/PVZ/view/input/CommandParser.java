package com.PVZ.view.input;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;

/**
 * this file will be deleted after graphic
 * input command parser package will all be deleted after graphic
 */

public class CommandParser {
    private static ConsoleInputHandler console;

    public static InputDTO getNext() {
        String command = console.pollCommand();
        if (command == null) {
            return null;
        }
        command = command.trim();
        MenuType menuType = AppStatus.currentMenuType;
        return menuType.getCurrentMenu().parseNextCommand(command);
    }

    public static void start() {
        System.out.println("Command input thread is started");
        console = new ConsoleInputHandler();
        console.start();
    }

    public static void end() {
        if (console != null) {
            console.stop();
            console = null;
        }
    }
}
