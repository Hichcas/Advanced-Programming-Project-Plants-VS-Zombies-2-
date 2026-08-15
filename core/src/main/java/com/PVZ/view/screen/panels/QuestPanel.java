package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.QuestMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.quest.Quest;
import com.PVZ.model.quest.QuestManager;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.QuestInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.BaseScreen;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuestPanel extends BasePanel {

    private final QuestMenuController controller = new QuestMenuController();

    private static final float VW = BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = BaseScreen.VIRTUAL_HEIGHT;

    private static final float HEADER_HEIGHT = 140f;
    private static final float CARD_WIDTH = 1200f;
    private static final float CARD_PAD = 20f;
    private static final float REWARD_ICON_SIZE = 50f;
    private static final float CLAIM_BUTTON_WIDTH = 180f;
    private static final float CLAIM_BUTTON_HEIGHT = 60f;

    private static final String HEADER_BG = "IMAGE_UI_QUESTS_PANEL_EDGE_TO_EDGE";
    private static final String LIST_BG = "IMAGE_UI_QUESTS_QUESTBORDER";
    private static final String CLOSE_UP = "IMAGE_UI_QUESTS_CLOSE_TAB";
    private static final String CLOSE_DOWN = "IMAGE_UI_QUESTS_CLOSE_TAB_DOWN";
    private static final String REFRESH_UP = "IMAGE_UI_QUESTS_REPLACE_QUEST_BUTTON";
    private static final String REFRESH_DOWN = "IMAGE_UI_QUESTS_REPLACE_QUEST_BUTTON_DOWN";
    private static final String REWARD_COINS = "IMAGE_UI_QUESTS_EPIC_REWARD_COINS";
    private static final String REWARD_GEMS = "IMAGE_UI_QUESTS_EPIC_REWARD_GEMS";
    private static final String REWARD_SEEDS = "IMAGE_UI_QUESTS_QUESTICONS_PREMIUMSEEDS";
    private static final String REWARD_SEEDS2 = "IMAGE_UI_QUESTS_QUESTICONS_PREMIUMSEEDS2";
    private static final String REWARD_MINTS = "IMAGE_UI_QUESTS_QUESTICONS_MINTS";
    private static final String CARD_BG = "image_ui_if_bundle_reward1_bg_10";

    private Skin skin;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private BitmapFont descFont;

    private Table questListTable;
    private Label dailyResetLabel;
    private Label statusLabel;

    private Texture closeUpTex, closeDownTex;
    private Texture refreshUpTex, refreshDownTex;
    private Texture coinsTex, gemsTex, seedsTex, seeds2Tex, mintsTex;

    // ===== Popup برای نمایش جایزه =====
    private Table popupTable;
    private Label popupTextLabel;
    private MenuButton popupCloseBtn;

    public QuestPanel() {
        setFillParent(true);

        skin = PvzSkin.get();
        titleFont = FontManager.getInstance().getEnglishTitleFont();
        bodyFont = FontManager.getInstance().getEnglishMenuFont();
        descFont = FontManager.getInstance().getEnglishTinyFont();

        loadTextures();

        Table root = new Table();
        root.setFillParent(true);
        addActor(root);

        Table header = buildHeader();
        root.add(header).growX().height(HEADER_HEIGHT).row();

        Table contentArea = new Table();
        contentArea.setBackground(createDrawableFromTexture(safeTextureFromRegion(LIST_BG)));

        questListTable = new Table();
        questListTable.top();

        ScrollPane scrollPane = new ScrollPane(questListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        contentArea.add(scrollPane).grow().padTop(170f).padBottom(40f).padLeft(35f).padRight(35f);

        root.add(contentArea).grow().row();

        dailyResetLabel = new Label("", new Label.LabelStyle(descFont, Color.WHITE));
        statusLabel = new Label("", new Label.LabelStyle(bodyFont, Color.SALMON));

        refreshQuests();

        // ===== ساخت Popup جایزه =====
        popupTable = new Table();
        popupTable.setVisible(false);
        popupTable.setTouchable(Touchable.enabled);
        popupTable.setSize(600f, 280f);
        popupTable.setPosition(
            (VW - popupTable.getWidth()) / 2f,
            (VH - popupTable.getHeight()) / 2f
        );
        popupTable.pad(25f);
        popupTable.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        popupTextLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        popupTextLabel.setWrap(true);
        popupTextLabel.setAlignment(Align.center);
        popupTable.add(popupTextLabel).width(500f).padBottom(20f).row();

        popupCloseBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_greenbutton_10"),
            "OK", bodyFont,
            skin.getDrawable("image_ui_generic_greenbutton_down_10"),
            null, null,
            () -> popupTable.setVisible(false)
        );
        popupCloseBtn.setSize(160f, 60f);
        popupTable.add(popupCloseBtn).padTop(10f).row();

        addActor(popupTable);
    }

    private Table buildHeader() {
        Table header = new Table();

        Label titleLabel = new Label("QUESTS", new Label.LabelStyle(titleFont, Color.GOLD));
        titleLabel.setFontScale(1.5f);
        header.add(titleLabel).left().padLeft(40f).padRight(40f).height(HEADER_HEIGHT);

        header.add().expandX();

        MenuButton refreshBtn = new MenuButton(refreshUpTex, null, null,
            refreshDownTex, null, null, this::onRefreshDaily);
        refreshBtn.setSize(64f, 64f);

        MenuButton closeBtn = new MenuButton(closeUpTex, null, null,
            closeDownTex, null, null, this::onClose);
        closeBtn.setSize(64f, 64f);

        header.add(refreshBtn).size(80f, 80f).padBottom(-140f).padRight(20f);
        header.add(closeBtn).size(64f, 64f).padBottom(-140f).padRight(30f);
        return header;
    }

    private Drawable getHeaderDrawable() {
        try {
            if (skin.has("image_ui_quests_panel_edge_to_edge_ten", Drawable.class)) {
                return skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten");
            }
        } catch (Exception ignored) {
        }
        Texture tex = safeTextureFromRegion(HEADER_BG);
        if (tex != null) {
            return new TextureRegionDrawable(tex);
        }
        return null;
    }

    private Drawable createDrawableFromTexture(Texture texture) {
        return texture != null ? new TextureRegionDrawable(texture) : null;
    }

    private void loadTextures() {
        closeUpTex = safeTextureFromRegion(CLOSE_UP);
        closeDownTex = safeTextureFromRegion(CLOSE_DOWN);
        refreshUpTex = safeTextureFromRegion(REFRESH_UP);
        refreshDownTex = safeTextureFromRegion(REFRESH_DOWN);
        coinsTex = safeTextureFromRegion(REWARD_COINS);
        gemsTex = safeTextureFromRegion(REWARD_GEMS);
        seedsTex = safeTextureFromRegion(REWARD_SEEDS);
        seeds2Tex = safeTextureFromRegion(REWARD_SEEDS2);
        mintsTex = safeTextureFromRegion(REWARD_MINTS);
    }

    private void refreshQuests() {
        questListTable.clearChildren();

        User user = AppStatus.currentUser;
        if (user == null || user.questState == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }

        QuestManager qm = user.questState.getQuestManager();
        qm.refreshDailyIfNeeded();
        qm.updateChapterQuests();
        dailyResetLabel.setText("Next daily reset: " + qm.getTimeUntilReset());

        List<Quest> quests = new ArrayList<>(qm.getActiveQuests());
        quests.sort(Comparator.comparingInt(this::statusGroup)
            .thenComparingInt(this::priorityRank));

        if (quests.isEmpty()) {
            questListTable.add(new Label("No active quests.", new Label.LabelStyle(bodyFont, Color.WHITE)))
                .pad(40f).row();
            return;
        }

        for (Quest quest : quests) {
            questListTable.add(buildQuestCard(quest))
                .width(CARD_WIDTH)
                .pad(CARD_PAD)
                .row();
        }
    }

    private int statusGroup(Quest q) {
        if (q.isCompleted() && !q.isClaimed()) return 0;
        if (q.isClaimed()) return 2;
        return 1;
    }

    private int priorityRank(Quest q) {
        return switch (q.getPriority()) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private Table buildQuestCard(Quest quest) {
        Table card = new Table();
        card.setBackground(skin.getDrawable(CARD_BG));
        card.pad(20f);
        card.left();

        Label title = new Label(quest.getFormattedTitle(), new Label.LabelStyle(titleFont,
            quest.isClaimed() ? Color.GRAY : Color.GOLD));
        title.setWrap(true);
        title.setAlignment(Align.left);
        card.add(title).growX().colspan(2).left().padBottom(8f).row();

        Label desc = new Label(quest.getFormattedDescription(), new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        desc.setWrap(true);
        desc.setAlignment(Align.left);
        card.add(desc).growX().colspan(2).left().padBottom(12f).row();

        card.add(buildProgressBar(quest)).growX().colspan(2).padBottom(12f).row();

        Table bottomRow = new Table();
        bottomRow.add(buildRewardIcon(quest.getReward())).size(REWARD_ICON_SIZE).padRight(20f);
        Label rewardText = new Label(formatRewardText(quest.getReward()),
            new Label.LabelStyle(descFont, Color.WHITE));
        bottomRow.add(rewardText).left().expandX();

        if (quest.isCompleted() && !quest.isClaimed()) {
            MenuButton claimBtn = new MenuButton(
                skin.getDrawable("image_ui_generic_greenbutton_10"),
                "CLAIM", bodyFont,
                skin.getDrawable("image_ui_generic_greenbutton_down_10"),
                null, null, () -> onClaim(quest.getId())
            );
            claimBtn.setSize(CLAIM_BUTTON_WIDTH, CLAIM_BUTTON_HEIGHT);
            bottomRow.add(claimBtn).right();
        } else if (quest.isClaimed()) {
            Label claimedLabel = new Label("CLAIMED", new Label.LabelStyle(descFont, Color.GREEN));
            bottomRow.add(claimedLabel).right();
        }

        card.add(bottomRow).growX().colspan(2);
        return card;
    }

    private ProgressBar buildProgressBar(Quest quest) {
        ProgressBar bar;
        try {
            bar = new ProgressBar(0f, Math.max(1, quest.getTargetCount()), 1f, false, skin, "xp_green");
        } catch (Exception e) {
            bar = new ProgressBar(0f, 100f, 1f, false, skin);
        }

        if (quest.getTargetCount() == 0) {
            bar.setRange(0f, 1f);
            bar.setValue(quest.isCompleted() ? 1f : 0f);
        } else {
            bar.setRange(0f, quest.getTargetCount());
            bar.setValue(Math.min(quest.getCurrentCount(), quest.getTargetCount()));
        }
        return bar;
    }

    private Actor buildRewardIcon(Quest.Reward reward) {
        if (reward == null) return new Image();
        Stack stack = new Stack();

        switch (reward.getType()) {
            case COINS -> stack.add(new Image(coinsTex));
            case DIAMONDS -> stack.add(new Image(gemsTex));
            case SEED_PACKETS -> stack.add(new Image(reward.getAmount() > 1 ? seeds2Tex : seedsTex));
            case UNLOCK_PLANT -> {
                if (reward.getTargetPlant() != null) {
                    PlantPreviewActor plant = new PlantPreviewActor(reward.getTargetPlant());
                    plant.setSize(REWARD_ICON_SIZE, REWARD_ICON_SIZE);
                    stack.add(plant);
                } else {
                    stack.add(new Image(seedsTex));
                }
            }
        }
        return stack;
    }

    private String formatRewardText(Quest.Reward reward) {
        if (reward == null) return "No reward";
        return switch (reward.getType()) {
            case COINS -> reward.getAmount() + " Coins";
            case DIAMONDS -> reward.getAmount() + " Gems";
            case SEED_PACKETS -> reward.getAmount() + " Seed Packets";
            case UNLOCK_PLANT -> "Unlock " +
                (reward.getTargetPlant() != null ? reward.getTargetPlant().getDisplayName() : "Plant");
        };
    }

    private void onClaim(String questId) {
        OutputDTO result = controller.handle(new QuestInputDTO(
            com.PVZ.model.enums.commands.QuestCommand.CLAIM, questId, null));
        if (result != null) {
            if (result.isSuccess()) {
                // نمایش Popup جایزه
                showRewardPopup(result.getMessage());
                statusLabel.setText("");
            } else {
                statusLabel.setText(result.getMessage());
                statusLabel.setColor(Color.SALMON);
            }
        }
        refreshQuests();
    }

    private void showRewardPopup(String message) {
        popupTextLabel.setText(message);
        popupTable.setVisible(true);
        popupTable.toFront();
    }

    private void onRefreshDaily() {
        OutputDTO result = controller.handle(new QuestInputDTO(
            com.PVZ.model.enums.commands.QuestCommand.DEBUG_RESET_DAILY, null, null));
        if (result != null) {
            statusLabel.setText(result.getMessage());
            statusLabel.setColor(result.isSuccess() ? Color.GREEN : Color.SALMON);
        }
        refreshQuests();
    }

    private void onClose() {
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    @Override
    public void dispose() {
        if (closeUpTex != null) closeUpTex.dispose();
        if (closeDownTex != null) closeDownTex.dispose();
        if (refreshUpTex != null) refreshUpTex.dispose();
        if (refreshDownTex != null) refreshDownTex.dispose();
        if (coinsTex != null) coinsTex.dispose();
        if (gemsTex != null) gemsTex.dispose();
        if (seedsTex != null) seedsTex.dispose();
        if (seeds2Tex != null) seeds2Tex.dispose();
        if (mintsTex != null) mintsTex.dispose();

        super.dispose();
    }

    private static class PlantPreviewActor extends Actor {
        private final PlantType type;
        private float time = (float) (Math.random() * 2.0);
        PlantPreviewActor(PlantType type) { this.type = type; }
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPlant((SpriteBatch) batch, type.name(), time,
                getX() + getWidth() / 2f, getY() + getHeight() * 0.55f);
        }
    }
}
