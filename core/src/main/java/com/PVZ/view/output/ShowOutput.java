package com.PVZ.view.output;

public class ShowOutput {

    private ShowOutput() {
    }

    public static void show(OutputDTO output) {

        if (output == null)
            return;

        System.out.println(output.getMessage());

    }

}
