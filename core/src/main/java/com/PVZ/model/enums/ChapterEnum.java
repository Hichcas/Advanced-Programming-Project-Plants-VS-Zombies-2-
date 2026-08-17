package com.PVZ.model.enums;

public enum ChapterEnum {
    ANCIENT_EGYPT("Ancient Egypt", "music/2-02. Ancient Egypt (First Wave).mp3"),
    FROSTBITE_CAVES("Frostbite Caves", "music/9-02. Frostbite Caves (First Wave).mp3"),
    BIG_WAVE_BEACH("Big Wave Beach", "music/8-02. Big Wave Beach (First Wave).mp3"),
    DARK_AGES("Dark Ages", "music/7-02. Dark Ages (First Wave).mp3");

    private final String displayName;
    private final String musicPath;

    ChapterEnum(String displayName, String musicPath) {
        this.displayName = displayName;
        this.musicPath = musicPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMusicPath() {
        return musicPath;
    }
}
