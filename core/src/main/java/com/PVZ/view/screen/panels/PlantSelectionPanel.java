package com.PVZ.view.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.PVZ.model.entity.ChapterMapPaths;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand;
import com.PVZ.model.enums.commands.PlantSelectionCommand;
import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;
import com.PVZ.view.input.DTO.PlantSelectionInputDTO;
import com.PVZ.controller.menuControllers.ChapterAndLevelSelectionMenuController;
import com.PVZ.controller.menuControllers.CollectionMenuController;
import com.PVZ.controller.menuControllers.PlantSelectionMenuController;
import com.PVZ.model.enums.commands.CollectionCommand;
import com.PVZ.view.input.DTO.CollectionInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.view.screen.ui.PlantCardActor;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectionPanel extends BasePanel {
    private final PlantSelectionMenuController plantController = new PlantSelectionMenuController();
    private final List<PlantCardActor> cards = new ArrayList<>();
    private final List<DetailPreviewActor> selectedSlotPreviews = new ArrayList<>();
    private static final int MAX_SELECTED_SLOTS = 8;
    private Label statusLabel;
    private Label countLabel;
    private String chapterName;
    private boolean readyToShow = true;
    private Label detailNameLabel;
    private Label detailDescriptionLabel;
    private Label detailCostLabel;
    private Label detailStatusLabel;
    private DetailPreviewActor detailPreviewActor;
    private PlantType detailPlantType;
    private MenuButton upgradeButton;
    private MenuButton boostButton;

    public PlantSelectionPanel(String chapterName, int stage) {
        this.chapterName = chapterName != null ? chapterName : "Frontyard";
        if (!AppStatus.isMultiplayerMatch) {
            OutputDTO enterResult = new ChapterAndLevelSelectionMenuController().handle(
                new ChapterAndLevelSelectionInputDTO(ChapterAndLevelSelectionCommand.ENTER_CHAPTER,
                    chapterName, null, null, stage));

            if (!enterResult.isSuccess()) {
                buildErrorOnly(stripColorCodes(enterResult.getMessage()));
                return;
            }
            if (AppStatus.currentMenuType != MenuType.PLANT_SELECTION) {
                readyToShow = false;
                ScreenManager.getInstance().performTransition(() -> new GameScreen(
                    ChapterMapPaths.resolve(chapterName),
                    "music/TitleScreen.mp3",
                    AppStatus.getGameEngine()
                ));
                return;
            }
        }

        buildPicker(this.chapterName);
    }

    private void buildErrorOnly(String message) {
        setFillParent(true);
        align(Align.center);
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        statusLabel = new Label(message, new Label.LabelStyle(font, Color.SALMON));
        add(statusLabel).pad(20f).row();
    }

    private void buildPicker(String chapterName) {
        setFillParent(true);
        align(Align.center);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        String mapPath = ChapterMapPaths.resolve(chapterName);
        Image levelBackground = new Image(new Texture(com.badlogic.gdx.Gdx.files.internal(mapPath)));
        levelBackground.setFillParent(true);
        levelBackground.setScaling(com.badlogic.gdx.utils.Scaling.fill);
        addActor(levelBackground);

        Table selectedTray = buildSelectedTray();
        Table detailPanel = buildDetailPanel(font);

        Table grid = new Table();
        grid.top().left();
        Drawable cardSlotBg = resolveCardSlotBackground();
        int col = 0;
        for (PlantType type : PlantType.values()) {
            PlantCardActor card = new PlantCardActor(type, font);
            card.setOnClick(() -> onCardClicked(card));
            cards.add(card);

            float slotW = card.getWidth() + 20f;
            float slotH = card.getHeight() + 26f;
            Stack cell = new Stack();
            if (cardSlotBg != null) {
                Table slotFrame = new Table();
                slotFrame.setBackground(cardSlotBg);
                cell.add(slotFrame);
            }
            Table cardHolder = new Table();
            cardHolder.add(card).size(card.getWidth(), card.getHeight());
            cell.add(cardHolder);

            grid.add(cell).size(slotW, slotH).pad(20f);
            col++;
            if (col >= 6) {
                col = 0;
                grid.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFadeScrollBars(false);

        countLabel = new Label("0 / 8 selected", new Label.LabelStyle(font, Color.WHITE));
        statusLabel = new Label("", new Label.LabelStyle(font, Color.SALMON));

        // دکمه LET'S ROCK (جایگزین با MenuButton)
        Texture greenUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON");
        Texture greenDown = safeTextureFromRegion("IMAGE_UI_GENERIC_GREENBUTTON_DOWN");
        MenuButton letsRock = new MenuButton(greenUp, "LET'S ROCK", font, greenDown, null, null, this::onLetsRock);
        letsRock.setSize(240f, 64f);

        Table window = new Table();
        window.pad(24f);
        window.add(selectedTray).padBottom(10f).row();
        window.add(detailPanel).width(6 * 150f).padBottom(14f).row();
        window.add(scrollPane).size(6 * 150f, 4 * 175f).row();
        window.add(countLabel).padTop(8f).row();
        window.add(statusLabel).padTop(4f).row();
        window.add(letsRock).padTop(12f).size(240f, 64f).row();

        // The dialog texture is a separate, semi-transparent layer BEHIND the
        // window's actual content (rather than an opaque Table background), so
        // the level's own background image loaded above stays visible through
        // it instead of always looking like the same flat backdrop everywhere.
        Image windowBackdrop = new Image(resolveWindowBackground());
        windowBackdrop.setColor(1f, 1f, 1f, 0.6f);

        Stack windowStack = new Stack();
        windowStack.add(windowBackdrop);
        windowStack.add(window);
        ImageButton closeButton = buildCloseButton();
        if (closeButton != null) {
            Table closeOverlay = new Table();
            closeOverlay.top().right();
            closeOverlay.add(closeButton).size(48f, 48f).padTop(-10f).padRight(-10f);
            windowStack.add(closeOverlay);
        }

        add(windowStack).center();

        refresh();
    }

    /**
     * 8 fixed seed-packet slots showing the currently selected plants in
     * order, each with the same small animated plant preview used in the
     * detail panel. Slot background is just the almanac card-frame drawable
     * (semi-transparent, no fill) so the level's own background stays visible
     * behind/around it instead of a flat fixed backdrop.
     */
    private Table buildSelectedTray() {
        Table tray = new Table();
        tray.defaults().pad(6f);
        Drawable slotFrame = resolveCardSlotBackground();

        selectedSlotPreviews.clear();
        for (int i = 0; i < MAX_SELECTED_SLOTS; i++) {
            Stack slot = new Stack();
            if (slotFrame != null) {
                Table frame = new Table();
                frame.setBackground(slotFrame);
                frame.setColor(1f, 1f, 1f, 0.75f);
                slot.add(frame);
            }
            DetailPreviewActor preview = new DetailPreviewActor();
            selectedSlotPreviews.add(preview);
            Table previewHolder = new Table();
            previewHolder.add(preview).size(64f, 78f);
            slot.add(previewHolder);
            tray.add(slot).size(74f, 90f);
        }
        return tray;
    }

    private void updateSelectedTray() {
        if (selectedSlotPreviews.isEmpty()) {
            return;
        }
        List<PlantType> selected = new ArrayList<>(AppStatus.SELECTED_PLANTS);
        for (int i = 0; i < selectedSlotPreviews.size(); i++) {
            selectedSlotPreviews.get(i).setPlantType(i < selected.size() ? selected.get(i) : null);
        }
    }

    private Table buildDetailPanel(BitmapFont font) {
        Table panel = new Table();
        Drawable panelBg = resolveCardSlotBackground();
        if (panelBg != null) {
            panel.setBackground(panelBg);
        }
        panel.pad(10f);

        detailPreviewActor = new DetailPreviewActor();
        detailNameLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        detailDescriptionLabel = new Label("", new Label.LabelStyle(font, Color.LIGHT_GRAY));
        detailDescriptionLabel.setWrap(true);
        detailCostLabel = new Label("", new Label.LabelStyle(font, Color.GOLD));
        detailStatusLabel = new Label("", new Label.LabelStyle(font, Color.SKY));
        SunIconActor sunIcon = new SunIconActor();

        Table infoColumn = new Table();
        infoColumn.top().left();
        infoColumn.add(detailNameLabel).left().row();
        infoColumn.add(detailDescriptionLabel).left().width(4 * 150f).padTop(4f).row();
        Table costRow = new Table();
        costRow.add(sunIcon).size(28f, 28f).padRight(6f);
        costRow.add(detailCostLabel).left();
        infoColumn.add(costRow).left().padTop(6f).row();
        infoColumn.add(detailStatusLabel).left().padTop(4f).row();

        // دکمه‌های UPGRADE و BOOST با MenuButton سفارشی
        Texture purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = null;

        upgradeButton = new MenuButton(purpleUp, "UPGRADE", font, purpleDown, null, marker, this::onUpgradeClicked);
        upgradeButton.setSize(140f, 48f);
        boostButton = new MenuButton(purpleUp, "BOOST", font, purpleDown, null, marker, this::onBoostClicked);
        boostButton.setSize(140f, 48f);

        Table actionColumn = new Table();
        actionColumn.add(upgradeButton).size(140f, 48f).padBottom(8f).row();
        actionColumn.add(boostButton).size(140f, 48f).row();

        panel.add(detailPreviewActor).size(96f, 116f).padRight(10f);
        panel.add(infoColumn).expandX().fillX();
        panel.add(actionColumn).padLeft(10f);

        showDetailFor(null);
        return panel;
    }

    private void showDetailFor(PlantType type) {
        detailPlantType = type;
        if (type == null) {
            detailNameLabel.setText("یک گیاه را برای دیدن جزئیات انتخاب کنید");
            detailDescriptionLabel.setText("");
            detailCostLabel.setText("");
            detailStatusLabel.setText("");
            detailPreviewActor.setPlantType(null);
            setActionButtonsEnabled(false, false);
            return;
        }
        PlantDefinition def = PlantLibrary.findByType(type).orElse(null);
        detailPreviewActor.setPlantType(type);
        if (def == null) {
            detailNameLabel.setText(type.name());
            detailDescriptionLabel.setText("");
            detailCostLabel.setText("");
            detailStatusLabel.setText("");
            setActionButtonsEnabled(false, false);
            return;
        }
        detailNameLabel.setText(def.getName());
        String description = def.getBaseAbility() != null ? def.getBaseAbility().getRaw() : "";
        detailDescriptionLabel.setText(description == null ? "" : description);
        detailCostLabel.setText(String.valueOf(PlantLibrary.getEffectiveCost(type)));
        updateDetailStatus(type);
    }

    /** Shows the plant's current level (1-based, capped by its real JSON tier count) and boost state. */
    private void updateDetailStatus(PlantType type) {
        User user = AppStatus.currentUser;
        boolean owned = user != null && user.collectionState != null
            && user.collectionState.isPlantUnlocked(type);
        boolean selected = AppStatus.SELECTED_PLANTS.contains(type);

        if (!owned) {
            detailStatusLabel.setText("Not owned yet");
            setActionButtonsEnabled(false, false);
            return;
        }

        int currentLevel = user.collectionState.getPlantLevel(type);
        int maxRawLevel = PlantLibrary.findByType(type)
            .map(PlantDefinition::getMaxLevel)
            .orElse(4) - 1;
        boolean maxed = currentLevel >= maxRawLevel;
        boolean boosted = AppStatus.BOOSTED_PLANTS.contains(type);

        StringBuilder status = new StringBuilder("Level ").append(currentLevel + 1)
            .append(" / ").append(maxRawLevel + 1);
        if (boosted) {
            status.append("  -  BOOSTED");
        }
        detailStatusLabel.setText(status.toString());
        setActionButtonsEnabled(!maxed, selected && !boosted);
    }

    private void setActionButtonsEnabled(boolean upgradeEnabled, boolean boostEnabled) {
        if (upgradeButton != null) {
            upgradeButton.setColor(1f, 1f, 1f, upgradeEnabled ? 1f : 0.5f);
            upgradeButton.setTouchable(upgradeEnabled
                ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
                : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }
        if (boostButton != null) {
            boostButton.setColor(1f, 1f, 1f, boostEnabled ? 1f : 0.5f);
            boostButton.setTouchable(boostEnabled
                ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
                : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }
    }

    private void onUpgradeClicked() {
        if (detailPlantType == null) {
            return;
        }
        OutputDTO result = new CollectionMenuController().handle(
            new CollectionInputDTO(CollectionCommand.UPGRADE_PLANT, detailPlantType.name(), null));
        statusLabel.setText(stripColorCodes(result.getMessage()));
        updateDetailStatus(detailPlantType);
        refresh();
    }

    private void onBoostClicked() {
        if (detailPlantType == null) {
            return;
        }
        OutputDTO result = plantController.handle(
            new PlantSelectionInputDTO(PlantSelectionCommand.BOOST_PLANT, detailPlantType.name()));
        statusLabel.setText(stripColorCodes(result.getMessage()));
        updateDetailStatus(detailPlantType);
        refresh();
    }

    // --- متدهای قدیمی ساخت دکمه حذف شده‌اند و دیگر استفاده نمی‌شوند ---

    private Drawable resolveCardSlotBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_cards_almanac_plant_card_10", Drawable.class)) {
                return skin.getDrawable("image_ui_cards_almanac_plant_card_10");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Drawable resolveWindowBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
        return new NinePatchDrawable(buildWindowNinePatch());
    }

    private ImageButton buildCloseButton() {
        Skin skin;
        try {
            skin = PvzSkin.get();
        } catch (Exception e) {
            return null;
        }
        if (skin == null || !skin.has("generic_close_circle", ImageButton.ImageButtonStyle.class)) {
            return null;
        }
        ImageButton button = new ImageButton(skin, "generic_close_circle");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClosePanel();
            }
        });
        return button;
    }

    private static NinePatch buildWindowNinePatch() {
        int size = 32;
        int border = 10;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.16f, 0.11f, 0.06f, 0.92f);
        pixmap.fill();
        pixmap.setColor(0.55f, 0.38f, 0.18f, 1f);
        pixmap.drawRectangle(0, 0, size, size);
        pixmap.drawRectangle(1, 1, size - 2, size - 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new NinePatch(texture, border, border, border, border);
    }

    private void onClosePanel() {
        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(PlantSelectionCommand.EXIT, null));
        if (!result.isSuccess()) {
            statusLabel.setText(stripColorCodes(result.getMessage()));
        }
    }

    private void onCardClicked(PlantCardActor card) {
        PlantType type = card.getPlantType();
        boolean currentlySelected = AppStatus.SELECTED_PLANTS.contains(type);

        if (AppStatus.isMultiplayerMatch) {
            if (currentlySelected) {
                AppStatus.SELECTED_PLANTS.remove(type);
                statusLabel.setText("");
            } else {
                if (AppStatus.SELECTED_PLANTS.size() < MAX_SELECTED_SLOTS) {
                    AppStatus.SELECTED_PLANTS.add(type);
                    statusLabel.setText("");
                } else {
                    statusLabel.setText("You can select up to " + MAX_SELECTED_SLOTS + " plants.");
                }
            }
            showDetailFor(type);
            refresh();
            return;
        }

        PlantSelectionCommand command = currentlySelected
            ? PlantSelectionCommand.REMOVE_PLANT
            : PlantSelectionCommand.ADD_PLANT;
        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(command, type.name()));
        statusLabel.setText(result.isSuccess() ? "" : stripColorCodes(result.getMessage()));
        showDetailFor(type);
        refresh();
    }

    private void onLetsRock() {
        if (AppStatus.isMultiplayerMatch) {
            if (AppStatus.SELECTED_PLANTS.isEmpty()) {
                statusLabel.setText("Please select at least 1 plant.");
                return;
            }
            AppStatus.currentMenuType = MenuType.IN_GAME;
            com.PVZ.model.game.IZombieMultiplayerGameEngine engine = new com.PVZ.model.game.IZombieMultiplayerGameEngine(
                "PLANT",
                AppStatus.multiplayerOpponent,
                AppStatus.multiplayerRoomId,
                AppStatus.multiplayerLevelId,
                new ArrayList<>(AppStatus.SELECTED_PLANTS),
                null
            );
            AppStatus.setGameEngine(engine);
            ScreenManager.getInstance().performTransition(() -> new GameScreen(
                "maps/Frontyard.jpg",
                "music/TitleScreen.mp3",
                engine
            ));
            return;
        }

        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(PlantSelectionCommand.START_GAME, null));
        if (!result.isSuccess()) {
            statusLabel.setText(stripColorCodes(result.getMessage()));
            return;
        }
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            ChapterMapPaths.resolve(chapterName),
            "music/TitleScreen.mp3",
            AppStatus.getGameEngine()
        ));
    }

    public void refresh() {
        if (!readyToShow || cards.isEmpty()) {
            return;
        }
        User user = AppStatus.currentUser;
        for (PlantCardActor card : cards) {
            PlantType type = card.getPlantType();
            boolean owned = user != null && user.collectionState != null
                && user.collectionState.isPlantUnlocked(type);
            boolean stageLocked = !AppStatus.isMultiplayerMatch && AppStatus.CURRENT_STAGE_LOCKED_PLANTS.contains(type);
            boolean selected = AppStatus.SELECTED_PLANTS.contains(type);
            // A plant already selected is never locked out by its own family (that check
            // only blocks *other* members of the family), so it stays tappable to remove.
            boolean familyLocked = !AppStatus.isMultiplayerMatch && !selected && plantController.isFamilyLockedByOtherPick(type);
            card.setLocked(!owned || stageLocked || familyLocked);
            card.setSelected(selected);
        }
        countLabel.setText(AppStatus.SELECTED_PLANTS.size() + " / 8 selected");
        updateSelectedTray();
    }

    private static String stripColorCodes(String s) {
        return s == null ? "" : s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private static final class DetailPreviewActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private PlantType type;
        private float animTime;

        void setPlantType(PlantType type) {
            this.type = type;
            this.animTime = 0f;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            animTime += delta;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            if (type == null) {
                return;
            }
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            EntityRenderer.getInstance().renderPlant((com.badlogic.gdx.graphics.g2d.SpriteBatch) batch,
                type.name(), animTime, cx, cy);
            batch.setColor(Color.WHITE);
        }
    }

    private static final class SunIconActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
        private float animTime;

        @Override
        public void act(float delta) {
            super.act(delta);
            animTime += delta;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            EntityRenderer.getInstance().renderPam(
                (com.badlogic.gdx.graphics.g2d.SpriteBatch) batch, SUN_PAM, animTime, cx, cy);
            batch.setColor(Color.WHITE);
        }
    }
}
