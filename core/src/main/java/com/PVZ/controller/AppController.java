package com.PVZ.controller;

import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.GetInput;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.output.ShowOutput;

public class AppController {

    private AppController() {
    }

    public static void render() {


            InputDTO input = GetInput.get();

            if (input == null)
                return;

            OutputDTO output =
                    AppStatus.currentMenuType.getCurrentMenu()
                            .handleInput(input);

            ShowOutput.show(output);


    }

}
