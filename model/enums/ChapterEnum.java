package model.enums;

public enum ChapterEnum {
    ANCIENT_EGYPT("Ancient Egypt"),
    FROSTBITE_CAVES("Frostbite Caves"),
    BIG_WAVE_BEACH("Big Wave Beach"),
    DARK_AGES("Dark Ages");

    private final String displayName;

    ChapterEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}