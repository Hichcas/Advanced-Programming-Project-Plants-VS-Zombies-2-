package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.controller.menuControllers.LoginMenuController;
import com.PVZ.model.enums.commands.LoginCommand;
import com.PVZ.view.input.DTO.LoginInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;


public class LoginMenuCommandParser {

    private LoginMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        if (LoginMenuController.isWaitingForNewPassword()) {
            for (LoginCommand loginCommand : new LoginCommand[]{LoginCommand.SHOW_CURRENT_MENU, LoginCommand.EXIT}) {
                Matcher matcher = loginCommand.matcher(command);
                if (matcher.matches()) {
                    return loginCommand.createDTO(matcher);
                }
            }
            return new LoginInputDTO(
                    LoginCommand.NEW_PASSWORD,
                    null,
                    null,
                    false,
                    null,
                    null,
                    command == null ? null : command.trim()
            );
        }

        for (LoginCommand loginCommand : LoginCommand.values()) {
            if (loginCommand == LoginCommand.NEW_PASSWORD) {
                continue;
            }
            Matcher matcher = loginCommand.matcher(command);
            if (matcher.matches()) {
                return loginCommand.createDTO(matcher);
            }
        }

        return LoginInputDTO.invalid();
    }
}
