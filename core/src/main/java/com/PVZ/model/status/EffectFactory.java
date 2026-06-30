package com.PVZ.model.status;

public final class EffectFactory {
    private EffectFactory() {
    }

    public static TimedEffect freeze(double durationSeconds) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.FREEZE, durationSeconds);
        effect.putParam("movementMultiplier", 0.0);
        effect.putParam("attackMultiplier", 0.0);
        return effect;
    }

    public static TimedEffect chill(double durationSeconds, double slowFactor) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.CHILL, durationSeconds);
        effect.putParam("movementMultiplier", clampMultiplier(slowFactor));
        effect.putParam("attackMultiplier", clampMultiplier(slowFactor));
        return effect;
    }

    public static TimedEffect slow(double durationSeconds, double slowFactor) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.SLOW, durationSeconds);
        effect.putParam("movementMultiplier", clampMultiplier(slowFactor));
        effect.putParam("attackMultiplier", clampMultiplier(slowFactor));
        return effect;
    }

    public static TimedEffect poison(double durationSeconds, int damagePerSecond) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.POISON, durationSeconds);
        effect.putParam("damagePerSecond", Math.max(0, damagePerSecond));
        return effect;
    }

    public static TimedEffect burn(double durationSeconds, int damagePerSecond) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.BURN, durationSeconds);
        effect.putParam("damagePerSecond", Math.max(0, damagePerSecond));
        return effect;
    }

    public static TimedEffect stun(double durationSeconds) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.STUN, durationSeconds);
        effect.putParam("movementMultiplier", 0.0);
        effect.putParam("attackMultiplier", 0.0);
        return effect;
    }

    public static TimedEffect hypnotize(double durationSeconds) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.HYPNOTIZE, durationSeconds);
        effect.putParam("invertTargeting", Boolean.TRUE);
        return effect;
    }

    public static TimedEffect confuse(double durationSeconds) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.CONFUSE, durationSeconds);
        effect.putParam("randomMovement", Boolean.TRUE);
        effect.putParam("randomTargeting", Boolean.TRUE);
        return effect;
    }

    public static TimedEffect splash(double durationSeconds, int radius, int damage) {
        ConfiguredTimedEffect effect = new ConfiguredTimedEffect(EffectType.SPLASH, durationSeconds);
        effect.putParam("radius", Math.max(0, radius));
        effect.putParam("damage", Math.max(0, damage));
        return effect;
    }

    public static TimedEffect custom(EffectType type, double durationSeconds) {
        return new ConfiguredTimedEffect(type, durationSeconds);
    }

    private static double clampMultiplier(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private static final class ConfiguredTimedEffect extends TimedEffect {
        private ConfiguredTimedEffect(EffectType type, double durationSeconds) {
            super(type, durationSeconds);
        }

        private ConfiguredTimedEffect(ConfiguredTimedEffect other) {
            super(other);
            getParams().forEach((key, value) -> {
                // no-op: params are copied in the parent through the protected copy constructor
            });
        }

        @Override
        public Effect copy() {
            ConfiguredTimedEffect copy = new ConfiguredTimedEffect(getType(), getDurationSeconds());
            for (String key : getParams().keySet()) {
                copy.putParam(key, getParams().get(key));
            }
            copy.setRemainingSeconds(getRemainingSeconds());
            return copy;
        }
    }
}
