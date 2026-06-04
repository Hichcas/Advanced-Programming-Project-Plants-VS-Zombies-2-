package database;

public class DataBaseManager implements DataBaseConnection {
private static volatile DataBaseManager manager;

    private DataBaseManager() {
    }

    public static DataBaseManager gDataBaseManager() {
        if (manager == null) {
            synchronized (DataBaseManager.class) {
                if (manager == null) {
                    manager = new DataBaseManager();
                }
            }
        }
        return manager;
    }


}
