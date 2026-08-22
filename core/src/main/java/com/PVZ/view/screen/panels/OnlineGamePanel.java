package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.io.IOException;

/**
 * پنل بازی آنلاین – انتخاب رقیب (تصادفی یا با یوزرنیم مشخص).
 * شامل رفتارهای کامل: چالش با کاربر مشخص، صف تصادفی با قابلیت انصراف،
 * و نمایش خطاهای مناسب (یوزر موجود نیست / خودتی / آفلاین و غیره).
 */
public class OnlineGamePanel extends BasePanel {

    private TextField usernameField;
    private Label statusLabel;
    private MenuButton randomButton;
    private MenuButton challengeButton;
    private MenuButton backButton;

    private Table invitationPopup;
    private Label invitationLabel;
    private String pendingInviter;

    private boolean inQueue = false;

    private final BitmapFont bigFont;
    private final Texture purpleUp, purpleDown, greenUp, greenDown, marker;
    private final Label.LabelStyle labelStyle;
    private final TextField.TextFieldStyle fieldStyle;

    private final float FIELD_WIDTH;
    private final float BUTTON_HEIGHT;
    private final float SCREEN_H;

    public OnlineGamePanel() {
        setFillParent(true);

        float screenW = Gdx.graphics.getWidth();
        SCREEN_H = Gdx.graphics.getHeight();
        FIELD_WIDTH = screenW * 0.25f;
        BUTTON_HEIGHT = SCREEN_H * 0.08f;

        purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        greenUp    = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        greenDown  = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        marker     = new Texture(Gdx.files.internal("global/button_marker.png"));

        Skin skin = PvzSkin.get();
        bigFont   = skin.getFont("FBUSV8C5EI_1_outline");

        labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        labelStyle.font = bigFont;
        labelStyle.fontColor = Color.WHITE;

        fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = bigFont;
        fieldStyle.fontColor = new Color(0.8f, 0.6f, 0.0f, 1f);
        fieldStyle.messageFont = bigFont;

        buildUi();
        connectToServer();
        registerPushListeners();
    }

    private void buildUi() {
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);
        mainTable.align(Align.center);

        // عنوان
        MenuButton title = createTitleButton("ONLINE GAME");
        mainTable.add(title).padBottom(SCREEN_H * 0.02f).row();

        // دکمه Random Match
        randomButton = createButton("RANDOM MATCH", this::onRandomMatch, greenUp, greenDown);
        mainTable.add(randomButton).padBottom(15f).row();

        // فاصله
        mainTable.add(new Label("", labelStyle)).row();

        // فیلد یوزرنیم
        Label userLabel = new Label("Opponent Username:", labelStyle);
        mainTable.add(userLabel).center().padBottom(6f).row();

        usernameField = createField("Enter username");
        mainTable.add(usernameField).width(FIELD_WIDTH).height(BUTTON_HEIGHT).center().row();

        // دکمه Challenge
        challengeButton = createButton("CHALLENGE", this::onChallengeUser, purpleUp, purpleDown);
        mainTable.add(challengeButton).padTop(10f).padBottom(20f).row();

        // وضعیت / پیام خطا
        statusLabel = new Label("", labelStyle);
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        mainTable.add(statusLabel).width(FIELD_WIDTH * 1.5f).height(80f).padBottom(15f).row();

        // دکمه بازگشت
        backButton = createButton("BACK", this::onBack, purpleUp, purpleDown);
        mainTable.add(backButton).padTop(10f).row();

        // اسکرول برای کل محتوا
        ScrollPane scrollPane = new ScrollPane(mainTable, PvzSkin.get());
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, true);
        addActor(scrollPane);

        // Popup دعوت
        invitationPopup = buildInvitationPopup();
        invitationPopup.setVisible(false);
        addActor(invitationPopup);
    }

    private Table buildInvitationPopup() {
        Table popup = new Table();
        popup.setSize(600f, 260f);
        popup.setPosition((Gdx.graphics.getWidth() - 600f) / 2f,
            (Gdx.graphics.getHeight() - 260f) / 2f);
        popup.setBackground(PvzSkin.get().getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        popup.pad(25f);

        invitationLabel = new Label("", labelStyle);
        invitationLabel.setWrap(true);
        invitationLabel.setAlignment(Align.center);
        popup.add(invitationLabel).colspan(2).width(500f).padBottom(20f).row();

        MenuButton acceptBtn = createButton("ACCEPT", () -> respondToInvitation(true), greenUp, greenDown);
        acceptBtn.setSize(160f, 60f);

        MenuButton declineBtn = createButton("DECLINE", () -> respondToInvitation(false), purpleUp, purpleDown);
        declineBtn.setSize(160f, 60f);

        popup.add(acceptBtn).size(160f, 60f).padRight(30f);
        popup.add(declineBtn).size(160f, 60f);

        popup.setTouchable(Touchable.enabled); // جلوگیری از کلیک پس‌زمینه
        popup.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true; // جلوگیری از بستن تصادفی پاپ‌آپ
            }
        });
        return popup;
    }

    private void connectToServer() {
        try {
            if (!NetworkSession.isConnected()) {
                NetworkSession.connect("localhost");
            }
            setStatus("Connected to server.", Color.GREEN);
        } catch (IOException e) {
            setStatus("Could not connect to server: " + e.getMessage(), Color.SALMON);
            setButtonsEnabled(false);
        }
    }

    private void registerPushListeners() {
        // دعوت‌نامه از طرف کاربر دیگر
        NetworkSession.client().on(MessageType.CHALLENGE_INVITATION, msg -> {
            String from = msg.getString("from");
            Gdx.app.postRunnable(() -> showInvitation(from));
        });

        // شروع بازی وقتی حریف پیدا شد
        NetworkSession.client().on(MessageType.MATCH_FOUND, msg -> {
            Gdx.app.postRunnable(() -> onMatchFound(msg));
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        randomButton.setDisabled(!enabled);
        challengeButton.setDisabled(!enabled);
        usernameField.setDisabled(!enabled);
        backButton.setDisabled(!enabled);
    }

    /**
     * در حالت صف فقط دکمه‌ی Random (که به Cancel تبدیل شده) فعال است؛
     * بقیه‌ی دکمه‌ها و فیلد یوزرنیم غیرفعال می‌شوند.
     */
    private void setButtonsForQueue(boolean queueActive) {
        randomButton.setDisabled(false); // همیشه فعال: یا RANDOM MATCH یا CANCEL
        challengeButton.setDisabled(queueActive);
        usernameField.setDisabled(queueActive);
        backButton.setDisabled(queueActive);
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setColor(color);
    }

    private void setStatus(String text) {
        setStatus(text, Color.WHITE);
    }

    // ======================== اکشن‌ها ========================

    private void onRandomMatch() {
        if (!NetworkSession.isConnected()) {
            setStatus("Not connected to server.", Color.SALMON);
            return;
        }

        if (inQueue) {
            // خروج از صف
            NetworkSession.client().sendFireAndForget(
                NetworkMessage.push(MessageType.LEAVE_RANDOM_QUEUE));
            inQueue = false;
            setButtonsForQueue(false);
            randomButton.setText("RANDOM MATCH");
            setStatus("Left random queue.", Color.WHITE);
            return;
        }

        // ورود به صف
        inQueue = true;
        setButtonsForQueue(true);
        randomButton.setText("CANCEL");
        setStatus("Searching for an opponent...", Color.GOLD);

        NetworkMessage request = NetworkMessage.request(MessageType.JOIN_RANDOM_QUEUE);
        NetworkSession.client().sendRequest(request).thenAccept(response -> {
            Gdx.app.postRunnable(() -> {
                if (response.getBoolean("success", true)) {
                    setStatus("Waiting in random queue...", Color.GOLD);
                } else {
                    setStatus(response.getString("message", "Failed to join queue."), Color.SALMON);
                    inQueue = false;
                    setButtonsForQueue(false);
                    randomButton.setText("RANDOM MATCH");
                }
            });
        }).exceptionally(ex -> {
            Gdx.app.postRunnable(() -> {
                setStatus("Connection error: " + ex.getMessage(), Color.SALMON);
                inQueue = false;
                setButtonsForQueue(false);
                randomButton.setText("RANDOM MATCH");
            });
            return null;
        });
    }

    private void onChallengeUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            setStatus("Please enter a username.", Color.SALMON);
            return;
        }
        if (!NetworkSession.isConnected()) {
            setStatus("Not connected to server.", Color.SALMON);
            return;
        }

        setStatus("Sending challenge...", Color.GOLD);

        NetworkMessage request = NetworkMessage.request(MessageType.CHALLENGE_USER)
            .with("username", username);

        NetworkSession.client().sendRequest(request).thenAccept(response -> {
            Gdx.app.postRunnable(() -> {
                if (response.getBoolean("success", false)) {
                    setStatus("Invitation sent to " + username + ".", Color.GREEN);
                } else {
                    setStatus(response.getString("message", "Challenge failed."), Color.SALMON);
                }
            });
        }).exceptionally(ex -> {
            Gdx.app.postRunnable(() ->
                setStatus("Connection error: " + ex.getMessage(), Color.SALMON));
            return null;
        });
    }

    private void onBack() {
        if (inQueue) {
            // اگر در صف است، فقط پیام بده و اجازه‌ی خروج نده
            setStatus("Please cancel the queue first.", Color.GOLD);
            return;
        }
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    // ======================== پاپ‌آپ دعوت ========================

    private void showInvitation(String from) {
        pendingInviter = from;
        invitationLabel.setText(from + " has invited you to play!");
        invitationPopup.setVisible(true);
        invitationPopup.toFront();
    }

    private void respondToInvitation(boolean accept) {
        if (pendingInviter == null) {
            return;
        }

        NetworkMessage response = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE)
            .with("accept", accept)
            .with("inviter", pendingInviter);

        NetworkSession.client().sendRequest(response).thenAccept(resp -> {
            Gdx.app.postRunnable(() -> {
                invitationPopup.setVisible(false);
                pendingInviter = null;
                if (resp.getBoolean("success", false)) {
                    setStatus("Invitation accepted. Waiting for game start...", Color.GREEN);
                } else {
                    setStatus(resp.getString("message", "Invitation declined."), Color.SALMON);
                }
            });
        }).exceptionally(ex -> {
            Gdx.app.postRunnable(() -> {
                invitationPopup.setVisible(false);
                pendingInviter = null;
                setStatus("Connection error: " + ex.getMessage(), Color.SALMON);
            });
            return null;
        });
    }

    private void onMatchFound(NetworkMessage msg) {
        // TODO: بعداً به بازی آنلاین منتقل شوید
        setStatus("Match found! Starting game...", Color.GREEN);
        // اینجا می‌توانید GameScreen آنلاین را راه‌اندازی کنید
        // برای مثال: AppStatus.setGameEngine(...);
    }

    // ======================== متدهای کمکی ساخت UI ========================

    private MenuButton createTitleButton(String text) {
        MenuButton btn = new MenuButton(purpleUp, text, bigFont, purpleDown, null, marker, () -> {});
        btn.setDisabled(true);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 250f), BUTTON_HEIGHT * 1.2f);
        return btn;
    }

    private MenuButton createButton(String text, Runnable action, Texture up, Texture down) {
        MenuButton btn = new MenuButton(up, text, bigFont, down, null, marker, action);
        btn.setSize(Math.max(btn.getTextWidth() + 60f, 200f), BUTTON_HEIGHT);
        return btn;
    }

    private TextField createField(String placeholder) {
        TextField f = new TextField("", fieldStyle);
        f.setMessageText(placeholder);
        return f;
    }

    @Override
    public void dispose() {
        if (inQueue) {
            NetworkSession.client().sendFireAndForget(
                NetworkMessage.push(MessageType.LEAVE_RANDOM_QUEUE));
        }
        super.dispose();
    }
}
