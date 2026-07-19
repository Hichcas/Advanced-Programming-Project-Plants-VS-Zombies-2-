package com.PVZ.model.minigame.izombie;

public class ZombieOption {

    private String alias;
    private int cost;
    private String displayName;

    public ZombieOption() {
    }

    public ZombieOption(String alias, int cost, String displayName) {
        this.alias = alias;
        this.cost = cost;
        this.displayName = displayName;
    }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
