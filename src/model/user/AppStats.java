package model.user;

public class AppStats {
    private int difficultyLevel = 3;

    private int soundVolume = 15;

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel < 1) {
            this.difficultyLevel = 1;
        } else if (difficultyLevel > 5) {
            this.difficultyLevel = 5;
        } else {
            this.difficultyLevel = difficultyLevel;
        }
    }

    public int getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(int soundVolume) {
        if (soundVolume < 0) {
            this.soundVolume = 0;
        } else if (soundVolume > 30) {
            this.soundVolume = 30;
        } else {
            this.soundVolume = soundVolume;
        }
    }
}