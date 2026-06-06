package model.entity.plants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDefinition {

    private int id;
    private String name;
    private String category;
    private List<String> tags = new ArrayList<>();
    private int cost;
    private int baseHp;
    private int damage;
    private String baseAbility;
    private String plantFoodEffect;
    private Double actionIntervalSeconds;
    private Double rechargeSeconds;
    private List<Upgrade> upgrades = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Upgrade {
        private int level;
        private String raw;
        private ParsedUpgrade parsed;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getRaw() { return raw; }
        public void setRaw(String raw) { this.raw = raw; }
        public ParsedUpgrade getParsed() { return parsed; }
        public void setParsed(ParsedUpgrade parsed) { this.parsed = parsed; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedUpgrade {
        private String kind;  // نوع آپگرید: "stat" یا "flag"
        private String stat;  // اگر stat باشد: مثلا "hp", "range", "aoeDamage"
        private String flag;  // اگر flag باشد: مثلا "doubleSunChance"
        private String op;    // عملیات ریاضی: "add" یا "set"
        private double value; // مقدار عددی تغییر
        private String unit;  // واحد سنجش (مثلا "s" برای ثانیه)

        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getStat() { return stat; }
        public void setStat(String stat) { this.stat = stat; }
        public String getFlag() { return flag; }
        public void setFlag(String flag) { this.flag = flag; }
        public String getOp() { return op; }
        public void setOp(String op) { this.op = op; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public String getBaseAbility() { return baseAbility; }
    public void setBaseAbility(String baseAbility) { this.baseAbility = baseAbility; }

    public String getPlantFoodEffect() { return plantFoodEffect; }
    public void setPlantFoodEffect(String plantFoodEffect) { this.plantFoodEffect = plantFoodEffect; }

    public Double getActionIntervalSeconds() { return actionIntervalSeconds; }
    public void setActionIntervalSeconds(Double actionIntervalSeconds) { this.actionIntervalSeconds = actionIntervalSeconds; }

    public Double getRechargeSeconds() { return rechargeSeconds; }
    public void setRechargeSeconds(Double rechargeSeconds) { this.rechargeSeconds = rechargeSeconds; }

    public List<Upgrade> getUpgrades() { return upgrades; }
    public void setUpgrades(List<Upgrade> upgrades) { this.upgrades = upgrades; }
}