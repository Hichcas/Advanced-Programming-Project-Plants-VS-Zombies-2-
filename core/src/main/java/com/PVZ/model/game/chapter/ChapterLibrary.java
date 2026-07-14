package com.PVZ.model.game.chapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChapterLibrary {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Map<String, ChapterConfig> CHAPTERS = new HashMap<>();

    private ChapterLibrary() {
    }

    public static void load() {
        try {
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("data/chapters.json");
            if (inputStream == null) {
                throw new RuntimeException("chapters.json not found on classpath");
            }
            try (InputStream in = inputStream) {
                Root root = MAPPER.readValue(in, Root.class);
                if (root.chapters != null) {
                    for (ChapterConfig config : root.chapters) {
                        CHAPTERS.put(config.getName(), config);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load chapters.json", e);
        }
    }

    public static ChapterConfig getChapterConfig(String name) {
        return CHAPTERS.get(name);
    }

    public static Chapter getChapter(String name) {
        ChapterConfig config = CHAPTERS.get(name);
        return config != null ? new Chapter(config) : null;
    }

    public static StageConfig getStageConfig(String chapterName, int stageNumber) {
        ChapterConfig config = CHAPTERS.get(chapterName);
        if (config == null || config.getStages() == null) {
            return null;
        }
        return config.getStages().stream()
                .filter(s -> s.getStageNumber() == stageNumber)
                .findFirst()
                .orElse(null);
    }

    public static List<String> getChapterNames() {
        return List.copyOf(CHAPTERS.keySet());
    }

    private static class Root {
        public List<ChapterConfig> chapters;
    }
}
