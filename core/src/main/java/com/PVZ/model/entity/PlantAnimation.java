package com.PVZ.model.entity;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.enums.PlantType;

public final class PlantAnimation {
    public static final String STATE_KEY = "animOverrideState";
    public static final String REMAINING_KEY = "animOverrideRemaining";

    private PlantAnimation() {
    }

    public static void trigger(PlantInstance instance, String canonicalState, double fallbackSeconds) {
        if (instance == null || canonicalState == null) {
            return;
        }
        instance.putRuntimeState(STATE_KEY, canonicalState);
        instance.putRuntimeState(REMAINING_KEY, resolveDuration(instance, canonicalState, fallbackSeconds));
    }

    public static double resolveDuration(PlantInstance instance, String canonicalState, double fallbackSeconds) {
        PlantType type = instance == null ? null : instance.getType();
        if (type == null) {
            return fallbackSeconds;
        }
        String pamPath = PlantTexturePaths.getPamPath(type.name());
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

    public static boolean isActive(PlantInstance instance) {
        if (instance == null) {
            return false;
        }
        Object v = instance.getRuntimeState(REMAINING_KEY);
        return v instanceof Number && ((Number) v).doubleValue() > 0;
    }

    public static String getState(PlantInstance instance) {
        if (instance == null) {
            return null;
        }
        Object v = instance.getRuntimeState(STATE_KEY);
        return v instanceof String ? (String) v : null;
    }

    public static void tick(PlantInstance instance, double deltaTimeSeconds) {
        if (instance == null) {
            return;
        }
        Object v = instance.getRuntimeState(REMAINING_KEY);
        double remaining = v instanceof Number ? ((Number) v).doubleValue() : 0.0;
        if (remaining <= 0) {
            return;
        }
        remaining -= deltaTimeSeconds;
        instance.putRuntimeState(REMAINING_KEY, Math.max(0.0, remaining));
    }
}
