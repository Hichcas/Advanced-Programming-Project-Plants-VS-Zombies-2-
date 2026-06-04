package controller;

import model.status.AppStatus;
import view.input.GetInput;
import view.input.InputDTO;

public class AppController {
    public static void start() {
        while (true) {
            InputDTO input = GetInput.get();

            if (input == null)
                continue;

            AppStatus.currentMenuType.currentMenu.handleInput(input);

            // TODO
            /**
             * some way to print screen menu fram by frame
             */
        }
    }
}
