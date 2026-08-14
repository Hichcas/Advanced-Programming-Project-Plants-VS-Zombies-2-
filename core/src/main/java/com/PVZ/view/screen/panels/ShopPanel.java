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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class ShopPanel extends BasePanel {
    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;
    private static final float CARD_W = 430f;
    private static final float CARD_H = 400f;
    private static final float CARD_ICON_SIZE = 170f;

    private static final String COIN_PAM = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM";
    private static final String DIAMOND_PAM = "768/INITIAL/EFFECTS/TUTORIAL_DIAMOND/TUTORIAL_DIAMOND.PAM";

    private final ShopMenuController controller = new ShopMenuController();
    private final Skin skin = PvzSkin.get();
    private final BitmapFont titleFont = skin.getFont("FBUSV8C5EI_1_outline");
    private final BitmapFont bodyFont = skin.getFont("FBUSV8C5EI_2");

    private Label balanceLabel;
    private Label diamondLabel;
    private Label statusLabel;
    private Table content;
    private CountdownLabel dailyCountdown;
    private String activeTab = "permanent";

    public ShopPanel() {
        setFillParent(true);
        build();
    }

    /**
     * The shop is only reachable from the greenhouse, so it reuses the same
     * cropped Zen Garden backdrop the greenhouse panel uses instead of a flat
     * fill - there is no dedicated "store" background asset in the skin.
     */
    private void addBackground() {
        TextureRegion originalBg = getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        if (originalBg == null) {
            return;
        }
        int cropLeft = 180;
        int cropRight = 180;
        TextureRegion cropped = new TextureRegion(
            originalBg.getTexture(),
            originalBg.getRegionX() + cropLeft,
            originalBg.getRegionY(),
            originalBg.getRegionWidth() - cropLeft - cropRight,
            originalBg.getRegionHeight());
        Image bgImage = new Image(new TextureRegionDrawable(cropped));
        bgImage.setFillParent(true);
        bgImage.setScaling(Scaling.fill);
        bgImage.setColor(1f, 1f, 1f, 0.55f);
        addActor(bgImage);

        Image dim = new Image(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        dim.setFillParent(true);
        dim.setColor(1f, 1f, 1f, 0.35f);
        addActor(dim);
    }

    private void build() {
        clearChildren();
        addBackground();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(36f);
        addActor(root);

        Label title = new Label("SHOP", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        title.setFontScale(1.3f);
        root.add(title).growX().height(64f).top().row();

        root.add(buildBalanceRow()).height(72f).padBottom(10f).row();

        Table tabs = new Table();
        tabs.defaults().space(10f);
        tabs.add(tabButton("PERMANENT ITEMS", "permanent")).width(340f).height(58f);
        tabs.add(tabButton("DAILY OFFER", "daily")).width(340f).height(58f);
        root.add(tabs).height(64f).padBottom(14f).row();

        content = new Table();
        content.defaults().pad(10f);
        content.top();
        root.add(content).grow().row();

        Table footer = new Table();
        footer.defaults().pad(8f);
        statusLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        statusLabel.setAlignment(Align.center);
        statusLabel.setFontScale(0.7f);
        footer.add(statusLabel).growX().width(720f);

        TextButton closeButton = makeTextButton("BACK", "brown", this::exitShop);
        footer.add(closeButton).width(180f).height(62f);
        root.add(footer).growX().height(80f).bottom();

        refresh();
    }

    private Table buildBalanceRow() {
        Table row = new Table();
        row.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        row.defaults().pad(6f);

        row.add(new PamIconActor(COIN_PAM, "animation", 45f, 28f)).size(28f).padRight(8f);
        balanceLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        balanceLabel.setFontScale(0.9f);
        row.add(balanceLabel).padRight(40f);

        row.add(new PamIconActor(DIAMOND_PAM, "idle", 200f, 56f)).size(56f).padRight(8f);
        diamondLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        diamondLabel.setFontScale(0.9f);
        row.add(diamondLabel);
        return row;
    }

    private TextButton tabButton(String label, String tabId) {
        return makeTextButton(label, activeTab.equals(tabId) ? "green" : "brown", () -> {
            activeTab = tabId;
            build();
        });
    }

    private void refresh() {
        content.clearChildren();
        updateBalance();

        User user = AppStatus.getCurrentUser();
        if (user == null) {
            Label missing = new Label("Please login first.", new Label.LabelStyle(bodyFont, Color.SALMON));
            missing.setAlignment(Align.center);
            content.add(missing).grow();
            return;
        }

        if (activeTab.equals("daily")) {
            buildDailyPanel(user);
        } else {
            buildPermanentPanel();
        }
    }

    // ----------------------------------------------------------- permanent

    private void buildPermanentPanel() {
        int card = 0;
        card = addPermanentCard(card, "POT", "Unlock 1 greenhouse slot (max 20)", 2000, false,
            () -> buy("pot", 1, null),
            "768/INITIAL/ZEN_GARDEN/ZEN_POT_WATER/ZEN_POT_WATER.PAM", "animation", 390f);
        card = addPermanentCard(card, "PLANT FOOD", "+1 plant food (cap 3)", 3, true,
            () -> buy("food", 1, null),
            "768/FULL/BACKGROUNDS/TILE_PLANTFOOD/TILE_PLANTFOOD.PAM", "active_idle", 390f);
        card = addPermanentCard(card, "RANDOM SEED PACK", "5 packets for one random unlocked plant",
            1000, false, () -> buy("random-seed", 1, null),
            "768/INITIAL/EFFECTS/PRIZE_PLANT_CARD_LEVEL/PRIZE_PLANT_CARD_LEVEL.PAM", "loop", 700f);
        card = addPermanentCard(card, "SELECTED SEED PACK", "10 packets for one unlocked plant",
            5, true, this::showSelectedSeedDialog,
            "768/INITIAL/EFFECTS/PRIZE_PLANT_CARD_LEVEL/PRIZE_PLANT_CARD_LEVEL.PAM", "loop", 700f);
        addPermanentCard(card, "CURRENCY EXCHANGE", "Convert 5 diamonds into 500 coins",
            5, true, () -> buy("exchange", 1, null),
            "768/INITIAL/EFFECTS/PRIZE_COINS_MID/PRIZE_COINS_MID.PAM", "idle", 390f);
    }

    private int addPermanentCard(int cardIndex, String title, String description, int price,
                                 boolean priceInDiamonds, Runnable action,
                                 String iconPath, String iconClip, float iconCanvas) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(8f);

        Label name = new Label(title, new Label.LabelStyle(bodyFont, Color.GOLD));
        name.setAlignment(Align.center);
        name.setFontScale(0.85f);
        cell.add(name).growX().height(40f).row();

        cell.add(new PamIconActor(iconPath, iconClip, iconCanvas, CARD_ICON_SIZE))
            .size(CARD_ICON_SIZE).padTop(6f).padBottom(6f).row();

        Label desc = new Label(description, new Label.LabelStyle(bodyFont, Color.WHITE));
        desc.setWrap(true);
        desc.setAlignment(Align.center);
        desc.setFontScale(0.68f);
        cell.add(desc).growX().height(56f).row();

        cell.add(buildPriceButton(price, priceInDiamonds, action)).width(230f).height(64f).row();

        content.add(cell).width(CARD_W).height(CARD_H);
        int next = cardIndex + 1;
        if (next % 3 == 0) {
            content.row();
        }
        return next;
    }

    /**
     * Icon + price fused into one button, matching the same pattern the
     * greenhouse's buy/upgrade buttons already use, instead of a plain text
     * button - this is what keeps the coin/diamond icon from floating loose
     * next to the price text.
     */
    private Table buildPriceButton(int price, boolean diamonds, Runnable action) {
        Drawable up = skin.getDrawable("image_ui_generic_purplebutton_10");
        Drawable down = skin.getDrawable("image_ui_generic_purplebutton_down_10");
        com.PVZ.view.screen.ui.MenuButton button = new com.PVZ.view.screen.ui.MenuButton(
            up, "", bodyFont, down, null, null, action);
        button.setSize(250f, 70f);

        Table overlay = new Table();
        overlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        PamIconActor icon = diamonds
            ? new PamIconActor(DIAMOND_PAM, "idle", 200f, 46f)
            : new PamIconActor(COIN_PAM, "animation", 45f, 23f);
        overlay.add(icon).size(46f).padRight(8f);
        Label priceLabel = new Label(String.valueOf(price), new Label.LabelStyle(bodyFont, Color.WHITE));
        priceLabel.setFontScale(0.85f);
        overlay.add(priceLabel);

        Stack stack = new Stack();
        stack.setSize(250f, 70f);
        stack.add(button);
        stack.add(overlay);
        return wrapAsTable(stack);
    }

    private Table wrapAsTable(Stack stack) {
        Table t = new Table();
        t.add(stack).size(250f, 70f);
        return t;
    }

    // --------------------------------------------------------------- daily

    private void buildDailyPanel(User user) {
        ShopDaily daily = user.shopDaily;
        if (daily == null) {
            daily = new ShopDaily();
            user.shopDaily = daily;
        }
        daily.generateIfNeeded();
        PlantType offer = daily.getOfferPlant();

        Table panel = new Table();
        panel.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        panel.defaults().pad(10f);

        Label name = new Label("TODAY'S OFFER", new Label.LabelStyle(titleFont, Color.GOLD));
        name.setAlignment(Align.center);
        name.setFontScale(1.0f);
        panel.add(name).growX().height(56f).row();

        // The one deliberate flourish in the shop: a soft sparkle drifting
        // behind the offered plant's own idle animation.
        Stack preview = new Stack();
        if (offer != null) {
            preview.add(new PamIconActor(
                "768/INITIAL/UI/STORE/CARD_SPARKLE/CARD_SPARKLE.PAM", null, 390f, 220f));
            Table plantWrap = new Table();
            plantWrap.add(new PlantPreviewActor(offer.name())).size(170f);
            preview.add(plantWrap);
        }
        panel.add(preview).size(220f).padTop(4f).padBottom(4f).row();

        String plantName = offer == null ? "No unlocked plant" : offer.getDisplayName();
        Label desc = new Label("10 seed packets for " + plantName, new Label.LabelStyle(bodyFont, Color.WHITE));
        desc.setAlignment(Align.center);
        desc.setFontScale(0.85f);
        panel.add(desc).growX().height(40f).row();

        Label discount = new Label("Base price 2000 coins  -  20% off today",
            new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
        discount.setAlignment(Align.center);
        discount.setFontScale(0.62f);
        panel.add(discount).growX().height(32f).row();

        if (dailyCountdown == null) {
            dailyCountdown = new CountdownLabel(bodyFont);
        }
        Table countdownRow = new Table();
        Label refreshText = new Label("Refreshes in", new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
        refreshText.setFontScale(0.62f);
        countdownRow.add(refreshText).padRight(10f);
        countdownRow.add(dailyCountdown);
        panel.add(countdownRow).height(44f).padTop(4f).row();

        boolean available = daily.isAvailableToday();
        Table buyChip = buildPriceButton(1600, false,
            available ? () -> buy("daily", 1, null)
                      : () -> setStatus("Today's daily offer is already purchased.", true));
        if (!available) {
            Label purchased = new Label("ALREADY PURCHASED TODAY", new Label.LabelStyle(bodyFont, Color.SALMON));
            purchased.setFontScale(0.6f);
            panel.add(purchased).height(50f).padTop(10f).row();
        } else {
            panel.add(buyChip).size(260f, 66f).padTop(10f).row();
        }

        content.add(panel).width(460f).height(560f);
    }

    /** Ticking countdown to the next local midnight, when the daily offer resets. */
    private static final class CountdownLabel extends Label {
        CountdownLabel(BitmapFont font) {
            super("", new LabelStyle(font, Color.LIME));
            setFontScale(0.85f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atStartOfDay();
            Duration remaining = Duration.between(now, nextMidnight);
            long h = Math.max(0, remaining.toHours());
            long m = Math.max(0, remaining.toMinutesPart());
            long s = Math.max(0, remaining.toSecondsPart());
            setText(String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    // -------------------------------------------------------------- helpers

    private TextButton makeTextButton(String text, String style, Runnable action) {
        TextButton.TextButtonStyle styleData = skin.get(style, TextButton.TextButtonStyle.class);
        TextButton button = new TextButton(text, styleData);
        button.getLabel().setFontScale(0.75f);
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

        Label title = new Label("SELECT A PLANT", new Label.LabelStyle(titleFont, Color.GOLD));
        title.setFontScale(0.85f);
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

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.getStyle().fontColor = success ? Color.WHITE : Color.SALMON;
    }

    private void updateBalance() {
        User user = AppStatus.getCurrentUser();
        if (user == null) {
            balanceLabel.setText("-");
            diamondLabel.setText("-");
            return;
        }
        balanceLabel.setText(String.valueOf(user.userStats.getCoins()));
        diamondLabel.setText(String.valueOf(user.userStats.getDiamonds()));
    }

    private void exitShop() {
        // The shop is entered from the greenhouse (GreenhouseMenuController), so
        // backing out returns there too, not to chapter select.
        AppStatus.setCurrentMenuType(MenuType.GREENHOUSE);
        com.PVZ.view.screen.manager.PanelManager.getInstance()
            .performPanelTransition(new GreenhousePanel());
    }

    /**
     * Draws a PAM at a fixed on-screen pixel size regardless of its native
     * canvas (several of these assets are authored at 390x390), by scaling
     * the batch's transform matrix around the icon's own center and
     * restoring it right after.
     */
    private static void drawScaledPam(SpriteBatch batch, String path, String clip, float nativeCanvas,
                                       float targetSize, float time, float cx, float cy) {
        float scale = targetSize / nativeCanvas;
        Matrix4 old = batch.getTransformMatrix().cpy();
        Matrix4 scaled = old.cpy().translate(cx, cy, 0f).scale(scale, scale, 1f).translate(-cx, -cy, 0f);
        batch.setTransformMatrix(scaled);
        EntityRenderer.getInstance().renderPam(batch, path, clip, time, cx, cy);
        batch.setTransformMatrix(old);
    }

    private static final class PamIconActor extends Actor {
        private final String path;
        private final String clip;
        private final float nativeCanvas;
        private final float targetSize;
        private float time;

        PamIconActor(String path, String clip, float nativeCanvas, float targetSize) {
            this.path = path;
            this.clip = clip;
            this.nativeCanvas = nativeCanvas;
            this.targetSize = targetSize;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            drawScaledPam((SpriteBatch) batch, path, clip, nativeCanvas, targetSize, time,
                getX() + getWidth() / 2f, getY() + getHeight() / 2f);
        }
    }

    private static final class PlantPreviewActor extends Actor {
        private final String plantName;
        private float time;
        PlantPreviewActor(String plantName) { this.plantName = plantName; }
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPlant((SpriteBatch) batch,
                plantName, "idle", time, getX() + getWidth() / 2f, getY() + getHeight() / 2f);
        }
    }
}
