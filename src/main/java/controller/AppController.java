package controller;

import model.status.AppStatus;
import view.input.GetInput;
import view.input.InputDTO;
import view.output.OutputDTO;
import view.output.ShowOutput;

public class AppController {

    private AppController() {
    }

    public static void start() {

        while (true) {

            InputDTO input = GetInput.get();

            if (input == null)
                continue;

            OutputDTO output =
                    AppStatus.currentMenuType.getCurrentMenu()
                            .handleInput(input);

            ShowOutput.show(output);

        }

    }

}