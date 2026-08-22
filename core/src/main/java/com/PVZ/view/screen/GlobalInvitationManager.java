package com.PVZ.view.screen;

import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

/**
 * نمایش سراسری دعوت‌نامه‌ی بازی آنلاین.
 *
 * یک‌بار در ابتدای برنامه install() صدا زده می‌شود تا Listener روی
 * پیام‌های CHALLENGE_INVITATION ثبت شود. سپس هر Screen جدید باید
 * setStage(stage) را صدا بزند تا popup روی همان Stage نمایش داده شود.
 */
public class GlobalInvitationManager {

    private static GlobalInvitationManager instance;

    private Stage stage;
    private Table overlay;
    private String pendingInviter;

    private GlobalInvitationManager() {
    }

    public static GlobalInvitationManager getInstance() {
        if (instance == null) {
            instance = new GlobalInvitationManager();
        }
        return instance;
    }

    /** باید فقط یک‌بار در ابتدای برنامه صدا زده شود. */
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

                com.PVZ.model.status.AppStatus.isMultiplayerMatch = true;
                com.PVZ.model.status.AppStatus.multiplayerRole = role;
                com.PVZ.model.status.AppStatus.multiplayerOpponent = opponent;
                com.PVZ.model.status.AppStatus.multiplayerRoomId = roomId;
                com.PVZ.model.status.AppStatus.multiplayerLevelId = levelId;
                com.PVZ.model.status.AppStatus.pendingIZombieLevelId = levelId;

                if ("PLANT".equalsIgnoreCase(role)) {
                    com.PVZ.model.status.AppStatus.currentChapterName = "Frontyard";
                    com.PVZ.model.status.AppStatus.currentStageNumber = 1;
                    com.PVZ.model.status.AppStatus.SELECTED_PLANTS.clear();
                    com.PVZ.model.status.AppStatus.setCurrentMenuType(com.PVZ.model.enums.MenuType.PLANT_SELECTION);
                } else {
                    com.PVZ.model.status.AppStatus.SELECTED_ZOMBIES.clear();
                    com.PVZ.model.status.AppStatus.setCurrentMenuType(com.PVZ.model.enums.MenuType.I_ZOMBIE_SELECTION);
                }
            });
        });
    }

    /** در show() هر BaseScreen صدا زده می‌شود تا Stage جاری را بشناسیم. */
    public void setStage(Stage newStage) {
        this.stage = newStage;
        if (overlay != null && overlay.getStage() != null) {
            overlay.remove();
            overlay = null;
        }
    }

    /** نمایش popup دعوت روی Stage فعلی. */
    public void showInvitation(String from) {
        showInvitation(from, "ZOMBIE", 1);
    }

    public void showInvitation(String from, String targetRole) {
        showInvitation(from, targetRole, 1);
    }

    public void showInvitation(String from, String targetRole, int levelId) {
        if (stage == null) {
            return;
        }
        hideInvitation();
        pendingInviter = from;

        com.badlogic.gdx.scenes.scene2d.ui.Skin skin = PvzSkin.get();
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
        Label invitationLabel = new Label(
            prompt,
            new Label.LabelStyle(font, Color.WHITE)
        );
        invitationLabel.setWrap(true);
        invitationLabel.setAlignment(Align.center);
        overlay.add(invitationLabel).colspan(2).width(500f).padBottom(20f).row();

        MenuButton acceptBtn = new MenuButton(
            greenUp, "ACCEPT", font,
            greenDown, null, null,
            () -> respondToInvitation(true)
        );
        acceptBtn.setSize(160f, 60f);

        MenuButton declineBtn = new MenuButton(
            purpleUp, "DECLINE", font,
            purpleDown, null, null,
            () -> respondToInvitation(false)
        );
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
        if (pendingInviter == null) {
            return;
        }

        NetworkMessage response = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE)
            .with("accept", accept)
            .with("inviter", pendingInviter);

        NetworkSession.client().sendRequest(response).thenAccept(resp -> {
            Gdx.app.postRunnable(() -> {
                hideInvitation();
                pendingInviter = null;
                // بعداً می‌توان اینجا نتیجه را به AppStatus یا GameScreen منتقل کرد
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
