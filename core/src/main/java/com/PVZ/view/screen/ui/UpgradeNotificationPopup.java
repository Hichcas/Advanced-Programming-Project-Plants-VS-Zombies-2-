package com.PVZ.view.screen.ui;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.UpgradeRule;
import com.PVZ.model.enums.PlantType;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

import java.util.Locale;

/**
 * Simple, non-modal upgrade notification.
 * No PAM animation, no buttons, no input blocking.
 * It appears for a few seconds and removes itself automatically.
 */
public final class UpgradeNotificationPopup {
    private static final float SHOW_SECONDS = 3.0f;
    private static final float FADE_SECONDS = 0.25f;

    private UpgradeNotificationPopup() {
    }

    public static void show(Table host,
                            PlantType plantType,
                            PlantDefinition definition,
                            int oldDisplayLevel,
                            int newDisplayLevel,
                            UpgradeRule rule,
                            Runnable onClose) {
        if (host == null || host.getStage() == null || definition == null || rule == null) {
            return;
        }

        Skin skin = PvzSkin.get();
        FontManager fonts = FontManager.getInstance();

        Table notification = new Table();
        notification.setTouchable(Touchable.disabled);
        notification.setBackground(getNotificationBackground(skin));
        notification.pad(14f, 22f, 14f, 22f);
        notification.getColor().a = 0f;

        String plantName = definition.getName();
        if (plantName == null || plantName.isBlank()) {
            plantName = plantType == null ? "Plant" : prettyPlantName(plantType.name());
        }

        Label title = new Label(
            "UPGRADE COMPLETE",
            new Label.LabelStyle(fonts.getEnglishTitleFont(), Color.GOLD));
        title.setAlignment(Align.center);

        Label plantLine = new Label(
            plantName + "  •  Level " + newDisplayLevel,
            new Label.LabelStyle(fonts.getEnglishMenuFont(), Color.WHITE));
        plantLine.setAlignment(Align.center);

        Label message = new Label(
            readableUpgradeMessage(definition, rule, newDisplayLevel),
            new Label.LabelStyle(fonts.getEnglishTinyFont(), Color.WHITE));
        message.setWrap(true);
        message.setAlignment(Align.center);

        Label levelLine = new Label(
            "Level " + oldDisplayLevel + "  →  " + newDisplayLevel,
            new Label.LabelStyle(fonts.getEnglishTinyFont(), Color.valueOf("D7E8FF")));
        levelLine.setAlignment(Align.center);

        notification.add(title).center().growX().padBottom(4f).row();
        notification.add(plantLine).center().growX().padBottom(7f).row();
        notification.add(message).width(520f).center().growX().padBottom(7f).row();
        notification.add(levelLine).center().growX();

        notification.pack();

        float worldWidth = host.getStage().getViewport().getWorldWidth();
        float worldHeight = host.getStage().getViewport().getWorldHeight();
        float x = (worldWidth - notification.getWidth()) * 0.5f;
        float y = worldHeight - notification.getHeight() - 28f;
        notification.setPosition(Math.max(12f, x), Math.max(12f, y));
        host.getStage().addActor(notification);

        notification.addAction(Actions.sequence(
            Actions.fadeIn(FADE_SECONDS),
            Actions.delay(SHOW_SECONDS),
            Actions.fadeOut(FADE_SECONDS),
            Actions.run(() -> {
                notification.remove();
                if (onClose != null) {
                    onClose.run();
                }
            })
        ));
    }

    private static Drawable getNotificationBackground(Skin skin) {
        Drawable background = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
        if (background == null) {
            background = skin.getDrawable("image_ui_cards_almanac_plant_card_10");
        }
        return background;
    }

    private static String readableUpgradeMessage(PlantDefinition definition, UpgradeRule rule, int level) {
        String catalogDescription = UpgradeDescriptionCatalog.find(definition.getName(), level);
        if ((catalogDescription == null || catalogDescription.isBlank()) && definition != null) {
            for (PlantType type : PlantType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(definition.getName())) {
                    catalogDescription = UpgradeDescriptionCatalog.find(type.name(), level);
                    break;
                }
            }
        }
        if (catalogDescription != null && !catalogDescription.isBlank()) {
            return catalogDescription;
        }
        UpgradeRule.UpgradeKind kind = rule.getKindEnum();
        if (kind == UpgradeRule.UpgradeKind.STAT) {
            String stat = rule.getStat();
            String unit = rule.getValue() != null ? formatNumber(rule.getValue()) : "";
            String suffix = rule.getRaw() != null && rule.getRaw().contains("%") ? "%" : "";
            UpgradeRule.UpgradeOperation op = rule.getOperationEnum();

            if (stat != null) {
                String value = unit + suffix;
                String readableStat = switch (stat.toUpperCase(Locale.ROOT)) {
                    case "DAMAGE" -> "Damage";
                    case "DAMAGE_PER_TICK" -> "Damage per tick";
                    case "HP" -> "Health";
                    case "COST" -> "Planting cost";
                    case "RECHARGE" -> "Recharge time";
                    case "DURATION" -> "Duration";
                    case "SUN_AMOUNT" -> "Sun production";
                    case "FREEZE_TIME" -> "Freeze time";
                    case "ATTACK_SPEED" -> "Attack speed";
                    case "EAT_TIME" -> "Eat time";
                    case "TARGETS" -> "Target count";
                    default -> humanize(stat);
                };

                return switch (op) {
                    case ADD -> readableStat + " increased by " + value + ".";
                    case SUBTRACT -> readableStat + " reduced by " + value + ".";
                    case SET -> readableStat + " is now " + value + ".";
                    case MULTIPLY -> readableStat + " increased by " + value + ".";
                    default -> "New upgrade: " + firstLetterUpper(rule.getRaw());
                };
            }
        }

        if (kind == UpgradeRule.UpgradeKind.SPECIAL) {
            return "New ability unlocked: " + firstLetterUpper(humanize(rule.getSpecial()));
        }

        if (rule.getRaw() != null && !rule.getRaw().isBlank()) {
            return "New upgrade unlocked: " + rule.getRaw().trim();
        }

        return "A new permanent upgrade is now active.";
    }

    private static String prettyPlantName(String name) {
        return humanize(name.replace('_', ' '));
    }

    private static String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String cleaned = value.toLowerCase(Locale.ROOT)
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceAll("\\s+", " ")
            .trim();
        return firstLetterUpper(cleaned);
    }

    private static String firstLetterUpper(String value) {
        if (value == null || value.isBlank()) return "";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value)
            ? Integer.toString((int) value)
            : String.format(Locale.ROOT, "%.2f", value);
    }
}
