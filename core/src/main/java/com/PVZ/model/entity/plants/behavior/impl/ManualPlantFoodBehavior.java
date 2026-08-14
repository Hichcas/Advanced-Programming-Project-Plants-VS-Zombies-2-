package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Locale;


public class ManualPlantFoodBehavior implements PlantFoodBehavior {

    private final PlantDefinition definition;
    private final AbilitySpec abilitySpec;

    public ManualPlantFoodBehavior(PlantDefinition definition, AbilitySpec abilitySpec) {
        this.definition = definition;
        this.abilitySpec = abilitySpec;
    }

    @Override
    public void onPlantFood(PlantInstance plant, BehaviorContext context) {
        if (plant == null || context == null) {
            return;
        }

        String behaviorId = normalize(resolveBehaviorId());
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        plant.setPlantFoodActive(true);
        plant.setPlantFoodSeconds(5);

        applyBehavior(plant, context, lane, row, col, behaviorId);
    }

    private void applyBehavior(PlantInstance plant, BehaviorContext context,
                               int lane, int row, int col, String behaviorId) {
        switch (behaviorId) {
            case "instant_sun" -> handleInstantSun(plant, context, row, col);
            case "burst_attack", "burst_shot", "multi_shot", "double_projectile" -> handleBurstAttack(plant, context);
            case "freeze_burst" -> handleFreezeBurst(plant, context);
            case "plasma_burst" -> handlePlasmaBurst(plant, context, lane, row);
            case "hypnotize" -> handleHypnotize(context, lane);
            case "fire_burst" -> handleFireBurst(plant);
            case "lane_clear" -> handleLaneClear(plant, context, lane, row);
            case "water_clone" -> handleWaterClone(plant);
            case "magnet_pulse" -> handleMagnetPulse(context, lane);
            case "custom" -> handleCustom(plant, context, lane, row, col);
            case "none" -> { /* do nothing */ }
            default -> handleGenericCustom(plant, context, lane, row, col, behaviorId);
        }
    }

    private void handleInstantSun(PlantInstance plant, BehaviorContext context, int row, int col) {
        int amount = abilitySpec == null ? 0 : abilitySpec.getIntParam("sunAmount", 150);
        if (amount <= 0) {
            amount = 150;
        }
        context.spawnSunAt(row, col, amount);
    }

    private void handleBurstAttack(PlantInstance plant, BehaviorContext context) {
        plant.getStats().putExtra("plantFoodProjectileCount",
            abilitySpec == null ? 5 : abilitySpec.getIntParam("projectiles", 5));
        plant.getStats().putExtra("plantFoodDamageMultiplier",
            abilitySpec == null ? 2.0 : abilitySpec.getDoubleParam("damageMultiplier", 2.0));
        plant.getStats().putExtra("burstAttack", Boolean.TRUE);
    }

    private void handleFreezeBurst(PlantInstance plant, BehaviorContext context) {
        context.freezeAllZombies(3.0);
        plant.getStats().putExtra("iceAttack", Boolean.TRUE);
    }

    private void handlePlasmaBurst(PlantInstance plant, BehaviorContext context, int lane, int row) {
        int damage = Math.max(plant.getStats().getDamage() * 20, 1000);
        context.damageArea(lane, row, damage);
    }

    private void handleHypnotize(BehaviorContext context, int lane) {
        context.hypnotizeZombiesInLane(lane, 5.0);
    }

    private void handleFireBurst(PlantInstance plant) {
        plant.getStats().putExtra("fireAttack", Boolean.TRUE);
        plant.getStats().putExtra("plantFoodDamageMultiplier", 2.0);
        plant.getStats().putExtra("plantFoodProjectileCount", 5);
    }

    private void handleLaneClear(PlantInstance plant, BehaviorContext context, int lane, int row) {
        int damage = Math.max(plant.getStats().getDamage(), 500);
        context.damageArea(lane, row, damage);
    }

    private void handleWaterClone(PlantInstance plant) {
        plant.putRuntimeState("waterCloneReady", Boolean.TRUE);
    }

    private void handleMagnetPulse(BehaviorContext context, int lane) {
        context.disarmZombiesInLane(lane);
    }

    private void handleCustom(PlantInstance plant, BehaviorContext context,
                              int lane, int row, int col) {
        String plantKey = definition == null ? "" : normalize(definition.getName());
        switch (plantKey) {
            case "electric_blueberry" -> handleElectricBlueberry(context);
            case "cactus" -> handleCactus(plant);
            case "fume_shroom" -> handleFumeShroom(plant, context, lane, row, col);
            case "cabbage_pult" -> handlePultFamily(plant, plantKey);
            case "melon_pult" -> handleRandomDamageTargets(context, abilitySpec, 3, 160, false);
            case "winter_melon" -> handleRandomDamageTargets(context, abilitySpec, 3, 160, true);
            case "pepper_pult" -> handleRandomDamageTargets(context, abilitySpec, 3, 100, true);
            case "kernel_pult" -> handleKernelPult(context, abilitySpec);
            case "potato_mine", "primal_potato_mine" -> handlePotatoMine(plant);
            case "squash" -> handleSquash(context, abilitySpec);
            case "iceberg_lettuce" -> handleIcebergLettuce(context);
            case "phat_beet" -> handlePhatBeet(plant, context, lane, row);
            case "chomper" -> handleChomper(context, lane);
            case "wasabi_whip" -> handleWasabiWhip(plant, context, lane, row);
            case "wall_nut", "tall_nut", "endurian", "pumpkin" -> handleDefensivePlant(plant);
            case "sweet_potato" -> handleSweetPotato(context, lane);
            case "explode_o_nut" -> handleExplodeONut(plant);
            case "sun_bean" -> handleSunBean(plant, context, row, col);
            case "hypno_shroom" -> handleHypnoShroom(context, lane);
            case "sun_shroom" -> handleSunShroom(plant, context, row, col);
            default -> handleGenericCustom(plant, context, lane, row, col, plantKey);
        }
    }

    private void handleElectricBlueberry(BehaviorContext context) {
        context.killRandomZombies(3);
    }

    private void handleCactus(PlantInstance plant) {
        plant.getStats().putExtra("pierceBoost", 999);
        plant.getStats().putExtra("plantFoodDamageMultiplier", 20.0);
        plant.getStats().putExtra("plantFoodProjectileCount", 1);
        plant.getStats().putExtra("passThrough", Boolean.TRUE);
    }

    private void handleFumeShroom(PlantInstance plant, BehaviorContext context,
                                  int lane, int row, int col) {
        plant.getStats().putExtra("pierceBoost", 99);
        plant.getStats().putExtra("plantFoodDamageMultiplier", 4.0);
        plant.getStats().putExtra("passThrough", Boolean.TRUE);

        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (!zombies.isEmpty()) {
            double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", col * 177.0), col * 177.0);
            for (Zombie zombie : zombies) {
                if (zombie != null && !zombie.isDead()) {
                    zombie.setX(zombie.getX() + 120.0);
                    if (zombie.getX() < plantX) {
                        zombie.setX(plantX + 120.0);
                    }
                }
            }
        }
    }

    private void handleKernelPult(BehaviorContext context, AbilitySpec spec) {
        // The current zombie model has no persistent "buttered" state, so the closest
        // supported gameplay primitive is a temporary stun on every zombie.
        double duration = spec == null ? 8.0 : spec.getDoubleParam("butterDurationSeconds", 8.0);
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie != null && !zombie.isDead()) {
                zombie.stunOnHit();
            }
        }
        if (context instanceof com.PVZ.model.game.RegularGameEngine) {
            context.freezeAllZombies(Math.max(0.5, Math.min(duration, 8.0)));
        }
    }

    private void handleRandomDamageTargets(BehaviorContext context, AbilitySpec spec,
                                           int fallbackCount, int fallbackDamage, boolean slow) {
        int count = spec == null ? fallbackCount : spec.getIntParam("count", fallbackCount);
        int damage = spec == null ? fallbackDamage : spec.getIntParam("damage", fallbackDamage);
        java.util.List<Zombie> targets = new java.util.ArrayList<>(context.getAllZombies());
        targets.removeIf(z -> z == null || z.isDead());
        java.util.Collections.shuffle(targets);
        count = Math.min(count, targets.size());
        for (int i = 0; i < count; i++) {
            Zombie z = targets.get(i);
            z.takeDamage(Math.max(1, damage));
            if (slow) {
                z.applyEffect(new com.PVZ.model.entity.zombies.base.StatusEffect(
                    com.PVZ.model.enums.DamageType.ICE, 3.0f));
            }
        }
    }

    private void handlePotatoMine(PlantInstance plant) {
        plant.putRuntimeState("armed", Boolean.TRUE);
        plant.putRuntimeState("armTimer", 0.0);
        plant.setPlantFoodActive(true);
        plant.setPlantFoodSeconds(1);
    }

    private void handleSquash(BehaviorContext context, AbilitySpec spec) {
        int count = spec == null ? 2 : spec.getIntParam("count", 2);
        java.util.List<Zombie> targets = new java.util.ArrayList<>(context.getAllZombies());
        targets.removeIf(z -> z == null || z.isDead());
        targets.sort((a, b) -> Double.compare(a.getX(), b.getX()));
        for (int i = 0; i < Math.min(count, targets.size()); i++) {
            context.damageSingleTarget(targets.get(i), 999999);
        }
    }

    private void handlePultFamily(PlantInstance plant, String plantKey) {
        plant.getStats().putExtra("plantFoodProjectileCount", 3);
        plant.getStats().putExtra("plantFoodDamageMultiplier", 3.0);
        if (plantKey.contains("winter")) {
            plant.getStats().putExtra("iceAttack", Boolean.TRUE);
        }
        if (plantKey.contains("pepper")) {
            plant.getStats().putExtra("fireAttack", Boolean.TRUE);
        }
    }

    private void handleIcebergLettuce(BehaviorContext context) {
        context.freezeAllZombies(5.0);
    }

    private void handlePhatBeet(PlantInstance plant, BehaviorContext context, int lane, int row) {
        int aoe = Math.max(plant.getStats().getAoeDamage(), plant.getStats().getDamage() * 4);
        context.damageArea(lane, row, aoe);
        if (lane > 0) {
            context.damageArea(lane - 1, row, aoe);
        }
        context.damageArea(lane + 1, row, aoe);
    }

    private void handleChomper(BehaviorContext context, int lane) {
        List<Zombie> zombies = new java.util.ArrayList<>(context.getZombiesInLane(lane));
        zombies.sort((a, b) -> Double.compare(a.getX(), b.getX()));
        int bites = Math.min(3, zombies.size());
        for (int i = 0; i < bites; i++) {
            Zombie zombie = zombies.get(i);
            if (zombie != null && !zombie.isDead()) {
                zombie.takeDamage(999999);
            }
        }
    }

    private void handleWasabiWhip(PlantInstance plant, BehaviorContext context, int lane, int row) {
        int damage = Math.max(plant.getStats().getDamage() * 3, 120);
        context.damageArea(lane, row, damage);
        if (lane > 0) {
            context.damageArea(lane - 1, row, damage);
        }
        context.damageArea(lane + 1, row, damage);
    }

    private void handleDefensivePlant(PlantInstance plant) {
        plant.heal(Math.max(plant.getStats().getMaxHp() / 2, 500));
        plant.getStats().putExtra("fortified", Boolean.TRUE);
    }

    private void handleSweetPotato(BehaviorContext context, int lane) {
        context.pullAdjacentZombiesToLane(lane);
    }

    private void handleExplodeONut(PlantInstance plant) {
        plant.getStats().putExtra("explodeOnDeath", Boolean.TRUE);
        plant.heal(Math.max(plant.getStats().getMaxHp() / 2, 400));
    }

    private void handleSunBean(PlantInstance plant, BehaviorContext context, int row, int col) {
        int fortifyAmount = Math.max(plant.getStats().getMaxHp() / 2, 500);
        context.fortifyPlantAt(row, col, fortifyAmount);
        plant.getStats().putExtra("fortified", Boolean.TRUE);
    }

    private void handleSunShroom(PlantInstance plant, BehaviorContext context, int row, int col) {
        plant.putRuntimeState("growthStage", 2);
        plant.putRuntimeState("growthTriggered", Boolean.TRUE);
        context.spawnSunAt(row, col, 225);
    }

    private void handleHypnoShroom(BehaviorContext context, int lane) {
        context.hypnotizeZombiesInLane(lane, 6.0);
    }


    private void handleGenericCustom(PlantInstance plant, BehaviorContext context,
                                     int lane, int row, int col, String behaviorId) {
        if (behaviorId.contains("burst")) {
            plant.getStats().putExtra("plantFoodProjectileCount", 5);
            plant.getStats().putExtra("plantFoodDamageMultiplier", 2.0);
        }
        if (behaviorId.contains("freeze")) {
            context.freezeZombiesInLane(lane, 3.0);
        }
        if (behaviorId.contains("fire")) {
            plant.getStats().putExtra("fireAttack", Boolean.TRUE);
        }
        if (behaviorId.contains("ice")) {
            plant.getStats().putExtra("iceAttack", Boolean.TRUE);
        }
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
        if (definition != null && definition.getPlantFoodEffect() != null) {
            return definition.getPlantFoodEffect().getResolvedBehaviorId();
        }
        return "";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
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
