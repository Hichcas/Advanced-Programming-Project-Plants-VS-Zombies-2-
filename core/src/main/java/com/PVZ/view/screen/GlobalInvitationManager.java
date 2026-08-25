package com.PVZ.view.screen;

import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.game.IZombieMultiplayerGameEngine;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class GlobalInvitationManager {

    private static GlobalInvitationManager instance;
    private Stage stage;
    private Table overlay;
    private String pendingInviter;

    private GlobalInvitationManager() {}

    public static GlobalInvitationManager getInstance() {
        if (instance == null) instance = new GlobalInvitationManager();
        return instance;
    }

    public void install() {
        NetworkSession.client().on(MessageType.CHALLENGE_INVITATION, msg -> {
            final String from = msg.getString("from");
            final String targetRole = msg.getString("targetRole", "ZOMBIE");
            final int levelId = msg.getInt("levelId", 1);
            Gdx.app.postRunnable(() -> showInvitation(from, targetRole, levelId));
        });

        NetworkSession.client().on(MessageType.MATCH_FOUND, msg -> {
            Gdx.app.postRunnable(() -> {
                hideInvitation();
                String opponent = msg.getString("opponent", "Opponent");
                String roomId = msg.getString("roomId", "");
                String role = msg.getString("role", "ZOMBIE");
                int levelId = msg.getInt("levelId", 1);

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
            });
        });

        NetworkSession.client().on(MessageType.GAME_START_SYNC, msg -> {
            Gdx.app.postRunnable(() -> {
                long startAt = msg.get("startAt") instanceof Number
                    ? ((Number) msg.get("startAt")).longValue()
                    : System.currentTimeMillis() + 3000L;

                Stage currentStage = this.stage;
                if (currentStage != null) {
                    CountdownOverlay.show(currentStage, startAt, this::startMultiplayerGame);
                }
            });
        });
    }

    public void setStage(Stage newStage) {
        this.stage = newStage;
        if (overlay != null && overlay.getStage() != null) {
            overlay.remove();
            overlay = null;
        }
    }

    private void startMultiplayerGame() {
        String role = AppStatus.multiplayerRole;
        int levelId = AppStatus.multiplayerLevelId;
        List<PlantType> selectedPlants = new ArrayList<>(AppStatus.SELECTED_PLANTS);
        List<String> selectedZombies = new ArrayList<>(AppStatus.SELECTED_ZOMBIES);

        IZombieMultiplayerGameEngine engine = new IZombieMultiplayerGameEngine(
            role,
            AppStatus.multiplayerOpponent,
            AppStatus.multiplayerRoomId,
            levelId,
            selectedPlants,
            selectedZombies
        );

        AppStatus.setGameEngine(engine);
        AppStatus.setCurrentMenuType(MenuType.IN_GAME);
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            "maps/Frontyard.jpg",
            "music/TitleScreen.mp3",
            engine
        ));
    }

    public void showInvitation(String from) { showInvitation(from, "ZOMBIE", 1); }
    public void showInvitation(String from, String targetRole) { showInvitation(from, targetRole, 1); }

    public void showInvitation(String from, String targetRole, int levelId) {
        if (stage == null) return;
        hideInvitation();
        pendingInviter = from;

        Skin skin = PvzSkin.get();
        BitmapFont font = skin.getFont("FBUSV8C5EI_1_outline");
        Drawable purpleUp = skin.getDrawable("image_ui_generic_purplebutton_10");
        Drawable purpleDown = skin.getDrawable("image_ui_generic_purplebutton_down_10");
        Drawable greenUp = skin.getDrawable("image_ui_generic_greenbutton_10");
        Drawable greenDown = skin.getDrawable("image_ui_generic_greenbutton_down_10");

        overlay = new Table();
        overlay.setSize(620f, 280f);
        overlay.setPosition(
            (BaseScreen.VIRTUAL_WIDTH - 620f) / 2f,
            (BaseScreen.VIRTUAL_HEIGHT - 280f) / 2f
        );
        overlay.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        overlay.pad(25f);
        overlay.setTouchable(Touchable.enabled);

        String prompt = from + " invited you to play!\nRole: " + targetRole + " | Level: " + levelId;
        Label invitationLabel = new Label(prompt, new Label.LabelStyle(font, Color.WHITE));
        invitationLabel.setWrap(true);
        invitationLabel.setAlignment(Align.center);
        overlay.add(invitationLabel).colspan(2).width(500f).padBottom(20f).row();

        MenuButton acceptBtn = new MenuButton(greenUp, "ACCEPT", font, greenDown, null, null,
            () -> respondToInvitation(true));
        acceptBtn.setSize(160f, 60f);

        MenuButton declineBtn = new MenuButton(purpleUp, "DECLINE", font, purpleDown, null, null,
            () -> respondToInvitation(false));
        declineBtn.setSize(160f, 60f);

        overlay.add(acceptBtn).size(160f, 60f).padRight(30f);
        overlay.add(declineBtn).size(160f, 60f);

        stage.addActor(overlay);
        overlay.toFront();
    }

    public void hideInvitation() {
        if (overlay != null) {
            overlay.remove();
            overlay = null;
        }
    }

    private void respondToInvitation(boolean accept) {
        if (pendingInviter == null) return;
        NetworkMessage response = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE)
            .with("accept", accept)
            .with("inviter", pendingInviter);
        NetworkSession.client().sendRequest(response).thenAccept(resp -> {
            Gdx.app.postRunnable(() -> {
                hideInvitation();
                pendingInviter = null;
            });
        }).exceptionally(ex -> {
            Gdx.app.postRunnable(() -> {
                hideInvitation();
                pendingInviter = null;
            });
            return null;
        });
    }
}
