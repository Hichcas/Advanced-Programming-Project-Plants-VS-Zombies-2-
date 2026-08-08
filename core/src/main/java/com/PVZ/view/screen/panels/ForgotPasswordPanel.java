package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.LoginMenuController;
import com.PVZ.view.input.DTO.LoginInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.screen.manager.PanelManager;
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

public class ForgotPasswordPanel extends BasePanel {

    private final LoginMenuController controller = new LoginMenuController();

    // ---------- فیلدهای مرحله ۱ ----------
    private TextField usernameField, emailField;

    // ---------- فیلدهای مرحله ۲-الف ----------
    private Label questionLabel;
    private TextField answerField;

    // ---------- فیلدهای مرحله ۲-ب ----------
    private TextField newPasswordField, confirmPasswordField;
    private ImageButton eyeNewPass, eyeConfirmPass;

    private Label errorLabel;

    private Table mainTable;

    // منابع مشترک
    private final BitmapFont bigFont;
    private final Texture purpleUp, purpleDown, marker;
    private final Label.LabelStyle labelStyle;
    private final TextField.TextFieldStyle fieldStyle;
    private Texture eyeOpenTex, eyeCloseTex;

    // وضعیت
    private int step = 1;

    private final float FIELD_WIDTH, FIELD_HEIGHT, BUTTON_HEIGHT, EYE_SIZE;
    private final float SCREEN_H;

    public ForgotPasswordPanel() {
        setFillParent(true);
        align(Align.center);

        float screenW = Gdx.graphics.getWidth();
        SCREEN_H = Gdx.graphics.getHeight();
        FIELD_WIDTH   = screenW * 0.25f;
        FIELD_HEIGHT  = SCREEN_H * 0.06f;
        BUTTON_HEIGHT = SCREEN_H * 0.08f;
        EYE_SIZE      = SCREEN_H * 0.04f;

        // تصاویر
        purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        marker     = new Texture(Gdx.files.internal("global/button_marker.png"));
        eyeOpenTex = new Texture(Gdx.files.internal("global/eyeopen.png"));
        eyeCloseTex = new Texture(Gdx.files.internal("global/eyeclose.png"));

        Skin skin = PvzSkin.get();
        bigFont   = skin.getFont("FBUSV8C5EI_1_outline");

        fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = bigFont;
        fieldStyle.fontColor = new Color(0.8f, 0.6f, 0.0f, 1f);
        fieldStyle.messageFont = bigFont;

        labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.font = bigFont;
        labelStyle.fontColor = Color.WHITE;

        mainTable = new Table();
        mainTable.defaults().pad(8f);
        add(mainTable);

        buildStep1();
    }

    // ---------- متدهای کمکی ----------
    private void clearContent() {
        mainTable.clearChildren();
        errorLabel = null;
    }

    private MenuButton createButton(String text, Runnable action) {
        MenuButton btn = new MenuButton(purpleUp, text, bigFont, purpleDown, null, marker, action);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 200f), BUTTON_HEIGHT);
        return btn;
    }

    private MenuButton createTitleButton(String text) {
        MenuButton btn = new MenuButton(purpleUp, text, bigFont, purpleDown, null, marker, () -> {});
        btn.setDisabled(true);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 250f), BUTTON_HEIGHT * 1.2f);
        return btn;
    }

    private TextField createField(String placeholder) {
        TextField f = new TextField("", fieldStyle);
        f.setMessageText(placeholder);
        return f;
    }

    private ImageButton createEyeButton(TextField targetField) {
        TextureRegionDrawable eyeClose = new TextureRegionDrawable(new TextureRegion(eyeCloseTex));
        TextureRegionDrawable eyeOpen  = new TextureRegionDrawable(new TextureRegion(eyeOpenTex));
        ImageButton button = new ImageButton(eyeClose);
        button.getStyle().imageChecked = eyeOpen;
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean show = button.isChecked();
                targetField.setPasswordMode(!show);
                targetField.setPasswordCharacter(show ? '\0' : '*');
            }
        });
        return button;
    }

    private Table rowWithEye(TextField field, ImageButton eye) {
        Table row = new Table();
        row.add(field).width(FIELD_WIDTH - EYE_SIZE - 10).height(FIELD_HEIGHT);
        row.add(eye).size(EYE_SIZE).padLeft(10);
        return row;
    }

    private void addErrorLabel() {
        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        mainTable.add(errorLabel).colspan(2).width(FIELD_WIDTH).padTop(10).row();
    }

    // ==================== مراحل ====================
    private void buildStep1() {
        step = 1;
        clearContent();

        MenuButton title = createTitleButton("FORGOT PASSWORD");
        mainTable.add(title).colspan(2).padBottom(20f).row();

        usernameField = createField("Username");
        mainTable.add(new Label("Username:", labelStyle)).right();
        mainTable.add(usernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        emailField = createField("Email");
        mainTable.add(new Label("Email:", labelStyle)).right();
        mainTable.add(emailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        addErrorLabel();

        Table btnRow = new Table();
        MenuButton submitBtn = createButton("Submit", this::onStep1Submit);
        MenuButton backBtn = createButton("Back", this::onBackToLogin);
        btnRow.add(submitBtn).padRight(30f);
        btnRow.add(backBtn);
        mainTable.add(btnRow).colspan(2).padTop(SCREEN_H * 0.02f).row();
    }

    private void buildStep2a(String question) {
        step = 2;
        clearContent();

        MenuButton title = createTitleButton("SECURITY QUESTION");
        mainTable.add(title).colspan(2).padBottom(20f).row();

        questionLabel = new Label(question, labelStyle);
        questionLabel.setWrap(true);
        mainTable.add(questionLabel).colspan(2).width(FIELD_WIDTH).padBottom(10f).row();

        answerField = createField("Answer");
        mainTable.add(new Label("Answer:", labelStyle)).right();
        mainTable.add(answerField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        addErrorLabel();

        Table btnRow = new Table();
        MenuButton verifyBtn = createButton("Verify Answer", this::onVerifyAnswer);
        MenuButton backBtn = createButton("Back", this::onBackToLogin);
        btnRow.add(verifyBtn).padRight(30f);
        btnRow.add(backBtn);
        mainTable.add(btnRow).colspan(2).padTop(SCREEN_H * 0.02f).row();
    }

    private void buildStep2b() {
        step = 3;
        clearContent();

        MenuButton title = createTitleButton("NEW PASSWORD");
        mainTable.add(title).colspan(2).padBottom(20f).row();

        // ردیف رمز جدید + چشم
        newPasswordField = createField("New Password");
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        eyeNewPass = createEyeButton(newPasswordField);
        mainTable.add(new Label("New Password:", labelStyle)).right();
        mainTable.add(rowWithEye(newPasswordField, eyeNewPass)).left().row();

        // ردیف تأیید رمز + چشم
        confirmPasswordField = createField("Confirm Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        eyeConfirmPass = createEyeButton(confirmPasswordField);
        mainTable.add(new Label("Confirm:", labelStyle)).right();
        mainTable.add(rowWithEye(confirmPasswordField, eyeConfirmPass)).left().row();

        addErrorLabel();

        Table btnRow = new Table();
        MenuButton changeBtn = createButton("Change Password", this::onChangePassword);
        MenuButton backBtn = createButton("Back", this::onBackToLogin);
        btnRow.add(changeBtn).padRight(30f);
        btnRow.add(backBtn);
        mainTable.add(btnRow).colspan(2).padTop(SCREEN_H * 0.02f).row();
    }

    private void buildStep4Success() {
        step = 4;
        clearContent();

        Label successLabel = new Label("Password changed successfully!", labelStyle);
        successLabel.setColor(Color.GREEN);
        mainTable.add(successLabel).padBottom(20f).row();

        MenuButton loginBtn = createButton("Go to Login", () -> {
            LoginMenuController.resetForgotPasswordState();
            PanelManager.getInstance().performPanelTransition(new LoginPanel());
        });
        mainTable.add(loginBtn).row();
    }

    // ==================== رویدادها ====================
    private void onStep1Submit() {
        LoginInputDTO dto = new LoginInputDTO(
            com.PVZ.model.enums.commands.LoginCommand.FORGET_PASSWORD,
            usernameField.getText(), null, false,
            emailField.getText(), null, null
        );
        OutputDTO result = controller.handle(dto);
        if (result != null && result.isSuccess()) {
            buildStep2a(result.getMessage());
        } else if (result != null) {
            errorLabel.setText(result.getMessage());
        }
    }

    private void onVerifyAnswer() {
        LoginInputDTO dto = new LoginInputDTO(
            com.PVZ.model.enums.commands.LoginCommand.ANSWER,
            null, null, false, null,
            answerField.getText(), null
        );
        OutputDTO result = controller.handle(dto);
        if (result != null && result.isSuccess()) {
            buildStep2b();
        } else if (result != null) {
            errorLabel.setText(result.getMessage());
        }
    }

    private void onChangePassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            errorLabel.setText("Please fill in both password fields.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // اصلاح: رمز جدید از طریق فیلد newPassword ارسال شود
        LoginInputDTO dto = new LoginInputDTO(
            com.PVZ.model.enums.commands.LoginCommand.NEW_PASSWORD,
            null, null, false, null, null, newPass   // <-- رمز در آخرین آرگومان
        );
        OutputDTO result = controller.handle(dto);
        if (result != null && result.isSuccess()) {
            buildStep4Success();
        } else if (result != null) {
            errorLabel.setText(result.getMessage());
        }
    }

    private void onBackToLogin() {
        LoginMenuController.resetForgotPasswordState();
        PanelManager.getInstance().performPanelTransition(new LoginPanel());
    }

    @Override
    public void dispose() {
        if (eyeOpenTex != null) eyeOpenTex.dispose();
        if (eyeCloseTex != null) eyeCloseTex.dispose();
        super.dispose();
    }
}
