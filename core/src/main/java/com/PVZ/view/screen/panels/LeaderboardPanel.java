package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.leaderboard.Leaderboard;
import com.PVZ.model.leaderboard.LeaderboardEntry;
import com.PVZ.model.leaderboard.LeaderboardSortField;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.List;

public class LeaderboardPanel extends BasePanel {

    private static final float VW = com.PVZ.view.screen.BaseScreen.VIRTUAL_WIDTH;
    private static final float VH = com.PVZ.view.screen.BaseScreen.VIRTUAL_HEIGHT;

    private static final float HEADER_HEIGHT = 150f;
    private static final float ROW_HEIGHT = 62f;
    private static final float HEADER_ROW_HEIGHT = 64f;

    // ستون‌ها: فاصله کم و عرض کنترل‌شده
    private static final float[] COL_WIDTHS = {
        150f,   // Username
        150f,   // Stage
        90f,    // Minigames
        80f,    // Daily
        100f,   // Non-Daily
        80f     // Score
    };

    private static final float CELL_PAD = 6f;

    private static final Color COLOR_TITLE = Color.GOLD;
    private static final Color COLOR_USERNAME = Color.YELLOW;
    private static final Color COLOR_STAGE = new Color(0.35f, 0.75f, 1f, 1f);      // آبی آسمانی
    private static final Color COLOR_MINIGAMES = new Color(0.35f, 0.95f, 0.35f, 1f); // سبز
    private static final Color COLOR_DAILY = new Color(1f, 0.6f, 0.1f, 1f);          // نارنجی
    private static final Color COLOR_NON_DAILY = new Color(0.3f, 0.9f, 0.9f, 1f);    // فیروزه‌ای
    private static final Color COLOR_SCORE = new Color(1f, 0.35f, 0.35f, 1f);        // قرمز

    private static final Color COLOR_ROW_ODD = new Color(1f, 1f, 1f, 0.92f);
    private static final Color COLOR_ROW_EVEN = new Color(0.75f, 0.85f, 1f, 0.95f);

    private Skin skin;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private BitmapFont smallFont;

    private Table entriesTable;
    private SelectBox<String> sortBox;
    private MenuButton ascendingButton;

    private LeaderboardSortField currentSort = LeaderboardSortField.USERNAME;
    private boolean ascending = true;

    public LeaderboardPanel() {
        setFillParent(true);

        skin = PvzSkin.get();
        titleFont = skin.getFont("FBUSV8C5EI_1_outline");
        bodyFont = skin.getFont("FBUSV8C5EI_2");
        smallFont = skin.getFont("FBUSV8C6EI_3");

        build();
    }

    private void build() {
        clearChildren();

        Texture purpleUp = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        Texture purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        addActor(root);

        // ---------- Header (بدون پس‌زمینه) ----------
        Table header = new Table();
        Label title = new Label("LEADERBOARD", new Label.LabelStyle(titleFont, COLOR_TITLE));
        title.setFontScale(1.6f);
        title.setAlignment(Align.center);
        header.add(title).center().padBottom(22f).row();

        // کنترل‌های مرتب‌سازی
        Table controls = new Table();
        Label sortLabel = new Label("Sort by", new Label.LabelStyle(bodyFont, Color.WHITE));
        sortLabel.setFontScale(1.15f);

        sortBox = new SelectBox<>(skin);
        sortBox.setItems(
            "Username",
            "Last Stage",
            "Minigames",
            "Daily Quests",
            "Non-Daily Quests",
            "MyoPoint"
        );
        sortBox.setAlignment(Align.center);
        sortBox.getStyle().font = bodyFont;
        sortBox.getStyle().fontColor = Color.YELLOW;
        sortBox.getList().getStyle().font = bodyFont;
        sortBox.getList().getStyle().fontColorSelected = Color.GOLD;
        sortBox.getList().getStyle().fontColorUnselected = Color.WHITE;
        sortBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                currentSort = mapSortField(sortBox.getSelected());
                refreshEntries();
            }
        });

        ascendingButton = new MenuButton(
            purpleUp, "Ascending", bodyFont, purpleDown, null, marker,
            () -> {
                ascending = !ascending;
                ascendingButton.setText(ascending ? "Ascending" : "Descending");
                refreshEntries();
            }
        );
        ascendingButton.setSize(220f, 62f);

        controls.add(sortLabel).padRight(14f);
        controls.add(sortBox).width(280f).height(60f).padRight(24f);
        controls.add(ascendingButton).size(220f, 62f);

        header.add(controls).center().padBottom(10f).row();
        root.add(header).growX().height(HEADER_HEIGHT).row();

        // ---------- جدول رکوردها با پس‌زمینه‌ی شیشه‌ای شفاف ----------
        entriesTable = new Table();
        entriesTable.top().pad(8f);
        entriesTable.setBackground(createGlassBackground());

        ScrollPane scroll = new ScrollPane(entriesTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        root.add(scroll).grow().padTop(4f).row();

        // ---------- دکمه بازگشت ----------
        Table bottom = new Table();
        MenuButton backButton = new MenuButton(
            purpleUp, "BACK", bodyFont, purpleDown, null, marker,
            () -> AppStatus.setCurrentMenuType(MenuType.MAIN)
        );
        backButton.setSize(220f, 62f);
        bottom.add(backButton).padTop(18f);
        root.add(bottom).growX().height(90f).bottom().row();

        refreshEntries();
    }

    private NinePatchDrawable createGlassBackground() {
        int size = 16;
        int border = 8;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.35f);
        pixmap.fill();
        pixmap.setColor(0.55f, 0.5f, 0.35f, 0.9f);
        pixmap.drawRectangle(0, 0, size, size);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new NinePatchDrawable(new NinePatch(texture, border, border, border, border));
    }

    private LeaderboardSortField mapSortField(String selection) {
        if (selection == null) return LeaderboardSortField.USERNAME;
        return switch (selection) {
            case "Last Stage" -> LeaderboardSortField.LAST_STAGE;
            case "Minigames" -> LeaderboardSortField.MINIGAMES;
            case "Daily Quests" -> LeaderboardSortField.DAILY_QUESTS;
            case "Non-Daily Quests" -> LeaderboardSortField.NON_DAILY_QUESTS;
            case "MyoPoint" -> LeaderboardSortField.HIGHEST_SCORE;
            default -> LeaderboardSortField.USERNAME;
        };
    }

    private List<LeaderboardEntry> fetchEntries() {
        if (com.PVZ.network.client.NetworkSession.isConnected()) {
            try {
                com.PVZ.network.common.NetworkMessage request =
                        com.PVZ.network.common.NetworkMessage.request(com.PVZ.network.common.MessageType.FETCH_LEADERBOARD)
                                .with("sort", currentSort != null ? currentSort.name() : null)
                                .with("ascending", ascending);
                com.PVZ.network.common.NetworkMessage response =
                        com.PVZ.network.client.NetworkSession.client().sendRequestBlocking(request);
                if (response.getBoolean("success", false)) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = com.PVZ.network.common.JsonCodec.mapper();
                    com.fasterxml.jackson.databind.JavaType listType =
                            mapper.getTypeFactory().constructCollectionType(List.class, LeaderboardEntry.class);
                    return mapper.convertValue(response.get("entries"), listType);
                }
            } catch (Exception e) {
                // آفلاین شد یا سرور جواب نداد - می‌ریم سراغ fallback زیر
            }
        }
        // آفلاین/بدون سرور: حداقل دیتای محلی رو نشون بده تا صفحه خالی نمونه.
        return Leaderboard.getEntries(currentSort, ascending);
    }

    private void refreshEntries() {
        entriesTable.clearChildren();

        // این همون جایی بود که همکارتون درست شک کرد: قبلا فیکس رو فقط توی
        // LeaderboardMenuController زده بودم (که مسیر DTO/کنسولیه) ولی خودِ
        // این پنل - چیزی که واقعا توی بازی باز می‌شه - مستقیم Leaderboard.getEntries()
        // رو صدا می‌زد و اصلا از شبکه رد نمی‌شد. الان همینجا هم از سرور می‌خونیم.
        List<LeaderboardEntry> entries = fetchEntries();

        if (entries.isEmpty()) {
            Label empty = new Label("No leaderboard data.", new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
            empty.setFontScale(1.2f);
            entriesTable.add(empty).pad(40f).row();
            return;
        }

        // Header row
        addHeaderCell(entriesTable, "Username", COLOR_USERNAME, 0);
        addHeaderCell(entriesTable, "Stage", COLOR_STAGE, 1);
        addHeaderCell(entriesTable, "Mini", COLOR_MINIGAMES, 2);
        addHeaderCell(entriesTable, "Daily", COLOR_DAILY, 3);
        addHeaderCell(entriesTable, "Non-D", COLOR_NON_DAILY, 4);
        addHeaderCell(entriesTable, "MyoPoint", COLOR_SCORE, 5);
        entriesTable.row();

// خط جداکننده طلایی تمام‌عرض
        Image divider = new Image(createSolidTexture(Color.GOLD));
        entriesTable.add(divider)
            .colspan(6)
            .growX()
            .height(2f)
            .padTop(2f)
            .padBottom(6f)
            .row();

        // بدنه جدول
        int index = 0;
        for (LeaderboardEntry e : entries) {
            Color rowColor = (index % 2 == 0) ? COLOR_ROW_ODD : COLOR_ROW_EVEN;
            addBodyCell(entriesTable, e.getUsername(), rowColor, 0);
            addBodyCell(entriesTable, e.getLastStageInfo(), rowColor, 1);
            addBodyCell(entriesTable, String.valueOf(e.getMinigamesCompleted()), rowColor, 2);
            addBodyCell(entriesTable, String.valueOf(e.getDailyQuestsCompleted()), rowColor, 3);
            addBodyCell(entriesTable, String.valueOf(e.getNonDailyQuestsCompleted()), rowColor, 4);
            addBodyCell(entriesTable, String.valueOf(e.getHighestScore()), rowColor, 5);
            entriesTable.row();
            index++;
        }
    }

    private void addHeaderCell(Table table, String text, Color color, int columnIndex) {
        Label label = new Label(text, new Label.LabelStyle(bodyFont, color));
        label.setFontScale(1.25f);
        label.setAlignment(Align.center);
        table.add(label)
            .width(COL_WIDTHS[columnIndex])
            .height(HEADER_ROW_HEIGHT)
            .padLeft(CELL_PAD)
            .padRight(CELL_PAD);
    }

    private void addBodyCell(Table table, String text, Color color, int columnIndex) {
        Label label = new Label(text, new Label.LabelStyle(bodyFont, color));
        label.setFontScale(1.1f);
        label.setAlignment(Align.center);
        table.add(label)
            .width(COL_WIDTHS[columnIndex])
            .height(ROW_HEIGHT)
            .padLeft(CELL_PAD)
            .padRight(CELL_PAD);
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
