package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.ProfileCommand;
import com.PVZ.view.input.DTO.ProfileInputDTO;
import com.PVZ.view.input.InputDTO;

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
