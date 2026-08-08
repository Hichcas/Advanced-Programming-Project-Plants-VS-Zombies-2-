package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.RegisterMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.RegisterInputDTO;
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

public class RegisterPanel extends BasePanel {

    private final RegisterMenuController controller = new RegisterMenuController();

    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private TextField nicknameField;
    private TextField emailField;
    private SelectBox<String> genderBox;
    private SelectBox<String> questionBox;
    private TextField answerField;
    private TextField answerConfirmField;
    private Label errorLabel;

    private ImageButton eyeButtonPassword;
    private ImageButton eyeButtonConfirm;

    private final float FIELD_WIDTH;
    private final float FIELD_HEIGHT;
    private final float BUTTON_HEIGHT;
    private final float MIN_BUTTON_WIDTH;
    private final float EYE_SIZE;

    private Texture eyeOpenTex;
    private Texture eyeCloseTex;

    public RegisterPanel() {
        setFillParent(true);
        align(Align.center);

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        FIELD_WIDTH = screenW * 0.25f;
        FIELD_HEIGHT = screenH * 0.06f;
        BUTTON_HEIGHT = screenH * 0.08f;
        MIN_BUTTON_WIDTH = screenW * 0.15f;
        EYE_SIZE = screenH * 0.04f;

        eyeOpenTex = new Texture(Gdx.files.internal("global/eyeopen.png"));
        eyeCloseTex = new Texture(Gdx.files.internal("global/eyeclose.png"));

        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        Skin skin = PvzSkin.get();
        BitmapFont bigFont = skin.getFont("FBUSV8C5EI_1_outline");

        // ----- استایل فیلد متنی با رنگ زرد -----
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = bigFont;
        // قبل از ایجاد فیلدها، در بخش استایل:
        fieldStyle.fontColor = new Color(0.8f, 0.6f, 0.0f, 1f);
        fieldStyle.messageFont = fieldStyle.font;   // یا مستقیماً bigFont// زرد تیره (کهربایی)

        // ----- استایل برچسب (سفید، هم‌اندازه) -----
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.font = bigFont;
        labelStyle.fontColor = Color.WHITE;                   // برچسب‌ها سفید

        // ----- استایل کشویی با متن زرد -----
        SelectBox.SelectBoxStyle sbStyle = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));
        sbStyle.font = bigFont;
        sbStyle.fontColor = Color.YELLOW;                     // متن انتخاب‌شده زرد
        List.ListStyle listStyle = new List.ListStyle(skin.get(List.ListStyle.class));
        listStyle.font = bigFont;
        listStyle.fontColorSelected = Color.YELLOW;
        listStyle.fontColorUnselected = Color.YELLOW;         // گزینه‌های لیست زرد
        sbStyle.listStyle = listStyle;

        // ---------- جدول اصلی ----------
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);

        BitmapFont btnFont = bigFont;
        MenuButton title = createButton("REGISTER", () -> {
        }, purpleUp, purpleDown, btnFont, marker);
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

        confirmPasswordField = createField("Confirm Password", fieldStyle);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        eyeButtonConfirm = createEyeButton(confirmPasswordField);
        mainTable.add(createLabel("Confirm:", labelStyle)).right();
        mainTable.add(rowWithEye(confirmPasswordField, eyeButtonConfirm)).left().row();

        nicknameField = createField("Nickname", fieldStyle);
        mainTable.add(createLabel("Nickname:", labelStyle)).right();
        mainTable.add(nicknameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        emailField = createField("Email", fieldStyle);
        mainTable.add(createLabel("Email:", labelStyle)).right();
        mainTable.add(emailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        genderBox = new SelectBox<>(sbStyle);
        genderBox.setItems("Male", "Female");
        mainTable.add(createLabel("Gender:", labelStyle)).right();
        mainTable.add(genderBox).left().row();

        questionBox = new SelectBox<>(sbStyle);
        questionBox.setItems(
            "What is your favorite color?",
            "What is your first pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What is your favorite food?"
        );
        mainTable.add(createLabel("Security Question:", labelStyle)).right();
        mainTable.add(questionBox).width(FIELD_WIDTH).left().row();

        answerField = createField("Answer", fieldStyle);
        mainTable.add(createLabel("Answer:", labelStyle)).right();
        mainTable.add(answerField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        answerConfirmField = createField("Confirm Answer", fieldStyle);
        mainTable.add(createLabel("Confirm:", labelStyle)).right();
        mainTable.add(answerConfirmField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        mainTable.add(errorLabel).colspan(2).width(FIELD_WIDTH).padTop(10).row();

        Table btnRow = new Table();
        MenuButton registerBtn = createButton("Register", this::onRegister, purpleUp, purpleDown, btnFont, marker);
        MenuButton loginBtn = createButton("Login", this::onEnterLogin, purpleUp, purpleDown, btnFont, marker);
        MenuButton quitBtn = createButton("Quit", this::onQuit, purpleUp, purpleDown, btnFont, marker);
        btnRow.add(registerBtn).padRight(40f);
        btnRow.add(loginBtn).padRight(40f);
        btnRow.add(quitBtn);
        mainTable.add(btnRow).colspan(2).padTop(screenH * 0.02f).row();

        add(mainTable);
    }

    private Label createLabel(String text, Label.LabelStyle style) {
        return new Label(text, style);   // رنگ و فونت از استایل می‌آید
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
        TextureRegionDrawable eyeOpen = new TextureRegionDrawable(new TextureRegion(eyeOpenTex));

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

    private void onRegister() {
        errorLabel.setText("");

        RegisterInputDTO dto = new RegisterInputDTO(
            com.PVZ.model.enums.commands.RegisterCommand.REGISTER,
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText(),
            nicknameField.getText(),
            emailField.getText(),
            genderBox.getSelected().toLowerCase(),
            questionBox.getSelectedIndex() + 1,
            answerField.getText(),
            answerConfirmField.getText()
        );

        OutputDTO result = controller.handle(dto);
        if (result != null) {
            if (result.isSuccess()) {
                // کاربر مستقیماً وارد بازی می‌شود
            } else {
                errorLabel.setText(result.getMessage());
            }
        }
    }

    private void onEnterLogin() {
        AppStatus.setCurrentMenuType(MenuType.LOGIN);
    }

    private void onQuit() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {
        if (eyeOpenTex != null) eyeOpenTex.dispose();
        if (eyeCloseTex != null) eyeCloseTex.dispose();
        super.dispose();
    }
}
