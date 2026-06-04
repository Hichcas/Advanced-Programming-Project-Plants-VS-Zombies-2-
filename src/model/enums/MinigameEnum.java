package model.enums;

public enum MinigameEnum {
    VASEBREAKER("Vasebreaker"),
    WALLNUT_BOWLING("Wallnut Bowling"),
    I_ZOMBIE("I, Zombie"),
    BEGHOULED("Beghouled"),
    ZOMBOTANY("Zombotany");   // بخش امتیازی

    private final String displayName;

    MinigameEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}