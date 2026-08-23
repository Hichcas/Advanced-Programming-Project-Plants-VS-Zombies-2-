package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.ProfileMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.ProfileInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

public class ProfilePanel extends BasePanel {

    private final ProfileMenuController controller = new ProfileMenuController();

    private Label usernameLabel, nicknameLabel, gamesLabel, coinsLabel, diamondsLabel, stagesLabel, scoreLabel;

    private TextField newUsernameField, newNicknameField, newEmailField;
    private TextField oldPasswordField, newPasswordField, confirmPasswordField;
    private ImageButton eyeOldPass, eyeNewPass, eyeConfirmPass;

    private Label messageLabel;   // جایگزین errorLabel

    private final BitmapFont bigFont;
    private final Texture purpleUp, purpleDown, marker;
    private final Label.LabelStyle labelStyle, valueStyle;
    private final TextField.TextFieldStyle fieldStyle;
    private Texture eyeOpenTex, eyeCloseTex;

    private final float FIELD_WIDTH, FIELD_HEIGHT, BUTTON_HEIGHT, EYE_SIZE;
    private final float SCREEN_H;

    public ProfilePanel() {
        setFillParent(true);

        float screenW = Gdx.graphics.getWidth();
        SCREEN_H = Gdx.graphics.getHeight();
        FIELD_WIDTH   = screenW * 0.25f;
        FIELD_HEIGHT  = SCREEN_H * 0.06f;
        BUTTON_HEIGHT = SCREEN_H * 0.08f;
        EYE_SIZE      = SCREEN_H * 0.04f;

        purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        marker     = new Texture(Gdx.files.internal("global/button_marker.png"));
        eyeOpenTex = new Texture(Gdx.files.internal("global/eyeopen.png"));
        eyeCloseTex = new Texture(Gdx.files.internal("global/eyeclose.png"));

        Skin skin = PvzSkin.get();
        bigFont   = skin.getFont("FBUSV8C5EI_1_outline");

        labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.font = bigFont;
        labelStyle.fontColor = Color.WHITE;

        valueStyle = new Label.LabelStyle(labelStyle);
        valueStyle.fontColor = Color.YELLOW;

        fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = bigFont;
        fieldStyle.fontColor = new Color(0.8f, 0.6f, 0.0f, 1f);
        fieldStyle.messageFont = bigFont;

        // ---------- جدول اصلی ----------
        Table mainTable = new Table();
        mainTable.defaults().pad(6f);
        mainTable.align(Align.center);

        // عنوان
        MenuButton title = createTitleButton("PROFILE");
        mainTable.add(title).padBottom(20f).row();

        // اطلاعات کاربر
        usernameLabel = addInfoRow(mainTable, "Username:", "Loading...");
        nicknameLabel = addInfoRow(mainTable, "Nickname:", "");
        gamesLabel    = addInfoRow(mainTable, "Games played:", "");
        coinsLabel    = addInfoRow(mainTable, "Coins:", "");
        diamondsLabel = addInfoRow(mainTable, "Diamonds:", "");
        stagesLabel   = addInfoRow(mainTable, "Stages completed:", "");
        scoreLabel    = addInfoRow(mainTable, "MyoPoint:", "");

        mainTable.row().padTop(10f);
        mainTable.add(new Label("", labelStyle)).row();

        // تغییر نام کاربری
        mainTable.add(new Label("Change Username:", labelStyle)).center().row();
        newUsernameField = createField("New username");
        mainTable.add(newUsernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).center().row();
        mainTable.add(createButton("Change", this::onChangeUsername)).center().padBottom(10).row();

        // تغییر نام مستعار
        mainTable.add(new Label("Change Nickname:", labelStyle)).center().row();
        newNicknameField = createField("New nickname");
        mainTable.add(newNicknameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).center().row();
        mainTable.add(createButton("Change", this::onChangeNickname)).center().padBottom(10).row();

        // تغییر ایمیل
        mainTable.add(new Label("Change Email:", labelStyle)).center().row();
        newEmailField = createField("New email");
        mainTable.add(newEmailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).center().row();
        mainTable.add(createButton("Change", this::onChangeEmail)).center().padBottom(10).row();

        // تغییر رمز عبور
        mainTable.add(new Label("Change Password:", labelStyle)).center().padTop(10).row();

        oldPasswordField = createField("Old password");
        oldPasswordField.setPasswordMode(true);
        oldPasswordField.setPasswordCharacter('*');
        eyeOldPass = createEyeButton(oldPasswordField);
        mainTable.add(rowWithEye(oldPasswordField, eyeOldPass)).center().row();

        newPasswordField = createField("New password");
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        eyeNewPass = createEyeButton(newPasswordField);
        mainTable.add(rowWithEye(newPasswordField, eyeNewPass)).center().row();

        confirmPasswordField = createField("Confirm new password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        eyeConfirmPass = createEyeButton(confirmPasswordField);
        mainTable.add(rowWithEye(confirmPasswordField, eyeConfirmPass)).center().row();

        mainTable.add(createButton("Change Password", this::onChangePassword)).center().padBottom(10).row();

        // برچسب پیام (قرمز/سبز)
        messageLabel = new Label("", labelStyle);
        messageLabel.setAlignment(Align.center);
        mainTable.add(messageLabel).width(FIELD_WIDTH).padTop(10).center().row();

        // دکمه‌های پایین
        Table bottomBtns = new Table();
        bottomBtns.add(createButton("Back", () -> AppStatus.setCurrentMenuType(MenuType.MAIN))).padRight(40f);
        bottomBtns.add(createButton("Logout", this::onLogout));
        mainTable.add(bottomBtns).padTop(20).center().row();

        // اسکرول
        ScrollPane scrollPane = new ScrollPane(mainTable, skin);
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, true);
        scrollPane.layout();
        scrollPane.scrollTo(0, 0, 0, 0);

        addActor(scrollPane);

        refreshInfo();
    }

    // ---------- متدهای کمکی ----------
    private Label addInfoRow(Table table, String labelText, String valueText) {
        Table row = new Table();
        row.add(new Label(labelText, labelStyle)).padRight(10f);
        Label valueLbl = new Label(valueText, valueStyle);
        row.add(valueLbl);
        table.add(row).center().row();
        return valueLbl;
    }

    private void refreshInfo() {
        User user = AppStatus.currentUser;
        if (user != null) {
            usernameLabel.setText(user.profile.getUsername());
            nicknameLabel.setText(user.profile.getNickname());
            gamesLabel.setText(String.valueOf(user.userStats.getGamesPlayed()));
            coinsLabel.setText(String.valueOf(user.userStats.getCoins()));
            diamondsLabel.setText(String.valueOf(user.userStats.getDiamonds()));
            stagesLabel.setText(String.valueOf(user.userStats.getStagesCompleted()));
            scoreLabel.setText(String.valueOf(user.userStats.getHighestScore()));
        }
    }

    /** نمایش پیام – رنگ سبز برای موفقیت، قرمز برای خطا */
    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setColor(success ? Color.GREEN : Color.RED);
    }

    private void clearMessage() {
        messageLabel.setText("");
    }

    // ---------- ساخت ویجت‌ها ----------
    private MenuButton createTitleButton(String text) {
        MenuButton btn = new MenuButton(purpleUp, text, bigFont, purpleDown, null, marker, () -> {});
        btn.setDisabled(true);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 250f), BUTTON_HEIGHT * 1.2f);
        return btn;
    }

    private MenuButton createButton(String text, Runnable action) {
        MenuButton btn = new MenuButton(purpleUp, text, bigFont, purpleDown, null, marker, action);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 200f), BUTTON_HEIGHT);
        return btn;
    }

    private TextField createField(String placeholder) {
        TextField f = new TextField("", fieldStyle);
        f.setMessageText(placeholder);
        return f;
    }

    private ImageButton createEyeButton(TextField target) {
        TextureRegionDrawable eyeClose = new TextureRegionDrawable(new TextureRegion(eyeCloseTex));
        TextureRegionDrawable eyeOpen  = new TextureRegionDrawable(new TextureRegion(eyeOpenTex));
        ImageButton btn = new ImageButton(eyeClose);
        btn.getStyle().imageChecked = eyeOpen;
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean show = btn.isChecked();
                target.setPasswordMode(!show);
                target.setPasswordCharacter(show ? '\0' : '*');
            }
        });
        return btn;
    }

    private Table rowWithEye(TextField field, ImageButton eye) {
        Table row = new Table();
        row.add(field).width(FIELD_WIDTH - EYE_SIZE - 10).height(FIELD_HEIGHT);
        row.add(eye).size(EYE_SIZE).padLeft(10);
        return row;
    }

    // ---------- رویدادها ----------
    private void onChangeUsername() {
        clearMessage();
        String newUser = newUsernameField.getText().trim();
        if (newUser.isEmpty()) {
            showMessage("Username cannot be empty.", false);
            return;
        }
        ProfileInputDTO dto = new ProfileInputDTO(
            com.PVZ.model.enums.commands.ProfileCommand.CHANGE_USERNAME,
            newUser, null, null, null, null
        );
        OutputDTO result = controller.handle(dto);
        if (result.isSuccess()) {
            showMessage("Username changed successfully.", true);
            refreshInfo();
            newUsernameField.setText("");
        } else {
            showMessage(result.getMessage(), false);
        }
    }

    private void onChangeNickname() {
        clearMessage();
        String newNick = newNicknameField.getText().trim();
        ProfileInputDTO dto = new ProfileInputDTO(
            com.PVZ.model.enums.commands.ProfileCommand.CHANGE_NICKNAME,
            null, newNick, null, null, null
        );
        OutputDTO result = controller.handle(dto);
        if (result.isSuccess()) {
            showMessage("Nickname changed successfully.", true);
            refreshInfo();
            newNicknameField.setText("");
        } else {
            showMessage(result.getMessage(), false);
        }
    }

    private void onChangeEmail() {
        clearMessage();
        String newEmail = newEmailField.getText().trim();
        ProfileInputDTO dto = new ProfileInputDTO(
            com.PVZ.model.enums.commands.ProfileCommand.CHANGE_EMAIL,
            null, null, newEmail, null, null
        );
        OutputDTO result = controller.handle(dto);
        if (result.isSuccess()) {
            showMessage("Email changed successfully.", true);
            refreshInfo();
            newEmailField.setText("");
        } else {
            showMessage(result.getMessage(), false);
        }
    }

    private void onChangePassword() {
        clearMessage();
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showMessage("Please fill in all password fields.", false);
            return;
        }
        if (!newPass.equals(confirm)) {
            showMessage("New passwords do not match.", false);
            return;
        }

        ProfileInputDTO dto = new ProfileInputDTO(
            com.PVZ.model.enums.commands.ProfileCommand.CHANGE_PASSWORD,
            null, null, null, newPass, oldPass
        );
        OutputDTO result = controller.handle(dto);
        if (result.isSuccess()) {
            showMessage("Password changed successfully!", true);
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            showMessage(result.getMessage(), false);
        }
    }

    private void onLogout() {
        User currentUser = AppStatus.currentUser;
        if (currentUser != null) {
            currentUser.setStayLoggedIn(false);
            if (currentUser.profile != null) {
                UserRegistry.saveUserToDatabase(currentUser.profile.getUsername());
            }
        }
        AppStatus.currentUser = null;
        AppStatus.currentMenuType = MenuType.REGISTER;
    }

    @Override
    public void dispose() {
        if (eyeOpenTex != null) eyeOpenTex.dispose();
        if (eyeCloseTex != null) eyeCloseTex.dispose();
        super.dispose();
    }
}
