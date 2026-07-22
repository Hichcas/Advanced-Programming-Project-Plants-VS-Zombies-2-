package com.PVZ.model.minigame.beghouled;

import com.PVZ.model.enums.PlantType;

public class BeghouledUpgrade {

    private PlantType from;
    private PlantType to;
    private int cost;

    public BeghouledUpgrade() {
    }

    public BeghouledUpgrade(PlantType from, PlantType to, int cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    public PlantType getFrom() { return from; }
    public void setFrom(PlantType from) { this.from = from; }

    public PlantType getTo() { return to; }
    public void setTo(PlantType to) { this.to = to; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public String describe() {
        String fromName = from != null ? from.getDisplayName() : "?";
        String toName = to != null ? to.getDisplayName() : "?";
        return fromName + " -> " + toName + " (" + cost + " sun)";
    }
}
