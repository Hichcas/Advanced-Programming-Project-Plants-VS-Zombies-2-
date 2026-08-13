package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.ShopMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.ShopDaily;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.ShopInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphical shop for the command-driven shop backend.
 * The prices and purchase semantics stay in ShopMenuController and are only
 * surfaced through this Scene2D panel.
 */
public class ShopPanel extends BasePanel {
    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;
    private static final float CARD_W = 350f;
    private static final float CARD_H = 310f;

    private final ShopMenuController controller = new ShopMenuController();
    private final BitmapFont titleFont = FontManager.getInstance().getEnglishTitleFont();
    private final BitmapFont bodyFont = FontManager.getInstance().getEnglishMenuFont();
    private final Skin skin = PvzSkin.get();
    private final List<CardSparkleActor> sparkles = new ArrayList<>();

    private Label balanceLabel;
    private Label statusLabel;
    private Table content;

    public ShopPanel() {
        setFillParent(true);
        build();
    }

    private void build() {
        Drawable panelBg = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        setBackground(panelBg);
        pad(36f);

        Table root = new Table();
        root.setFillParent(true);
        addActor(root);

        Label title = new Label("SHOP", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        root.add(title).growX().height(72f).top().row();

        balanceLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        balanceLabel.setAlignment(Align.center);
        root.add(balanceLabel).growX().height(40f).padBottom(14f).row();

        content = new Table();
        content.defaults().pad(10f);
        root.add(content).grow().row();

        Table footer = new Table();
        footer.defaults().pad(8f);
        statusLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        statusLabel.setAlignment(Align.center);
        footer.add(statusLabel).growX().width(720f);

        TextButton closeButton = makeTextButton("BACK", "brown", this::exitShop);
        footer.add(closeButton).width(180f).height(62f);
        root.add(footer).growX().height(80f).bottom();

        refresh();
    }

    private void refresh() {
        content.clearChildren();
        sparkles.clear();
        updateBalance();

        User user = AppStatus.getCurrentUser();
        if (user == null) {
            Label missing = new Label("Please login first.", new Label.LabelStyle(bodyFont, Color.SALMON));
            missing.setAlignment(Align.center);
            content.add(missing).grow();
            return;
        }

        int card = 0;
        card = addPermanentCard(card, "POT", "Unlock 1 greenhouse slot", "2000 coins", "pot",
            () -> buy("pot", 1, null));
        card = addPermanentCard(card, "PLANT FOOD", "+1 plant food", "3 diamonds", "food",
            () -> buy("food", 1, null));
        card = addPermanentCard(card, "RANDOM SEED PACK", "5 packets for one random unlocked plant",
            "1000 coins", "random-seed", () -> buy("random-seed", 1, null));
        card = addPermanentCard(card, "SELECTED SEED PACK", "10 packets for one unlocked plant",
            "5 diamonds", "selected-seed", this::showSelectedSeedDialog);
        if (card % 3 != 0) {
            content.row();
        }
        buildDailyCard(user);
    }

    private int addPermanentCard(int cardIndex, String title, String description, String price,
                                 String itemId, Runnable action) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(8f);

        Label name = new Label(title, new Label.LabelStyle(bodyFont, Color.WHITE));
        name.setAlignment(Align.center);
        cell.add(name).growX().height(42f).row();

        ShopIconActor icon = new ShopIconActor(itemId);
        cell.add(icon).size(110f).padTop(6f).row();

        Label desc = new Label(description, new Label.LabelStyle(bodyFont, Color.WHITE));
        desc.setWrap(true);
        desc.setAlignment(Align.center);
        cell.add(desc).growX().height(56f).row();

        TextButton buy = makeTextButton(price, "green_small", action);
        cell.add(buy).width(200f).height(56f).row();

        content.add(cell).width(CARD_W).height(CARD_H);
        int next = cardIndex + 1;
        if (next % 3 == 0) {
            content.row();
        }
        return next;
    }

    private void buildDailyCard(User user) {
        ShopDaily daily = user.shopDaily;
        if (daily == null) {
            daily = new ShopDaily();
            user.shopDaily = daily;
        }
        daily.generateIfNeeded();

        PlantType offer = daily.getOfferPlant();
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(8f);

        Label name = new Label("DAILY OFFER", new Label.LabelStyle(titleFont, Color.WHITE));
        name.setAlignment(Align.center);
        cell.add(name).growX().height(44f).row();

        Table preview = new Table();
        if (offer != null) {
            PlantPreviewActor plant = new PlantPreviewActor(offer.name());
            preview.add(plant).size(125f);
            CardSparkleActor sparkle = new CardSparkleActor();
            sparkles.add(sparkle);
            preview.addActor(sparkle);
        }
        cell.add(preview).size(145f).row();

        String plantName = offer == null ? "No unlocked plant" : offer.getDisplayName();
        Label desc = new Label("10 seed packets for " + plantName + "\n20% daily discount", new Label.LabelStyle(bodyFont, Color.WHITE));
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        cell.add(desc).growX().height(56f).row();

        boolean available = daily.isAvailableToday();
        TextButton buy = makeTextButton(available ? "BUY 1600 COINS" : "PURCHASED", available ? "green" : "brown",
            available ? () -> buy("daily", 1, null) : () -> setStatus("Today's daily offer is already purchased."));
        cell.add(buy).width(230f).height(60f).row();

        content.add(cell).width(CARD_W).height(CARD_H);
    }

    private TextButton makeTextButton(String text, String style, Runnable action) {
        TextButton.TextButtonStyle styleData = skin.get(style, TextButton.TextButtonStyle.class);
        TextButton button = new TextButton(text, styleData);
        button.getLabel().setFontScale(0.9f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
    }

    private void buy(String itemId, int count, String plantType) {
        OutputDTO result = controller.handle(new ShopInputDTO(
            com.PVZ.model.enums.commands.ShopCommand.BUY, itemId, count, plantType));
        setStatus(result.getMessage(), result.isSuccess());
        refresh();
    }

    private void showSelectedSeedDialog() {
        User user = AppStatus.getCurrentUser();
        if (user == null || user.collectionState.getUnlockedPlants().isEmpty()) {
            setStatus("No unlocked plants are available for a selected seed pack.", false);
            return;
        }
        Table dialog = new Table();
        dialog.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        dialog.pad(24f);
        dialog.setSize(620f, 290f);
        dialog.setPosition((VW - 620f) / 2f, (VH - 290f) / 2f);

        Label title = new Label("SELECT A PLANT", new Label.LabelStyle(titleFont, Color.WHITE));
        SelectBox<PlantType> select = new SelectBox<>(skin);
        PlantType[] unlocked = user.collectionState.getUnlockedPlants().toArray(new PlantType[0]);
        select.setItems(unlocked);
        TextButton buy = makeTextButton("BUY 5 DIAMONDS", "green", () -> {
            PlantType chosen = select.getSelected();
            buy("selected-seed", 1, chosen.name());
            dialog.remove();
        });
        TextButton cancel = makeTextButton("CANCEL", "brown", dialog::remove);
        dialog.add(title).growX().height(48f).row();
        dialog.add(select).width(420f).height(58f).pad(18f).row();
        Table row = new Table();
        row.add(buy).width(220f).height(60f).pad(6f);
        row.add(cancel).width(180f).height(60f).pad(6f);
        dialog.add(row).growX();
        addActor(dialog);
    }

    private void setStatus(String message) {
        setStatus(message, true);
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.getStyle().fontColor = success ? Color.WHITE : Color.SALMON;
    }

    private void updateBalance() {
        User user = AppStatus.getCurrentUser();
        if (user == null) {
            balanceLabel.setText("Coins: -    Diamonds: -");
            return;
        }
        balanceLabel.setText("Coins: " + user.userStats.getCoins() + "    Diamonds: "
            + user.userStats.getDiamonds());
    }

    private void exitShop() {
        AppStatus.setCurrentMenuType(MenuType.CHAPTER_AND_LEVEL_SELECTION);
        com.PVZ.view.screen.manager.PanelManager.getInstance()
            .performPanelTransition(new ChapterSelectPanel());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (CardSparkleActor sparkle : sparkles) {
            sparkle.time += delta;
        }
    }

    @Override
    public void dispose() {
        sparkles.clear();
        super.dispose();
    }

    private static final class ShopIconActor extends Actor {
        private final String itemId;
        private float time;
        ShopIconActor(String itemId) {
            this.itemId = itemId;
        }
        @Override public void draw(Batch batch, float parentAlpha) {
            String path;
            switch (itemId) {
                case "food" -> path = "768/FULL/BACKGROUNDS/TILE_PLANTFOOD/TILE_PLANTFOOD.PAM";
                case "pot" -> path = "768/INITIAL/ZEN_GARDEN/PLANT_POOF/PLANT_POOF.PAM";
                case "random-seed", "selected-seed" -> path = "768/INITIAL/EFFECTS/PRIZE_PLANT_CARD_LEVEL/PRIZE_PLANT_CARD_LEVEL.PAM";
                case "exchange" -> path = "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM";
                default -> path = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM";
            }
            EntityRenderer renderer = EntityRenderer.getInstance();
            renderer.renderPam((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch, path, time,
                getX() + 55f, getY() + 55f);
        }
    }

    private static final class PlantPreviewActor extends Actor {
        private final String plantName;
        private float time;
        PlantPreviewActor(String plantName) { this.plantName = plantName; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPlant((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch,
                plantName, "idle", time, getX() + 12f, getY() + 8f);
        }
    }

    private static final class CardSparkleActor extends Actor {
        private float time;
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPam((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch,
                "768/INITIAL/UI/STORE/CARD_SPARKLE/CARD_SPARKLE.PAM", time,
                getX() - 75f, getY() - 72f);
        }
    }
}
