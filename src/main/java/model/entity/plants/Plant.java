package model.entity.plants;

import model.entity.plants.PlantDefinition.Upgrade;
import model.entity.plants.PlantDefinition.ParsedUpgrade;
import model.entity.plants.behaviors.*;

public class Plant {

    private final PlantDefinition definition;
    private PlantBehavior behavior;
    private int level;
    private int maxHp;
    private int currentHp;
    private int damage;
    private double actionIntervalSeconds;
    private double rechargeSeconds;
    private int range;
    private int aoeDamage;
    private boolean doubleSunChance;
    private int x;
    private int y;
    private int ticksSinceLastAction;

    public Plant(PlantDefinition definition, int level) {
        this.definition = definition;
        this.level = level;
        this.ticksSinceLastAction = 0;
        this.calculateStats();
    }

    public void calculateStats() {

        this.maxHp = definition.getBaseHp();
        this.currentHp = this.maxHp;
        this.damage = definition.getDamage();
        this.actionIntervalSeconds = definition.getActionIntervalSeconds() != null ? definition.getActionIntervalSeconds() : 0.0;
        this.rechargeSeconds = definition.getRechargeSeconds() != null ? definition.getRechargeSeconds() : 0.0;

        this.range = 0;
        this.aoeDamage = 0;
        this.doubleSunChance = false;

        for (Upgrade upgrade : definition.getUpgrades()) {
            if (upgrade.getLevel() <= this.level) {
                applyUpgrade(upgrade.getParsed());
            }
        }

        assignBehavior();
    }


    private void assignBehavior() {
        String category = definition.getCategory() != null ? definition.getCategory().toLowerCase() : "";

        if (category.contains("sun_producer") || category.contains("sun")) {
            this.behavior = new SunProducerBehavior();
        } else if (category.contains("shooter") || category.contains("attacker")) {
            this.behavior = new ShooterBehavior();
        } else if (definition.getTags().contains("instant_kill") || definition.getTags().contains("bomb")) {
            this.behavior = new BombBehavior();
        } else {
            this.behavior = (p, map) -> {};
        }
    }
    public void update(Object gameMap) {
        if (this.behavior != null && isAlive()) {
            this.behavior.tick(this, gameMap);
        }
    }

    private void applyUpgrade(ParsedUpgrade parsed) {
        if (parsed == null) return;

        if ("stat".equalsIgnoreCase(parsed.getKind()) && parsed.getStat() != null) {
            String statName = parsed.getStat().toLowerCase();
            double value = parsed.getValue();
            String operation = parsed.getOp();

            switch (statName) {
                case "hp":
                    this.maxHp = calculateNewValue(this.maxHp, value, operation);
                    this.currentHp = this.maxHp;
                    break;
                case "damage":
                    this.damage = calculateNewValue(this.damage, value, operation);
                    break;
                case "actioninterval":
                case "productiontime":
                    this.actionIntervalSeconds = calculateNewValue(this.actionIntervalSeconds, value, operation);
                    break;
                case "rechargeseconds":
                case "cooldown":
                    this.rechargeSeconds = calculateNewValue(this.rechargeSeconds, value, operation);
                    break;
                case "range":
                    this.range = calculateNewValue(this.range, value, operation);
                    break;
                case "aoedamage":
                    this.aoeDamage = calculateNewValue(this.aoeDamage, value, operation);
                    break;
            }
        } else if ("flag".equalsIgnoreCase(parsed.getKind()) && parsed.getFlag() != null) {
            if ("doublesunchance".equalsIgnoreCase(parsed.getFlag())) {
                this.doubleSunChance = true;
            }
        }
    }

    private int calculateNewValue(int currentValue, double upgradeValue, String op) {
        return "set".equalsIgnoreCase(op) ? (int) upgradeValue : currentValue + (int) upgradeValue;
    }

    private double calculateNewValue(double currentValue, double upgradeValue, String op) {
        return "set".equalsIgnoreCase(op) ? upgradeValue : currentValue + upgradeValue;
    }

    public void takeDamage(int amount) {
        this.currentHp = Math.max(0, this.currentHp - amount);
    }

    public boolean isAlive() { return this.currentHp > 0; }

    public PlantDefinition getDefinition() { return definition; }
    public String getName() { return definition.getName(); }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; calculateStats(); }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getDamage() { return damage; }
    public double getActionIntervalSeconds() { return actionIntervalSeconds; }
    public double getRechargeSeconds() { return rechargeSeconds; }
    public int getRange() { return range; }
    public int getAoeDamage() { return aoeDamage; }
    public boolean isDoubleSunChance() { return doubleSunChance; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public int getTicksSinceLastAction() { return ticksSinceLastAction; }
    public void incrementTicks() { this.ticksSinceLastAction++; }
    public void resetActionTicks() { this.ticksSinceLastAction = 0; }
}