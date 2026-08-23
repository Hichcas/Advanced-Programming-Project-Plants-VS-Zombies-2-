package com.PVZ.view.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.PVZ.controller.menuControllers.IZombieMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.enums.commands.IZombieCommand;
import com.PVZ.model.minigame.izombie.IZombieLevelDefinition;
import com.PVZ.model.minigame.izombie.IZombieLevelLoader;
import com.PVZ.model.minigame.izombie.IZombieTexturePaths;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.IZombieInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.MenuButton;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

/**
 * "Choose your zombies" screen for I, Zombie, shown before the level starts - same
 * look and flow as {@link PlantSelectionPanel}, but for the level's fixed 5-zombie
 * roster instead of the player's whole plant collection. Up to
 * {@link #MAX_SELECTED_SLOTS} zombies can be brought into the level (the roster
 * itself only ever has 5 entries per level, same slot cap as plant selection for a
 * consistent look).
 */
public class ZombieSelectionPanel extends BasePanel {

    private static final int MAX_SELECTED_SLOTS = 8;

    private final int levelId;
    private final List<ZombieOption> roster = new ArrayList<>();
    private final List<ZombieCardActor> cards = new ArrayList<>();
    private final List<ZombiePreviewActor> selectedSlotPreviews = new ArrayList<>();
    private Label countLabel;
    private Label statusLabel;
    private boolean readyToShow = true;

    public ZombieSelectionPanel(int levelId) {
        this.levelId = levelId;

        User user = AppStatus.getCurrentUser();
        if (user != null && user.collectionState != null && !user.collectionState.getSeenZombies().isEmpty()) {
            for (ZombieType zt : user.collectionState.getSeenZombies()) {
                int cost = calculateZombieCost(zt);
                roster.add(new ZombieOption(zt.alias, cost, formatZombieDisplayName(zt)));
            }
        } else {
            for (ZombieType zt : ZombieType.values()) {
                int cost = calculateZombieCost(zt);
                roster.add(new ZombieOption(zt.alias, cost, formatZombieDisplayName(zt)));
            }
        }

        if (roster.isEmpty()) {
            buildErrorOnly("This I, Zombie level has no zombie roster configured.");
            return;
        }

        buildPicker();
    }

    private static int calculateZombieCost(ZombieType type) {
        if (type == null) return 50;
        String name = type.name().toUpperCase();
        if (name.contains("GARGANTUAR") || name.contains("ZOMBOSS")) return 300;
        if (name.contains("BRICK") || name.contains("KNIGHT") || name.contains("ARMOR2") || name.contains("CENTURION")) return 150;
        if (name.contains("BUCKET") || name.contains("BARREL") || name.contains("JALAPENO") || name.contains("SQUASH")) return 125;
        if (name.contains("CONE") || name.contains("ARMOR1") || name.contains("HELMET") || name.contains("FLAG")) return 75;
        if (name.contains("IMP")) return 25;
        if (name.contains("ZOMBOTANY")) return 100;
        return 50;
    }

    private static String formatZombieDisplayName(ZombieType type) {
        if (type == null) return "Zombie";
        String name = type.name().replace("ZOMBOTANY_", "Zombotany ").replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String part : name.split(" ")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void buildErrorOnly(String message) {
        setFillParent(true);
        align(Align.center);
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        statusLabel = new Label(message, new Label.LabelStyle(font, Color.SALMON));
        add(statusLabel).pad(20f).row();
    }

    private void buildPicker() {
        setFillParent(true);
        align(Align.center);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        Image levelBackground = new Image(new Texture(
            com.badlogic.gdx.Gdx.files.internal(IZombieTexturePaths.BACKGROUND_LEFT)));
        levelBackground.setFillParent(true);
        levelBackground.setScaling(com.badlogic.gdx.utils.Scaling.fill);
        addActor(levelBackground);

        Label title = new Label("Choose Your Zombies", new Label.LabelStyle(font, Color.WHITE));
        title.setFontScale(1.2f);

        Table selectedTray = buildSelectedTray();

        Table grid = new Table();
        grid.top().left();
        Drawable cardSlotBg = resolveCardSlotBackground();
        int col = 0;
        for (ZombieOption option : roster) {
            ZombieCardActor card = new ZombieCardActor(option, font);
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

            grid.add(cell).size(slotW, slotH).pad(14f);
            col++;
            if (col >= 7) {
                col = 0;
                grid.row();
            }
        }

        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane =
            new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(grid);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        countLabel = new Label("0 / " + MAX_SELECTED_SLOTS + " selected", new Label.LabelStyle(font, Color.WHITE));
        statusLabel = new Label("", new Label.LabelStyle(font, Color.SALMON));

        MenuButton letsRock;
        try {
            Skin skin = PvzSkin.get();
            Drawable greenUp = skin.getDrawable("image_ui_generic_greenbutton_10");
            Drawable greenDown = skin.getDrawable("image_ui_generic_greenbutton_down_10");
            letsRock = new MenuButton(greenUp, "LET'S ROCK", font, greenDown, null, null, this::onLetsRock);
        } catch (Exception ex) {
            letsRock = new MenuButton("LET'S ROCK", font, this::onLetsRock);
        }
        letsRock.setSize(240f, 64f);

        Table window = new Table();
        window.pad(24f);
        window.add(title).padBottom(10f).row();
        window.add(selectedTray).padBottom(10f).row();
        window.add(scrollPane).size(1400f, 620f).padBottom(14f).row();
        window.add(countLabel).padTop(8f).row();
        window.add(statusLabel).padTop(4f).row();
        window.add(letsRock).padTop(12f).size(240f, 64f).row();

        Image windowBackdrop = new Image(resolveWindowBackground());
        windowBackdrop.setColor(1f, 1f, 1f, 0.6f);

        Stack windowStack = new Stack();
        windowStack.add(windowBackdrop);
        windowStack.add(window);

        add(windowStack).center();

        refresh();
    }

    /** Up to 8 slots showing the currently selected zombies, same tray style as plant selection. */
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
            ZombiePreviewActor preview = new ZombiePreviewActor();
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
        List<String> selected = new ArrayList<>(AppStatus.SELECTED_ZOMBIES);
        for (int i = 0; i < selectedSlotPreviews.size(); i++) {
            String alias = i < selected.size() ? selected.get(i) : null;
            selectedSlotPreviews.get(i).setAlias(alias);
        }
    }

    private void onCardClicked(ZombieCardActor card) {
        String alias = card.getOption().getAlias();
        boolean currentlySelected = AppStatus.SELECTED_ZOMBIES.contains(alias);
        if (currentlySelected) {
            // Keep at least one zombie selected - an empty roster can never start a level.
            if (AppStatus.SELECTED_ZOMBIES.size() <= 1) {
                statusLabel.setText("You need at least one zombie!");
                return;
            }
            AppStatus.SELECTED_ZOMBIES.remove(alias);
        } else if (AppStatus.SELECTED_ZOMBIES.size() >= MAX_SELECTED_SLOTS) {
            statusLabel.setText("You can only bring " + MAX_SELECTED_SLOTS + " zombies.");
            return;
        } else {
            AppStatus.SELECTED_ZOMBIES.add(alias);
        }
        statusLabel.setText("");
        refresh();
    }

    private void onLetsRock() {
        if (AppStatus.isMultiplayerMatch) {
            if (AppStatus.SELECTED_ZOMBIES.isEmpty()) {
                statusLabel.setText("Please select at least 1 zombie.");
                return;
            }
            AppStatus.currentMenuType = MenuType.IN_GAME;
            com.PVZ.model.game.IZombieMultiplayerGameEngine engine = new com.PVZ.model.game.IZombieMultiplayerGameEngine(
                "ZOMBIE",
                AppStatus.multiplayerOpponent,
                AppStatus.multiplayerRoomId,
                AppStatus.multiplayerLevelId,
                null,
                new ArrayList<>(AppStatus.SELECTED_ZOMBIES)
            );
            AppStatus.setGameEngine(engine);
            ScreenManager.getInstance().performTransition(() -> new GameScreen(
                "maps/Frontyard.jpg",
                "music/TitleScreen.mp3",
                engine
            ));
            return;
        }

        OutputDTO result = new IZombieMenuController().handle(
            new IZombieInputDTO(IZombieCommand.START_LEVEL, levelId, -1, -1, null));
        if (!result.isSuccess()) {
            statusLabel.setText(stripColorCodes(result.getMessage()));
            return;
        }
        AppStatus.currentMenuType = MenuType.IN_GAME;
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            "maps/Frontyard.jpg",
            "music/TitleScreen.mp3",
            AppStatus.getGameEngine()
        ));
    }

    public void refresh() {
        if (!readyToShow || cards.isEmpty()) {
            return;
        }
        for (ZombieCardActor card : cards) {
            boolean selected = AppStatus.SELECTED_ZOMBIES.contains(card.getOption().getAlias());
            card.setSelected(selected);
        }
        countLabel.setText(AppStatus.SELECTED_ZOMBIES.size() + " / " + MAX_SELECTED_SLOTS + " selected");
        updateSelectedTray();
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

    private static String stripColorCodes(String s) {
        return s == null ? "" : s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * A single roster card: animated zombie preview (rendered small, since full-size
     * zombie PAMs are much bigger than a plant seed packet and would spill out of the
     * slot), name, and cost. Tap to toggle selection.
     *
     * Draws everything itself in draw() (like PlantCardActor) instead of nesting child
     * Actors, since a plain (untransformed) Group does not auto-offset manually
     * positioned children by its own stage position - only Table's layout pass does
     * that arithmetic. Flat, self-drawing Actors sidestep the issue entirely.
     */
    private static final class ZombieCardActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private static final float WIDTH = 150f;
        private static final float HEIGHT = 170f;
        // Zombie PAMs render far larger than plant seed packets; scale way down so the
        // whole animation fits inside the card instead of being clipped.
        private static final float PREVIEW_SCALE = 0.32f;

        private final ZombieOption option;
        private final BitmapFont font;
        private boolean selected;
        private Runnable onClick;
        private float animTime = (float) (Math.random() * 2.0);
        private static Texture solidPixel;

        private static Texture solidPixel() {
            if (solidPixel == null) {
                Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
                pixmap.setColor(1f, 1f, 1f, 1f);
                pixmap.fill();
                solidPixel = new Texture(pixmap);
                pixmap.dispose();
            }
            return solidPixel;
        }

        ZombieCardActor(ZombieOption option, BitmapFont font) {
            this.option = option;
            this.font = font;
            setSize(WIDTH, HEIGHT);
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (onClick != null) onClick.run();
                }
            });
        }

        void setOnClick(Runnable onClick) {
            this.onClick = onClick;
        }

        ZombieOption getOption() {
            return option;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        boolean isSelected() {
            return selected;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            animTime += delta;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            float x = getX();
            float y = getY();
            float w = getWidth();
            float h = getHeight();

            batch.setColor(selected ? new Color(0.55f, 0.85f, 0.25f, 1f) : new Color(0.35f, 0.28f, 0.15f, 0.9f));
            batch.draw(solidPixel(), x, y, w, h);
            batch.setColor(1f, 1f, 1f, parentAlpha);

            float cx = x + w / 2f;
            float cy = y + h * 0.62f;
            try {
                EntityRenderer.getInstance().renderZombieAlias(
                    (com.badlogic.gdx.graphics.g2d.SpriteBatch) batch, option.getAlias(), "idle",
                    animTime, cx, cy, PREVIEW_SCALE);
            } catch (RuntimeException ignored) {
                // Leave the slot blank rather than crash if this alias has no clip.
            }

            if (font != null) {
                float prevScale = font.getData().scaleX;
                font.getData().setScale(0.55f);
                font.setColor(1f, 1f, 1f, parentAlpha);
                font.draw(batch, option.getDisplayName(), x + 4f, y + 22f, w - 8f, Align.center, false);
                font.getData().setScale(0.5f);
                font.setColor(1f, 0.9f, 0.2f, parentAlpha);
                font.draw(batch, option.getCost() + " sun", x + 4f, y + 8f, w - 8f, Align.center, false);
                font.getData().setScale(prevScale);
                font.setColor(Color.WHITE);
            }

            if (!selected) {
                batch.setColor(0f, 0f, 0f, 0.45f * parentAlpha);
                batch.draw(solidPixel(), x, y, w, h);
            }
            batch.setColor(Color.WHITE);
        }
    }

    /** Renders a small, looping idle animation of the given zombie alias from its PAM. */
    private static final class ZombiePreviewActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private String alias;
        private float scale = 1f;
        private float animTime;

        void setAlias(String alias) {
            this.alias = alias;
            this.animTime = 0f;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            animTime += delta;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            if (alias == null) {
                return;
            }
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;
            Color prev = batch.getColor().cpy();
            batch.setColor(prev.r, prev.g, prev.b, prev.a * parentAlpha);
            try {
                EntityRenderer.getInstance().renderZombieAlias(
                    (com.badlogic.gdx.graphics.g2d.SpriteBatch) batch, alias, "idle", animTime, cx, cy, scale);
            } catch (RuntimeException ignored) {
                // Some aliases may not resolve to a clip; leave the slot blank rather than crash.
            }
            batch.setColor(prev);
        }
    }
}
