
import controller.AppController;
import model.enums.MenuType;
import model.status.AppStatus;

public class Main {

    public static void main(String[] args) {

        AppStatus.currentMenuType = MenuType.REGISTER;

        AppController.start();
    }
}