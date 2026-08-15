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
import com.PVZ.controller.menuControllers.PlantSelectionMenuController;
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
    private Label statusLabel;
    private Label countLabel;
    private String chapterName;
    private boolean readyToShow = true;
    private Label detailNameLabel;
    private Label detailDescriptionLabel;
    private Label detailCostLabel;
    private DetailPreviewActor detailPreviewActor;
    private PlantType detailPlantType;

    public PlantSelectionPanel(String chapterName, int stage) {
        this.chapterName = chapterName;
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
                "music/Title Screen.mp3",
                AppStatus.getGameEngine()
            ));
            return;
        }

        buildPicker(chapterName);
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
        window.setBackground(resolveWindowBackground());
        window.add(detailPanel).width(6 * 150f).padBottom(14f).row();
        window.add(scrollPane).size(6 * 150f, 4 * 175f).row();
        window.add(countLabel).padTop(8f).row();
        window.add(statusLabel).padTop(4f).row();
        window.add(letsRock).padTop(12f).size(240f, 64f).row();
        Stack windowStack = new Stack();
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
        SunIconActor sunIcon = new SunIconActor();

        Table infoColumn = new Table();
        infoColumn.top().left();
        infoColumn.add(detailNameLabel).left().row();
        infoColumn.add(detailDescriptionLabel).left().width(4 * 150f).padTop(4f).row();
        Table costRow = new Table();
        costRow.add(sunIcon).size(28f, 28f).padRight(6f);
        costRow.add(detailCostLabel).left();
        infoColumn.add(costRow).left().padTop(6f).row();

        // دکمه‌های UPGRADE و BOOST با MenuButton سفارشی
        Texture purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = null;

        MenuButton upgradeButton = new MenuButton(purpleUp, "UPGRADE", font, purpleDown, null, marker, this::onUpgradeClicked);
        upgradeButton.setSize(140f, 48f);
        MenuButton boostButton = new MenuButton(purpleUp, "BOOST", font, purpleDown, null, marker, this::onBoostClicked);
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
            detailPreviewActor.setPlantType(null);
            return;
        }
        PlantDefinition def = PlantLibrary.findByType(type).orElse(null);
        detailPreviewActor.setPlantType(type);
        if (def == null) {
            detailNameLabel.setText(type.name());
            detailDescriptionLabel.setText("");
            detailCostLabel.setText("");
            return;
        }
        detailNameLabel.setText(def.getName());
        String description = def.getBaseAbility() != null ? def.getBaseAbility().getRaw() : "";
        detailDescriptionLabel.setText(description == null ? "" : description);
        detailCostLabel.setText(String.valueOf(PlantLibrary.getEffectiveCost(type)));
    }

    private void onUpgradeClicked() {
        if (detailPlantType == null) {
            return;
        }
        //todo
    }

    private void onBoostClicked() {
        if (detailPlantType == null) {
            return;
        }
        // TODO
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
        PlantSelectionCommand command = currentlySelected
            ? PlantSelectionCommand.REMOVE_PLANT
            : PlantSelectionCommand.ADD_PLANT;
        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(command, type.name()));
        statusLabel.setText(result.isSuccess() ? "" : stripColorCodes(result.getMessage()));
        showDetailFor(type);
        refresh();
    }

    private void onLetsRock() {
        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(PlantSelectionCommand.START_GAME, null));
        if (!result.isSuccess()) {
            statusLabel.setText(stripColorCodes(result.getMessage()));
            return;
        }
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            ChapterMapPaths.resolve(chapterName),
            "music/Title Screen.mp3",
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
            boolean stageLocked = AppStatus.CURRENT_STAGE_LOCKED_PLANTS.contains(type);
            card.setLocked(!owned || stageLocked);
            card.setSelected(AppStatus.SELECTED_PLANTS.contains(type));
        }
        countLabel.setText(AppStatus.SELECTED_PLANTS.size() + " / 8 selected");
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
