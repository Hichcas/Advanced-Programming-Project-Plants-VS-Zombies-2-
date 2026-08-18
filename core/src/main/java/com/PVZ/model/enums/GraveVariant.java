package com.PVZ.model.enums;

public enum GraveVariant {
    EGYPT("768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM",
          "768/INITIAL/EFFECTS/TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE/TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE.PAM"),
    DARK_NOOP("768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM",
              "768/FULL/EFFECTS/TOMBSTONE_DARK_BASE_DAMAGE/TOMBSTONE_DARK_BASE_DAMAGE.PAM"),
    DARK_SUN("768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM",
             "768/FULL/EFFECTS/TOMBSTONE_DARK_BASE_DAMAGE/TOMBSTONE_DARK_BASE_DAMAGE.PAM"),
    DARK_PLANTFOOD("768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM",
                   "768/FULL/EFFECTS/TOMBSTONE_DARK_BASE_DAMAGE/TOMBSTONE_DARK_BASE_DAMAGE.PAM"),
    TUTORIAL("768/INITIAL/GRAVESTONES/TUTORIAL_GRAVESTONE/TUTORIAL_GRAVESTONE.PAM",
             "768/INITIAL/EFFECTS/TOMBSTONE_TUTORIAL_DAMAGE/TOMBSTONE_TUTORIAL_DAMAGE.PAM");

    private final String pamPath;
    private final String damageFxPamPath;

    GraveVariant(String pamPath, String damageFxPamPath) {
        this.pamPath = pamPath;
        this.damageFxPamPath = damageFxPamPath;
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getDamageFxPamPath() {
        return damageFxPamPath;
    }

    /**
     * Resolves the damage clip based on remaining HP ratio.
     * > 0.80 -> undamaged
     * > 0.60 -> damage1
     * > 0.40 -> damage2
     * > 0.20 -> damage3
     * <= 0.20 -> damage4
     */
    public static String getClipForHpRatio(float ratio) {
        if (ratio > 0.80f) {
            return "undamaged";
        } else if (ratio > 0.60f) {
            return "damage1";
        } else if (ratio > 0.40f) {
            return "damage2";
        } else if (ratio > 0.20f) {
            return "damage3";
        } else {
            return "damage4";
        }
    }
}
