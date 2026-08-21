package com.PVZ.util;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.config.GameConfig;
import com.PVZ.model.entity.plants.PlantDataFile;
import com.PVZ.model.entity.plants.PlantDataLoader;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.network.client.NetworkSession;

import java.io.IOException;
import java.nio.file.Path;

public class GameInitialization {
    private static final PlantDataLoader PLANT_DATA_LOADER = new PlantDataLoader();

    /**
     * آدرس سرور. برای این مرحله ثابت است (localhost)؛ وقتی سیستم
     * تنظیمات/منوی شبکه آماده شد می‌توان این را قابل‌پیکربندی کرد
     * (مثلا یک فیلد در SettingsMenu یا آرگومان خط فرمان).
     */
    private static final String SERVER_HOST = "127.0.0.1";

    private GameInitialization() {
    }

    public static synchronized void initialize() throws IOException {
        AppStatus.currentMenuType = MenuType.LOGIN;
        tryConnectToServer();
        if (GameConfig.isPlantDataLoaded() && GameConfig.getPlantDataFile() != null) {
            return;
        }

        initializeFromResource(GameConfig.PLANTS_DATA_RESOURCE);
    }

    /**
     * تلاش برای اتصال به سرور در ابتدای اجرای بازی. عمدا fail-soft است:
     * اگر سرور بالا نیست (مثلا کسی محلی دارد تست می‌کند)، بازی همچنان
     * در حالت آفلاین بالا می‌آید - Login/Register به‌صورت خودکار به
     * مسیر محلی برمی‌گردند (نگاه کنید به LoginMenuController/RegisterMenuController).
     */
    private static void tryConnectToServer() {
        try {
            NetworkSession.connect(SERVER_HOST);
            System.out.println("[GameInitialization] Connected to server at " + SERVER_HOST);
        } catch (IOException e) {
            System.out.println("[GameInitialization] Could not reach server (" + e.getMessage()
                    + ") - continuing in offline mode.");
        }
    }

    public static synchronized void initializeFromResource(String resourcePath) throws IOException {
        PlantDataFile dataFile = PLANT_DATA_LOADER.loadFromResource(resourcePath);
        bootstrap(dataFile);
    }

    public static synchronized void initializeFromFile(Path path) throws IOException {
        PlantDataFile dataFile = PLANT_DATA_LOADER.loadFromFile(path);
        bootstrap(dataFile);
    }

    private static void bootstrap(PlantDataFile dataFile) {
        if (dataFile == null) {
            throw new IllegalArgumentException("Plant data file cannot be null");
        }

        PlantLibrary.clear();
        PlantLibrary.registerAll(dataFile.getPlants());
        GameConfig.setPlantDataFile(dataFile);
    }

}
