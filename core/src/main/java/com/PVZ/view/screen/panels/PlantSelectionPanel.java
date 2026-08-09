package com.PVZ.view.screen.panels;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.view.screen.ui.PlantCardActor;

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
        // این پنل باید یک پاپ‌آپ شناور و دقیقاً سنتر روی صفحه باشد (مثل تصویر انتخاب گیاه در
        // بازی اصلی)، نه چند تا Actor پراکنده که در گوشه‌ی پایین-چپ Stage می‌نشینند. برای این کار:
        // ۱) خودِ ریشه (این Table) با setFillParent + align(center) کل فضای Stage را می‌گیرد و
        //    محتوا را در وسط آن سنتر می‌کند.
        // ۲) محتوای واقعی داخل یک Table جدا («window») قرار می‌گیرد که یک پس‌زمینه‌ی مجزا (frame)
        //    دارد تا شبیه یک پنجره‌ی مستقل روی صفحه دیده شود، نه چند المان شناور روی زمینه‌ی بازی.
        setFillParent(true);
        align(Align.center);

        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();

        Table grid = new Table();
        grid.top().left();
        int col = 0;
        for (PlantType type : PlantType.values()) {
            PlantCardActor card = new PlantCardActor(type, font);
            card.setOnClick(() -> onCardClicked(card));
            cards.add(card);

            Table cell = new Table();
            cell.setSize(card.getWidth(), card.getHeight());
            cell.addActor(card);
            grid.add(cell).size(card.getWidth() + 6f, card.getHeight() + 6f).pad(2f);
            col++;
            if (col >= 8) {
                col = 0;
                grid.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFadeScrollBars(false);

        countLabel = new Label("0 / 8 selected", new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE));
        statusLabel = new Label("", new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.SALMON));

        MenuButton letsRock = new MenuButton("LET'S ROCK", font, this::onLetsRock);

        Table window = new Table();
        window.pad(24f);
        window.setBackground(new NinePatchDrawable(buildWindowNinePatch()));
        window.add(scrollPane).size(8 * 96f, 5 * 116f).row();
        window.add(countLabel).padTop(8f).row();
        window.add(statusLabel).padTop(4f).row();
        window.add(letsRock).padTop(12f).size(220f, 60f).row();

        add(window).center();

        refresh();
    }

    /**
     * پس‌زمینه‌ی ساده‌ی چوبی/تیره برای پنجره‌ی انتخاب گیاه؛ چون به آی‌دی تکسچر مشخصی از
     * TextureBank برای این کادر متکی نیستیم (ریسک نال‌بودن روی asset پک‌های مختلف)، یک بافت
     * قابل‌کش (nine-patch) با کد تولید می‌شود تا صرف‌نظر از سایز محتوا، لبه‌ها تمیز بمانند.
     */
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

