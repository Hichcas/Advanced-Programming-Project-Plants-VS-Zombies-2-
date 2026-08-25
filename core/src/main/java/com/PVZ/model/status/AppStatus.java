package com.PVZ.model.status;

import com.PVZ.PVZ;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.graphics.GraphicsQuality;
import com.PVZ.model.user.User;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.MainMenuScreen;
import com.PVZ.view.screen.manager.BrightnessController;
import com.PVZ.view.screen.manager.MusicManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.badlogic.gdx.graphics.OrthographicCamera;

import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

public final class AppStatus {

    private static GameEngine gameEngine;
    private PVZ pvzGame;
    public static GraphicsQuality quality = GraphicsQuality.Ultra_High;
    public static OrthographicCamera camera;
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final Set<PlantType> SELECTED_PLANTS = new LinkedHashSet<>();
    public static final Set<PlantType> BOOSTED_PLANTS = new LinkedHashSet<>();
    /** Zombies the player picked on the I, Zombie roster-selection screen for the level about to start. */
    public static final Set<String> SELECTED_ZOMBIES = new LinkedHashSet<>();
    /** The I, Zombie level the roster-selection screen is currently choosing zombies for. */
    public static int pendingIZombieLevelId = 1;
    public static final Set<PlantType> CURRENT_STAGE_LOCKED_PLANTS = new LinkedHashSet<>();
    public static final Set<PlantFamily> CURRENT_STAGE_EXCLUSIVE_FAMILIES = new LinkedHashSet<>();
    /**
     * Only true for LOCKED_PLANTS-type stages. A handful of other stage types (like
     * PLANT_WHAT_YOU_GET) also populate CURRENT_STAGE_LOCKED_PLANTS with a fixed,
     * explicit list of plants to grey out, without wanting the "picking one plant
     * locks every other plant sharing a tag with it" side effect that LOCKED_PLANTS
     * stages use their Tags column for - hence a separate flag instead of just
     * checking whether the locked set is non-empty.
     */
    public static boolean CURRENT_STAGE_TAG_EXCLUSIVITY_ENABLED = false;
    public static PVZ PVZ;
    public static MenuType currentMenuType = MenuType.REGISTER;
    public static User currentUser = null;
    public static String currentChapterName = null;
    public static com.PVZ.model.game.chapter.Chapter currentChapter = null;
    public static int currentStageNumber = 1;
    public static boolean tileDebugEnabled = true;
    public static boolean lastGameResultWin = false;
    public static MainMenuScreen lastMainMenu;

    // ====================== سیستم اعلان وسط بازی ======================
    /**
     * صف اعلان‌های در انتظار نمایش. چون بازی امتیازی می‌تواند در یک لحظه
     * چند بونوس بدهد (مثلا Quick Kill + Combo با هم)، دیگر یک اسلات تنها
     * کافی نیست - هر {@link #showAnnouncement} یک آیتم به صف اضافه می‌کند
     * و لایه‌ی View (GameScreen) هر فریم یکی را برمی‌دارد و به‌صورت
     * پاپ‌آپ نشان می‌دهد (نگاه کنید به AnnouncementPopup).
     */
    private static final java.util.Queue<String> announcementQueue = new java.util.ArrayDeque<>();

    public static void showAnnouncement(String text) {
        if (text == null || text.isBlank()) return;
        announcementQueue.add(text);
    }

    /** توسط GameScreen هر فریم صدا زده می‌شود - اگر اعلانی در صف باشد آن را برمی‌دارد، وگرنه null. */
    public static String pollAnnouncement() {
        return announcementQueue.poll();
    }

    // ====================== سیستم لرزش دوربین ======================
    private static Runnable cameraShakeTrigger;

    public static void registerCameraShakeTrigger(Runnable trigger) {
        cameraShakeTrigger = trigger;
    }

    public static void triggerCameraShake() {
        if (cameraShakeTrigger != null) {
            cameraShakeTrigger.run();
        }
    }

    // ====================== وضعیت بازی آنلاین دونفره ======================
    public static boolean isMultiplayerMatch = false;
    public static String multiplayerRole = null;
    public static String multiplayerOpponent = null;
    public static String multiplayerRoomId = null;
    public static int multiplayerLevelId = 1;

    // ====================== تنظیمات جدید ======================
    public enum Difficulty {
        EASY, NORMAL, HARD
    }

    private static Difficulty difficulty = Difficulty.NORMAL;
    private static int gameSpeed = 1;               // مقدار ۱ تا ۳
    private static boolean debugMode = false;       // حالت دیباگ (افزایش سکه، خورشید و ...)
    private static float sfxVolume = 1.0f;          // حجم صدای افکت‌ها (۰ تا ۱)
    private static boolean sfxMuted = false;

    // ====================== متدهای قبلی ======================
    public static GraphicsQuality getQuality() {
        return quality;
    }

    public static void setQuality(GraphicsQuality q) {
        quality = q;
        PVZ.updateGraphics(q);
    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void setGameEngine(GameEngine engine) {
        gameEngine = engine;
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

    public static PVZ getPVZ() {
        return PVZ;
    }

    public static void setPVZ(PVZ pvz) {
        PVZ = pvz;
    }

    public static OrthographicCamera getCamera() {
        return camera;
    }

    public static void setCamera(OrthographicCamera cam) {
        camera = cam;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static ChapterEnum getCurrentChapterEnum() {
        if (currentChapterName == null) return null;
        try {
            return ChapterEnum.valueOf(currentChapterName.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void setCurrentMenuType(MenuType currentMenuType) {
        AppStatus.currentMenuType = currentMenuType;
    }

    public static void returnToMainMenu(String message) {
        currentMenuType = MenuType.MAIN;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(MainMenuScreen::new, message);
    }

    public static void returnToTravelLog() {
        currentMenuType = MenuType.TRAVEL_LOG;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(MainMenuScreen::new);
    }


    public static void returnToChapterAndLevelSelection(String message) {
        currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        setGameEngine(null);
        ScreenManager.getInstance().performTransition(MainMenuScreen::new, message);
    }

    // ====================== getter/setter های جدید ======================
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(Difficulty d) {
        difficulty = d;
    }

    public static int getGameSpeed() {
        return gameSpeed;
    }

    public static void setGameSpeed(int speed) {
        if (speed >= 1 && speed <= 3) {
            gameSpeed = speed;
        }
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }

    public static void setSFXVolume(int amount) {
        sfxVolume = Math.max(0, Math.min(1, amount / 100f));
    }

    public static MenuType getCurrentMenuType() {
        return currentMenuType;
    }
}
