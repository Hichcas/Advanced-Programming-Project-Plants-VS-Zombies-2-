package com.PVZ.model.game;

public class GameStatus {
    private int sunflower;
    private int remainingZombieWaveInPercent;

    public int getRemainingZombieWaveInPercent() {
        return remainingZombieWaveInPercent;
    }

    public int getSunflower() {
        return sunflower;
    }

    public void setRemainingZombieWaveInPercent(int remainingZombieWaveInPercent) {
        this.remainingZombieWaveInPercent = remainingZombieWaveInPercent;
    }

    public void setSunflower(int sunflower) {
        this.sunflower = sunflower;
    }
}
