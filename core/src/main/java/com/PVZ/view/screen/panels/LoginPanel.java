package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.LoginMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.LoginInputDTO;
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

public class LoginPanel extends BasePanel {

    private final LoginMenuController controller = new LoginMenuController();

    private TextField usernameField;
    private TextField passwordField;
    private CheckBox stayLoggedInCheck;
    private Label errorLabel;

    private ImageButton eyeButtonPassword;

    // ابعاد پویا
    private final float FIELD_WIDTH;
    private final float FIELD_HEIGHT;
    private final float BUTTON_HEIGHT;
    private final float MIN_BUTTON_WIDTH;
    private final float EYE_SIZE;

    private Texture eyeOpenTex;
    private Texture eyeCloseTex;

    public LoginPanel() {
        setFillParent(true);
        align(Align.center);

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        FIELD_WIDTH      = screenW * 0.25f;
        FIELD_HEIGHT     = screenH * 0.06f;
        BUTTON_HEIGHT    = screenH * 0.08f;
        MIN_BUTTON_WIDTH = screenW * 0.15f;
        EYE_SIZE         = screenH * 0.04f;

        eyeOpenTex  = new Texture(Gdx.files.internal("global/eyeopen.png"));
        eyeCloseTex = new Texture(Gdx.files.internal("global/eyeclose.png"));

        Texture purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker     = new Texture(Gdx.files.internal("global/button_marker.png"));

        Skin skin = PvzSkin.get();
        BitmapFont bigFont = skin.getFont("FBUSV8C5EI_1_outline");

        // استایل فیلد متنی
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = bigFont;
        fieldStyle.fontColor = new Color(0.8f, 0.6f, 0.0f, 1f);
        fieldStyle.messageFont = bigFont;

        // استایل برچسب
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.font = bigFont;
        labelStyle.fontColor = Color.WHITE;

        // استایل چک‌باکس
        CheckBox.CheckBoxStyle cbStyle = new CheckBox.CheckBoxStyle(
            skin.get(CheckBox.CheckBoxStyle.class));
        cbStyle.font = bigFont;
        cbStyle.fontColor = Color.WHITE;

        // ---------- جدول اصلی ----------
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);

        BitmapFont btnFont = bigFont;
        MenuButton title = createButton("LOGIN", () -> {}, purpleUp, purpleDown, btnFont, marker);
        title.setDisabled(true);
        mainTable.add(title).colspan(2).padBottom(screenH * 0.02f).row();

        usernameField = createField("Username", fieldStyle);
        mainTable.add(createLabel("Username:", labelStyle)).right();
        mainTable.add(usernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        passwordField = createField("Password", fieldStyle);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        eyeButtonPassword = createEyeButton(passwordField);
        mainTable.add(createLabel("Password:", labelStyle)).right();
        mainTable.add(rowWithEye(passwordField, eyeButtonPassword)).left().row();

        // چک‌باکس "Stay Logged In"
        stayLoggedInCheck = new CheckBox(" Stay Logged In", cbStyle);
        mainTable.add(stayLoggedInCheck).colspan(2).padTop(10).row();

        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        mainTable.add(errorLabel).colspan(2).width(FIELD_WIDTH).padTop(10).row();

        Table btnRow = new Table();
        MenuButton loginBtn = createButton("Login", this::onLogin, purpleUp, purpleDown, btnFont, marker);
        MenuButton exitBtn  = createButton("Quit", this::onExit, purpleUp, purpleDown, btnFont, marker);
        btnRow.add(loginBtn).padRight(40f);
        btnRow.add(exitBtn).padRight(40f);
        mainTable.add(btnRow).colspan(2).padTop(screenH * 0.02f).row();

        add(mainTable);
    }

    private Label createLabel(String text, Label.LabelStyle style) {
        return new Label(text, style);
    }

    private MenuButton createButton(String text, Runnable action, Texture up, Texture down,
                                    BitmapFont font, Texture marker) {
        MenuButton btn = new MenuButton(up, text, font, down, null, marker, action);
        float naturalWidth = btn.getTextWidth() + 40f;
        float finalWidth = Math.max(naturalWidth, MIN_BUTTON_WIDTH);
        btn.setSize(finalWidth, BUTTON_HEIGHT);
        return btn;
    }

    private TextField createField(String placeholder, TextField.TextFieldStyle style) {
        TextField field = new TextField("", style);
        field.setMessageText(placeholder);
        return field;
    }

    private Table rowWithEye(TextField field, ImageButton eye) {
        Table row = new Table();
        row.add(field).width(FIELD_WIDTH - EYE_SIZE - 10).height(FIELD_HEIGHT);
        row.add(eye).size(EYE_SIZE).padLeft(10);
        return row;
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

    private void onLogin() {
        errorLabel.setText("");

        LoginInputDTO dto = new LoginInputDTO(
            com.PVZ.model.enums.commands.LoginCommand.LOGIN,
            usernameField.getText(),
            passwordField.getText(),
            stayLoggedInCheck.isChecked(),
            null, null, null
        );

        OutputDTO result = controller.handle(dto);
        if (result != null) {
            if (result.isSuccess()) {
                // ورود موفقیت‌آمیز – کنترلر AppStatus.currentMenuType را MAIN کرده است
                // پنل به‌طور خودکار عوض می‌شود
            } else {
                errorLabel.setText(result.getMessage());
            }
        }
    }

    private void onExit() {
        // برگشت به منوی ثبت‌نام
        AppStatus.currentMenuType = MenuType.REGISTER;
    }

    @Override
    public void dispose() {
        if (eyeOpenTex != null) eyeOpenTex.dispose();
        if (eyeCloseTex != null) eyeCloseTex.dispose();
        super.dispose();
    }
}
