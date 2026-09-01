package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.model.leaderboard.LeaderboardEntry;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.List;
import java.util.Map;

public class OnlineLeaderboardPanel extends BasePanel {

    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;

    private final BitmapFont bigFont;
    private final BitmapFont bodyFont;
    private final Texture purpleUp, purpleDown, greenUp, greenDown, marker;
    private final Label.LabelStyle labelStyle;

    private final float BUTTON_HEIGHT;
    private final float SCREEN_H;

    private Label contentLabel;

    public OnlineLeaderboardPanel() {
        setFillParent(true);

        float screenW = Gdx.graphics.getWidth();
        SCREEN_H = Gdx.graphics.getHeight();
        BUTTON_HEIGHT = SCREEN_H * 0.08f;

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

    private void fetchLeaderboard() {
        if (!NetworkSession.isConnected()) {
            setContent("Not connected to server.");return;}
        NetworkMessage request = NetworkMessage.request(MessageType.FETCH_LEADERBOARD);
        NetworkSession.client().sendRequest(request).thenAccept(response -> {
            Gdx.app.postRunnable(() -> {
                if (response.getBoolean("success", false)) {
                    Object rawEntries = response.get("entries");
                    if (rawEntries instanceof List) {
                        List<?> entries = (List<?>) rawEntries;
                        if (entries.isEmpty()) {
                            setContent("No leaderboard data.");return;}
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.format("%-20s %-25s %-10s %-10s %-10s %-10s%n",
                                "Username", "Last Stage", "Minigames", "Daily", "Non-Daily", "Score"));
                        sb.append("-".repeat(90)).append("\n");
                        for (Object obj : entries) {
                            if (obj instanceof LeaderboardEntry entry) {
                                sb.append(String.format("%-20s %-25s %-10d %-10d %-10d %-10d%n",
                                        entry.getUsername(), entry.getLastStageInfo(),
                                        entry.getMinigamesCompleted(), entry.getDailyQuestsCompleted(),
                                        entry.getNonDailyQuestsCompleted(), entry.getHighestScore()));
                            } else if (obj instanceof Map) {
                                Map<?, ?> map = (Map<?, ?>) obj;
                                String username = map.get("username") != null ? map.get("username").toString() : "?";
                                String lastStage = map.get("lastStageInfo") != null ?
                                    map.get("lastStageInfo").toString() : "?";
                                int minigames = map.get("minigamesCompleted") instanceof Number ?
                                    ((Number) map.get("minigamesCompleted")).intValue() : 0;
                                int daily = map.get("dailyQuestsCompleted") instanceof Number ?
                                    ((Number) map.get("dailyQuestsCompleted")).intValue() : 0;
                                int nonDaily = map.get("nonDailyQuestsCompleted") instanceof Number ?
                                    ((Number) map.get("nonDailyQuestsCompleted")).intValue() : 0;
                                int score = map.get("highestScore") instanceof Number ?
                                    ((Number) map.get("highestScore")).intValue() : 0;
                                sb.append(String.format("%-20s %-25s %-10d %-10d %-10d %-10d%n",
                                        username, lastStage, minigames, daily, nonDaily, score));}}
                        setContent(sb.toString());
                    } else {setContent("Unexpected response format.");}
                } else {setContent(response.getString("message",
                    "Failed to load leaderboard."));}});
        }).exceptionally(ex -> {
            Gdx.app.postRunnable(() -> setContent("Connection error: " + ex.getMessage()));
            return null;});
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

    private final float FIELD_WIDTH = Gdx.graphics.getWidth() * 0.25f;
}
