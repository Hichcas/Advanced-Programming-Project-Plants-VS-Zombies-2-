package view.input.inputCommandParsers;

import controller.menuControllers.LoginMenuController;
import model.enums.LoginCommand;
import view.input.DTO.LoginInputDTO;
import view.input.InputDTO;

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
