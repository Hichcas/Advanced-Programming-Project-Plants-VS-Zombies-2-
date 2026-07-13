package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Locale;

public class ManualPlantBehavior implements PlantBehavior {
    private final PlantDefinition definition;
    private final AbilitySpec abilitySpec;

    public ManualPlantBehavior(PlantDefinition definition, AbilitySpec abilitySpec) {
        this.definition = definition;
        this.abilitySpec = abilitySpec;
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        String plantKey = definition == null ? "" : normalize(definition.getPlantKey());
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        // Plants whose base ability isn't "shoot straight ahead in my own lane" but the
        // generic ShooterBehavior/LobberBehavior would otherwise be picked for them
        // (see BehaviorFactory) - handled by plant identity, not by the generic
        // baseAbility.kind string, since e.g. rotobaga's kind is the same "direct_shot"
        // as a plain peashooter.
        switch (plantKey) {
            case "rotobaga" -> {
                handleDiagonalShot(plant, context, lane, deltaTime);
                return;
            }
            case "threepeater" -> {
                handleTriLaneShot(plant, context, lane, deltaTime);
                return;
            }
            case "split_pea" -> {
                handleFrontBackShot(plant, context, lane, deltaTime);
                return;
            }
            case "starfruit" -> {
                handleStarShot(plant, context, lane, deltaTime);
                return;
            }
            case "cat_tail", "cattail" -> {
                handleHomingNearest(plant, context, lane, deltaTime);
                return;
            }
            case "bowling_bulb" -> {
                handleBounceMultiLane(plant, context, lane, deltaTime);
                return;
            }
            default -> {
            }
        }

        String behaviorId = normalize(resolveBehaviorId());

        switch (behaviorId) {
            case "instant_sun" -> handleInstantSun(plant, context, row, col);
            case "move_zombies", "garlic" -> handleMoveZombies(plant, context, lane, deltaTime);
            case "magnet_disarm", "magnet_pulse" -> handleMagnet(plant, context, lane, deltaTime);
            case "hypnotize" -> handleHypnotize(plant, context, lane, deltaTime);
            case "copy_plant" -> handleCopyPlant(plant);
            case "water_support" -> handleWaterSupport(plant);
            case "melee_eat" -> handleMelee(plant, context, lane, row, deltaTime);
            case "sun_production" -> handleSunBeanLike(plant, context, row, col, lane, deltaTime);
            default -> {
                if (definition != null && definition.getCategoryEnum().name().equals("MELEE")) {
                    handleMelee(plant, context, lane, row, deltaTime);
                }
            }
        }
    }

    private void handleInstantSun(PlantInstance plant, BehaviorContext context, int row, int col) {
        boolean triggered = asBoolean(plant.getRuntimeState().getOrDefault("instantSunTriggered", Boolean.FALSE), false);
        if (triggered) {
            return;
        }

        int amount = plant.getStats().getSunAmount();
        if (amount <= 0 && abilitySpec != null) {
            amount = abilitySpec.getIntParam("sunAmount", 375);
        }
        if (amount <= 0) {
            amount = 375;
        }
        System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col + ")");
        context.spawnSunAt(row, col, amount);
        plant.putRuntimeState("instantSunTriggered", Boolean.TRUE);
        // Gold Bloom: "تولید فوری و یک‌باره مقدار زیادی خورشید و سپس ناپدید شدن" -
        // it must vanish right after producing its one-time sun, not stay on the lawn.
        context.removePlant(row, col);
    }

    private void handleMoveZombies(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("moveTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("moveTimer", timer);
            return;
        }
        timer = 0.0;
        int targetLane = lane > 0 ? lane - 1 : lane + 1;
        context.moveZombiesFromLane(lane, targetLane);
        plant.putRuntimeState("moveTimer", timer);
    }

    private void handleMagnet(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("magnetTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("magnetTimer", timer);
            return;
        }
        timer = 0.0;
        context.disarmZombiesInLane(lane);
        plant.putRuntimeState("magnetTimer", timer);
    }

    private void handleHypnotize(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("hypnoTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("hypnoTimer", timer);
            return;
        }
        timer = 0.0;
        context.hypnotizeZombiesInLane(lane, 3.0);
        plant.putRuntimeState("hypnoTimer", timer);
    }

    private void handleCopyPlant(PlantInstance plant) {
        Object copied = plant.getStats().getExtra("copiedPlantType");
        if (copied != null) {
            plant.putRuntimeState("copiedPlantType", String.valueOf(copied));
        }
    }

    private void handleWaterSupport(PlantInstance plant) {
        plant.putRuntimeState("waterSupport", Boolean.TRUE);
    }

    private void handleMelee(PlantInstance plant, BehaviorContext context, int lane, int row, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return;
        }
        timer = 0.0;
        int damage = Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
        if (damage <= 0) {
            damage = 30;
        }
        context.damageArea(lane, row, damage);
        if (plant.getStats().getEatTimeSeconds() > 0) {
            plant.putRuntimeState("digestTimer", plant.getStats().getEatTimeSeconds());
        }
        plant.putRuntimeState("meleeTimer", timer);
    }

    private void handleSunBeanLike(PlantInstance plant, BehaviorContext context, int row, int col, int lane, double deltaTime) {
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        Double timer = asDouble(plant.getRuntimeState().getOrDefault("sunBeanTimer", 0.0), 0.0);
        timer += deltaTime;
        if (timer >= 1.0) {
            int amount = plant.getStats().getSunDropAmount();
            if (amount <= 0) {
                amount = 5;
            }
            System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col + ")");
            context.spawnSunAt(row, col, amount);
            timer = 0.0;
        }
        plant.putRuntimeState("sunBeanTimer", timer);
    }

    // --- Multi-direction shooters -------------------------------------------------

    private int computeDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double multiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            multiplier = Math.max(multiplier, plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        return (int) Math.round(damage * multiplier);
    }

    private void fireInto(PlantInstance plant, BehaviorContext context, int targetLane, boolean backward) {
        if (targetLane < 0) {
            return;
        }
        int damage = computeDamage(plant);
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        projectile.setLane(targetLane);
        projectile.putExtra("targetLane", targetLane);
        if (backward) {
            projectile.setSpeed(-Math.abs(projectile.getSpeed()));
        }
        context.spawnProjectile(projectile);
    }

    /** Rotobaga: "شلیک در ۴ جهت اریب" - 4 diagonal directions. This engine only models
     * lanes (rows) and forward/backward travel, so the 4 diagonals are approximated as
     * the two lanes next to it, hit both ahead and behind: (lane-1, fwd), (lane+1, fwd),
     * (lane-1, back), (lane+1, back) = 4 shots. */
    private void handleDiagonalShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "diagonalTimer", deltaTime)) {
            return;
        }
        fireInto(plant, context, lane - 1, false);
        fireInto(plant, context, lane + 1, false);
        fireInto(plant, context, lane - 1, true);
        fireInto(plant, context, lane + 1, true);
    }

    /** Threepeater: "شلیک همزمان تیر در ۳ لاین موازی" - own lane + both neighbors. */
    private void handleTriLaneShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "triLaneTimer", deltaTime)) {
            return;
        }
        boolean anyTarget = !context.getZombiesInLane(lane).isEmpty()
                || !context.getZombiesInLane(lane - 1).isEmpty()
                || !context.getZombiesInLane(lane + 1).isEmpty();
        if (!anyTarget) {
            return;
        }
        fireInto(plant, context, lane, false);
        fireInto(plant, context, lane - 1, false);
        fireInto(plant, context, lane + 1, false);
    }

    /** Split Pea: "شلیک ۱ تیر به جلو و ۲ تیر به عقب" - 1 forward + 2 backward, own lane. */
    private void handleFrontBackShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "frontBackTimer", deltaTime)) {
            return;
        }
        if (context.getZombiesInLane(lane).isEmpty()) {
            return;
        }
        fireInto(plant, context, lane, false);
        fireInto(plant, context, lane, true);
        fireInto(plant, context, lane, true);
    }

    /** Starfruit: "شلیک در ۵ جهت ستاره‌ای شامل عقب" - 5-point star including backward. */
    private void handleStarShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "starTimer", deltaTime)) {
            return;
        }
        fireInto(plant, context, lane, false);
        fireInto(plant, context, lane - 1, false);
        fireInto(plant, context, lane + 1, false);
        fireInto(plant, context, lane - 1, true);
        fireInto(plant, context, lane + 1, true);
    }

    /** Cat Tail: "تیر هدف‌دار به سمت نزدیک‌ترین زامبی" - homes on the nearest zombie
     * on the WHOLE lawn, not just its own lane. */
    private void handleHomingNearest(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "homingTimer", deltaTime)) {
            return;
        }
        List<Zombie> all = context.getAllZombies();
        if (all.isEmpty()) {
            return;
        }
        Zombie nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Zombie z : all) {
            if (z == null || z.isDead()) {
                continue;
            }
            double distance = Math.abs(z.getRow() - lane) * 1000.0 + z.getX();
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = z;
            }
        }
        if (nearest == null) {
            return;
        }
        fireInto(plant, context, (int) nearest.getRow(), false);
    }

    /** Bowling Bulb: 3 staggered bulbs (Cyan 2s / Blue 5s / Orange 10s, dealing the
     * tiered 40/120/180 damage from its damageSpec) that bounce between lanes and repeat
     * on that same cadence for as long as the plant is alive. True per-tile bounce
     * physics don't exist in this engine, so each bulb is approximated as an area hit
     * across its own lane and both neighbors, fired on a repeating timer at the
     * described delay instead of all at once.
     *
     * Previously each bulb had a one-shot "fired" boolean that was never reset, so
     * Bowling Bulb fired exactly 3 shots total in its entire lifetime and then went
     * permanently silent, and all 3 bulbs used the same flat damage (the tier's max,
     * 180) instead of their own 40/120/180 values. */
    private void handleBounceMultiLane(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        java.util.List<Integer> tiers = plant.getDefinition() != null && plant.getDefinition().getDamageSpec() != null
                ? plant.getDefinition().getDamageSpec().getTiers()
                : java.util.List.of();
        int cyanDamage = tiers.size() > 0 ? tiers.get(0) : computeDamage(plant);
        int blueDamage = tiers.size() > 1 ? tiers.get(1) : computeDamage(plant);
        int orangeDamage = tiers.size() > 2 ? tiers.get(2) : computeDamage(plant);

        launchBulbOnCycle(plant, context, lane, deltaTime, 2.0, "cyanTimer", cyanDamage);
        launchBulbOnCycle(plant, context, lane, deltaTime, 5.0, "blueTimer", blueDamage);
        launchBulbOnCycle(plant, context, lane, deltaTime, 10.0, "orangeTimer", orangeDamage);
    }

    private void launchBulbOnCycle(PlantInstance plant, BehaviorContext context, int lane, double deltaTime,
                                    double periodSeconds, String timerKey, int damage) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault(timerKey, 0.0), 0.0);
        timer += deltaTime;
        if (timer < periodSeconds) {
            plant.putRuntimeState(timerKey, timer);
            return;
        }
        plant.putRuntimeState(timerKey, 0.0);
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        context.damageArea(lane, row, damage);
        context.damageArea(lane - 1, row, damage);
        context.damageArea(lane + 1, row, damage);
    }

    private boolean tickCooldown(PlantInstance plant, String key, double deltaTime) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault(key, 0.0), 0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (timer < cooldown) {
            plant.putRuntimeState(key, timer);
            return false;
        }
        plant.putRuntimeState(key, 0.0);
        return true;
    }

    private String resolveBehaviorId() {
        if (abilitySpec != null) {
            if (abilitySpec.getBehaviorId() != null && !abilitySpec.getBehaviorId().isBlank()) {
                return abilitySpec.getBehaviorId();
            }
            if (abilitySpec.getId() != null && !abilitySpec.getId().isBlank()) {
                return abilitySpec.getId();
            }
        }
        if (definition != null && definition.getBaseAbility() != null) {
            return definition.getBaseAbility().getResolvedBehaviorId();
        }
        return "";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static double asDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
