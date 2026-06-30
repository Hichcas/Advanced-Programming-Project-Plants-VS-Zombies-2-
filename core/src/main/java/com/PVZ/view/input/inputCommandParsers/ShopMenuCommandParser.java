package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.ShopCommand;
import com.PVZ.view.input.DTO.ShopInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class ShopMenuCommandParser {

    private ShopMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (ShopCommand shopCommand : ShopCommand.values()) {
            Matcher matcher = shopCommand.matcher(command);
            if (matcher.matches()) {
                return shopCommand.createDTO(matcher);
            }
        }
        return ShopInputDTO.invalid();
    }
}
