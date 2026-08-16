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
        if (canonicalState.equals(zombie.getRuntimeState(STATE_KEY)) && isActive(zombie)) {
            return;
        }
        if (isActive(zombie)) {
            String currentState = (String) zombie.getRuntimeState(STATE_KEY);
            if (currentState != null && !currentState.equals("walk") && !currentState.equals("idle")) {
                if (canonicalState.equals("walk") || canonicalState.equals("idle")) {
                    return;
                }
            }
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
            return "walk";
        }
        if (isActive(zombie)) {
            Object v = zombie.getRuntimeState(STATE_KEY);
            if (v instanceof String) {
                return (String) v;
            }
        }
        if (!zombie.isMoving()) {
            return "idle";
        }
        return "walk";
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
