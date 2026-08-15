package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.GreenhouseMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.greenhouse.GreenhouseState;
import com.PVZ.model.greenhouse.Pot;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.GreenhouseInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.BaseScreen;
import com.PVZ.view.screen.MainMenuScreen;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class GreenhousePanel extends BasePanel {

    private final GreenhouseMenuController controller = new GreenhouseMenuController();

    private static final float GRID_START_X = 685f;
    private static final float GRID_START_Y = 180f;
    private static final float GAP_X = 73f;
    private static final float GAP_Y = 125f;
    private static final float SLOT_WIDTH = 240f;
    private static final float SLOT_HEIGHT = 180f;

    private static final float LOCK_OFFSET_X = 147f;
    private static final float LOCK_OFFSET_Y = 25f;
    private static final float PLANT_OFFSET_X = 0f;
    private static final float PLANT_OFFSET_Y = 50f;

    private static final float BUTTON_OFFSET_X = 22f;
    private static final float BUTTON_OFFSET_Y = -110f;
    private static final float BUTTON_WIDTH = 200f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_ICON_SIZE = 30f;

    private static final int ROWS = 3;
    private static final int COLS = 4;

    private static final String LOCK_PAM = "768/FULL/UI/LOCK_ANIMS/LOCK_ANIMS.PAM";
    private static final String MARIGOLD_PAM = "768/INITIAL/PLANT/MARIGOLD/MARIGOLD.PAM";
    private static final String DIAMOND_ICON = "IMAGE_EFFECTS_COIN_DIAMOND_COIN_DIAMOND_141X146";
    private static final String COIN_ICON = "IMAGE_EFFECTS_COIN_GOLD_COIN_GOLD_98X95";

    private Group gridGroup;
    private Label statusLabel;
    private Table popupTable;
    private Label popupTextLabel;
    private MenuButton popupCloseBtn;

    private final List<PotCell> potCells = new ArrayList<>();

    public GreenhousePanel() {
        setFillParent(true);

        TextureBank bank = getTextureBank();
        TextureRegion originalBg = bank.region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        if (originalBg != null) {
            int cropLeft = 180;
            int cropRight = 180;
            TextureRegion croppedBg = new TextureRegion(
                originalBg.getTexture(),
                originalBg.getRegionX() + cropLeft,
                originalBg.getRegionY(),
                originalBg.getRegionWidth() - cropLeft - cropRight,
                originalBg.getRegionHeight()
            );
            Image bgImage = new Image(new TextureRegionDrawable(croppedBg));
            bgImage.setFillParent(true);
            bgImage.setScaling(Scaling.fill);
            addActor(bgImage);
        }

        Skin skin = PvzSkin.get();
        BitmapFont font = skin.getFont("FBUSV8C5EI_1_outline");
        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        gridGroup = new Group();
        gridGroup.setSize(BaseScreen.VIRTUAL_WIDTH, BaseScreen.VIRTUAL_HEIGHT);
        addActor(gridGroup);

        statusLabel = new Label("", new Label.LabelStyle(font, Color.SALMON));
        statusLabel.setAlignment(Align.center);
        addActor(statusLabel);

        MenuButton backBtn = new MenuButton(purpleUp, "Back", font, purpleDown, null, marker,
            this::onBack);
        backBtn.setSize(200f, 70f);
        backBtn.setPosition(BaseScreen.VIRTUAL_WIDTH - 250f, 30f);
        addActor(backBtn);

        addActor(buildShopButton());

        buildGrid();

        // ======================= Popup جایزه (بدون آیکون) =======================
        popupTable = new Table();
        popupTable.setVisible(false);
        popupTable.setTouchable(Touchable.enabled);
        popupTable.setSize(500f, 220f);
        popupTable.setPosition(
            (BaseScreen.VIRTUAL_WIDTH - popupTable.getWidth()) / 2f,
            (BaseScreen.VIRTUAL_HEIGHT - popupTable.getHeight()) / 2f
        );
        popupTable.pad(20f);
        popupTable.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        popupTextLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        popupTextLabel.setWrap(true);
        popupTextLabel.setAlignment(Align.center);
        popupTable.add(popupTextLabel).width(400f).padBottom(10f).row();

        popupCloseBtn = new MenuButton(purpleUp, "OK", font, purpleDown, null, marker,
            () -> popupTable.setVisible(false));
        popupCloseBtn.setSize(140f, 60f);
        popupTable.add(popupCloseBtn).padTop(10f).row();

        addActor(popupTable);
    }

    private void buildGrid() {
        potCells.clear();
        gridGroup.clearChildren();

        User user = AppStatus.currentUser;
        if (user == null || user.greenhouseState == null) {
            statusLabel.setText("You must be logged in.");
            statusLabel.setPosition(
                (BaseScreen.VIRTUAL_WIDTH - statusLabel.getPrefWidth()) / 2f,
                BaseScreen.VIRTUAL_HEIGHT - 120f);
            return;
        }
        GreenhouseState state = user.greenhouseState;
        TextureRegion slotRegion = getTextureBank().region(
            "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161");
        TextureRegion coinRegion = getTextureBank().region(COIN_ICON);
        TextureRegion diamondRegion = getTextureBank().region(DIAMOND_ICON);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col + 1;
                int y = row + 1;
                Pot pot = state.getPot(x, y);

                PotCell cell = new PotCell(x, y, pot, slotRegion, coinRegion, diamondRegion);
                float posX = GRID_START_X + col * (SLOT_WIDTH + GAP_X);
                float posY = GRID_START_Y + (ROWS - 1 - row) * (SLOT_HEIGHT + GAP_Y);
                cell.setPosition(posX, posY);
                gridGroup.addActor(cell);
                potCells.add(cell);
            }
        }
        updateAllCells();
    }

    private void updateAllCells() {
        User user = AppStatus.currentUser;
        if (user == null || user.greenhouseState == null) return;
        for (PotCell cell : potCells) {
            Pot pot = user.greenhouseState.getPot(cell.x, cell.y);
            cell.update(pot);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateAllCells();
    }

    private void onBuyPot(int x, int y) {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null || user.greenhouseState == null) return;
        int cost = 2000;
        if (user.userStats.getCoins() < cost) {
            statusLabel.setText("Not enough coins!");
            statusLabel.setPosition(
                (BaseScreen.VIRTUAL_WIDTH - statusLabel.getPrefWidth()) / 2f,
                BaseScreen.VIRTUAL_HEIGHT - 120f);
            return;
        }
        user.userStats.spendCoins(cost);
        user.greenhouseState.unlockPot(x, y);
        UserRegistry.markDirty(user.profile.getUsername());
        updateAllCells();
    }

    private void onPlantPot(int x, int y) {
        OutputDTO result = controller.handle(new GreenhouseInputDTO(
            com.PVZ.model.enums.commands.GreenhouseCommand.PLANT_POT_AT, x, y, null));
        if (!result.isSuccess()) {
            statusLabel.setText(result.getMessage());
        } else {
            statusLabel.setText("");
        }
        updateAllCells();
    }

    private void onCollectPot(int x, int y) {
        User user = AppStatus.currentUser;
        if (user == null || user.greenhouseState == null) return;

        Pot pot = user.greenhouseState.getPot(x, y);
        if (pot.isEmpty()) return;

        OutputDTO result = controller.handle(new GreenhouseInputDTO(
            com.PVZ.model.enums.commands.GreenhouseCommand.COLLECT, x, y, null));

        if (!result.isSuccess()) {
            statusLabel.setText(result.getMessage());
        } else {
            showRewardPopup(result.getMessage());
            statusLabel.setText("");
        }
        updateAllCells();
    }

    private void onGrowPot(int x, int y) {
        OutputDTO result = controller.handle(new GreenhouseInputDTO(
            com.PVZ.model.enums.commands.GreenhouseCommand.GROW, x, y, null));
        if (!result.isSuccess()) {
            statusLabel.setText(result.getMessage());
        } else {
            statusLabel.setText("");
        }
        updateAllCells();
    }

    private void showRewardPopup(String message) {
        popupTextLabel.setText(message);
        popupTable.setVisible(true);
        popupTable.toFront();
    }

    /**
     * "enter shop" is only reachable from the greenhouse per the design doc.
     * Styled like the other greenhouse buttons (purple button skin) with an
     * animated seed-packet PAM icon and a passing sparkle for a bit of shop flair.
     */
    private Stack buildShopButton() {
        Skin skin = PvzSkin.get();
        BitmapFont font = skin.getFont("FBUSV8C5EI_1_outline");
        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        MenuButton shopBtn = new MenuButton(purpleUp, "SHOP", font, purpleDown, null, marker,
            () -> com.PVZ.view.screen.manager.PanelManager.getInstance()
                .performPanelTransition(new ShopPanel()));
        shopBtn.setSize(220f, 74f);

        // Small, fixed-size icon tucked in the button's left margin so it sits
        // beside the label instead of covering it (the PRIZE_COINS_MID PAM is
        // authored at 390x390 - drawing it unscaled would swallow the whole button).
        ShopIconGlintActor icon = new ShopIconGlintActor();
        icon.setTouchable(Touchable.disabled);
        icon.setSize(220f, 74f);

        Stack stack = new Stack();
        stack.setSize(220f, 74f);
        stack.setPosition(30f, 30f);
        stack.add(shopBtn);
        stack.add(icon);
        return stack;
    }

    /** Small fixed-size coin-pile icon drawn at the button's left edge, behind the label. */
    private static final class ShopIconGlintActor extends Actor {
        private static final String COINS_PAM = "768/INITIAL/EFFECTS/PRIZE_COINS_MID/PRIZE_COINS_MID.PAM";
        private static final float NATIVE_CANVAS = 390f;
        private static final float ICON_SIZE = 44f;
        private float time;

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            SpriteBatch sb = (SpriteBatch) batch;
            float cx = getX() + 34f;
            float cy = getY() + getHeight() / 2f;
            float scale = ICON_SIZE / NATIVE_CANVAS;
            com.badlogic.gdx.math.Matrix4 old = sb.getTransformMatrix().cpy();
            com.badlogic.gdx.math.Matrix4 scaled = old.cpy()
                .translate(cx, cy, 0f).scale(scale, scale, 1f).translate(-cx, -cy, 0f);
            sb.setTransformMatrix(scaled);
            EntityRenderer.getInstance().renderPam(sb, COINS_PAM, "idle", time, cx, cy);
            sb.setTransformMatrix(old);
        }
    }

    private void onBack() {
        if (AppStatus.lastMainMenu instanceof MainMenuScreen mainMenu) {
            mainMenu.restoreDefaultBackground();
        }
        AppStatus.setCurrentMenuType(MenuType.MAIN);
    }

    private String formatRemaining(double hours) {
        long totalSec = (long) (hours * 3600);
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private class PotCell extends Group {
        final int x, y;
        final Image slotBg;
        final LockAnimActor lock;
        final PlantPotActor plant;
        final Label timerLabel;

        final DynamicButton buyButton;
        final DynamicButton plantButton;
        final DynamicButton collectButton;
        final DynamicButton growButton;

        PotCell(int x, int y, Pot initialPot,
                TextureRegion slotRegion, TextureRegion coinRegion, TextureRegion diamondRegion) {
            this.x = x;
            this.y = y;
            setSize(SLOT_WIDTH, SLOT_HEIGHT);

            slotBg = new Image(new TextureRegionDrawable(slotRegion));
            slotBg.setSize(SLOT_WIDTH, SLOT_HEIGHT);
            slotBg.setPosition(0, 0);
            addActor(slotBg);

            lock = new LockAnimActor();
            lock.setSize(SLOT_WIDTH, SLOT_HEIGHT);
            lock.setPosition(LOCK_OFFSET_X, LOCK_OFFSET_Y);
            addActor(lock);

            plant = new PlantPotActor(null);
            plant.setSize(SLOT_WIDTH, SLOT_HEIGHT);
            plant.setPosition(PLANT_OFFSET_X, PLANT_OFFSET_Y);
            addActor(plant);

            timerLabel = new Label("", new Label.LabelStyle(
                PvzSkin.get().getFont("FBUSV8C5EI_1_outline"), Color.GREEN));
            timerLabel.setAlignment(Align.center);
            timerLabel.setFontScale(1.2f);
            addActor(timerLabel);

            buyButton = new DynamicButton("2000", coinRegion, () -> onBuyPot(x, y));
            buyButton.setPosition(BUTTON_OFFSET_X, BUTTON_OFFSET_Y);
            addActor(buyButton);

            plantButton = new DynamicButton("Plant", null, () -> onPlantPot(x, y));
            plantButton.setPosition(BUTTON_OFFSET_X, BUTTON_OFFSET_Y);
            addActor(plantButton);

            collectButton = new DynamicButton("Collect", null, () -> onCollectPot(x, y));
            collectButton.setPosition(BUTTON_OFFSET_X, BUTTON_OFFSET_Y);
            addActor(collectButton);

            growButton = new DynamicButton("", diamondRegion, () -> onGrowPot(x, y));
            growButton.setPosition(BUTTON_OFFSET_X, BUTTON_OFFSET_Y);
            addActor(growButton);

            update(initialPot);
        }

        void update(Pot pot) {
            boolean unlocked = pot.isUnlocked();
            boolean empty = pot.isEmpty();
            boolean ready = false;
            double remaining = 0;

            if (unlocked && !empty) {
                long now = System.currentTimeMillis();
                ready = AppStatus.currentUser.greenhouseState.isPlantReady(x, y, now);
                remaining = AppStatus.currentUser.greenhouseState.getRemainingHours(x, y, now);
            }

            lock.setVisible(!unlocked);
            buyButton.setVisible(!unlocked);

            plant.setPot(unlocked && !empty ? pot : null);
            plant.setVisible(unlocked && !empty);

            timerLabel.setVisible(unlocked && !empty);
            collectButton.setVisible(unlocked && !empty && ready);
            growButton.setVisible(unlocked && !empty && !ready);
            plantButton.setVisible(unlocked && empty);

            if (timerLabel.isVisible()) {
                if (ready) {
                    timerLabel.setText("READY");
                } else {
                    timerLabel.setText(formatRemaining(remaining));
                    int diamondCost = (int) Math.ceil(remaining);
                    growButton.setText(String.valueOf(diamondCost));
                    growButton.setIcon(getTextureBank().region(DIAMOND_ICON));
                }
                timerLabel.pack();
                float timerY = BUTTON_OFFSET_Y + BUTTON_HEIGHT + 10f;
                timerLabel.setPosition(
                    (SLOT_WIDTH - timerLabel.getWidth()) / 2f,
                    timerY);
            }
        }
    }

    private static class DynamicButton extends Stack {
        private final MenuButton button;
        private final Image icon;
        private final Label textLabel;

        DynamicButton(String text, TextureRegion iconRegion, Runnable action) {
            Skin skin = PvzSkin.get();
            BitmapFont font = skin.getFont("FBUSV8C5EI_2");
            Drawable up = skin.getDrawable("image_ui_generic_purplebutton_10");
            Drawable down = skin.getDrawable("image_ui_generic_purplebutton_down_10");

            button = new MenuButton(up, "", font, down, null, null, action);
            button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            add(button);

            Table content = new Table();
            content.setTouchable(Touchable.disabled);
            if (iconRegion != null) {
                icon = new Image(new TextureRegionDrawable(iconRegion));
                icon.setScaling(Scaling.fit);
                content.add(icon).size(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE).padRight(6f);
            } else {
                icon = new Image();
            }
            textLabel = new Label(text, new Label.LabelStyle(font, Color.WHITE));
            content.add(textLabel);
            add(content);

            setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        void setText(String text) {
            textLabel.setText(text);
        }

        void setIcon(TextureRegion region) {
            if (region != null) {
                icon.setDrawable(new TextureRegionDrawable(region));
            }
        }
    }

    private static class LockAnimActor extends Actor {
        private float stateTime;

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            EntityRenderer.getInstance().renderPam(
                (SpriteBatch) batch, LOCK_PAM, "locked", stateTime, cx, cy);
            batch.setColor(Color.WHITE);
        }
    }

    private static class PlantPotActor extends Actor {
        private Pot pot;
        private float stateTime;

        PlantPotActor(Pot pot) {
            this.pot = pot;
        }

        void setPot(Pot pot) {
            this.pot = pot;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (pot == null) return;
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;

            if (pot.isMarigold()) {
                EntityRenderer.getInstance().renderPam(
                    (SpriteBatch) batch, MARIGOLD_PAM, "idle", stateTime, cx, cy);
            } else {
                PlantType type = pot.getPlantType();
                if (type != null) {
                    EntityRenderer.getInstance().renderPlant(
                        (SpriteBatch) batch, type.name(), stateTime, cx, cy);
                }
            }
        }
    }
}
