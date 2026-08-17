package com.PVZ.model.entity.plants.behavior.impl;

import java.util.HashMap;
import java.util.Map;

public final class ProjectileSpawnOffsets {
    public static final float DEFAULT_DX = 0.5f;
    public static final float DEFAULT_DY = 0.35f;

    private static final Map<String, float[]> OFFSETS = new HashMap<>();

    static {
        // PLANT_TYPE_NAME -> {dxFraction, dyFraction}
        // Pea-family muzzle points. Fractions are relative to the tile size.
        // The slightly higher Y for Snow Pea keeps the shot visibly aligned with
        // the mouth/head rather than the torso.
        OFFSETS.put("PEASHOOTER", new float[]{0.63f, 0.47f});
        OFFSETS.put("REPEATER", new float[]{0.63f, 0.47f});
        OFFSETS.put("THREEPEATER", new float[]{0.63f, 0.47f});
        OFFSETS.put("SNOW_PEA", new float[]{0.63f, 0.54f});
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
