//package com.PVZ.screen.panels;
//
//import com.badlogic.gdx.utils.Align;
//import com.PVZ.model.enums.MenuType;
//import com.PVZ.model.status.AppStatus;
//import com.PVZ.screen.GameScreen;
//import com.PVZ.screen.MainMenuScreen;
//import com.PVZ.screen.manager.FontManager;
//import com.PVZ.screen.manager.PanelManager;
//import com.PVZ.screen.manager.ScreenManager;
//import com.PVZ.screen.ui.MenuButton;
//
///**
// * ☀️ پنل پاز — دقیقاً با همون الگوی MainMenuPanel (BasePanel + MenuButton + FontManager).
// * برخلاف MainMenuPanel که کاملاً خودکفاست، این پنل به یه رفرنس از GameScreen نیاز داره
// * چون باید بتونه بازی رو resume کنه یا برگرده به منوی اصلی — این دو تا کار state ای هستن
// * که خودِ GameScreen نگه‌شون می‌داره.
// */
//public class PausePanel extends BasePanel {
//
//    private final GameScreen gameScreen;
//
//    public PausePanel(GameScreen gameScreen) {
//        this.gameScreen = gameScreen;
//
//        // کل صفحه را پوشش دهد و محتوا را وسط‌چین کند
//        this.setFillParent(true);
//        this.align(Align.center);
//
//        float spacing = 80f; // فاصله‌ی عمودی دکمه‌ها، دقیقاً مثل MainMenuPanel
//
//        this.add(new MenuButton("CONTINUE",
//            FontManager.getInstance().getEnglishMenuFont(),
//            this::onContinue)).padBottom(spacing).row();
//
//        this.add(new MenuButton("SETTINGS",
//            FontManager.getInstance().getEnglishMenuFont(),
//            this::onSettings)).padBottom(spacing).row();
//
//        this.add(new MenuButton("GUIDE",
//            FontManager.getInstance().getEnglishMenuFont(),
//            this::onGuide)).padBottom(spacing).row();
//
//        this.add(new MenuButton("BACK TO MAIN MENU",
//            FontManager.getInstance().getEnglishMenuFont(),
//            this::onBackToMainMenu)).row();
//    }
//
//    // ========== رفتار هر دکمه ==========
//
//    private void onContinue() {
//        gameScreen.isPaused = false;
//        PanelManager.getInstance().dispose();
//    }
//
//    private void onSettings() {
//        PanelManager.getInstance().performPanelTransition(new SettingsPanel(true));
//    }
//
//    private void onGuide() {
//        PanelManager.getInstance().performPanelTransition(new GuidePanel(true));
//    }
//
//    private void onBackToMainMenu() {
//        ScreenManager.getInstance().performTransition(MainMenuScreen::new);
//        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
//    }
//
//    @Override
//    public void dispose() {
//        super.dispose();
//    }
//}
