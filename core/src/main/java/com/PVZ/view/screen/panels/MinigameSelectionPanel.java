package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.BeghouledMenuController;
import com.PVZ.controller.menuControllers.IZombieMenuController;
import com.PVZ.controller.menuControllers.VasebreakerMenuController;
import com.PVZ.controller.menuControllers.WallnutBowlingMenuController;
import com.PVZ.controller.menuControllers.ZombotanyMenuController;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.BeghouledInputDTO;
import com.PVZ.view.input.DTO.IZombieInputDTO;
import com.PVZ.view.input.DTO.VasebreakerInputDTO;
import com.PVZ.view.input.DTO.WallnutBowlingInputDTO;
import com.PVZ.view.input.DTO.ZombotanyInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.screen.GameScreen;
import com.PVZ.view.screen.manager.ScreenManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Vector2;
import pvz.skin.PvzSkin;
import com.PVZ.view.screen.manager.FontManager;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class MinigameSelectionPanel extends BasePanel {

    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;

    private static final float CARD_GAP = 26f;
    private static final float OUTER_MARGIN = 120f;
    private static final float POPUP_WIDTH = 680f;
    private static final float POPUP_HEIGHT = 380f;

    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final Skin skin;

    private final List<Texture> disposableTextures = new ArrayList<>();
    private final List<MinigameCard> cards = new ArrayList<>();

    private final Table popup;
    private Label popupTitle;
    private Label popupOverview;
    private Label popupDetail;
    private Label popupHint;
    private Table popupButtonRow;

    private final Drawable greenUp;
    private final Drawable greenDown;
    private final Drawable purpleUp;
    private final Drawable purpleDown;
    private final Drawable brownUp;
    private final Drawable brownDown;
    private final Texture markerTexture;

    private MinigameCard hoveredCard;
    private int hoveredLevel = 1;

    public MinigameSelectionPanel() {
        setFillParent(true);
        align(Align.center);

        skin = PvzSkin.get();
        titleFont = resolveFont("FBUSV8C5EI_1_outline", FontManager.getInstance().getEnglishTitleFont());
        bodyFont = resolveFont("FBUSV8C5EI_2", FontManager.getInstance().getEnglishMenuFont());

        greenUp = resolveSkinDrawable("image_ui_generic_greenbutton_10");
        greenDown = resolveSkinDrawable("image_ui_generic_greenbutton_down_10");
        purpleUp = resolveSkinDrawable("image_ui_generic_purplebutton_10");
        purpleDown = resolveSkinDrawable("image_ui_generic_purplebutton_down_10");
        brownUp = resolveSkinDrawable("image_ui_generic_brownbutton_10");
        brownDown = resolveSkinDrawable("image_ui_generic_brownbutton_down_10");
        markerTexture = null;

        Drawable titleBg = resolveWindowBackground();
        Drawable cardBg = resolveCardBackground();
        Drawable popupBg = resolvePopupBackground();

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.setBackground(titleBg);
        root.pad(22f, 42f, 22f, 42f);

        Table top = new Table();
        Label title = new Label("MINIGAMES", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setFontScale(1.15f);
        top.add(title).padBottom(18f).row();

        Label subtitle = new Label("Hover over a card to see the levels",
            new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
        top.add(subtitle).padBottom(18f).row();

        Table row = new Table();
        row.center();
        row.defaults().padRight(CARD_GAP);

        List<MinigameInfo> infos = List.of(
            new MinigameInfo(MinigameEnum.VASEBREAKER, "vasebreaker", "vasebreaker_cover.png",
                "Break vases to reveal plants, zombies, and seeds."),
            new MinigameInfo(MinigameEnum.WALLNUT_BOWLING, "wallnut_bowling", "wallnut_bowling_cover.png",
                "Launch nuts left of the red line and clear the lane."),
            new MinigameInfo(MinigameEnum.I_ZOMBIE, "i_zombie", "i_zombie_cover.png",
                "Pick zombies instead of plants and break through the line."),
            new MinigameInfo(MinigameEnum.BEGHOULED, "beghouled", "beghouled_cover.png",
                "Swap plants to make matches and unlock stronger upgrades."),
            new MinigameInfo(MinigameEnum.ZOMBOTANY, "zombotany", "zombotany_cover.png",
                "Plant-zombie chaos: place zombie plants and survive the waves.")
        );

        float cardWidth = Math.max(300f, Math.min(340f, (VW - (OUTER_MARGIN * 2f) - (CARD_GAP * 4f)) / 5f));
        float cardHeight = Math.max(380f, Math.min(430f, VH * 0.31f));

        for (int i = 0; i < infos.size(); i++) {
            MinigameInfo info = infos.get(i);
            MinigameCard card = new MinigameCard(info, cardBg, cardWidth, cardHeight);
            cards.add(card);
            row.add(card).size(cardWidth, cardHeight).padRight(i == infos.size() - 1 ? 0f : CARD_GAP);
        }

        Table bottom = new Table();
        MenuButton backButton = new MenuButton(purpleUp, "BACK", titleFont, purpleDown, null, markerTexture,
            () -> AppStatus.setCurrentMenuType(MenuType.MAIN));
        backButton.setSize(220f, 66f);
        bottom.add(backButton).size(220f, 66f).padTop(18f);

        root.add(top).expandX().padTop(70f).row();
        root.add(row).expand().fill().padTop(20f).row();
        root.add(bottom).expandX().padBottom(46f);
        addActor(root);

        popup = buildPopup(popupBg);
        popup.setVisible(false);
        popup.setTouchable(Touchable.enabled);
        addActor(popup);

        if (!cards.isEmpty()) {
            hidePopup();
        }
    }

    private Table buildPopup(Drawable popupBg) {
        Table box = new Table();
        box.setSize(POPUP_WIDTH, POPUP_HEIGHT);
        box.setTransform(true);
        box.pad(24f);
        box.setBackground(popupBg);
        box.setTouchable(Touchable.enabled);

        popupTitle = new Label("", new Label.LabelStyle(titleFont, Color.WHITE));
        popupTitle.setFontScale(0.95f);
        popupOverview = new Label("", new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
        popupOverview.setWrap(true);
        popupDetail = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        popupDetail.setWrap(true);
        popupHint = new Label("Choose a level", new Label.LabelStyle(bodyFont, Color.GOLD));

        popupButtonRow = new Table();
        popupButtonRow.defaults().padRight(10f);

        box.add(popupTitle).left().row();
        box.add(popupOverview).left().width(POPUP_WIDTH - 60f).padTop(12f).row();
        box.add(popupDetail).left().width(POPUP_WIDTH - 60f).padTop(10f).row();
        box.add(popupHint).left().padTop(14f).row();
        box.add(popupButtonRow).left().padTop(16f).row();

        box.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // keep visible while hovered
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (toActor == null || (!toActor.isDescendantOf(box) && toActor != hoveredCard)) {
                    hidePopup();
                }
            }
        });

        return box;
    }

    private void showPopupFor(MinigameCard card, int levelId) {
        hoveredCard = card;
        hoveredLevel = levelId;
        popupTitle.setText(card.info.displayName.getDisplayName());
        popupOverview.setText(card.info.overview);
        popupHint.setText("Level " + levelId);
        popupDetail.setText(buildLevelSummary(card.info, levelId));
        rebuildDifficultyButtons(card.info);
        positionPopup(card);
        popup.setVisible(true);
        popup.toFront();
    }

    private void rebuildDifficultyButtons(MinigameInfo info) {
        popupButtonRow.clearChildren();
        popupButtonRow.defaults().padRight(12f);

        popupButtonRow.add(buildDifficultyButton(info, 1, brownUp, brownDown)).size(170f, 58f);
        popupButtonRow.add(buildDifficultyButton(info, 2, greenUp, greenDown)).size(170f, 58f);
        popupButtonRow.add(buildDifficultyButton(info, 3, purpleUp, purpleDown)).size(170f, 58f);
    }

    private MenuButton buildDifficultyButton(MinigameInfo info, int levelId, Drawable up, Drawable down) {
        Drawable useUp = up != null ? up : resolveCardBackground();
        Drawable useDown = down != null ? down : resolvePopupBackground();
        MenuButton btn = new MenuButton(useUp, "LEVEL " + levelId, bodyFont, useDown, null, markerTexture,
            () -> launchMinigame(info, levelId));
        btn.setSize(170f, 58f);
        btn.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoveredLevel = levelId;
                popupHint.setText("Level " + levelId);
                popupDetail.setText(buildLevelSummary(info, levelId));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (hoveredCard != null) {
                    popupHint.setText("Level " + hoveredLevel);
                    popupDetail.setText(buildLevelSummary(info, hoveredLevel));
                }
            }
        });
        return btn;
    }

    private void positionPopup(MinigameCard card) {
        Vector2 stagePos = card.localToStageCoordinates(new Vector2(0f, 0f));
        float x = stagePos.x + card.getWidth() + 20f;
        if (x + POPUP_WIDTH > VW - OUTER_MARGIN) {
            x = stagePos.x - POPUP_WIDTH - 20f;
        }
        if (x < OUTER_MARGIN) {
            x = OUTER_MARGIN;
        }
        float y = stagePos.y + card.getHeight() * 0.5f - POPUP_HEIGHT * 0.5f;
        y = Math.max(90f, Math.min(y, VH - POPUP_HEIGHT - 120f));
        popup.setPosition(x, y);
    }

    private void hidePopup() {
        popup.setVisible(false);
        hoveredCard = null;
    }

    private void launchMinigame(MinigameInfo info, int levelId) {
        if (info.displayName == MinigameEnum.I_ZOMBIE) {
            // I, Zombie gets its own roster-selection screen (like Plant Selection)
            // instead of jumping straight into the level with a random loadout.
            AppStatus.pendingIZombieLevelId = levelId;
            AppStatus.SELECTED_ZOMBIES.clear();
            AppStatus.currentMenuType = MenuType.I_ZOMBIE_SELECTION;
            return;
        }
        OutputDTO result;
        switch (info.displayName) {
            case VASEBREAKER -> result = new VasebreakerMenuController().handle(
                new VasebreakerInputDTO(com.PVZ.model.enums.commands.VasebreakerCommand.START_LEVEL, levelId, -1, -1));
            case WALLNUT_BOWLING -> result = new WallnutBowlingMenuController().handle(
                new WallnutBowlingInputDTO(
                    com.PVZ.model.enums.commands.WallnutBowlingCommand.START_LEVEL, levelId, -1, -1));
            case I_ZOMBIE -> result = new IZombieMenuController().handle(
                new IZombieInputDTO(com.PVZ.model.enums.commands.IZombieCommand.START_LEVEL, levelId, -1, -1, null));
            case BEGHOULED -> result = new BeghouledMenuController().handle(
                new BeghouledInputDTO(com.PVZ.model.enums.commands.BeghouledCommand.START_LEVEL, levelId));
            case ZOMBOTANY -> result = new ZombotanyMenuController().handle(
                new ZombotanyInputDTO(com.PVZ.model.enums.commands.ZombotanyCommand.START_LEVEL, levelId));
            default -> result = new OutputDTO(false, "Unknown minigame.");
        }

        if (!result.isSuccess()) {
            popupHint.setText(stripColorCodes(result.getMessage()));
            return;
        }

        AppStatus.setCurrentMenuType(MenuType.IN_GAME);
        ScreenManager.getInstance().performTransition(() -> new GameScreen(
            "maps/Frontyard.jpg",
            "music/TitleScreen.mp3",
            AppStatus.getGameEngine()
        ));
    }

    private String buildLevelSummary(MinigameInfo info, int levelId) {
        switch (info.dataKey) {
            case "vasebreaker":
                return buildVasebreakerSummary(levelId);
            case "wallnut_bowling":
                return buildWallnutBowlingSummary(levelId);
            case "i_zombie":
                return buildIZombieSummary(levelId);
            case "beghouled":
                return buildBeghouledSummary(levelId);
            case "zombotany":
                return buildZombotanySummary(levelId);
            default:
                return "Level " + levelId;
        }
    }

    private String buildVasebreakerSummary(int levelId) {
        JsonNode level = jsonLevel("vasebreaker", levelId);
        if (level == null) return "Level " + levelId;
        return String.format(
            "Level %d  •  %d vases  •  seed packets last %.1fs  •  plant chance %.0f%%  •  gargantuar chance %.0f%%",
            levelId,
            level.path("vaseCount").asInt(0),
            level.path("seedPacketLifetimeSeconds").asDouble(0.0),
            level.path("plantVaseChance").asDouble(0.0) * 100.0,
            level.path("gargantuarVaseChance").asDouble(0.0) * 100.0
        );
    }

    private String buildWallnutBowlingSummary(int levelId) {
        JsonNode level = jsonLevel("wallnut_bowling", levelId);
        if (level == null) return "Level " + levelId;
        return String.format(
            "Level %d  •  red line col %d  •  %d zombies  •  nut speed %.0f  •  reload %.1fs",
            levelId,
            level.path("redLineCol").asInt(0),
            level.path("totalZombies").asInt(0),
            level.path("nutSpeed").asDouble(0.0),
            level.path("launchCooldownSeconds").asDouble(0.0)
        );
    }

    private String buildIZombieSummary(int levelId) {
        JsonNode level = jsonLevel("i_zombie", levelId);
        if (level == null) return "Level " + levelId;
        return String.format(
            "Level %d  •  starting sun %d  •  plant density %.0f%%  •  sun cap %.0f  •  sun every %.1fs",
            levelId,
            level.path("startingSun").asInt(0),
            level.path("plantDensity").asDouble(0.0) * 100.0,
            level.path("sunProductionCap").asDouble(0.0),
            level.path("sunProductionIntervalSeconds").asDouble(0.0)
        );
    }

    private String buildBeghouledSummary(int levelId) {
        JsonNode level = jsonLevel("beghouled", levelId);
        if (level == null) return "Level " + levelId;
        return String.format(
            "Level %d  •  target matches %d  •  starting sun %d  •  zombie spawn %.1fs",
            levelId,
            level.path("targetMatches").asInt(0),
            level.path("startingSun").asInt(0),
            level.path("zombieSpawnIntervalSeconds").asDouble(0.0)
        );
    }

    private String buildZombotanySummary(int levelId) {
        JsonNode level = jsonLevel("zombotany", levelId);
        if (level == null) return "Level " + levelId;
        return String.format(
            "Level %d  •  starting sun %d  •  plant pool %d types  •  waves %d",
            levelId,
            level.path("startingSun").asInt(0),
            level.path("plantPool").size(),
            level.path("waves").size()
        );
    }

    private JsonNode jsonLevel(String sectionKey, int levelId) {
        JsonNode section = MinigamesDataLoader.section(sectionKey);
        if (section == null) return null;

        JsonNode levels = section.get("levels");
        if (levels == null || !levels.isArray()) return null;

        for (JsonNode v : levels) {
            if (v.path("id").asInt(-1) == levelId) {
                return v;
            }
        }
        return levels.size() > 0 ? levels.get(0) : null;
    }

    private BitmapFont resolveFont(String skinFontName, BitmapFont fallback) {
        try {
            if (skin != null && skin.getFont(skinFontName) != null) {
                return skin.getFont(skinFontName);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private Drawable resolveSkinDrawable(String id) {
        try {
            if (skin != null && skin.getDrawable(id) != null) {
                return skin.getDrawable(id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Drawable resolveCardBackground() {
        try {
            if (skin != null && skin.has("image_ui_cards_almanac_plant_card_10", Drawable.class)) {
                return skin.getDrawable("image_ui_cards_almanac_plant_card_10");
            }
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
        return new NinePatchDrawable(buildNinePatch(0.18f, 0.16f, 0.1f, 0.95f));
    }

    private Drawable resolvePopupBackground() {
        try {
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
        } catch (Exception ignored) {
        }
        return new NinePatchDrawable(buildNinePatch(0.16f, 0.13f, 0.08f, 0.96f));
    }

    private Drawable resolveWindowBackground() {
        try {
            if (skin != null && skin.has("image_ui_dialog_asset_outer_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_outer_bkgd_10");
            }
            if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                return skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
            if (skin != null && skin.has("image_ui_cards_almanac_plant_card_10", Drawable.class)) {
                return skin.getDrawable("image_ui_cards_almanac_plant_card_10");
            }
        } catch (Exception ignored) {
        }
        return new NinePatchDrawable(buildNinePatch(0.08f, 0.07f, 0.04f, 0.94f));
    }

    private com.badlogic.gdx.graphics.g2d.NinePatch buildNinePatch(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        pixmap.setColor(0.42f, 0.3f, 0.16f, 1f);
        pixmap.drawRectangle(0, 0, 32, 32);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        disposableTextures.add(texture);
        return new com.badlogic.gdx.graphics.g2d.NinePatch(texture, 8, 8, 8, 8);
    }

    private Texture loadCoverTexture(String fileName) {
        try {
            String path = "pvz-assets/Minigames/Covers/" + fileName;
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                System.err.println("MinigameSelectionPanel: cover not found: " + path);
                return createSolidTexture(new Color(0.3f, 0.3f, 0.3f, 1f));
            }

            Texture texture = new Texture(file);
            disposableTextures.add(texture);
            return texture;
        } catch (Exception e) {
            System.err.println("MinigameSelectionPanel: failed to load cover " + fileName + ": " + e.getMessage());
            return createSolidTexture(new Color(0.3f, 0.3f, 0.3f, 1f));
        }
    }

    private Texture loadTextureFromRegion(String id) {
        try {
            if (getTextureBank() == null) return createSolidTexture(new Color(0.3f, 0.3f, 0.3f, 1f));
            TextureRegion region = getTextureBank().region(id);
            if (region == null) {
                return createSolidTexture(new Color(0.3f, 0.3f, 0.3f, 1f));
            }
            Texture tex = textureFromRegion(region);
            if (tex != null) disposableTextures.add(tex);
            return tex;
        } catch (Exception e) {
            return createSolidTexture(new Color(0.3f, 0.3f, 0.3f, 1f));
        }
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        disposableTextures.add(texture);
        return texture;
    }

    private static String stripColorCodes(String s) {
        return s == null ? "" : s.replaceAll("\\u001B\\[[;\\d]*m", "");
    }

    @Override
    public void dispose() {
        for (Texture texture : disposableTextures) {
            if (texture != null) texture.dispose();
        }
        disposableTextures.clear();
        super.dispose();
    }

    private final class MinigameCard extends Table {
        private final MinigameInfo info;

        MinigameCard(MinigameInfo info, Drawable bg, float w, float h) {
            this.info = info;
            setSize(w, h);
            setBackground(bg);
            pad(18f);
            setTouchable(Touchable.enabled);

            Texture iconTex = loadCoverTexture(info.iconResource);
            Image icon = new Image(iconTex);
            icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);

            Label name = new Label(info.displayName.getDisplayName(), new Label.LabelStyle(titleFont, Color.WHITE));
            name.setAlignment(Align.center);
            name.setWrap(true);
            name.setFontScale(0.8f);

            add(icon).size(w - 70f, h * 0.62f).center().row();
            add(name).width(w - 36f).padTop(14f).row();

            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    showPopupFor(MinigameCard.this, 1);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (toActor == null || (!toActor.isDescendantOf(popup) && toActor != MinigameCard.this)) {
                        hidePopup();
                    }
                }

                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    showPopupFor(MinigameCard.this, hoveredLevel);
                    return true;
                }
            });
        }
    }

    private static final class MinigameInfo {
        final MinigameEnum displayName;
        final String dataKey;
        final String iconResource;
        final String overview;

        private MinigameInfo(MinigameEnum displayName, String dataKey, String iconResource, String overview) {
            this.displayName = displayName;
            this.dataKey = dataKey;
            this.iconResource = iconResource;
            this.overview = overview;
        }
    }
}
