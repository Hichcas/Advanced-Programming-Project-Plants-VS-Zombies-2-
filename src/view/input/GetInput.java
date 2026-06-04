package view.input;

/**
 * this file will be modified after graphic
 */

public class GetInput {
    public static InputDTO get() {
        return CommandParser.getNext();
    }
}
