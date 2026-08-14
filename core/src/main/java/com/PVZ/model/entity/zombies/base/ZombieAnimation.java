package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.PamAnimationCatalog;

public final class ZombieAnimation {
    public static final String STATE_KEY = "animOverrideState";
    public static final String REMAINING_KEY = "animOverrideRemaining";

    private ZombieAnimation() {
    }

    public static void trigger(Zombie zombie, String canonicalState, double fallbackSeconds) {
        if (zombie == null || canonicalState == null) {
            return;
        }
        zombie.putRuntimeState(STATE_KEY, canonicalState);
        zombie.putRuntimeState(REMAINING_KEY, resolveDuration(zombie, canonicalState, fallbackSeconds));
    }

    public static double resolveDuration(Zombie zombie, String canonicalState, double fallbackSeconds) {
        if (zombie == null) {
            return fallbackSeconds;
        }
        String alias = ZombieTexturePaths.getEffectivePamAlias(zombie);
        String pamPath = ZombieTexturePaths.getPamPath(alias);
        if (pamPath == null) {
            return fallbackSeconds;
        }
        String clip = PamAnimationCatalog.resolveClip(pamPath, canonicalState);
        if (clip == null) {
            return fallbackSeconds;
        }
        Double duration = PamAnimationCatalog.clipDuration(pamPath, clip);
        return (duration != null && duration > 0) ? duration : fallbackSeconds;
    }

    public static boolean isActive(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        Object v = zombie.getRuntimeState(REMAINING_KEY);
        return v instanceof Number && ((Number) v).doubleValue() > 0;
    }

    public static String getState(Zombie zombie) {
        if (zombie == null) {
            return null;
        }
        Object v = zombie.getRuntimeState(STATE_KEY);
        return v instanceof String ? (String) v : null;
    }

    public static void tick(Zombie zombie, double deltaTimeSeconds) {
        if (zombie == null) {
            return;
        }
        Object v = zombie.getRuntimeState(REMAINING_KEY);
        double remaining = v instanceof Number ? ((Number) v).doubleValue() : 0.0;
        if (remaining <= 0) {
            return;
        }
        remaining -= deltaTimeSeconds;
        zombie.putRuntimeState(REMAINING_KEY, Math.max(0.0, remaining));
    }
}
