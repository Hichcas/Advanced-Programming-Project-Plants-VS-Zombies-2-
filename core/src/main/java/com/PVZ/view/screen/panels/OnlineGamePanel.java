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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.io.IOException;

public class OnlineGamePanel extends BasePanel {

    private TextField usernameField;
    private Label statusLabel;
    private MenuButton randomButton;
    private MenuButton roleButton;
    private MenuButton levelButton;
    private MenuButton challengeButton;
    private MenuButton leaderboardButton;
    private MenuButton backButton;

    private String selectedRole = "PLANT";
    private int selectedLevel = 1;
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
    }

    private void buildUi() {
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);
        mainTable.align(Align.center);

        MenuButton title = createTitleButton("ONLINE GAME");
        mainTable.add(title).padBottom(SCREEN_H * 0.02f).row();

        randomButton = createButton("RANDOM MATCH", this::onRandomMatch, greenUp, greenDown);
        mainTable.add(randomButton).padBottom(15f).row();

        Label userLabel = new Label("Opponent Username:", labelStyle);
        mainTable.add(userLabel).center().padBottom(6f).row();

        usernameField = createField("Enter username");
        mainTable.add(usernameField).width(FIELD_WIDTH).height(BUTTON_HEIGHT).center().row();

        roleButton = createButton("PLAY AS: " + selectedRole, this::onToggleRole, greenUp, greenDown);
        mainTable.add(roleButton).padTop(10f).padBottom(5f).row();

        levelButton = createButton("LEVEL: " + selectedLevel, this::onToggleLevel, purpleUp, purpleDown);
        mainTable.add(levelButton).padTop(5f).padBottom(5f).row();

        challengeButton = createButton("CHALLENGE", this::onChallengeUser, purpleUp, purpleDown);
        mainTable.add(challengeButton).padTop(5f).padBottom(15f).row();

        leaderboardButton = createButton("LEADERBOARD", this::onLeaderboard, greenUp, greenDown);
        mainTable.add(leaderboardButton).padTop(5f).padBottom(20f).row();

        statusLabel = new Label("", labelStyle);
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        mainTable.add(statusLabel).width(FIELD_WIDTH * 1.5f).height(80f).padBottom(15f).row();

        backButton = createButton("BACK", this::onBack, purpleUp, purpleDown);
        mainTable.add(backButton).padTop(10f).row();

        ScrollPane scrollPane = new ScrollPane(mainTable, PvzSkin.get());
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, true);
        addActor(scrollPane);
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


    private void setButtonsEnabled(boolean enabled) {
        randomButton.setDisabled(!enabled);
        roleButton.setDisabled(!enabled);
        levelButton.setDisabled(!enabled);
        challengeButton.setDisabled(!enabled);
        leaderboardButton.setDisabled(!enabled);
        usernameField.setDisabled(!enabled);
        backButton.setDisabled(!enabled);
    }

    private void setButtonsForQueue(boolean queueActive) {
        randomButton.setDisabled(false);
        roleButton.setDisabled(queueActive);
        levelButton.setDisabled(queueActive);
        challengeButton.setDisabled(queueActive);
        leaderboardButton.setDisabled(queueActive);
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

    private void onRandomMatch() {
        if (!NetworkSession.isConnected()) {
            setStatus("Not connected to server.", Color.SALMON);
            return;
        }

        if (inQueue) {
            NetworkSession.client().sendFireAndForget(
                NetworkMessage.push(MessageType.LEAVE_RANDOM_QUEUE));
            inQueue = false;
            setButtonsForQueue(false);
            randomButton.setText("RANDOM MATCH");
            setStatus("Left random queue.", Color.WHITE);
            return;
        }

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

    private void onToggleRole() {
        selectedRole = selectedRole.equals("PLANT") ? "ZOMBIE" : "PLANT";
        roleButton.setText("PLAY AS: " + selectedRole);
    }

    private void onToggleLevel() {
        selectedLevel = (selectedLevel % 3) + 1;
        levelButton.setText("LEVEL: " + selectedLevel);
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
            .with("username", username)
            .with("role", selectedRole)
            .with("levelId", selectedLevel);

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

    private void onLeaderboard() {
        if (!NetworkSession.isConnected()) {
            setStatus("Not connected to server.", Color.SALMON);
            return;
        }
        AppStatus.setCurrentMenuType(MenuType.ONLINE_LEADERBOARD);
    }

    private void onBack() {
        if (inQueue) {
            setStatus("Please cancel the queue first.", Color.GOLD);
            return;
        }
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    private void onMatchFound(NetworkMessage msg) {
        setStatus("Match found! Directing to selection...", Color.GREEN);
        String opponent = msg.getString("opponent", "Opponent");
        String roomId = msg.getString("roomId", "");
        String role = msg.getString("role", "ZOMBIE");
        int levelId = msg.getInt("levelId", 1);

        inQueue = false;
        setButtonsForQueue(false);
        randomButton.setText("RANDOM MATCH");

        AppStatus.isMultiplayerMatch = true;
        AppStatus.multiplayerRole = role;
        AppStatus.multiplayerOpponent = opponent;
        AppStatus.multiplayerRoomId = roomId;
        AppStatus.multiplayerLevelId = levelId;
        AppStatus.pendingIZombieLevelId = levelId;

        if ("PLANT".equalsIgnoreCase(role)) {
            AppStatus.currentChapterName = "Frontyard";
            AppStatus.currentStageNumber = 1;
            AppStatus.SELECTED_PLANTS.clear();
            AppStatus.setCurrentMenuType(MenuType.PLANT_SELECTION);
        } else {
            AppStatus.SELECTED_ZOMBIES.clear();
            AppStatus.setCurrentMenuType(MenuType.I_ZOMBIE_SELECTION);
        }
    }

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
