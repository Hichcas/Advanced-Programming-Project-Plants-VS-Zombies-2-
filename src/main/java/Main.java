import controller.AppController;

public class Main {
    public static void main(String [] args) {
        // TODO 
        /**
         * game intialization must be started from here
         * configs must be imported from database
         * login user must be imported
         * first menu to start must be defined
         */

        util.GameInitialization.initialize();;
        AppController.start();
    }
}
