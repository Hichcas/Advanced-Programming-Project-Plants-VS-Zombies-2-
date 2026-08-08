package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.RegisterMenuController;
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

    // ---------- فیلدها ----------
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

    // ---------- ثابت‌های قابل تنظیم ----------
    private static final float BUTTON_HEIGHT = 80f;
    private static final float HORIZONTAL_PADDING = 40f;
    private static final float FIELD_WIDTH = 500f;
    private static final float FIELD_HEIGHT = 50f;
    private static final float EYE_SIZE = 40f;

    private Texture eyeOpenTex;
    private Texture eyeCloseTex;

    public RegisterPanel() {
        setFillParent(true);
        align(Align.center);

        // بارگذاری تصاویر
        eyeOpenTex = new Texture(Gdx.files.internal("global/eyeopen.png"));
        eyeCloseTex = new Texture(Gdx.files.internal("global/eyeclose.png"));

        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));
        BitmapFont btnFont = PvzSkin.get().getFont("FBUSV8C5EI_1_outline");

        Skin skin = PvzSkin.get();
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = skin.getFont("FBUSV8C5EI_2"); // فونت بزرگ‌تر

        // ---------- جدول اصلی ----------
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);

        // عنوان
        MenuButton title = createButton("REGISTER", () -> {
        }, purpleUp, purpleDown, btnFont, marker);
        title.setDisabled(true);
        mainTable.add(title).colspan(2).padBottom(20).row();

        // نام کاربری
        usernameField = createField("Username", fieldStyle);
        mainTable.add(label("Username:")).right();
        mainTable.add(usernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // رمز عبور + چشم
        passwordField = createField("Password", fieldStyle);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        eyeButtonPassword = createEyeButton(passwordField);
        mainTable.add(label("Password:")).right();
        mainTable.add(rowWithEye(passwordField, eyeButtonPassword)).left().row();

        // تأیید رمز + چشم
        confirmPasswordField = createField("Confirm Password", fieldStyle);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        eyeButtonConfirm = createEyeButton(confirmPasswordField);
        mainTable.add(label("Confirm:")).right();
        mainTable.add(rowWithEye(confirmPasswordField, eyeButtonConfirm)).left().row();

        // نام مستعار
        nicknameField = createField("Nickname", fieldStyle);
        mainTable.add(label("Nickname:")).right();
        mainTable.add(nicknameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // ایمیل
        emailField = createField("Email", fieldStyle);
        mainTable.add(label("Email:")).right();
        mainTable.add(emailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // جنسیت
        genderBox = new SelectBox<>(skin);
        genderBox.setItems("Male", "Female");
        mainTable.add(label("Gender:")).right();
        mainTable.add(genderBox).left().row();

        // سؤال امنیتی
        questionBox = new SelectBox<>(skin);
        questionBox.setItems(
            "What is your favorite color?",
            "What is your first pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What is your favorite food?"
        );
        mainTable.add(label("Security Question:")).right();
        mainTable.add(questionBox).width(FIELD_WIDTH).left().row();

        // پاسخ
        answerField = createField("Answer", fieldStyle);
        mainTable.add(label("Answer:")).right();
        mainTable.add(answerField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // تأیید پاسخ
        answerConfirmField = createField("Confirm Answer", fieldStyle);
        mainTable.add(label("Confirm:")).right();
        mainTable.add(answerConfirmField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // خطا
        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        mainTable.add(errorLabel).colspan(2).width(FIELD_WIDTH).padTop(10).row();

        // دکمه‌ها
        Table btnRow = new Table();
        btnRow.add(createButton("Register", this::onRegister, purpleUp, purpleDown, btnFont, marker)).padRight(20);
        btnRow.add(createButton("Login", this::onEnterLogin, purpleUp, purpleDown, btnFont, marker)).padRight(20);
        btnRow.add(createButton("Quit", this::onQuit, purpleUp, purpleDown, btnFont, marker));
        mainTable.add(btnRow).colspan(2).padTop(20).row();

        add(mainTable);
    }

    // ---------------------- متدهای کمکی ----------------------
    private Label label(String text) {
        Skin skin = PvzSkin.get();
        Label l = new Label(text, skin);
        l.setColor(Color.WHITE);
        return l;
    }

    private MenuButton createButton(String text, Runnable action, Texture up, Texture down, BitmapFont font, Texture marker) {
        MenuButton btn = new MenuButton(up, text, font, down, null, marker, action);
        btn.setSize(btn.getTextWidth() + HORIZONTAL_PADDING * 2, BUTTON_HEIGHT);
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

    // ---------------------- رویدادها ----------------------
    private void onRegister() {
        errorLabel.setText("");

        // ساخت DTO با تمام اطلاعات، از جمله سؤال امنیتی
        RegisterInputDTO dto = new RegisterInputDTO(
            com.PVZ.model.enums.commands.RegisterCommand.REGISTER,
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText(),
            nicknameField.getText(),
            emailField.getText(),
            genderBox.getSelected().toLowerCase(),
            questionBox.getSelectedIndex() + 1,   // شماره سوال (۱ تا ۵)
            answerField.getText(),
            answerConfirmField.getText()
        );

        OutputDTO result = controller.handle(dto);
        if (result != null) {
            if (!result.isSuccess()) {
                errorLabel.setText(result.getMessage());

            }
        }
    }

    private void onEnterLogin() {
        // رفتن به صفحه ورود (در صورت وجود LoginPanel)
        // AppStatus.currentMenuType = MenuType.LOGIN;
        // PanelManager.getInstance().performPanelTransition(new LoginPanel());
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
