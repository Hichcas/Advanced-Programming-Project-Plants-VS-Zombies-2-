package com.PVZ.model.status;

import com.PVZ.PVZ;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.user.User;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.screen.GameScreen;
import com.PVZ.screen.manager.ScreenManager;
import com.PVZ.screen.manager.BrightnessController;
import com.PVZ.screen.manager.MusicManager;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.PVZ.model.enums.ChapterEnum;

import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

public final class AppStatus {
    private static GameEngine gameEngine;
    private PVZ pvzGame;
    public static GraphicsQuality Quality = GraphicsQuality.Ultra_High;
    public static OrthographicCamera camera;

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
    public static final Scanner scanner = new Scanner(System.in);
    public static String currentChapterName = null;
    public static com.PVZ.model.game.chapter.Chapter currentChapter = null;
    public static int currentStageNumber = 1;
    public static final Set<PlantType> selectedPlants = new LinkedHashSet<>();
    public static final Set<PlantType> boostedPlants = new LinkedHashSet<>();
    public static final Set<PlantType> currentStageLockedPlants = new LinkedHashSet<>();
    /**
     * Families that are "pick-one" for the current stage (Type-1 rule from the doc):
     * the player may freely choose ANY member of the family, but as soon as one member
     * is selected, the rest of that family becomes locked for the remainder of selection.
     * This is dynamic (depends on what the player has already picked), unlike
     * {@link #currentStageLockedPlants} which is a fixed, static lock list.
     */
    public static final Set<PlantFamily> currentStageExclusiveFamilies = new LinkedHashSet<>();

    public static boolean tileDebugEnabled = false;

    public static ChapterEnum getCurrentChapterEnum() {
        if (currentChapterName == null) return null;
        try {
            return ChapterEnum.valueOf(currentChapterName.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static User getCurrentUser() { return currentUser;}

    public static PVZ getPVZ() {
        return PVZ;
    }

    public static void setPVZ(PVZ PVZ) {
        AppStatus.PVZ = PVZ;
    }

    public static OrthographicCamera getCamera() {
        return camera;
    }
    public static void setCamera(OrthographicCamera camera) {
        AppStatus.camera = camera;
    }
    public static void returnToMainMenu() {
        returnToMainMenu(null);
    }

    public static void returnToMainMenu(String message) {
        currentMenuType = MenuType.MAIN;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(() ->
            new GameScreen("maps/Frontyard.jpg", "music/Title Screen.mp3", new RegularGameEngine(new GameStatus())),
            message);
    }

    public static void returnToTravelLog() {
        currentMenuType = MenuType.TRAVEL_LOG;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(() ->
            new GameScreen("maps/Frontyard.jpg", "music/Title Screen.mp3", new RegularGameEngine(new GameStatus())));
    }

    /**
     * Leaves an in-progress level (via "menu exit" or a loss) and goes back to level select.
     * Unlike just flipping currentMenuType, this also drops the old GameEngine reference and
     * asks the ScreenManager to dispose the current GameScreen and swap in a fresh one — without
     * this, the old screen (and its engine) kept rendering/ticking in the background: leftover
     * planted plants stayed on the field and sun kept falling even after "exiting" the level.
     */
    public static void returnToChapterAndLevelSelection(String message) {
        currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(() ->
            new GameScreen("maps/Frontyard.jpg", "music/Title Screen.mp3", new RegularGameEngine(new GameStatus())),
            message);
    }

}
