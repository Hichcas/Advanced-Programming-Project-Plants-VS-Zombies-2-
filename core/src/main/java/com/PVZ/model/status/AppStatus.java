package com.PVZ.model.status;

import com.PVZ.PVZ;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.user.User;
import com.PVZ.screen.manager.BrightnessController;
import com.PVZ.screen.manager.MusicManager;

public final class AppStatus {
    private static GameEngine gameEngine;
    private PVZ pvzGame;
    public static GraphicsQuality Quality = GraphicsQuality.Ultra_High;

    public static GraphicsQuality getQuality() {
        return Quality;
    }

    public static void setQuality(GraphicsQuality quality) {
        Quality = quality;
        PVZ.updateGraphics(quality);
    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void setGameEngine(GameEngine gameEngine) {
        AppStatus.gameEngine = gameEngine;
    }

    public PVZ getPvzGame() {
        return pvzGame;
    }

    public void setPvzGame(PVZ pvzGame) {
        this.pvzGame = pvzGame;
    }

    public static int getMusicVolume() {
        return (int) (MusicManager.getInstance().getVolume() * 100);
    }

    public static void setMusicVolume(int amount) {
        MusicManager.getInstance().setVolume(amount / 100f);
    }

    public static void setMutedMusic(boolean muted) {
        MusicManager.getInstance().setMuted(muted);
    }

    public static boolean getMuteMusic() {
        return MusicManager.getInstance().getMute();
    }

    public static int getBrightness() {
        return (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f);
    }

    public static void setBrightness(int value) {
        BrightnessController.getInstance().setBrightness((value / 50f) - 1f);
    }


    public static PVZ PVZ;

    public static MenuType currentMenuType = MenuType.REGISTER;
    public static User currentUser = null;

    public static PVZ getPVZ() {
        return PVZ;
    }

    public static void setPVZ(PVZ PVZ) {
        AppStatus.PVZ = PVZ;
    }
}
