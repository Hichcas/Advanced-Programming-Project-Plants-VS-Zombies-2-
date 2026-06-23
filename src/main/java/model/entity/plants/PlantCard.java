package model.entity.plants;


public class PlantCard {

    private final PlantDefinition definition;
    private final int cost;
    private int currentCooldownTicks;
    private int maxCooldownTicks;


    public PlantCard(PlantDefinition definition, int userLevel) {
        this.definition = definition;


        this.cost = definition.getCost();

        this.currentCooldownTicks = 0;

        double rechargeSeconds = definition.getRechargeSeconds() != null ? definition.getRechargeSeconds() : 0.0;
        this.maxCooldownTicks = (int) (rechargeSeconds * 20);
    }

    public void tick() {
        if (this.currentCooldownTicks > 0) {
            this.currentCooldownTicks--;
        }
    }

    public boolean canPlayerBuy(int currentSunAmount) {
        return isReady() && currentSunAmount >= this.cost;
    }


    public boolean isReady() {
        return this.currentCooldownTicks == 0;
    }


    public void startCooldown() {
        this.currentCooldownTicks = this.maxCooldownTicks;
    }

    public double getCooldownRatio() {
        if (maxCooldownTicks == 0) return 0.0;
        return (double) currentCooldownTicks / maxCooldownTicks;
    }

    public PlantDefinition getDefinition() { return definition; }
    public String getPlantName() { return definition.getName(); }
    public int getCost() { return cost; }
    public int getCurrentCooldownTicks() { return currentCooldownTicks; }
}