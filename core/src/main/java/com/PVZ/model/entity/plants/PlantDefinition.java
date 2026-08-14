package com.PVZ.model.entity.plants;

import com.PVZ.model.enums.PlantCategory;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDefinition {

    @JsonAlias({"legacyId", "plantId", "typeId"})
    private int legacyId;

    @JsonAlias({"id", "key", "slug", "plantKey"})
    private String plantKey;

    @JsonAlias({"name", "displayName"})
    private String name;

    private String category;

    @JsonAlias({"tags", "tagList"})
    private List<String> tags = new ArrayList<>();

    private int cost;
    private int baseHp;
    private int damage;
    private DamageSpec damageSpec;
    private AbilitySpec baseAbility;
    private AbilitySpec plantFoodEffect;
    private List<UpgradeRule> upgrades = new ArrayList<>();
    private double actionIntervalSeconds;
    private double rechargeSeconds;
    private boolean unlockedByDefault;
    private String note;

    public PlantDefinition() {
    }

    public static Builder builder(PlantType type) {
        return new Builder()
            .legacyId(type.getLegacyId())
            .plantKey(type.getDisplayName())
            .name(type.getDisplayName());
    }

    public static Builder builder(int legacyId, String name) {
        return new Builder().legacyId(legacyId).name(name);
    }

    public PlantType getType() {
        if (plantKey != null && !plantKey.isBlank()) {
            try {
                return PlantType.fromName(plantKey);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (name != null && !name.isBlank()) {
            try {
                return PlantType.fromName(name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return PlantType.fromId(legacyId);
    }

    public int getId() {
        return legacyId;
    }

    public int getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(int legacyId) {
        this.legacyId = legacyId;
    }

    public String getPlantKey() {
        return plantKey;
    }

    public void setPlantKey(String plantKey) {
        this.plantKey = plantKey;
    }

    public String getName() {
        return name;
    }

    public PlantDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public PlantDefinition setCategory(String category) {
        this.category = category;
        return this;
    }

    public PlantCategory getCategoryEnum() {
        if (category == null || category.trim().isEmpty()) {
            return PlantCategory.UNKNOWN;
        }

        String normalized = category.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);

        try {
            return PlantCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return PlantCategory.fromRaw(category);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public PlantDefinition setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        return this;
    }

    public Set<PlantTag> getTagEnums() {
        Set<PlantTag> result = EnumSet.noneOf(PlantTag.class);
        if (tags == null) {
            return result;
        }

        for (String tag : tags) {
            PlantTag parsed = PlantTag.fromRaw(tag);
            if (parsed != PlantTag.UNKNOWN) {
                result.add(parsed);
            }
        }
        return result;
    }

    public boolean hasTag(PlantTag tag) {
        return getTagEnums().contains(tag);
    }

    public int getCost() {
        return cost;
    }

    public PlantDefinition setCost(int cost) {
        this.cost = cost;
        return this;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public PlantDefinition setBaseHp(int baseHp) {
        this.baseHp = baseHp;
        return this;
    }

    public int getDamage() {
        return damage;
    }

    public PlantDefinition setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public DamageSpec getDamageSpec() {
        return damageSpec;
    }

    public PlantDefinition setDamageSpec(DamageSpec damageSpec) {
        this.damageSpec = damageSpec;
        return this;
    }

    public int getEffectiveDamage() {
        return damageSpec != null ? damageSpec.getEffectiveDamage() : damage;
    }

    public AbilitySpec getBaseAbility() {
        return baseAbility;
    }

    public PlantDefinition setBaseAbility(AbilitySpec baseAbility) {
        this.baseAbility = baseAbility;
        return this;
    }

    public AbilitySpec getPlantFoodEffect() {
        return plantFoodEffect;
    }

    public PlantDefinition setPlantFoodEffect(AbilitySpec plantFoodEffect) {
        this.plantFoodEffect = plantFoodEffect;
        return this;
    }

    public List<UpgradeRule> getUpgrades() {
        return upgrades;
    }

    public PlantDefinition setUpgrades(List<UpgradeRule> upgrades) {
        this.upgrades = upgrades == null ? new ArrayList<>() : new ArrayList<>(upgrades);
        return this;
    }

    public double getActionIntervalSeconds() {
        return actionIntervalSeconds;
    }

    public PlantDefinition setActionIntervalSeconds(double actionIntervalSeconds) {
        this.actionIntervalSeconds = actionIntervalSeconds;
        return this;
    }

    public double getRechargeSeconds() {
        return rechargeSeconds;
    }

    public PlantDefinition setRechargeSeconds(double rechargeSeconds) {
        this.rechargeSeconds = rechargeSeconds;
        return this;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    public PlantDefinition setUnlockedByDefault(boolean unlockedByDefault) {
        this.unlockedByDefault = unlockedByDefault;
        return this;
    }

    public String getNote() {
        return note;
    }

    public PlantDefinition setNote(String note) {
        this.note = note;
        return this;
    }

    public UpgradeRule getUpgradeForLevel(int level) {
        if (upgrades == null) {
            return null;
        }

        for (UpgradeRule upgrade : upgrades) {
            if (upgrade != null && upgrade.getLevel() == level) {
                return upgrade;
            }
        }
        return null;
    }

    /**
     * Highest level this plant can actually reach, straight from the JSON's own
     * upgrade list (base level 1 + however many upgrade tiers it defines - almost
     * always 3, so max level 4). Plants with no upgrades at all cap at level 1.
     */
    public int getMaxLevel() {
        if (upgrades == null || upgrades.isEmpty()) {
            return 1;
        }
        int max = 1;
        for (UpgradeRule upgrade : upgrades) {
            if (upgrade != null && upgrade.getLevel() > max) {
                max = upgrade.getLevel();
            }
        }
        return max;
    }

    public static final class Builder {
        private final PlantDefinition definition = new PlantDefinition();

        private Builder() {
        }

        public Builder legacyId(int legacyId) {
            definition.setLegacyId(legacyId);
            return this;
        }

        public Builder plantKey(String plantKey) {
            definition.setPlantKey(plantKey);
            return this;
        }

        public Builder name(String name) {
            definition.setName(name);
            return this;
        }

        public Builder category(String category) {
            definition.setCategory(category);
            return this;
        }

        public Builder category(PlantCategory category) {
            definition.setCategory(category == null ? null : category.name());
            return this;
        }

        public Builder tag(String tag) {
            definition.tags.add(tag);
            return this;
        }

        public Builder tags(String... tags) {
            if (tags != null) {
                for (String tag : tags) {
                    definition.tags.add(tag);
                }
            }
            return this;
        }

        public Builder cost(int cost) {
            definition.setCost(cost);
            return this;
        }

        public Builder baseHp(int baseHp) {
            definition.setBaseHp(baseHp);
            return this;
        }

        public Builder damage(int damage) {
            definition.setDamage(damage);
            return this;
        }

        public Builder damageSpec(DamageSpec damageSpec) {
            definition.setDamageSpec(damageSpec);
            return this;
        }

        public Builder baseAbility(AbilitySpec baseAbility) {
            definition.setBaseAbility(baseAbility);
            return this;
        }

        public Builder plantFoodEffect(AbilitySpec plantFoodEffect) {
            definition.setPlantFoodEffect(plantFoodEffect);
            return this;
        }

        public Builder addUpgrade(UpgradeRule upgradeRule) {
            if (upgradeRule != null) {
                definition.upgrades.add(upgradeRule);
            }
            return this;
        }

        public Builder upgrades(List<UpgradeRule> upgrades) {
            definition.setUpgrades(upgrades);
            return this;
        }

        public Builder actionIntervalSeconds(double actionIntervalSeconds) {
            definition.setActionIntervalSeconds(actionIntervalSeconds);
            return this;
        }

        public Builder rechargeSeconds(double rechargeSeconds) {
            definition.setRechargeSeconds(rechargeSeconds);
            return this;
        }

        public Builder unlockedByDefault(boolean unlockedByDefault) {
            definition.setUnlockedByDefault(unlockedByDefault);
            return this;
        }

        public Builder note(String note) {
            definition.setNote(note);
            return this;
        }

        public PlantDefinition build() {
            return definition;
        }
    }

    @Override
    public String toString() {
        return "PlantDefinition{"
            + "legacyId=" + legacyId
            + ", plantKey='" + plantKey + '\''
            + ", name='" + name + '\''
            + ", category='" + category + '\''
            + '}';
    }
}
