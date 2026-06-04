package controller;

import view.input.GetInput;
import view.input.InputDTO;

public class AppController {
    public static void start() {
        InputDTO input = GetInput.get();
    }
}
