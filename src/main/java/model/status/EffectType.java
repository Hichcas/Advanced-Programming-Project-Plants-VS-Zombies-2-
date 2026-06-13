package model.status;

import java.util.Locale;

public enum EffectType {
    FREEZE,
    CHILL,
    POISON,
    BURN,
    STUN,
    SLOW,
    SPLASH,
    HYPNOTIZE,
    CONFUSE,
    UNKNOWN;

    public static EffectType fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        try {
            return EffectType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}