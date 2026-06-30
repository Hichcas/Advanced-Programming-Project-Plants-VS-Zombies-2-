package com.PVZ.view.input;

import com.PVZ.model.status.AppStatus;

public class GetInput {

    private GetInput() {
    }

    public static InputDTO get() {

        String command = AppStatus.scanner.nextLine().trim();

        return AppStatus.currentMenuType
                .getCurrentMenu()
                .parseNextCommand(command);
    }

}
