package model.menus;

import view.input.InputDTO;

public class ProfileMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.ProfileMenuCommandParser.parseCommand(command);
    }}
