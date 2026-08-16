package com.PVZ.model.game.chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Human-readable rule explanations for every stage type, shown in the
 * level-start overlay and the pause menu. Texts follow the project spec:
 * CONVEYOR_BELT, LOCKED_PLANTS, NIGHT_OPS, LOVE_YOUR_PLANTS, DEAD_LINE.
 */
public final class StageRules {

    private StageRules() {
    }

    public static List<String> describe(StageConfig config) {
        List<String> out = new ArrayList<>();
        if (config == null) {
            out.add("Don't let the zombies reach your house!");
            return out;
        }

        String type = config.getType() == null ? "" : config.getType().trim().toUpperCase();
        String special = config.getSpecialLevel() == null ? "" : config.getSpecialLevel().trim().toUpperCase();
        boolean deadLine = type.contains("DEADLINE") || special.contains("DEAD_LINE");

        if (deadLine) {
            out.add("DEAD LINE: Every lane has its own red line at a random position of the lawn.");
            out.add("The moment any zombie crosses its lane's red line, you lose the level!");
        } else if (type.equals("CONVEYOR_BELT")) {
            out.add("CONVEYOR BELT: No seed selection — you enter the level directly.");
            double interval = config.getConveyorInterval() > 0 ? config.getConveyorInterval() : 12.0;
            String seconds = interval == Math.floor(interval)
                ? String.valueOf((long) interval) : String.valueOf(interval);
            out.add("The belt in the corner of the screen brings a random plant (from the plants you own) every "
                + seconds + " seconds — the first one arrives the moment you enter!");
        } else if (type.equals("LOCKED_PLANTS")) {
            out.add("LOCKED PLANTS: Some seeds are unavailable in this level.");
            if (config.getLockedFamilies() != null && !config.getLockedFamilies().isEmpty()) {
                String families = config.getLockedFamilies().stream()
                    .map(StageConfig.FamilyLockEntry::getFamily)
                    .collect(Collectors.joining(", "));
                out.add("Only ONE plant of each locked family may be picked — the rest of the family is locked: "
                    + families + ".");
            }
            if (config.getLockedPlants() != null && !config.getLockedPlants().isEmpty()) {
                out.add("These plants are completely locked: " + String.join(", ", config.getLockedPlants()) + ".");
            }
        } else if (type.equals("NIGHT_OPS") || special.equals("NIGHT_OPS")) {
            out.add("NIGHT OPS: Night has fallen — no sun drops from the sky.");
            out.add("Only sun produced by your plants (like Sunflower) keeps you alive!");
        } else if (type.equals("TIMED_WAR") || special.contains("TIMED_WAR")) {
            double secs = config.getTimedWarSeconds() > 0 ? config.getTimedWarSeconds() : 120;
            String time = secs == Math.floor(secs)
                ? String.valueOf((long) secs) + " seconds" : String.valueOf(secs) + " seconds";
            out.add("TIMED WAR: A timer at the top of the screen is ticking!");
            boolean anyGoal = false;
            if (config.getTimedWarZombieKills() > 0) {
                out.add("Kill " + config.getTimedWarZombieKills() + " zombies before the "
                    + time + " run out!");
                anyGoal = true;
            }
            if (config.getTimedWarSunTarget() > 0) {
                out.add("Produce (collect) " + config.getTimedWarSunTarget() + " sun before the "
                    + time + " run out!");
                anyGoal = true;
            }
            if (!anyGoal) {
                out.add("Kill 12 zombies before the " + time + " run out!");
            }
        } else if (type.equals("LOVE_YOUR_PLANTS") || special.equals("LOVE_YOUR_PLANTS")) {
            int q = config.getMaxPlantDeaths() > 0 ? config.getMaxPlantDeaths() : 5;
            out.add("LOVE YOUR PLANTS: If " + q + " of your plants are destroyed or eaten by zombies, you lose!");
        }

        if (out.isEmpty()) {
            out.add("Pick your seeds, collect sun, and stop the zombie waves before they reach your house!");
        } else if (!deadLine) {
            out.add("Clear all waves — don't let the zombies reach your house!");
        }

        if (config.isDisableFallingSun() && !type.equals("NIGHT_OPS") && !special.equals("NIGHT_OPS")) {
            out.add("No sun falls from the sky this level!");
        }
        if (config.getPlantLimit() > 0) {
            out.add("You may pick up to " + config.getPlantLimit() + " plants for this level.");
        }
        return out;
    }

    public static String summary(StageConfig config) {
        List<String> lines = describe(config);
        return lines.isEmpty() ? "" : lines.get(0);
    }
}
