package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.leaderboard.Leaderboard;
import com.PVZ.model.leaderboard.LeaderboardEntry;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OnlineLeaderboardPanel extends BasePanel {

    private static final float BUTTON_HEIGHT_RATIO = 0.08f;

    private final BitmapFont bigFont;
    private final BitmapFont bodyFont;
    private final Texture purpleUp, purpleDown, greenUp, greenDown, marker;
    private final Label.LabelStyle labelStyle;

    private final float BUTTON_HEIGHT;
    private final float SCREEN_H;
    private final float FIELD_WIDTH;

    private Label contentLabel;
    private int fetchGeneration;

    public OnlineLeaderboardPanel() {
        setFillParent(true);

        SCREEN_H = Gdx.graphics.getHeight();
        BUTTON_HEIGHT = SCREEN_H * BUTTON_HEIGHT_RATIO;
        FIELD_WIDTH = Gdx.graphics.getWidth() * 0.25f;

        purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        greenUp = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        greenDown = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        Skin skin = PvzSkin.get();
        bigFont = skin.getFont("FBUSV8C5EI_1_outline");
        bodyFont = skin.getFont("FBUSV8C5EI_2");

        labelStyle = new Label.LabelStyle(bodyFont, Color.WHITE);

        buildUi();
        fetchLeaderboard();
    }

    private void buildUi() {
        Table mainTable = new Table();
        mainTable.defaults().pad(8f);
        mainTable.align(Align.center);

        MenuButton title = createTitleButton("ONLINE LEADERBOARD");
        mainTable.add(title).padBottom(SCREEN_H * 0.02f).row();

        contentLabel = new Label("Loading...", labelStyle);
        contentLabel.setAlignment(Align.center);
        contentLabel.setWrap(true);
        mainTable.add(contentLabel).width(FIELD_WIDTH * 1.5f).height(SCREEN_H * 0.6f).padBottom(20f).row();

        MenuButton refreshButton = createButton("REFRESH", this::onRefresh, greenUp, greenDown);
        mainTable.add(refreshButton).padBottom(15f).row();

        MenuButton backButton = createButton("BACK", this::onBack, purpleUp, purpleDown);
        mainTable.add(backButton).padTop(10f).row();

        ScrollPane scrollPane = new ScrollPane(mainTable, PvzSkin.get());
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, true);
        addActor(scrollPane);
    }

    private CompletableFuture<NetworkMessage> syncCurrentUserIfNeeded() {
        if (NetworkSession.isSessionAuthenticated() && AppStatus.currentUser != null) {
            return NetworkSession.client().sendRequest(
                NetworkMessage.request(MessageType.SYNC_USER).with("user", AppStatus.currentUser));
        }
        return CompletableFuture.completedFuture(null);
    }

    private void fetchLeaderboard() {
        int generation = ++fetchGeneration;
        if (!NetworkSession.isConnected()) {
            setContent("Not connected to server.");
            return;
        }
        setContent("Loading...");
        syncCurrentUserIfNeeded()
            .exceptionally(ex -> null)
            .thenCompose(ignored -> NetworkSession.client().sendRequest(
                NetworkMessage.request(MessageType.FETCH_LEADERBOARD)
                    .with("sort", "HIGHEST_SCORE")
                    .with("ascending", false)))
            .thenAccept(response -> Gdx.app.postRunnable(() -> {
                if (generation != fetchGeneration) {
                    return;
                }
                if (response.getBoolean("success", false)) {
                    renderEntries(Leaderboard.parseEntries(response.get("entries")));
                } else {
                    setContent(response.getString("message", "Failed to load leaderboard."));
                }
            }))
            .exceptionally(ex -> {
                Gdx.app.postRunnable(() -> {
                    if (generation == fetchGeneration) {
                        setContent("Connection error: " + ex.getMessage());
                    }
                });
                return null;
            });
    }

    private void renderEntries(List<LeaderboardEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            setContent("No leaderboard data.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-25s %-10s %-10s %-10s %-10s%n",
            "Username", "Last Stage", "Minigames", "Daily", "Non-Daily", "MyoPoint"));
        sb.append("-".repeat(90)).append("\n");
        for (LeaderboardEntry entry : entries) {
            sb.append(String.format("%-20s %-25s %-10d %-10d %-10d %-10d%n",
                entry.getUsername(), entry.getLastStageInfo(),
                entry.getMinigamesCompleted(), entry.getDailyQuestsCompleted(),
                entry.getNonDailyQuestsCompleted(), entry.getHighestScore()));
        }
        setContent(sb.toString());
    }

    private void setContent(String text) {
        contentLabel.setText(text);
    }

    private void onRefresh() {
        fetchLeaderboard();
    }

    private void onBack() {
        AppStatus.setCurrentMenuType(MenuType.NETWORK);
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
}
