package com.PVZ.model.game;

public class GameStatus {
    private boolean gameOver = false;
    private boolean won = false;
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

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}
