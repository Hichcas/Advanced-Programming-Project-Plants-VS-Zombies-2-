package com.PVZ.model.entity.plants.behavior.impl;

import java.util.HashMap;
import java.util.Map;

public final class ProjectileSpawnOffsets {
    public static final float DEFAULT_DX = 0.5f;
    public static final float DEFAULT_DY = 0.35f;

    private static final Map<String, float[]> OFFSETS = new HashMap<>();

    static {
        // PLANT_TYPE_NAME -> {dxFraction, dyFraction}
        // (empty for now — filled in plant-by-plant on request)
    }

    private ProjectileSpawnOffsets() {
    }

    public static float dx(String plantTypeName) {
        float[] o = plantTypeName == null ? null : OFFSETS.get(plantTypeName);
        return o != null ? o[0] : DEFAULT_DX;
    }

    public static float dy(String plantTypeName) {
        float[] o = plantTypeName == null ? null : OFFSETS.get(plantTypeName);
        return o != null ? o[1] : DEFAULT_DY;
    }

    public static void set(String plantTypeName, float dxFraction, float dyFraction) {
        OFFSETS.put(plantTypeName, new float[]{dxFraction, dyFraction});
    }
}
