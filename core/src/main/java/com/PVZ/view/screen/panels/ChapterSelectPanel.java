package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.ChapterAndLevelSelectionMenuController;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.chapter.ChapterConfig;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChapterSelectPanel extends BasePanel {

    private final ChapterAndLevelSelectionMenuController controller =
        new ChapterAndLevelSelectionMenuController();

    private Label errorLabel;

    private static final Map<ChapterEnum, String[]> SMALL_ISLANDS = new LinkedHashMap<>();
    static {
        SMALL_ISLANDS.put(ChapterEnum.ANCIENT_EGYPT, new String[]{
            "IMAGE_WORLDMAP_EGYPT_ISLAND4",
            "IMAGE_WORLDMAP_EGYPT_ISLAND5"
        });
        SMALL_ISLANDS.put(ChapterEnum.FROSTBITE_CAVES, new String[]{
            "IMAGE_WORLDMAP_ICEAGE_ANIM12_ANIM12_400X500",
            "IMAGE_WORLDMAP_ICEAGE_ANIM26_ANIM26_375X281",
            "IMAGE_WORLDMAP_ICEAGE_ANIM11_ANIM11_400X500"
        });
        SMALL_ISLANDS.put(ChapterEnum.BIG_WAVE_BEACH, new String[]{
            "IMAGE_WORLDMAP_BEACH_ANIM16_ANIM16_339X318",
            "IMAGE_WORLDMAP_BEACH_ANIM10_ANIM10_295X271",
            "IMAGE_WORLDMAP_BEACH_ANIM11_ANIM11_297X281"
        });
        SMALL_ISLANDS.put(ChapterEnum.DARK_AGES, new String[]{
            "IMAGE_WORLDMAP_DANGER_NODE_DARK_DANGER_NODE_DARK_389X448",
            "IMAGE_WORLDMAP_DANGER_NODE_DARK_DANGER_NODE_DARK_384X459"
        });
    }

    private static final Map<ChapterEnum, String> BIG_NODES = new LinkedHashMap<>();
    static {
        BIG_NODES.put(ChapterEnum.ANCIENT_EGYPT,
            "IMAGE_WORLDMAP_ZOMBOSS_NODE_EGYPT_ZOMBOSS_NODE_EGYPT_914X994");
        BIG_NODES.put(ChapterEnum.FROSTBITE_CAVES,
            "IMAGE_WORLDMAP_ICEAGE_ANIM3_ANIM3_1307X1318");
        BIG_NODES.put(ChapterEnum.BIG_WAVE_BEACH,
            "IMAGE_WORLDMAP_ZOMBOSS_NODE_BEACH_ZOMBOSS_NODE_BEACH_905X1096");
        BIG_NODES.put(ChapterEnum.DARK_AGES,
            "IMAGE_WORLDMAP_DARK_ANIM1_ANIM1_1201X1413");
    }

    public ChapterSelectPanel() {
        setFillParent(true);

        Skin skin = PvzSkin.get();
        BitmapFont bigFont = skin.getFont("FBUSV8C5EI_1_outline");
        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");

        float islandWidth = 150f, islandHeight = 150f;
        float bigWidth = 350f, bigHeight = 350f;
        float gapBetweenItems = 30f;
        float gapBetweenChapters = 80f;
        float verticalOffset = 60f;

        Group contentGroup = new Group();
        float currentX = 100f;

        User user = AppStatus.currentUser;

        for (ChapterEnum chapterEnum : ChapterEnum.values()) {
            ChapterConfig config = ChapterLibrary.getChapterConfig(chapterEnum.name());
            if (config == null || config.getStages().isEmpty()) continue;

            int stageCount = config.getStages().size();
            String[] smallIds = SMALL_ISLANDS.get(chapterEnum);
            String bigId = BIG_NODES.get(chapterEnum);

            Texture bigTex = safeTextureFromRegion(bigId);
            Texture[] smallTexs = new Texture[smallIds.length];
            for (int j = 0; j < smallIds.length; j++) {
                smallTexs[j] = safeTextureFromRegion(smallIds[j]);
            }

            if (bigTex != null) {
                bigWidth = Math.max(350f, bigTex.getWidth());
                bigHeight = Math.max(350f, bigTex.getHeight());
            }

            float centerY = Gdx.graphics.getHeight() / 2f;
            float bossY = centerY - bigHeight / 2f;

            // بلوک بزرگ (غیرتعاملی)
            Image bossImage = new Image(new TextureRegionDrawable(bigTex));
            bossImage.setSize(bigWidth, bigHeight);
            bossImage.setPosition(currentX, bossY);
            contentGroup.addActor(bossImage);

            float nextX = currentX + bigWidth + gapBetweenItems;
            for (int i = 0; i < stageCount; i++) {
                int stageNum = i + 1;
                Texture tex = smallTexs[i % smallTexs.length];
                boolean locked = user != null && user.progressState != null
                    && !user.progressState.isLevelUnlocked(chapterEnum, stageNum);

                float yOff = (stageNum % 2 == 0) ? -verticalOffset : verticalOffset;
                float islandY = centerY - islandHeight / 2f + yOff;

                MenuButton btn = new MenuButton(
                    tex, null, null,
                    tex, tex, null,
                    new Runnable() {
                        @Override
                        public void run() {
                            enterStage(chapterEnum, stageNum);
                        }
                    }
                );
                btn.setSize(islandWidth, islandHeight);
                btn.setPosition(nextX, islandY);
                if (locked) btn.setDisabled(true);
                contentGroup.addActor(btn);

                Color numberColor = locked ? Color.RED : Color.GREEN;
                Label numLabel = new Label(String.valueOf(stageNum),
                    new Label.LabelStyle(bigFont, numberColor));
                numLabel.setAlignment(Align.center);
                numLabel.setSize(islandWidth, 30f);
                numLabel.setPosition(nextX, islandY + islandHeight / 2f - 15f);
                contentGroup.addActor(numLabel);

                nextX += islandWidth + gapBetweenItems;
            }

            currentX = nextX + gapBetweenChapters - gapBetweenItems;
        }

        float contentWidth = Math.max(currentX - gapBetweenChapters + gapBetweenItems,
            Gdx.graphics.getWidth());
        contentGroup.setSize(contentWidth, Gdx.graphics.getHeight());

        ScrollPane scrollPane = new ScrollPane(contentGroup, skin);
        scrollPane.setFillParent(true);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        addActor(scrollPane);

        // دکمه بازگشت
        MenuButton backBtn = new MenuButton(purpleUp, "Back", bigFont, purpleDown, null, null,
            new Runnable() {
                @Override
                public void run() {
                    AppStatus.setCurrentMenuType(MenuType.MAIN);
                }
            }
        );
        backBtn.setSize(200, 80);
        backBtn.setPosition(50, 50);
        addActor(backBtn);

        // برچسب خطا
        errorLabel = new Label("", new Label.LabelStyle(bigFont, Color.RED));
        errorLabel.setAlignment(Align.center);
        errorLabel.setSize(Gdx.graphics.getWidth() * 0.6f, 50);
        errorLabel.setPosition(Gdx.graphics.getWidth() * 0.2f, 20);
        addActor(errorLabel);
    }

    private void enterStage(ChapterEnum chapter, int stage) {
        ChapterAndLevelSelectionInputDTO dto = new ChapterAndLevelSelectionInputDTO(
            com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand.ENTER_CHAPTER,
            chapter.name(), null, null, stage
        );
        OutputDTO result = controller.handle(dto);
        if (!result.isSuccess()) {
            showError(result.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
