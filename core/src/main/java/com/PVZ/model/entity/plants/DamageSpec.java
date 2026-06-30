package com.PVZ.model.entity.plants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DamageSpec {

    public enum DamageKind {
        SINGLE,
        MULTI_PROJECTILE,
        TIERED,
        INSTANT_KILL,
        AREA,
        CONTINUOUS,
        UNKNOWN;

        public static DamageKind fromRaw(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return UNKNOWN;
            }

            String value = raw.trim()
                    .toLowerCase(Locale.ROOT)
                    .replace('-', '_')
                    .replace(' ', '_');

            return switch (value) {
                case "single" -> SINGLE;
                case "multi_projectile", "multishot", "multi_shot" -> MULTI_PROJECTILE;
                case "tiered" -> TIERED;
                case "instant_kill", "instakill" -> INSTANT_KILL;
                case "area", "aoe" -> AREA;
                case "continuous" -> CONTINUOUS;
                default -> UNKNOWN;
            };
        }
    }

    private String raw;
    private String kind;
    private Integer totalDamage;
    private Integer damagePerProjectile;
    private Integer projectiles;
    private List<Integer> tiers = new ArrayList<>();
    private Integer radius;
    private Integer pierce;
    private String note;

    public DamageSpec() {
    }

    public static DamageSpec single(int damage) {
        DamageSpec spec = new DamageSpec();
        spec.setKind(DamageKind.SINGLE.name());
        spec.setTotalDamage(damage);
        return spec;
    }

    public static DamageSpec multiProjectile(int damagePerProjectile, int projectiles) {
        DamageSpec spec = new DamageSpec();
        spec.setKind(DamageKind.MULTI_PROJECTILE.name());
        spec.setDamagePerProjectile(damagePerProjectile);
        spec.setProjectiles(projectiles);
        spec.setTotalDamage(damagePerProjectile * projectiles);
        return spec;
    }

    public static DamageSpec tiered(List<Integer> tiers) {
        DamageSpec spec = new DamageSpec();
        spec.setKind(DamageKind.TIERED.name());
        spec.setTiers(tiers);
        if (tiers != null && !tiers.isEmpty()) {
            spec.setTotalDamage(tiers.get(tiers.size() - 1));
        }
        return spec;
    }

    public static DamageSpec instantKill() {
        DamageSpec spec = new DamageSpec();
        spec.setKind(DamageKind.INSTANT_KILL.name());
        spec.setTotalDamage(Integer.MAX_VALUE);
        return spec;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public DamageKind getKindEnum() {
        return DamageKind.fromRaw(kind);
    }

    public Integer getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(Integer totalDamage) {
        this.totalDamage = totalDamage;
    }

    public Integer getDamagePerProjectile() {
        return damagePerProjectile;
    }

    public void setDamagePerProjectile(Integer damagePerProjectile) {
        this.damagePerProjectile = damagePerProjectile;
    }

    public Integer getProjectiles() {
        return projectiles;
    }

    public void setProjectiles(Integer projectiles) {
        this.projectiles = projectiles;
    }

    public List<Integer> getTiers() {
        return tiers;
    }

    public void setTiers(List<Integer> tiers) {
        this.tiers = tiers == null ? new ArrayList<>() : new ArrayList<>(tiers);
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public Integer getPierce() {
        return pierce;
    }

    public void setPierce(Integer pierce) {
        this.pierce = pierce;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getEffectiveDamage() {
        if (totalDamage != null) {
            return totalDamage;
        }
        if (damagePerProjectile != null && projectiles != null) {
            return damagePerProjectile * projectiles;
        }
        if (tiers != null && !tiers.isEmpty()) {
            return tiers.get(tiers.size() - 1);
        }
        return 0;
    }

    public boolean isInstantKill() {
        return getKindEnum() == DamageKind.INSTANT_KILL
                || Integer.valueOf(Integer.MAX_VALUE).equals(totalDamage);
    }
}
