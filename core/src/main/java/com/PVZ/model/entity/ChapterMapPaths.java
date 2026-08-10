package com.PVZ.model.entity;

import com.badlogic.gdx.Gdx;
import com.PVZ.model.enums.ChapterEnum;

import java.util.HashMap;
import java.util.Map;

public final class ChapterMapPaths {
    private static final String FALLBACK = "maps/Frontyard.jpg";

    private static final Map<ChapterEnum, String> PATHS = new HashMap<>();
    static {
        PATHS.put(ChapterEnum.ANCIENT_EGYPT, "maps/Egypt.jpg");
        PATHS.put(ChapterEnum.FROSTBITE_CAVES, "maps/FrostbiteCaves.jpg");
        PATHS.put(ChapterEnum.BIG_WAVE_BEACH, "maps/BigWaveBeach.jpg");
        PATHS.put(ChapterEnum.DARK_AGES, "maps/DarkAges.jpg");
    }

    private ChapterMapPaths() {
    }

    public static String resolve(String chapterName) {
        if (chapterName != null && !chapterName.isBlank()) {
            try {
                ChapterEnum chapter = ChapterEnum.valueOf(chapterName.trim().toUpperCase().replace(" ", "_"));
                String candidate = PATHS.get(chapter);
                if (candidate != null && Gdx.files.internal(candidate).exists()) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }
        return FALLBACK;
    }
}
