package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Set;

/**
 * Covers all EXPLOSIVE-category one-shot plants (potato_mine, primal_potato_mine,
 * cherry_bomb, grapeshot, jalapeno, doom_shroom, tangle_kelp, iceberg_lettuce,
 * ice_shroom).
 *
 * Two bugs fixed here:
 * 1) The old "arm" check was `armTime > 0 && armTimer >= armTime`. Only
 *    potato_mine/primal_potato_mine ever had a non-zero armTimeSeconds (and even that
 *    was 0 before PlantFactory's fix), so for every plant with armTime <= 0 (i.e.
 *    every instant explosive - cherry_bomb, jalapeno, grapeshot, doom_shroom,
 *    ice_shroom, ...) the `armTime > 0` guard was permanently false and the plant
 *    NEVER armed - it just sat on the lawn doing nothing forever. armTime <= 0 now
 *    means "arms immediately", matching "انفجار فوری" (instant explosion).
 * 2) Once armed, the old code required a zombie already standing in its exact lane
 *    before it would ever explode/consume itself - correct for the two true
 *    contact-triggered plants ("انفجار هنگام تماس" - potato_mine/primal_potato_mine,
 *    and "اولین زامبی ... که به آن برسد/پا بگذارد" - tangle_kelp/iceberg_lettuce),
 *    but wrong for the instant one-shot plants, which detonate the moment they're
 *    armed regardless of whether a zombie happens to be in that lane.
 *
 * Not fixed here (needs new BehaviorContext capabilities this engine doesn't have
 * yet, so left as a known gap rather than guessed at): doom_shroom's whole-garden
 * blast + unplantable crater only hits its own lane below; ice_shroom now correctly
 * freezes every zombie instead of dealing lane damage; grave_buster (removing a
 * grave) and hot_potato (melting an ice tile) aren't zombie-damage abilities at all
 * and have no corresponding context method, so they fall back to a harmless no-op
 * self-consume instead of doing nothing forever.
 */
public class ExplosiveBehavior implements PlantBehavior {

    private static final Set<String> CONTACT_TRIGGERED = Set.of(
            "potato_mine", "primal_potato_mine", "tangle_kelp", "iceberg_lettuce");

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed == null || !armed) {
            Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
            armTimer += deltaTime;

            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTimer >= Math.max(armTime, 0.0)) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }

            plant.putRuntimeState("armTimer", armTimer);
            if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE))) {
                return;
            }
        }

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();

        boolean requiresContact = key != null && CONTACT_TRIGGERED.contains(key);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (requiresContact && zombies.isEmpty()) {
            // Still waiting for a zombie to actually reach it - stay armed and keep checking.
            return;
        }

        if ("ice_shroom".equals(key)) {
            context.freezeAllZombies(Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            plant.takeDamage(plant.getCurrentHp());
            return;
        }

        int damage = plant.getStats().getExplodeDamage() > 0
                ? plant.getStats().getExplodeDamage()
                : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());

        context.damageArea(lane, row, damage);
        plant.takeDamage(plant.getCurrentHp());
    }
}
