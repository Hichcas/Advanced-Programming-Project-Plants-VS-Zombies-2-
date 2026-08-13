package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.ChapterAndLevelSelectionMenuController;
import com.PVZ.controller.menuControllers.InGameMenuController;
import com.PVZ.controller.menuControllers.ShopMenuController;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand;
import com.PVZ.model.enums.commands.InGameCommand;
import com.PVZ.model.enums.commands.ShopCommand;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;
import com.PVZ.view.input.DTO.InGameInputDTO;
import com.PVZ.view.input.DTO.ShopInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.CursorManager;
import com.PVZ.view.screen.manager.SoundManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

/**
 * Graphical "cheat shop": a floating, shop-styled window that lists every cheat
 * command described in the design doc as a clickable card instead of a typed
 * command. It never replaces the text pipeline — every button here simply
 * builds the exact same InputDTO the CLI parser would have produced and hands
 * it to the existing controllers, so game rules/validation stay in one place.
 *
 * The panel is a plain overlay actor (NOT routed through PanelManager, which
 * swaps full-screen panels). That means it can be dropped on top of the stage
 * of ANY screen — menus or the live GameScreen — without disturbing whatever
 * else is already on that stage. Use {@link #attachToggleButton(Stage)} once
 * per screen to get a floating skull button that opens/closes it.
 */
public class CheatPanel extends BasePanel {
    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;
    private static final float PANEL_W = 1500f;
    private static final float PANEL_H = 900f;
    /** Pushed down from dead-center so the title/tabs never clip the top of the virtual screen. */
    private static final float PANEL_Y = (VH - PANEL_H) / 2f - 110f;
    private static final int GRID_COLUMNS = 4;
    private static final float CARD_W = 330f;
    private static final float CARD_H = 300f;
    private static final String SKULL_PAM = "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM";
    private static final String COIN_PAM = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM";
    private static final String DIAMOND_PAM = "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM";
    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String SPARKLE_PAM = "768/INITIAL/UI/STORE/CARD_SPARKLE/CARD_SPARKLE.PAM";

    /** Bridge to the currently running level, supplied only when the panel is opened in-game. */
    public interface InGameCheatBridge {
        OutputDTO handle(InGameInputDTO dto);
    }

    private final Skin skin = PvzSkin.get();
    private final BitmapFont titleFont = FontManager.getInstance().getEnglishTitleFont();
    private final BitmapFont bodyFont = FontManager.getInstance().getEnglishMenuFont();
    /** Lighter, non-outlined font for description/body copy - keeps titleFont/bodyFont for headers and buttons. */
    private final BitmapFont descFont = FontManager.getInstance().getEnglishTinyFont();
    private final ChapterAndLevelSelectionMenuController economyController = new ChapterAndLevelSelectionMenuController();
    private final ShopMenuController shopController = new ShopMenuController();
    private final InGameCheatBridge inGameBridge;

    private Table content;
    private Label statusLabel;
    private String activeTab = "economy";

    private CheatPanel(InGameCheatBridge inGameBridge) {
        this.inGameBridge = inGameBridge;
        setSize(PANEL_W, PANEL_H);
        setPosition((VW - PANEL_W) / 2f, Math.max(40f, PANEL_Y));
        build();
    }

    /** Cheat panel with only economy/chapter cheats (safe from any menu screen). */
    public static CheatPanel forMenu() {
        return new CheatPanel(null);
    }

    /** Cheat panel that also exposes live in-game cheats (suns, plant food, zombies, nuke...). */
    public static CheatPanel forGame(InGameCheatBridge bridge) {
        return new CheatPanel(bridge);
    }

    /**
     * Adds a small floating skull button to {@code stage} that toggles a fresh
     * CheatPanel on/off. Safe to call from every screen's constructor - one line.
     *
     * The button registers itself with {@link com.PVZ.view.screen.manager.PanelManager}
     * as a persistent overlay, so it keeps floating above LoginPanel, RegisterPanel,
     * ShopPanel, ChapterSelectPanel, etc. - every panel the manager swaps in from
     * now on - instead of getting buried underneath the next full-screen panel.
     */
    public static Actor attachToggleButton(Stage stage, InGameCheatBridge bridgeOrNull) {
        Table corner = new Table();
        corner.setFillParent(true);
        corner.left().pad(18f);

        CheatButtonActor icon = new CheatButtonActor();
        icon.addListener(new ClickListener() {
            private CheatPanel open;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    SoundManager.getInstance().playSound(getHoverSound());
                    CursorManager.getInstance().setPointerMode(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    CursorManager.getInstance().setPointerMode(false);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound(getClickSound());
                if (open != null) {
                    open.remove();
                    open.dispose();
                    open = null;
                    return;
                }
                open = bridgeOrNull == null ? CheatPanel.forMenu() : CheatPanel.forGame(bridgeOrNull);
                stage.addActor(open);
                open.toFront();
            }
        });
        corner.add(icon).size(84f);
        stage.addActor(corner);
        com.PVZ.view.screen.manager.PanelManager.getInstance().addPersistentOverlay(corner);
        return corner;
    }

    private void build() {
        Drawable panelBg = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        setBackground(panelBg);
        pad(30f);
        clearChildren();

        Table root = new Table();
        root.setFillParent(true);
        addActor(root);

        Table header = new Table();
        SkullIconActor skull = new SkullIconActor();
        header.add(skull).size(72f).padRight(12f);
        Label title = new Label("CHEAT VAULT", new Label.LabelStyle(titleFont, Color.GOLD));
        header.add(title).left();
        root.add(header).growX().height(80f).row();

        Table tabs = new Table();
        tabs.add(makeTextButton("ECONOMY", tabStyle("economy"), () -> switchTab("economy"))).width(280f).height(58f).pad(6f);
        tabs.add(makeTextButton("CHAPTERS", tabStyle("chapters"), () -> switchTab("chapters"))).width(280f).height(58f).pad(6f);
        tabs.add(makeTextButton("PLANTS", tabStyle("plants"), () -> switchTab("plants"))).width(280f).height(58f).pad(6f);
        if (inGameBridge != null) {
            tabs.add(makeTextButton("IN-GAME", tabStyle("ingame"), () -> switchTab("ingame"))).width(280f).height(58f).pad(6f);
        }
        root.add(tabs).growX().height(70f).row();

        ScrollPane scroll = buildScrollableContent();
        root.add(scroll).grow().row();

        Table footer = new Table();
        statusLabel = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        footer.add(statusLabel).growX().width(1050f);
        footer.add(makeTextButton("CLOSE", "brown", () -> { remove(); dispose(); })).width(180f).height(58f);
        root.add(footer).growX().height(70f).bottom();

        refresh();
    }

    private ScrollPane buildScrollableContent() {
        content = new Table();
        content.top();
        content.defaults().pad(10f);
        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        return scroll;
    }

    private String tabStyle(String tabId) {
        return activeTab.equals(tabId) ? "green" : "brown";
    }

    private void switchTab(String tabId) {
        this.activeTab = tabId;
        build();
    }

    private void refresh() {
        content.clearChildren();
        User user = AppStatus.getCurrentUser();
        if (user == null) {
            content.add(new Label("Please login first.", new Label.LabelStyle(bodyFont, Color.SALMON))).grow();
            return;
        }
        switch (activeTab) {
            case "chapters" -> buildChapterTab();
            case "plants" -> buildPlantsTab();
            case "ingame" -> buildInGameTab();
            default -> buildEconomyTab();
        }
    }

    // ---------------------------------------------------------------- economy

    /** Half of the usable inner card width - every 2-across button/field row in a card uses this. */
    private static final float HALF_COL = (CARD_W - 2f * 16f) / 2f;

    private void buildEconomyTab() {
        int col = 0;
        col = addCurrencyCard(col, "COINS", COIN_PAM, "coin");
        col = addCurrencyCard(col, "DIAMONDS", DIAMOND_PAM, "diamond");
        col = addActionCard(col, "UNLOCK ALL CHAPTERS", "Every chapter and stage becomes playable.",
            () -> runEconomy(ChapterAndLevelSelectionCommand.CHEAT_UNLOCK_ALL, null, null, null, null));
        col = addActionCard(col, "LOCK ALL CHAPTERS", "Resets progress back to locked (for testing).",
            () -> runEconomy(ChapterAndLevelSelectionCommand.CHEAT_LOCK_ALL, null, null, null, null));
    }

    private int addCurrencyCard(int col, String label, String iconPam, String currency) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(8f);

        cell.add(new Label(label, new Label.LabelStyle(titleFont, Color.WHITE))).growX().height(40f).row();
        cell.add(new PamIconActor(iconPam)).size(110f).row();

        TextField amountField = new TextField("100", skin);
        amountField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        cell.add(amountField).width(2f * HALF_COL).height(52f).row();

        cell.add(makeTextButton("ADD", "green_small", () -> {
            int amount = parseIntOr(amountField.getText(), 100);
            runEconomy(ChapterAndLevelSelectionCommand.CHEAT_ADD, null, amount, currency, null);
        })).width(2f * HALF_COL).height(56f).row();

        return placeCard(cell, col);
    }

    // --------------------------------------------------------------- chapters

    private void buildChapterTab() {
        int col = 0;
        for (ChapterEnum chapter : ChapterEnum.values()) {
            col = addChapterCard(col, chapter);
        }
    }

    /**
     * Every row in this card is exactly two HALF_COL-wide cells, so the field/
     * button in row 2 line up cleanly under the buttons in row 1 instead of
     * spilling past the edge of the card.
     */
    private int addChapterCard(int col, ChapterEnum chapter) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(4f).align(Align.center);

        Label nameLabel = new Label(chapter.getDisplayName(), new Label.LabelStyle(titleFont, Color.GOLD));
        nameLabel.setAlignment(Align.center);
        nameLabel.setWrap(true);
        cell.add(nameLabel).growX().height(48f).colspan(2).row();

        cell.add(makeTextButton("COMPLETE", "green_small",
            () -> runEconomy(ChapterAndLevelSelectionCommand.CHEAT_COMPLETE_CHAPTER, chapter.name(), null, null, null)))
            .width(HALF_COL).height(50f);
        cell.add(makeTextButton("LOCK", "brown",
            () -> runEconomy(ChapterAndLevelSelectionCommand.CHEAT_LOCK_CHAPTER, chapter.name(), null, null, null)))
            .width(HALF_COL).height(50f).row();

        TextField stageField = new TextField("1", skin);
        stageField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        cell.add(stageField).width(HALF_COL).height(50f);
        cell.add(makeTextButton("STAGE +", "green_small", () -> {
            int stage = parseIntOr(stageField.getText(), 1);
            runEconomy(ChapterAndLevelSelectionCommand.CHEAT_COMPLETE_STAGE, chapter.name(), null, null, stage);
        })).width(HALF_COL).height(50f).row();

        cell.add(makeTextButton("LOCK THAT STAGE", "brown", () -> {
            int stage = parseIntOr(stageField.getText(), 1);
            runEconomy(ChapterAndLevelSelectionCommand.CHEAT_LOCK_STAGE, chapter.name(), null, null, stage);
        })).width(2f * HALF_COL).height(46f).colspan(2).row();

        return placeCard(cell, col);
    }

    // --------------------------------------------------------------- in-game

    private void buildInGameTab() {
        if (inGameBridge == null) {
            content.add(new Label("Not available here - open the cheat vault from inside a level.",
                new Label.LabelStyle(bodyFont, Color.SALMON))).grow();
            return;
        }
        int col = 0;

        Table sunCell = new Table();
        sunCell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        sunCell.defaults().pad(8f);
        sunCell.add(new Label("SUNS", new Label.LabelStyle(titleFont, Color.WHITE))).growX().height(40f).row();
        sunCell.add(new PamIconActor(SUN_PAM)).size(110f).row();
        TextField sunField = new TextField("100", skin);
        sunField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        sunCell.add(sunField).width(2f * HALF_COL).height(52f).row();
        sunCell.add(makeTextButton("ADD", "green_small", () ->
            runInGame(new InGameInputDTO(InGameCommand.CHEAT_ADD_SUNS, null,
                parseIntOr(sunField.getText(), 100), null, null, null, null)))).width(2f * HALF_COL).height(56f).row();
        col = placeCard(sunCell, col);

        col = addActionCard(col, "ADD PLANT FOOD", "+1 plant food, up to the cap of 3.",
            () -> runInGame(new InGameInputDTO(InGameCommand.CHEAT_ADD_PLANT_FOOD, null, null, null, null, null, null)));

        col = addActionCard(col, "REMOVE COOLDOWNS", "Every plant becomes plantable immediately.",
            () -> runInGame(new InGameInputDTO(InGameCommand.CHEAT_REMOVE_COOLDOWN, null, null, null, null, null, null)));

        col = addActionCard(col, "RELEASE THE NUKE", "Kills every zombie currently on the lawn.",
            () -> runInGame(new InGameInputDTO(InGameCommand.CHEAT_RELEASE_NUKE, null, null, null, null, null, null)));

        col = addSpawnZombieCard(col);
        col = addTileCard(col, "SET TILE WATERED", "green_small", InGameCommand.CHEAT_SET_WATER);
        col = addTileCard(col, "SET TILE DRY", "brown", InGameCommand.CHEAT_SET_DRY);
    }

    private int addSpawnZombieCard(int col) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(4f).align(Align.center);

        cell.add(new Label("SPAWN ZOMBIE", new Label.LabelStyle(titleFont, Color.WHITE))).growX().colspan(2).height(32f).row();

        SelectBox<ZombieType> zombieSelect = new SelectBox<>(skin);
        zombieSelect.setItems(ZombieType.values());
        cell.add(zombieSelect).width(2f * HALF_COL).height(46f).colspan(2).row();

        TextField xField = new TextField("0", skin);
        xField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        TextField yField = new TextField("0", skin);
        yField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        cell.add(labeledField("Col (x)", xField, skin)).width(HALF_COL);
        cell.add(labeledField("Row (y)", yField, skin)).width(HALF_COL).row();

        cell.add(makeTextButton("SPAWN", "green_small", () -> {
            ZombieType type = zombieSelect.getSelected();
            int x = parseIntOr(xField.getText(), 0);
            int y = parseIntOr(yField.getText(), 0);
            runInGame(new InGameInputDTO(InGameCommand.CHEAT_SPAWN_ZOMBIE, null, null, null, type.name(), x, y));
        })).colspan(2).width(2f * HALF_COL).height(50f).row();

        return placeCard(cell, col);
    }

    /** Shared layout for the two tile-state cheats (set-water / set-dry) - just a col/row pair. */
    private int addTileCard(int col, String title, String buttonStyle, InGameCommand command) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(4f).align(Align.center);

        Label label = new Label(title, new Label.LabelStyle(titleFont, Color.WHITE));
        label.setWrap(true);
        label.setAlignment(Align.center);
        cell.add(label).growX().colspan(2).height(60f).row();

        TextField xField = new TextField("0", skin);
        xField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        TextField yField = new TextField("0", skin);
        yField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        cell.add(labeledField("Col (x)", xField, skin)).width(HALF_COL);
        cell.add(labeledField("Row (y)", yField, skin)).width(HALF_COL).row();

        cell.add(makeTextButton("APPLY", buttonStyle, () -> {
            int x = parseIntOr(xField.getText(), 0);
            int y = parseIntOr(yField.getText(), 0);
            runInGame(new InGameInputDTO(command, null, null, null, null, x, y));
        })).colspan(2).width(2f * HALF_COL).height(50f).row();

        return placeCard(cell, col);
    }

    private static Table labeledField(String label, TextField field, Skin skin) {
        Table t = new Table();
        t.add(new Label(label, skin)).row();
        t.add(field).growX();
        return t;
    }

    // ----------------------------------------------------------------- plants

    private void buildPlantsTab() {
        int col = 0;
        col = addActionCard(col, "UNLOCK ALL PLANTS", "Every plant species becomes available in the seed bank.",
            () -> runShop(ShopCommand.UNLOCK_ALL_PLANTS, null));
        col = addPlantPickerCard(col);
    }

    /**
     * Shop-style card: a live idle animation of the currently selected plant on
     * top, a species dropdown, and an UNLOCK button - so a single specific plant
     * can be unlocked without touching the rest of the collection.
     */
    private int addPlantPickerCard(int col) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(4f).align(Align.center);

        cell.add(new Label("UNLOCK SPECIES", new Label.LabelStyle(titleFont, Color.GOLD)))
            .growX().height(28f).row();

        Table previewHolder = new Table();
        cell.add(previewHolder).size(96f, 116f).row();

        SelectBox<PlantType> plantSelect = new SelectBox<>(skin);
        plantSelect.setItems(PlantType.values());
        cell.add(plantSelect).width(2f * HALF_COL).height(46f).row();

        Runnable refreshPreview = () -> {
            previewHolder.clearChildren();
            PlantType selected = plantSelect.getSelected();
            if (selected != null) {
                previewHolder.add(new PlantPreviewActor(selected)).size(90f, 110f);
            }
        };
        refreshPreview.run();
        plantSelect.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshPreview.run();
            }
        });

        cell.add(makeTextButton("UNLOCK", "green_small", () -> {
            PlantType selected = plantSelect.getSelected();
            if (selected != null) {
                runShop(ShopCommand.UNLOCK_PLANT, selected.name());
            }
        })).width(2f * HALF_COL).height(50f).row();

        return placeCard(cell, col);
    }

    private void runShop(ShopCommand command, String plantType) {
        OutputDTO result = shopController.handle(new ShopInputDTO(command, null, null, plantType));
        setStatus(result);
    }

    private int addActionCard(int col, String title, String description, Runnable action) {
        Table cell = new Table();
        cell.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        cell.defaults().pad(8f).align(Align.center);
        Label titleLabel = new Label(title, new Label.LabelStyle(titleFont, Color.WHITE));
        titleLabel.setAlignment(Align.center);
        cell.add(titleLabel).growX().height(40f).row();
        Label desc = new Label(description, new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        desc.setWrap(true);
        desc.setAlignment(Align.center);
        cell.add(desc).growX().height(80f).row();
        cell.add(makeTextButton("APPLY", "green_small", action)).width(2f * HALF_COL).height(56f).row();
        return placeCard(cell, col);
    }

    /**
     * Every card is placed in a fixed-size CARD_W x CARD_H slot in a GRID_COLUMNS-wide
     * grid, top-aligned, so cards of different internal shape (chapter cards, action
     * cards, currency cards...) still line up cleanly row after row instead of
     * drifting based on their own content height.
     */
    private int placeCard(Table cell, int col) {
        content.add(cell).width(CARD_W).height(CARD_H).top();
        int next = col + 1;
        if (next % GRID_COLUMNS == 0) {
            content.row();
        }
        return next;
    }

    private void runEconomy(ChapterAndLevelSelectionCommand command, String chapterName, Integer amount,
                            String currency, Integer stage) {
        OutputDTO result = economyController.handle(
            new ChapterAndLevelSelectionInputDTO(command, chapterName, amount, currency, stage));
        setStatus(result);
    }

    private void runInGame(InGameInputDTO dto) {
        if (inGameBridge == null) {
            return;
        }
        setStatus(inGameBridge.handle(dto));
    }

    private void setStatus(OutputDTO result) {
        if (result == null) {
            statusLabel.setText("");
            return;
        }
        statusLabel.setColor(result.isSuccess() ? Color.LIME : Color.SALMON);
        statusLabel.setText(result.getMessage());
    }

    private static int parseIntOr(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Sound hoverSound;
    private static Sound clickSound;

    private static Sound getHoverSound() {
        if (hoverSound == null) {
            hoverSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomSelection.mp3"));
        }
        return hoverSound;
    }

    private static Sound getClickSound() {
        if (clickSound == null) {
            clickSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomClicked.mp3"));
        }
        return clickSound;
    }

    /**
     * Same feedback every other button in the game gives: a hover blip
     * (+ tiny grow) from {@code global/BottomSelection.mp3} and a click
     * pop (+ tiny squash) from {@code global/BottomClicked.mp3}, exactly
     * like {@link com.PVZ.view.screen.ui.MenuButton} does elsewhere.
     */
    private TextButton makeTextButton(String text, String style, Runnable action) {
        TextButton.TextButtonStyle styleData = skin.get(style, TextButton.TextButtonStyle.class);
        TextButton button = new TextButton(text, styleData);
        button.getLabel().setFontScale(0.85f);
        button.setTransform(true);
        button.setOrigin(Align.center);
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    SoundManager.getInstance().playSound(getHoverSound());
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.06f, 1.06f, 0.08f));
                    CursorManager.getInstance().setPointerMode(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1f, 1f, 0.08f));
                    CursorManager.getInstance().setPointerMode(false);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playSound(getClickSound());
                button.clearActions();
                button.addAction(Actions.sequence(
                    Actions.scaleTo(0.92f, 0.92f, 0.05f),
                    Actions.scaleTo(1f, 1f, 0.08f)));
                action.run();
            }
        });
        return button;
    }

    // ----------------------------------------------------------- pam actors

    private static final class PamIconActor extends Actor {
        private final String path;
        private float time;
        PamIconActor(String path) { this.path = path; }
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPam((SpriteBatch) batch, path, time, getX() + 55f, getY() + 55f);
        }
    }

    private static final class SkullIconActor extends Actor {
        private float time;
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPam((SpriteBatch) batch, SKULL_PAM, time, getX() + 36f, getY() + 36f);
        }
    }

    /** Small live idle-animation preview of a plant species, reusing the same renderer the lawn uses. */
    private static final class PlantPreviewActor extends Actor {
        private final PlantType type;
        private float time = (float) (Math.random() * 2.0);
        PlantPreviewActor(PlantType type) { this.type = type; }
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPlant((SpriteBatch) batch, type.name(), time,
                getX() + getWidth() / 2f, getY() + getHeight() * 0.55f);
        }
    }

    /** Small floating skull button used to open/close the cheat vault from any screen. */
    private static final class CheatButtonActor extends Actor {
        private float time;
        CheatButtonActor() { setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled); }
        @Override public void act(float delta) { super.act(delta); time += delta; }
        @Override public void draw(Batch batch, float parentAlpha) {
            EntityRenderer.getInstance().renderPam((SpriteBatch) batch, SKULL_PAM, time, getX() + 42f, getY() + 42f);
        }
    }
}
