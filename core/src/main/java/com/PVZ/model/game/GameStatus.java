package com.PVZ.model.game;

public class GameStatus {
    private int sunflower;
    private int remainingZomieWaveInPercent;

    public int getRemainingZomieWaveInPercent() {
        return remainingZomieWaveInPercent;
    }

    public int getSunflower() {
        return sunflower;
    }

    public void setRemainingZomieWaveInPercent(int remainingZomieWaveInPercent) {
        this.remainingZomieWaveInPercent = remainingZomieWaveInPercent;
    }

    public void setSunflower(int sunflower) {
        this.sunflower = sunflower;
    }
}
