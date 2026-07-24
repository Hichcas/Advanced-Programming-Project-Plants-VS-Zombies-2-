package com.PVZ.view.input;

public class GetInput {

    public static InputDTO get() {
        return CommandParser.getNext();
    }

}
