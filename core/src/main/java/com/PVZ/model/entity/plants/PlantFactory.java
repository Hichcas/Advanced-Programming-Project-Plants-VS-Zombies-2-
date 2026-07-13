package com.PVZ.model.entity.plants;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;

public final class PlantFactory {

    private PlantFactory() {
    }

    public static PlantInstance create(PlantDefinition definition, int level) {
        if (definition == null) {
            throw new IllegalArgumentException("Plant definition cannot be null");
        }

        int safeLevel = Math.max(1, level);
        PlantStats stats = UpgradeResolver.resolveStats(definition, safeLevel);
        applyInnateSpecials(definition, stats);
        return new PlantInstance(definition, stats, safeLevel);
    }

    /**
     * A handful of plants have a base-kit passive that isn't represented anywhere in the
     * generic JSON ability schema (WallBehavior already knows how to *use* sunDropAmount /
     * reflectDamage once set — via UpgradeResolver's REFLECT_DAMAGE/SUN_DROP upgrade kind —
     * but nothing was ever setting a base value at level 1). This fills those in.
     */
    private static void applyInnateSpecials(PlantDefinition definition, PlantStats stats) {
        String key = definition.getPlantKey();
        if (key == null) {
            return;
        }

        // Base-kit multi-shot plants: definition.getEffectiveDamage() (used by
        // UpgradeResolver.baseStats() to seed stats.damage) returns DamageSpec's
        // *totalDamage* for a "multiProjectile" spec (e.g. repeater "20x2" -> 40,
        // mega_gatling_pea "20x4" -> 80, rotobaga "10x3" -> 30). Left alone, that total
        // gets fired as the damage of a SINGLE projectile/hit instead of being split
        // across the plant's actual multiple shots - so repeater/mega_gatling_pea only
        // ever hit one zombie per volley for the full combined amount, and rotobaga
        // (whose ManualPlantBehavior fires 4 real diagonal shots) was hitting for the
        // full 30 on EACH of its 4 shots (120 total instead of the intended ~30-40).
        // Fix: use per-projectile damage, and (for plain ShooterBehavior plants) tell it
        // how many projectiles to actually fire so total output matches the spec.
        DamageSpec damageSpec = definition.getDamageSpec();
        if (damageSpec != null) {
            if (damageSpec.getKindEnum() == DamageSpec.DamageKind.MULTI_PROJECTILE
                    && damageSpec.getDamagePerProjectile() != null) {
                stats.setDamage(damageSpec.getDamagePerProjectile());
                if (damageSpec.getProjectiles() != null && damageSpec.getProjectiles() > 1) {
                    switch (key) {
                        case "rotobaga", "threepeater", "split_pea", "starfruit", "cat_tail", "bowling_bulb" -> {
                            // These already fire N discrete shots themselves via
                            // ManualPlantBehavior's dedicated handlers (one fireInto() call
                            // per direction) - don't also multiply by projectileCount, or
                            // they'd double-dip.
                        }
                        default -> stats.putExtra("projectileCount", damageSpec.getProjectiles());
                    }
                }
            } else if (damageSpec.getKindEnum() == DamageSpec.DamageKind.TIERED
                    && damageSpec.getTiers() != null && !damageSpec.getTiers().isEmpty()) {
                // Tiered damage should start at the first tier, not the final/max tier.
                // Otherwise plants like Pea Pod / Kernel-pult / Kiwibeast all inherit their
                // end-of-spectrum damage as their default base damage.
                stats.setDamage(damageSpec.getTiers().get(0));
            }
        }

        if (definition.hasTag(PlantTag.FIRE)) {
            stats.putExtra("fireAttack", Boolean.TRUE);
        }
        if (definition.hasTag(PlantTag.ICE)) {
            stats.putExtra("iceAttack", Boolean.TRUE);
            stats.putExtra("freezeAttack", Boolean.TRUE);
        }
        if (definition.hasTag(PlantTag.POISON)) {
            stats.putExtra("poisonAttack", Boolean.TRUE);
        }
        if (definition.hasTag(PlantTag.CHARGE) && stats.getChargeTimeSeconds() <= 0) {
            stats.setChargeTimeSeconds(2.0);
        }

        switch (key) {
            case "sun_bean" -> {
                if (stats.getSunDropAmount() <= 0) {
                    stats.setSunDropAmount(5);
                }
            }
            case "endurian" -> {
                if (stats.getReflectDamage() <= 0) {
                    stats.setReflectDamage(Math.max(20, stats.getDamage()));
                }
            }
            case "explode_o_nut" -> {
                // "به محض از بین رفتن جانش، انفجار مساحتی ایجاد می‌کند" — this is base-kit,
                // not something that should require plant food first.
                stats.putExtra("explodeOnDeath", Boolean.TRUE);
            }
            case "potato_mine" -> {
                // "مسلح شدن با تاخیر ۱۵ ثانیه؛ انفجار هنگام تماس." — this delay has no
                // structured JSON field, so armTimeSeconds defaulted to 0. Since
                // ExplosiveBehavior only ever arms when armTimeSeconds > 0, the mine
                // never armed at all before this fix — it just sat there forever, a dud.
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(15.0);
                }
            }
            case "primal_potato_mine" -> {
                // "مسلح شدن سریع‌تر ۵ ثانیه‌ای..." — same missing-arm-time bug as potato_mine.
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(5.0);
                }
            }
            case "cactus" -> {
                // "شلیک خار مستقیم (عبور از ۳ زامبی)" — pierce count has no structured
                // JSON field, so ProjectileFactory's Math.max(1, stats.getPierce()) was
                // always falling back to 1 (hits exactly one zombie, no pass-through).
                if (stats.getPierce() <= 0) {
                    stats.setPierce(3);
                }
            }
            case "fume_shroom" -> {
                // "شلیک دود ... که از زامبی‌ها رد می‌شود" — unlimited pass-through, same
                // missing-pierce-field issue as cactus.
                if (stats.getPierce() <= 0) {
                    stats.setPierce(99);
                }
            }
            default -> { }
        }
    }

    public static Plant createPlant(PlantDefinition definition, int userLevel) {
        PlantInstance instance = create(definition, userLevel);
        return new Plant(instance);
    }

    public static Plant createPlant(String name, int userLevel) {
        PlantDefinition definition = PlantLibrary.findByName(name).orElse(null);

        if (definition == null) {
            System.err.println(" Factory Warning: Cannot create plant. Unknown plant name: " + name);
            return null;
        }

        PlantInstance instance = create(definition, userLevel);
        Plant created = new Plant(instance);
        System.out.println(" Factory: Created " + created.getType() + " (Level " + instance.getLevel() + ")");
        return created;
    }

    public static Plant createPlant(PlantType type, int userLevel) {
        if (type == null) {
            return null;
        }

        PlantDefinition definition = type.getDefinition();
        if (definition == null) {
            System.err.println(" Factory Warning: Cannot create plant. Unknown plant type: " + type);
            return null;
        }

        return createPlant(definition, userLevel);
    }
}
