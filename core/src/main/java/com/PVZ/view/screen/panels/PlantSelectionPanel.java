package com.PVZ.view.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
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
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.PlantCardActor;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectionPanel extends BasePanel {
    private final PlantSelectionMenuController plantController = new PlantSelectionMenuController();
    private final List<PlantCardActor> cards = new ArrayList<>();
    private Label statusLabel;
    private Label countLabel;
    private boolean readyToShow = true;

    public PlantSelectionPanel(String chapterName, int stage) {
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
                "maps/Frontyard.jpg",
                "music/Title Screen.mp3",
                AppStatus.getGameEngine()
            ));
            return;
        }

        buildPicker();
    }

    private void buildErrorOnly(String message) {
        setFillParent(true);
        align(Align.center);
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        statusLabel = new Label(message, new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.SALMON));
        add(statusLabel).pad(20f).row();
    }

    private void buildPicker() {
        setFillParent(true);
        align(Align.center);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

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

        countLabel = new Label("0 / 8 selected", new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE));
        statusLabel = new Label("", new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.SALMON));

        TextButton letsRock = buildLetsRockButton(font);

        Table window = new Table();
        window.pad(24f);
        window.setBackground(resolveWindowBackground());
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

    private TextButton buildLetsRockButton(BitmapFont font) {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("green", TextButton.TextButtonStyle.class)) {
                TextButton button = new TextButton("LET'S ROCK", skin, "green");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        onLetsRock();
                    }
                });
                return button;
            }
        } catch (Exception ignored) {
        }
        TextButton.TextButtonStyle fallbackStyle = new TextButton.TextButtonStyle();
        fallbackStyle.font = font;
        fallbackStyle.fontColor = Color.WHITE;
        TextButton fallback = new TextButton("LET'S ROCK", fallbackStyle);
        fallback.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onLetsRock();
            }
        });
        return fallback;
    }

    private Drawable resolveWindowBackground() {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10",
                    Drawable.class)) {
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
        refresh();
    }

    private void onLetsRock() {
        OutputDTO result = plantController.handle(new PlantSelectionInputDTO(PlantSelectionCommand.START_GAME, null));
        if (!result.isSuccess()) {
            statusLabel.setText(stripColorCodes(result.getMessage()));
            return;
        }
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            "maps/Frontyard.jpg",
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
}
