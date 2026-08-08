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

    // ---------- اجزای مرحله ۱ ----------
    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private TextField nicknameField;
    private TextField emailField;
    private SelectBox<String> genderBox;
    private Label errorLabel;

    private ImageButton eyeButtonPassword;
    private ImageButton eyeButtonConfirm;

    // ---------- اجزای مرحله ۲ ----------
    private SelectBox<String> questionBox;
    private TextField answerField;
    private TextField answerConfirmField;
    private Label errorLabel2;

    private Table step1Table;
    private Table step2Table;
    private boolean step2Active = false;

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

        // ======================= ساخت جداول =======================
        step1Table = new Table();
        step1Table.defaults().pad(8f);

        step2Table = new Table();
        step2Table.defaults().pad(8f);
        step2Table.setVisible(false); // مخفی در ابتدا

        // ---------- پر کردن مرحله ۱ ----------
        MenuButton title1 = createButton("REGISTER", () -> {}, purpleUp, purpleDown, btnFont, marker);
        title1.setDisabled(true);
        step1Table.add(title1).colspan(2).padBottom(20).row();

        usernameField = createField("Username", fieldStyle);
        step1Table.add(label("Username:")).right();
        step1Table.add(usernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        // ردیف رمز با دکمه چشم
        passwordField = createField("Password", fieldStyle);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        eyeButtonPassword = createEyeButton(passwordField);
        step1Table.add(label("Password:")).right();
        step1Table.add(rowWithEye(passwordField, eyeButtonPassword)).left().row();

        // ردیف تأیید رمز با دکمه چشم
        confirmPasswordField = createField("Confirm Password", fieldStyle);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        eyeButtonConfirm = createEyeButton(confirmPasswordField);
        step1Table.add(label("Confirm:")).right();
        step1Table.add(rowWithEye(confirmPasswordField, eyeButtonConfirm)).left().row();

        nicknameField = createField("Nickname", fieldStyle);
        step1Table.add(label("Nickname:")).right();
        step1Table.add(nicknameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        emailField = createField("Email", fieldStyle);
        step1Table.add(label("Email:")).right();
        step1Table.add(emailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        genderBox = new SelectBox<>(skin);
        genderBox.setItems("Male", "Female");
        step1Table.add(label("Gender:")).right();
        step1Table.add(genderBox).left().row();

        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setAlignment(Align.center);
        step1Table.add(errorLabel).colspan(2).width(FIELD_WIDTH).padTop(10).row();

        // دکمه‌های مرحله ۱
        Table btnRow1 = new Table();
        btnRow1.add(createButton("Register", this::onRegister, purpleUp, purpleDown, btnFont, marker)).padRight(20);
        btnRow1.add(createButton("Login", this::onEnterLogin, purpleUp, purpleDown, btnFont, marker)).padRight(20);
        btnRow1.add(createButton("Quit", this::onQuit, purpleUp, purpleDown, btnFont, marker));
        step1Table.add(btnRow1).colspan(2).padTop(20).row();

        // ---------- پر کردن مرحله ۲ ----------
        MenuButton title2 = createButton("SECURITY QUESTION", () -> {}, purpleUp, purpleDown, btnFont, marker);
        title2.setDisabled(true);
        step2Table.add(title2).colspan(2).padBottom(20).row();

        questionBox = new SelectBox<>(skin);
        questionBox.setItems(
            "What is your favorite color?",
            "What is your first pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What is your favorite food?"
        );
        step2Table.add(label("Question:")).right();
        step2Table.add(questionBox).width(FIELD_WIDTH).left().row();

        answerField = createField("Answer", fieldStyle);
        step2Table.add(label("Answer:")).right();
        step2Table.add(answerField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        answerConfirmField = createField("Confirm Answer", fieldStyle);
        step2Table.add(label("Confirm:")).right();
        step2Table.add(answerConfirmField).width(FIELD_WIDTH).height(FIELD_HEIGHT).left().row();

        errorLabel2 = new Label("", skin);
        errorLabel2.setColor(Color.RED);
        errorLabel2.setAlignment(Align.center);
        step2Table.add(errorLabel2).colspan(2).width(FIELD_WIDTH).padTop(10).row();

        MenuButton confirmBtn = createButton("Complete Registration", this::onPickQuestion, purpleUp, purpleDown, btnFont, marker);
        step2Table.add(confirmBtn).colspan(2).padTop(20).row();

        // افزودن هر دو جدول به پنل (اول stage1، سپس stage2)
        add(step1Table);
        add(step2Table);
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

    /** ردیف حاوی فیلد و دکمه چشم */
    private Table rowWithEye(TextField field, ImageButton eye) {
        Table row = new Table();
        row.add(field).width(FIELD_WIDTH - EYE_SIZE - 10).height(FIELD_HEIGHT);
        row.add(eye).size(EYE_SIZE).padLeft(10);
        return row;
    }

    /** ساخت دکمه چشم برای یک فیلد مشخص */
    private ImageButton createEyeButton(TextField targetField) {
        TextureRegionDrawable eyeClose = new TextureRegionDrawable(new TextureRegion(eyeCloseTex));
        TextureRegionDrawable eyeOpen = new TextureRegionDrawable(new TextureRegion(eyeOpenTex));

        ImageButton button = new ImageButton(eyeClose); // حالت اولیه: چشم بسته
        button.getStyle().imageChecked = eyeOpen;       // حالت انتخاب‌شده: چشم باز

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

        RegisterInputDTO dto = new RegisterInputDTO(
            com.PVZ.model.enums.commands.RegisterCommand.REGISTER,
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText(),
            nicknameField.getText(),
            emailField.getText(),
            genderBox.getSelected().toLowerCase(),
            null, null, null
        );

        OutputDTO result = controller.handle(dto);
        processResult(result);
    }

    private void onPickQuestion() {
        errorLabel2.setText("");
        int qIndex = questionBox.getSelectedIndex() + 1;

        RegisterInputDTO dto = new RegisterInputDTO(
            com.PVZ.model.enums.commands.RegisterCommand.PICK_QUESTION,
            null, null, null, null, null, null,
            qIndex,
            answerField.getText(),
            answerConfirmField.getText()
        );

        OutputDTO result = controller.handle(dto);
        processResult(result);
    }

    private void processResult(OutputDTO result) {
        if (result == null) return;

        if (result.isSuccess()) {
            String msg = result.getMessage();
            if (msg != null && msg.contains("Choose your security question")) {
                // فعال‌سازی مرحله ۲
                step1Table.setVisible(false);
                step2Table.setVisible(true);
                step2Active = true;
            } else {
                // ثبت‌نام کامل شده؛ رفتن به صفحه ورود (در صورت وجود LoginPanel)
                // AppStatus.currentMenuType = MenuType.LOGIN;
                // PanelManager.getInstance().performPanelTransition(new LoginPanel());
            }
        } else {
            String error = result.getMessage();
            if (step2Active) {
                errorLabel2.setText(error);
            } else {
                errorLabel.setText(error);
            }
        }
    }

    private void onEnterLogin() {
        // رفتن به صفحه ورود (هنوز LoginPanel ساخته نشده، می‌توانید بعداً فعال کنید)
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
