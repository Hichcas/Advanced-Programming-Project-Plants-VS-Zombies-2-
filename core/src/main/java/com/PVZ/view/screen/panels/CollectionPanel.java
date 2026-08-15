package com.PVZ.view.screen.panels;

import com.PVZ.controller.menuControllers.CollectionMenuController;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.enums.commands.CollectionCommand;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.entity.plants.UpgradeRule;
import com.PVZ.model.entity.plants.UpgradeCostPolicy;
import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.PVZ.model.quest.PlantFamilyMapper;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.CollectionInputDTO;
import com.PVZ.view.output.OutputDTO;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.screen.ui.MenuButton;
import com.PVZ.view.screen.ui.PlantCardActor;
import com.PVZ.view.screen.ui.UpgradeDescriptionCatalog;
import com.PVZ.view.screen.ui.UpgradeNotificationPopup;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

public class CollectionPanel extends BasePanel {

    private final CollectionMenuController controller = new CollectionMenuController();

    private static final float CARD_SLOT_W = 135f * 1.5f;
    private static final float CARD_SLOT_H = 155f * 1.5f;
    private static final float DETAIL_PREVIEW_SIZE = 140f;
    private static final int GRID_COLS = 6; // ۶ ستون برای پر کردن کامل فضای سمت چپ

    private Skin skin;
    private BitmapFont titleFont, bodyFont, descFont;

    private Table filterRow;
    private Table contentGrid;
    private Table detailPanel;

    // Plant Stats UI
    private Table plantStatsTable;
    private Slider hpSlider, costSlider, damageSlider, levelSlider, seedSlider;
    private Label hpTitleLabel, costTitleLabel, damageTitleLabel, levelTitleLabel, seedTitleLabel;
    private Label hpValueLabel, costValueLabel, damageValueLabel, levelValueLabel, seedValueLabel;

    // Zombie Stats UI
    private Table zombieStatsTable;
    private Slider zombieHpSlider, zombieSpeedSlider, zombieDamageSlider;
    private Label zombieHpTitleLabel, zombieSpeedTitleLabel, zombieDamageTitleLabel;
    private Label zombieHpValueLabel, zombieSpeedValueLabel, zombieDamageValueLabel, zombieInfoLabel;
    private Label nextUpgradeLabel;

    private Table actionButtons;

    private SelectBox<String> familyFilter, lockFilter, upgradeFilter;

    private Label detailNameLabel;
    private DetailPreviewActor detailPreviewActor;
    private ZombiePreviewActor zombiePreviewActor;
    private Label statusLabel;

    private MenuButton upgradeBtn, buyBtn;

    private PlantType selectedPlant;
    private ZombieType selectedZombie;
    private boolean showingZombies = false;

    public CollectionPanel() {
        setFillParent(true);
        skin = PvzSkin.get();
        titleFont = FontManager.getInstance().getEnglishTitleFont();
        bodyFont = FontManager.getInstance().getEnglishMenuFont();
        descFont = FontManager.getInstance().getEnglishTinyFont();

        build();
    }

    private void build() {
        clearChildren();

        Drawable windowBg = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(windowBg);
        root.pad(15f);
        addActor(root);

        // عنوان
        Label title = new Label("COLLECTION", new Label.LabelStyle(titleFont, Color.GOLD));
        title.setFontScale(1.2f);
        root.add(title).colspan(2).padBottom(5f).row();

        // تب‌ها
        Table tabs = new Table();
        MenuButton plantsTab = new MenuButton(
            skin.getDrawable("image_ui_generic_greenbutton_10"),
            "PLANTS", bodyFont,
            skin.getDrawable("image_ui_generic_greenbutton_down_10"),
            null, null, () -> switchTab(false)
        );
        plantsTab.setSize(220f, 45f);

        MenuButton zombiesTab = new MenuButton(
            skin.getDrawable("image_ui_generic_brownbutton_10"),
            "ZOMBIES", bodyFont,
            skin.getDrawable("image_ui_generic_brownbutton_down_10"),
            null, null, () -> switchTab(true)
        );
        zombiesTab.setSize(220f, 45f);

        tabs.add(plantsTab).padRight(15f);
        tabs.add(zombiesTab);
        root.add(tabs).colspan(2).padBottom(8f).row();

        // فیلترها (Sun -> Sun Producer)
        filterRow = new Table();
        familyFilter = createSelectBox(new String[]{"All Families", "Sun Producer", "Shooter", "Melee", "Wall", "Explosive", "Mushroom", "Modifier"});
        lockFilter = createSelectBox(new String[]{"All", "Locked", "Unlocked"});
        upgradeFilter = createSelectBox(new String[]{"All", "Upgradable"});

        ChangeListener filterChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshGrid();
            }
        };
        familyFilter.addListener(filterChangeListener);
        lockFilter.addListener(filterChangeListener);
        upgradeFilter.addListener(filterChangeListener);

        filterRow.add(new Label("Family:", new Label.LabelStyle(bodyFont, Color.WHITE))).padRight(5f);
        filterRow.add(familyFilter).width(200f).padRight(15f);
        filterRow.add(new Label("Lock:", new Label.LabelStyle(bodyFont, Color.WHITE))).padRight(5f);
        filterRow.add(lockFilter).width(130f).padRight(15f);
        filterRow.add(new Label("Upgrade:", new Label.LabelStyle(bodyFont, Color.WHITE))).padRight(5f);
        filterRow.add(upgradeFilter).width(130f);

        root.add(filterRow).colspan(2).padBottom(10f).row();

        // بدنه اصلی (استفاده کامل و متوازن از کل فضای صفحه)
        Table mainBody = new Table();

        // گرید کارت‌ها (سمت چپ)
        contentGrid = new Table();
        contentGrid.top().left();
        ScrollPane scroll = new ScrollPane(contentGrid, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        mainBody.add(scroll).width(1500).growY().padRight(50f);

        // پنل جزئیات (سمت راست - رشد گسترده برای پر کردن کل فضای باقی‌مانده)
        detailPanel = buildDetailPanel();
        mainBody.add(detailPanel).width(900f).growY().top();

        root.add(mainBody).colspan(2).grow().row();

        // پیام وضعیت
        statusLabel = new Label("", new Label.LabelStyle(bodyFont, Color.SALMON));
        statusLabel.setAlignment(Align.center);
        root.add(statusLabel).colspan(2).growX().height(25f).padTop(5f).row();

        // دکمه بازگشت
        MenuButton backBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_brownbutton_10"),
            "BACK", bodyFont,
            skin.getDrawable("image_ui_generic_brownbutton_down_10"),
            null, null,
            () -> AppStatus.setCurrentMenuType(MenuType.MAIN)
        );
        backBtn.setSize(180f, 45f);
        root.add(backBtn).colspan(2).padTop(5f).row();

        switchTab(false);
    }

    private void switchTab(boolean zombies) {
        showingZombies = zombies;
        filterRow.setVisible(!zombies);

        selectedPlant = null;
        selectedZombie = null;

        if (!zombies && PlantType.values().length > 0) {
            selectedPlant = PlantType.values()[0];
        } else if (zombies && ZombieType.values().length > 0) {
            selectedZombie = ZombieType.values()[0];
        }

        refreshGrid();
        refreshDetail();
    }

    private SelectBox<String> createSelectBox(String[] items) {
        SelectBox<String> box = new SelectBox<>(skin);
        box.setItems(items);
        box.setAlignment(Align.center);
        return box;
    }

    private void refreshGrid() {
        contentGrid.clearChildren();
        User user = AppStatus.getCurrentUser();
        if (user == null) return;

        if (showingZombies) {
            int col = 0;
            for (ZombieType zt : ZombieType.values()) {
                boolean seen = user.collectionState.getSeenZombies().contains(zt);
                Stack cell = new Stack();
                cell.setSize(CARD_SLOT_W, CARD_SLOT_H);

                Drawable slotBg = skin.getDrawable("image_ui_cards_almanac_plant_card_10");
                if (slotBg != null) {
                    Table frame = new Table();
                    frame.setBackground(slotBg);
                    cell.add(frame);
                }

                if (seen) {
                    ZombiePreviewActor preview = new ZombiePreviewActor();
                    preview.setType(zt);
                    cell.add(preview);
                    cell.setTouchable(Touchable.enabled);
                    cell.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            selectedZombie = zt;
                            selectedPlant = null;
                            refreshDetail();
                        }
                    });
                } else {
                    Label unknown = new Label("?", new Label.LabelStyle(bodyFont, Color.GRAY));
                    unknown.setAlignment(Align.center);
                    cell.add(unknown);
                }

                contentGrid.add(cell).size(CARD_SLOT_W, CARD_SLOT_H).pad(15f);
                col++;
                if (col >= GRID_COLS) {
                    col = 0;
                    contentGrid.row();
                }
            }
        } else {
            int col = 0;
            for (PlantType type : PlantType.values()) {
                if (!passesPlantFilter(type, user)) continue;

                Stack cell = new Stack();
                cell.setSize(CARD_SLOT_W, CARD_SLOT_H);

                Drawable slotBg = skin.getDrawable("image_ui_cards_almanac_plant_card_10");
                if (slotBg != null) {
                    Table frame = new Table();
                    frame.setBackground(slotBg);
                    cell.add(frame);
                }

                PlantCardActor card = new PlantCardActor(type, bodyFont);
                boolean unlocked = user.collectionState.isPlantUnlocked(type);
                card.setLocked(!unlocked);
                card.setOnClick(() -> {
                    selectedPlant = type;
                    selectedZombie = null;
                    refreshDetail();
                });
                cell.add(card);

                // تبدیل لول به Base 1
                int level = user.collectionState.getPlantLevel(type);
                int displayLevel = level + 1;
                int seedPackets = user.collectionState.getSeedPacketCount(type);
                int maxDisplayLevel = PlantLibrary.findByType(type)
                    .map(PlantDefinition::getMaxLevel)
                    .orElse(4);
                boolean maxedOut = displayLevel >= maxDisplayLevel;
                int requiredPackets = maxedOut ? 0
                    : UpgradeCostPolicy.currentUpgradeRequirement(displayLevel, maxDisplayLevel);

                Table info = new Table();
                info.setFillParent(true);          // کل سطح کارت را بگیرد
                info.top().right();                // محتوا را بالا‑راست بچیند
                info.pad(-7f);
                info.padRight(-8f);
                info.setTouchable(Touchable.disabled);

                Label lvl = new Label(
                    maxedOut ? "MAX" : "Lv." + displayLevel,
                    new Label.LabelStyle(bodyFont, Color.YELLOW)
                );
                lvl.setFontScale(1f);            // کمی بزرگ‌تر و خوانا

                Label seeds = new Label(
                    maxedOut ? "" : seedPackets + "/" + requiredPackets,
                    new Label.LabelStyle(descFont, Color.WHITE)
                );

                info.add(lvl).right().row();
                info.add(seeds).right();

                cell.add(info);

                contentGrid.add(cell).size(CARD_SLOT_W, CARD_SLOT_H).pad(15f);
                col++;
                if (col >= GRID_COLS) {
                    col = 0;
                    contentGrid.row();
                }
            }
        }
    }

    private boolean passesPlantFilter(PlantType type, User user) {
        String familySelection = familyFilter.getSelected();
        if (!familySelection.equals("All Families")) {
            PlantFamily family = PlantFamilyMapper.getFamily(type);
            if (family == null) return false;

            String famStr = family.name().toLowerCase();
            String selStr = familySelection.toLowerCase().replace(" producer", "");

            boolean match = famStr.equals(selStr) || famStr.replace('_', ' ').equalsIgnoreCase(selStr);
            if (selStr.contains("sun") && famStr.contains("sun")) {
                match = true;
            }
            if (!match) return false;
        }

        boolean unlocked = user.collectionState.isPlantUnlocked(type);
        String lockSelection = lockFilter.getSelected();
        if (lockSelection.equals("Locked") && unlocked) return false;
        if (lockSelection.equals("Unlocked") && !unlocked) return false;

        String upgradeSelection = upgradeFilter.getSelected();
        if (upgradeSelection.equals("Upgradable")) {
            int currentLevel = user.collectionState.getPlantLevel(type);
            int maxRawLevel = PlantLibrary.findByType(type)
                .map(PlantDefinition::getMaxLevel)
                .orElse(4) - 1;
            if (currentLevel >= maxRawLevel) return false; // already maxed - never "upgradable"
            int needed = currentLevel + 1;
            if (user.collectionState.getSeedPacketCount(type) < needed) return false;
        }

        return true;
    }

    private Table buildDetailPanel() {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable("image_ui_cards_almanac_plant_card_10"));
        panel.pad(18f);
        panel.top();

        detailNameLabel = new Label("", new Label.LabelStyle(titleFont, Color.GOLD));
        detailNameLabel.setFontScale(1.1f);
        detailNameLabel.setAlignment(Align.center);
        panel.add(detailNameLabel).colspan(2).growX().padBottom(15f).row();

        // بخش نمایش انیمیشن
        Stack previewStack = new Stack();
        detailPreviewActor = new DetailPreviewActor();
        zombiePreviewActor = new ZombiePreviewActor();
        previewStack.add(detailPreviewActor);
        previewStack.add(zombiePreviewActor);

        panel.add(previewStack).size(DETAIL_PREVIEW_SIZE).padRight(15f).top();

        // ==================== Plant Stats Table (تفکیک ۳ ستونه برای عدم تداخل متن و اسلایدر) ====================
        plantStatsTable = new Table();
        plantStatsTable.top().left();

        hpSlider = createStatSlider(0, 1000);
        costSlider = createStatSlider(0, 300);
        damageSlider = createStatSlider(0, 300);
        levelSlider = createStatSlider(1, 10);
        seedSlider = createStatSlider(0, 50);

        hpTitleLabel = new Label("HP", new Label.LabelStyle(bodyFont, Color.WHITE));
        hpValueLabel = new Label("0", new Label.LabelStyle(bodyFont, Color.GOLD));

        costTitleLabel = new Label("Cost", new Label.LabelStyle(bodyFont, Color.WHITE));
        costValueLabel = new Label("0", new Label.LabelStyle(bodyFont, Color.GOLD));

        damageTitleLabel = new Label("Damage", new Label.LabelStyle(bodyFont, Color.WHITE));
        damageValueLabel = new Label("0", new Label.LabelStyle(bodyFont, Color.GOLD));

        levelTitleLabel = new Label("Level", new Label.LabelStyle(bodyFont, Color.WHITE));
        levelValueLabel = new Label("Lv.1", new Label.LabelStyle(bodyFont, Color.GOLD));

        seedTitleLabel = new Label("Seeds", new Label.LabelStyle(bodyFont, Color.WHITE));
        seedValueLabel = new Label("0/0", new Label.LabelStyle(bodyFont, Color.GOLD));

        addStatRow(plantStatsTable, hpTitleLabel, hpSlider, hpValueLabel);
        addStatRow(plantStatsTable, damageTitleLabel, damageSlider, damageValueLabel);
        addStatRow(plantStatsTable, costTitleLabel, costSlider, costValueLabel);
        addStatRow(plantStatsTable, levelTitleLabel, levelSlider, levelValueLabel);
        addStatRow(plantStatsTable, seedTitleLabel, seedSlider, seedValueLabel);
        nextUpgradeLabel = new Label("", new Label.LabelStyle(descFont, Color.valueOf("D7E8FF")));
        nextUpgradeLabel.setWrap(true);
        nextUpgradeLabel.setAlignment(Align.center);
        plantStatsTable.add(nextUpgradeLabel).colspan(3).growX().padTop(4f).padBottom(8f).row();

        // ==================== Zombie Stats Table (تفکیک ۳ ستونه) ====================
        zombieStatsTable = new Table();
        zombieStatsTable.top().left();

        zombieHpSlider = createStatSlider(0, 2000);
        zombieSpeedSlider = createStatSlider(0, 10);
        zombieDamageSlider = createStatSlider(0, 500);

        zombieHpTitleLabel = new Label("HP", new Label.LabelStyle(bodyFont, Color.WHITE));
        zombieHpValueLabel = new Label("200", new Label.LabelStyle(bodyFont, Color.GOLD));

        zombieSpeedTitleLabel = new Label("Speed", new Label.LabelStyle(bodyFont, Color.WHITE));
        zombieSpeedValueLabel = new Label("4.0", new Label.LabelStyle(bodyFont, Color.GOLD));

        zombieDamageTitleLabel = new Label("Damage", new Label.LabelStyle(bodyFont, Color.WHITE));
        zombieDamageValueLabel = new Label("100", new Label.LabelStyle(bodyFont, Color.GOLD));

        zombieInfoLabel = new Label("Zombie Information Details", new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        zombieInfoLabel.setWrap(true);

        addStatRow(zombieStatsTable, zombieHpTitleLabel, zombieHpSlider, zombieHpValueLabel);
        addStatRow(zombieStatsTable, zombieSpeedTitleLabel, zombieSpeedSlider, zombieSpeedValueLabel);
        addStatRow(zombieStatsTable, zombieDamageTitleLabel, zombieDamageSlider, zombieDamageValueLabel);
        zombieStatsTable.add(zombieInfoLabel).colspan(3).growX().padTop(10f).row();

        // قرار دادن جداول مشخصات در یک استک
        Stack statsStack = new Stack();
        statsStack.add(plantStatsTable);
        statsStack.add(zombieStatsTable);

        panel.add(statsStack).growX().top().row();

        // دکمه‌های عملیاتی (Upgrade و Buy)
        actionButtons = new Table();
        upgradeBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_purplebutton_10"),
            "UPGRADE", bodyFont,
            skin.getDrawable("image_ui_generic_purplebutton_down_10"),
            null, null, this::onUpgrade
        );
        upgradeBtn.setSize(140f, 48f);

        buyBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_greenbutton_10"),
            "BUY", bodyFont,
            skin.getDrawable("image_ui_generic_greenbutton_down_10"),
            null, null, this::onBuy
        );
        buyBtn.setSize(140f, 48f);

        actionButtons.add(upgradeBtn).padRight(10f);
        actionButtons.add(buyBtn);

        panel.add(actionButtons).colspan(2).padTop(25f).center();

        return panel;
    }

    private Slider createStatSlider(float min, float max) {
        Slider slider = new Slider(min, max, 1f, false, skin);
        slider.setTouchable(Touchable.disabled);
        return slider;
    }

    private void addStatRow(Table table, Label titleLabel, Slider slider, Label valueLabel) {
        table.add(titleLabel).width(70f).left().padBottom(12f);
        table.add(slider).width(420f).padLeft(5f).padRight(5f).padBottom(12f);
        table.add(valueLabel).width(80f).right().padBottom(12f).row();
    }

    private void refreshDetail() {
        detailNameLabel.setText("");
        detailPreviewActor.setType(null);
        zombiePreviewActor.setType(null);

        plantStatsTable.setVisible(false);
        zombieStatsTable.setVisible(false);
        actionButtons.setVisible(false);

        User user = AppStatus.getCurrentUser();
        if (user == null) return;

        if (!showingZombies && selectedPlant != null) {
            PlantDefinition def = PlantLibrary.findByType(selectedPlant).orElse(null);
            boolean unlocked = user.collectionState.isPlantUnlocked(selectedPlant);

            if (def != null) {
                detailNameLabel.setText(def.getName());
                int level = user.collectionState.getPlantLevel(selectedPlant);
                int displayLevel = level + 1; // display level is 1-based
                int seedPackets = user.collectionState.getSeedPacketCount(selectedPlant);
                int maxDisplayLevel = def.getMaxLevel();
                boolean maxedOut = displayLevel >= maxDisplayLevel;
                int required = maxedOut ? 0 : UpgradeCostPolicy.currentUpgradeRequirement(displayLevel, maxDisplayLevel);
                if (maxedOut) {
                    nextUpgradeLabel.setText("MAX LEVEL\nNo further upgrades available.");
                } else {
                    int nextLevel = displayLevel + 1;
                    String nextDescription = UpgradeDescriptionCatalog.find(def.getName(), nextLevel);
                    if (nextDescription == null || nextDescription.isBlank()) {
                        nextDescription = "New permanent upgrade available.";
                    }
                    nextUpgradeLabel.setText("NEXT UPGRADE — LEVEL " + nextLevel + "\n" + nextDescription);
                }
                // The sheet defines the actual upgrade effect per target level.
                // Seed cost is handled centrally and increases with each tier.

                // Show the player's ACTUAL current stats (HP/cost upgrades already
                // applied), not the plant's static level-1 baseline - otherwise the
                // detail sheet never visibly changes after an upgrade even though
                // the upgrade genuinely applied in-game.
                com.PVZ.model.entity.plants.PlantStats currentStats =
                    com.PVZ.model.entity.plants.UpgradeResolver.resolveStats(def, displayLevel);

                hpSlider.setRange(0, Math.max(1000, currentStats.getMaxHp()));
                hpSlider.setValue(currentStats.getMaxHp());
                hpValueLabel.setText(String.valueOf(currentStats.getMaxHp()));

                damageSlider.setRange(0, Math.max(300, currentStats.getDamage()));
                damageSlider.setValue(currentStats.getDamage());
                damageValueLabel.setText(String.valueOf(currentStats.getDamage()));

                costSlider.setRange(0, 300);
                costSlider.setValue(currentStats.getCost());
                costValueLabel.setText(String.valueOf(currentStats.getCost()));

                levelSlider.setRange(1, Math.max(1, maxDisplayLevel));
                levelSlider.setValue(displayLevel);
                levelValueLabel.setText("Lv." + displayLevel + "/" + maxDisplayLevel);

                seedSlider.setRange(0, Math.max(1, required));
                seedSlider.setValue(Math.min(seedPackets, required));
                seedValueLabel.setText(maxedOut ? "MAX" : seedPackets + "/" + required);

                plantStatsTable.setVisible(true);
                actionButtons.setVisible(true);

                if (!unlocked) {
                    buyBtn.setVisible(true);
                    upgradeBtn.setVisible(false);
                } else {
                    buyBtn.setVisible(true);
                    upgradeBtn.setVisible(true);
                    upgradeBtn.setDisabled(maxedOut);
                    upgradeBtn.setText(maxedOut ? "MAX LEVEL" : "UPGRADE");
                }
            }
            detailPreviewActor.setType(selectedPlant);

        } else if (showingZombies && selectedZombie != null) {
            detailNameLabel.setText(selectedZombie.name().replace('_', ' '));
            zombiePreviewActor.setType(selectedZombie);

            // محاسبه و تنظیم مقادیر زامبی
            int hp = getZombieHp(selectedZombie);
            float speed = getZombieSpeed(selectedZombie);
            int damage = getZombieDamage(selectedZombie);

            zombieHpSlider.setRange(0, Math.max(2000, hp));
            zombieHpSlider.setValue(hp);
            zombieHpValueLabel.setText(String.valueOf(hp));

            zombieSpeedSlider.setRange(0, 10);
            zombieSpeedSlider.setValue(speed);
            zombieSpeedValueLabel.setText(String.format("%.1f", speed));

            zombieDamageSlider.setRange(0, 500);
            zombieDamageSlider.setValue(damage);
            zombieDamageValueLabel.setText(String.valueOf(damage));

            zombieInfoLabel.setText(getZombieDescription(selectedZombie));

            zombieStatsTable.setVisible(true);
            actionButtons.setVisible(false);
        }
    }

    private int getZombieHp(ZombieType zt) {
        switch (zt) {
            case TUTORIAL_DEFAULT:
            case MUMMY_DEFAULT:
            case ICEAGE_DEFAULT:
            case BEACH_DEFAULT:
            case DARK_DEFAULT:
                return 200;

            case TUTORIAL_ARMOR1:
            case MUMMY_ARMOR1:
            case ICEAGE_ARMOR1:
            case BEACH_ARMOR1:
            case DARK_ARMOR1:
                return 370;

            case TUTORIAL_ARMOR2:
            case MUMMY_ARMOR2:
            case ICEAGE_ARMOR2:
            case BEACH_ARMOR2:
            case DARK_ARMOR2:
                return 1100;

            case TUTORIAL_ARMOR4:
            case MUMMY_ARMOR4:
            case DARK_ARMOR4:
                return 2200;

            case GARGANTUAR_BASIC:
            case GARGANTUAR_EGYPT:
            case GARGANTUAR_ICEAGE:
            case GARGANTUAR_BEACH:
            case GARGANTUAR_DARK:
                return 3000;

            case IMP_TUTORIAL:
            case IMP_EGYPT:
            case IMP_ICEAGE:
            case IMP_BEACH:
            case IMP_DARK:
                return 80;

            case PHARAOH:
            case CAMEL:
                return 500;

            case RA:
                return 250;

            case EXPLORER:
                return 150;

            case TOMB_RAISER:
                return 300;

            case ICEAGE_HUNTER:
                return 400;

            case BEACH_FISHERMAN:
                return 600;

            case BEACH_OCTOPUS:
                return 800;

            case WIZARD:
                return 500;

            case DARK_JUGGLER:
                return 600;

            case DARK_KING:
                return 2000;

            case ZOMBOSS_EGYPT:
            case ZOMBOSS_PIRATE:
            case ZOMBOSS_COWBOY:
            case ZOMBOSS_DARK:
                return 20000;

            case ZOMBOTANY_PEASHOOTER:
            case ZOMBOTANY_WALLNUT:
            case ZOMBOTANY_JALAPENO:
            case ZOMBOTANY_SQUASH:
                return 250;

            default:
                return 200;
        }
    }

    private float getZombieSpeed(ZombieType zt) {
        switch (zt) {
            case IMP_TUTORIAL:
            case IMP_EGYPT:
            case IMP_ICEAGE:
            case IMP_BEACH:
            case IMP_DARK:
            case WEASEL:
                return 8.0f;

            case GARGANTUAR_BASIC:
            case GARGANTUAR_EGYPT:
            case GARGANTUAR_ICEAGE:
            case GARGANTUAR_BEACH:
            case GARGANTUAR_DARK:
            case ZOMBOSS_EGYPT:
            case ZOMBOSS_PIRATE:
            case ZOMBOSS_COWBOY:
            case ZOMBOSS_DARK:
                return 2.0f;

            case ICEAGE_HUNTER:
            case BEACH_OCTOPUS:
                return 3.0f;

            case DODO:
            case BEACH_SURFER:
            case BEACH_FAST_SWIMMER:
                return 6.5f;

            default:
                return 4.0f;
        }
    }

    private int getZombieDamage(ZombieType zt) {
        switch (zt) {
            case GARGANTUAR_BASIC:
            case GARGANTUAR_EGYPT:
            case GARGANTUAR_ICEAGE:
            case GARGANTUAR_BEACH:
            case GARGANTUAR_DARK:
                return 500;

            case ZOMBOSS_EGYPT:
            case ZOMBOSS_PIRATE:
            case ZOMBOSS_COWBOY:
            case ZOMBOSS_DARK:
                return 1000;

            case ZOMBOTANY_PEASHOOTER:
            case ZOMBOTANY_JALAPENO:
            case ZOMBOTANY_SQUASH:
                return 150;

            default:
                return 100;
        }
    }

    private String getZombieDescription(ZombieType zt) {
        return "Type: " + zt.name() + "\nStandard enemy units that advance towards your lawn defense.";
    }

    private void onUpgrade() {
        if (selectedPlant == null) return;

        User user = AppStatus.getCurrentUser();
        if (user == null || user.collectionState == null) return;

        int oldDisplayLevel = user.collectionState.getPlantLevel(selectedPlant) + 1;
        PlantDefinition definition = PlantLibrary.findByType(selectedPlant).orElse(null);
        if (definition == null) return;

        OutputDTO result = controller.handle(new CollectionInputDTO(
            CollectionCommand.UPGRADE_PLANT, selectedPlant.name(), null));

        statusLabel.setText(result.getMessage());
        statusLabel.setColor(result.isSuccess() ? Color.GREEN : Color.SALMON);
        refreshGrid();
        refreshDetail();

        if (result.isSuccess()) {
            int newDisplayLevel = user.collectionState.getPlantLevel(selectedPlant) + 1;
            UpgradeRule upgradeRule = definition.getUpgradeForLevel(newDisplayLevel);

            UpgradeNotificationPopup.show(
                this,
                selectedPlant,
                definition,
                oldDisplayLevel,
                newDisplayLevel,
                upgradeRule,
                () -> {
                    statusLabel.setText("Upgrade complete!");
                    statusLabel.setColor(Color.GREEN);
                });
        }
    }

    private void onBuy() {
        if (selectedPlant == null) return;
        showBuyDialog(selectedPlant);
    }

    // ====================== پاپ‌آپ اختصاصی و بدون نیاز به WindowStyle (حل کاملاً قطعی Crash) ======================
    private void showBuyDialog(PlantType plant) {
        User user = AppStatus.getCurrentUser();
        if (user == null || getStage() == null) return;

        // لایه تیره مدال روی کل صفحه برای بلاک کردن کلیک‌های پس‌زمینه
        Table modalOverlay = new Table();
        modalOverlay.setFillParent(true);
        modalOverlay.setTouchable(Touchable.enabled);

        Table dialogBox = new Table();
        dialogBox.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        dialogBox.pad(25f);

        Label dialogTitle = new Label("BUY SEED PACKETS", new Label.LabelStyle(titleFont, Color.GOLD));
        dialogBox.add(dialogTitle).colspan(3).padBottom(15f).row();

        PlantDefinition def = PlantLibrary.findByType(plant).orElse(null);
        String plantName = (def != null) ? def.getName() : plant.name();
        Label plantInfo = new Label("Plant: " + plantName, new Label.LabelStyle(bodyFont, Color.WHITE));
        dialogBox.add(plantInfo).colspan(3).padBottom(6f).row();

        int previewLevel = user.collectionState.getPlantLevel(plant) + 1;
        int previewMaxLevel = def == null ? 4 : def.getMaxLevel();
        int previewRequired = UpgradeCostPolicy.currentUpgradeRequirement(previewLevel, previewMaxLevel);
        int previewOwned = user.collectionState.getSeedPacketCount(plant);
        String needText = previewRequired > 0
            ? "Next upgrade: Level " + (previewLevel + 1) + " • Need " + previewRequired + " seed packets • You have " + previewOwned
            : "Plant is at max level.";
        Label needInfo = new Label(needText, new Label.LabelStyle(descFont, Color.valueOf("CFE8FF")));
        needInfo.setWrap(true);
        dialogBox.add(needInfo).width(380f).colspan(3).padBottom(10f).row();

        final int DIAMOND_COST_PER_PACK = UpgradeCostPolicy.diamondsPerShopPack();
        final int SEEDS_PER_PACK = UpgradeCostPolicy.seedsPerShopPack();
        int currentLevel = user.collectionState.getPlantLevel(plant) + 1;
        int maxLevel = def == null ? 4 : def.getMaxLevel();
        int requiredSeeds = UpgradeCostPolicy.currentUpgradeRequirement(currentLevel, maxLevel);
        int ownedSeeds = user.collectionState.getSeedPacketCount(plant);
        int missingSeeds = Math.max(0, requiredSeeds - ownedSeeds);
        int initialPacks = Math.max(1, (missingSeeds + SEEDS_PER_PACK - 1) / SEEDS_PER_PACK);
        final int[] packCount = {initialPacks};

        Label seedsInfoLabel = new Label("", new Label.LabelStyle(bodyFont, Color.CYAN));
        Label costInfoLabel = new Label("", new Label.LabelStyle(bodyFont, Color.GOLD));
        Label diamondBalanceLabel = new Label("Your Diamonds: " + user.userStats.getDiamonds(), new Label.LabelStyle(descFont, Color.LIGHT_GRAY));

        Label countLabel = new Label(initialPacks + " Pack" + (initialPacks > 1 ? "s" : ""), new Label.LabelStyle(bodyFont, Color.WHITE));
        countLabel.setAlignment(Align.center);

        Runnable updateDialogStats = () -> {
            int totalSeeds = packCount[0] * SEEDS_PER_PACK;
            int totalCost = packCount[0] * DIAMOND_COST_PER_PACK;
            seedsInfoLabel.setText("Seed Packets: +" + totalSeeds);
            costInfoLabel.setText("Cost: " + totalCost + " Diamonds");
        };
        updateDialogStats.run();

        MenuButton minusBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_brownbutton_10"), "-", bodyFont,
            skin.getDrawable("image_ui_generic_brownbutton_down_10"), null, null, () -> {
            if (packCount[0] > 1) {
                packCount[0]--;
                countLabel.setText(packCount[0] + " Pack" + (packCount[0] > 1 ? "s" : ""));
                updateDialogStats.run();
            }
        }
        );

        MenuButton plusBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_brownbutton_10"), "+", bodyFont,
            skin.getDrawable("image_ui_generic_brownbutton_down_10"), null, null, () -> {
            if (packCount[0] < 99) {
                packCount[0]++;
                countLabel.setText(packCount[0] + " Packs");
                updateDialogStats.run();
            }
        }
        );

        Table packSelector = new Table();
        packSelector.add(minusBtn).size(45f, 40f).padRight(10f);
        packSelector.add(countLabel).width(110f);
        packSelector.add(plusBtn).size(45f, 40f).padLeft(10f);

        dialogBox.add(packSelector).colspan(3).padBottom(15f).row();
        dialogBox.add(seedsInfoLabel).colspan(3).padBottom(5f).row();
        dialogBox.add(costInfoLabel).colspan(3).padBottom(5f).row();
        dialogBox.add(diamondBalanceLabel).colspan(3).padBottom(20f).row();

        // دکمه‌های تایید و انصراف
        MenuButton confirmBuyBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_greenbutton_10"), "BUY", bodyFont,
            skin.getDrawable("image_ui_generic_greenbutton_down_10"), null, null, () -> {
            OutputDTO result = purchasePackets(plant, packCount[0]);
            statusLabel.setText(result.getMessage());
            statusLabel.setColor(result.isSuccess() ? Color.GREEN : Color.SALMON);
            modalOverlay.remove();
            refreshGrid();
            refreshDetail();
        }
        );
        confirmBuyBtn.setSize(130f, 45f);

        MenuButton cancelBtn = new MenuButton(
            skin.getDrawable("image_ui_generic_brownbutton_10"), "CANCEL", bodyFont,
            skin.getDrawable("image_ui_generic_brownbutton_down_10"), null, null, modalOverlay::remove
        );
        cancelBtn.setSize(130f, 45f);

        Table actionTable = new Table();
        actionTable.add(confirmBuyBtn).padRight(15f);
        actionTable.add(cancelBtn);

        dialogBox.add(actionTable).colspan(3).center();

        modalOverlay.add(dialogBox).center();
        getStage().addActor(modalOverlay);
    }

    // ====================== متد عملیاتی بررسی و خرید بر اساس Diamond ======================
    private OutputDTO purchasePackets(PlantType plant, int packCount) {
        User user = AppStatus.getCurrentUser();
        if (user == null) {
            return new OutputDTO(false, "User is not logged in!");
        }

        int totalDiamonds = packCount * 5;
        int totalSeeds = packCount * 10;
        int currentDiamonds = user.userStats.getDiamonds();

        // ۱. بررسی داشتن Diamond کافی
        if (currentDiamonds < totalDiamonds) {
            return new OutputDTO(false, "Not enough diamonds! Cost: " + totalDiamonds + " Diamonds, Available: " + currentDiamonds + " Diamonds");
        }

        // ۲. کسر Diamond و اضافه کردن Seed Packets به حساب کاربر
        user.userStats.setDiamonds(currentDiamonds - totalDiamonds);
        user.collectionState.addSeedPackets(plant, totalSeeds);

        if (!user.collectionState.isPlantUnlocked(plant)) {
            user.collectionState.unlockPlant(plant);
        }

        return new OutputDTO(true,
            "Bought " + totalSeeds + " seed packets for " + totalDiamonds + " diamonds.");
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // ====================== Actors ======================

    private static class DetailPreviewActor extends Actor {
        private PlantType type;
        private float time;

        void setType(PlantType type) {
            this.type = type;
            this.time = 0f;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (type == null) return;
            EntityRenderer.getInstance().renderPlant((SpriteBatch) batch, type.name(), "idle", time,
                getX() + getWidth() / 2f, getY() + getHeight() / 2f);
        }
    }

    private static class ZombiePreviewActor extends Actor {
        private ZombieType type;
        private float time;
        private com.PVZ.model.entity.zombies.base.Zombie cachedZombie;

        void setType(ZombieType type) {
            if (this.type != type) {
                this.type = type;
                this.time = 0f;
                this.cachedZombie = type != null ? type.create() : null;
                if (this.cachedZombie != null) {
                    com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this.cachedZombie, "idle", 999999f);
                }
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
            if (cachedZombie != null) {
                cachedZombie.update(delta, null);
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (type == null) return;
            float renderX = getX() + getWidth() / 2f;
            float renderY = getY() + getHeight() / 2f;
            if (cachedZombie != null) {
                com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(cachedZombie, "idle", 999999f);
                cachedZombie.setX(renderX);
                cachedZombie.setY(renderY);
                EntityRenderer.getInstance().renderZombie((SpriteBatch) batch, cachedZombie, time);
            } else {
                String pamPath = ZombieTexturePaths.getPamPath(type.alias);
                EntityRenderer.getInstance().renderPam((SpriteBatch) batch, pamPath, "idle", time,
                    renderX, renderY);
            }
        }
    }
}
