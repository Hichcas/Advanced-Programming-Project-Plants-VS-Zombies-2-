package com.PVZ.view.screen.panels;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.NewsEntry;
import com.PVZ.model.user.NewsState;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.screen.ui.MenuButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsPanel extends BasePanel {

    private Table newsListTable;
    private boolean showUnreadOnly = true;

    // مجموعهٔ موقتی خبرهایی که در این نشست نمایش داده شده‌اند
    private final Set<NewsEntry> displayedEntries = new HashSet<>();

    private MenuButton btnUnread;
    private MenuButton btnAll;

    private final float FIELD_WIDTH;
    private final float BUTTON_HEIGHT;
    private final float SCREEN_H;
    private final Skin skin;
    private final BitmapFont bigFont;
    private final Texture purpleUp, purpleDown;

    // رنگ‌ها
    private static final Color UNREAD_SUBJECT_COLOR = new Color(0.7f, 0.1f, 0.9f, 1f);   // بنفش پررنگ
    private static final Color UNREAD_DATE_COLOR    = new Color(0.9f, 0.7f, 0.1f, 1f);   // زرد تیره
    private static final Color ALL_SUBJECT_COLOR    = new Color(0.6f, 0.2f, 0.8f, 1f);   // بنفش ملایم
    private static final Color ALL_DATE_COLOR       = new Color(0.9f, 0.7f, 0.1f, 1f);   // زرد
    private static final Color READ_SUBJECT_COLOR   = Color.LIGHT_GRAY;
    private static final Color READ_DATE_COLOR      = Color.GRAY;

    public NewsPanel() {
        setFillParent(true);
        align(Align.top);

        float screenW = Gdx.graphics.getWidth();
        SCREEN_H = Gdx.graphics.getHeight();
        FIELD_WIDTH   = screenW * 0.7f;
        BUTTON_HEIGHT = SCREEN_H * 0.08f;

        skin = PvzSkin.get();
        bigFont = skin.getFont("FBUSV8C5EI_1_outline");
        purpleUp   = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON");
        purpleDown = safeTextureFromRegion("IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN");
        Texture marker = new Texture(Gdx.files.internal("global/button_marker.png"));

        // ==================== عنوان ====================
        Label titleLabel = new Label("NEWS", new Label.LabelStyle(bigFont, Color.YELLOW));
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(1.3f);
        add(titleLabel).padTop(SCREEN_H * 0.03f).padBottom(10f).row();

        // ==================== دکمه‌های انتخاب All / Unread ====================
        Table toggleRow = new Table();

        btnUnread = new MenuButton(purpleUp, "Unread", bigFont, purpleDown, null, marker, () -> {
            showUnreadOnly = true;
            refreshUI();
        });
        btnUnread.setSize(220f, BUTTON_HEIGHT * 0.9f);

        btnAll = new MenuButton(purpleUp, "All", bigFont, purpleDown, null, marker, () -> {
            showUnreadOnly = false;
            refreshUI();
        });
        btnAll.setSize(220f, BUTTON_HEIGHT * 0.9f);

        toggleRow.add(btnUnread).padRight(20f);
        toggleRow.add(btnAll);
        add(toggleRow).padBottom(15f).row();

        // ==================== لیست خبرها ====================
        newsListTable = new Table();
        newsListTable.defaults().pad(10f);
        ScrollPane scrollPane = new ScrollPane(newsListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        add(scrollPane).width(FIELD_WIDTH).height(SCREEN_H * 0.65f).row();

        // ==================== دکمه Back ====================
        MenuButton backBtn = new MenuButton(purpleUp, "Back", bigFont, purpleDown, null, marker, () -> {
            markDisplayedAsRead();
            AppStatus.setCurrentMenuType(MenuType.MAIN);
        });
        backBtn.setSize(200f, BUTTON_HEIGHT);
        add(backBtn).padTop(15f).row();

        refreshUI();
    }

    private void refreshUI() {
        refreshToggleButtons();
        refreshList();
    }

    private void refreshToggleButtons() {
        btnUnread.setDisabled(showUnreadOnly);
        btnAll.setDisabled(!showUnreadOnly);
    }

    private void refreshList() {
        newsListTable.clearChildren();
        NewsState state = AppStatus.currentUser != null ? AppStatus.currentUser.newsState : null;
        if (state == null) {
            newsListTable.add(new Label("No user data.", new Label.LabelStyle(bigFont, Color.RED)));
            return;
        }

        List<NewsEntry> entries = showUnreadOnly ? state.getUnreadNews() : state.getAllNews();
        if (entries.isEmpty()) {
            String msg = showUnreadOnly ? "No unread news." : "No news available.";
            newsListTable.add(new Label(msg, new Label.LabelStyle(bigFont, Color.LIGHT_GRAY)));
            return;
        }

        displayedEntries.addAll(entries);

        for (int i = 0; i < entries.size(); i++) {
            NewsEntry entry = entries.get(i);
            boolean isUnread = !entry.isRead();

            Color subjectColor, dateColor;
            float subjectScale = 1.0f;

            if (showUnreadOnly) {
                subjectColor = UNREAD_SUBJECT_COLOR;
                dateColor    = UNREAD_DATE_COLOR;
                subjectScale = 1.15f;
            } else {
                if (isUnread) {
                    subjectColor = ALL_SUBJECT_COLOR;
                    dateColor    = ALL_DATE_COLOR;
                    subjectScale = 1.05f;
                } else {
                    subjectColor = READ_SUBJECT_COLOR;
                    dateColor    = READ_DATE_COLOR;
                    subjectScale = 0.95f;
                }
            }

            String line = (i + 1) + ". " + entry.getFormattedText();
            Label.LabelStyle style = new Label.LabelStyle(bigFont, subjectColor);
            Label label = new Label(line, style);
            label.setWrap(true);
            label.setAlignment(Align.left);
            label.setFontScale(subjectScale);
            newsListTable.add(label).width(FIELD_WIDTH - 30f).left().row();
        }
    }

    private void markDisplayedAsRead() {
        if (displayedEntries.isEmpty()) return;
        for (NewsEntry entry : displayedEntries) {
            entry.setRead(true);
        }
        displayedEntries.clear();
        if (AppStatus.currentUser != null && AppStatus.currentUser.profile != null) {
            UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
        }
    }
}
