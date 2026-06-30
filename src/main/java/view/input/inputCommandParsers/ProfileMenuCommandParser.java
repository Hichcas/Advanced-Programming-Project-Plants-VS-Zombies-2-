package view.input.inputCommandParsers;

import model.enums.ProfileCommand;
import view.input.DTO.ProfileInputDTO;
import view.input.InputDTO;

import java.util.regex.Matcher;

public class ProfileMenuCommandParser {

    private ProfileMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (ProfileCommand profileCommand : ProfileCommand.values()) {
            Matcher matcher = profileCommand.matcher(command);
            if (matcher.matches()) {
                return profileCommand.createDTO(matcher);
            }
        }
        return ProfileInputDTO.invalid();
    }
}
