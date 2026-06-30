package com.PVZ.view.input;

import com.PVZ.model.status.AppStatus;

public class GetInput {

    public static InputDTO get() {
        return CommandParser.getNext();
    }

}
