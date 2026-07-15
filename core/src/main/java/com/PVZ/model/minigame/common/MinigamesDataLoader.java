package com.PVZ.model.minigame.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public final class MinigamesDataLoader {

    private static final String RESOURCE = "data/minigames/minigames.json";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static JsonNode root;

    private MinigamesDataLoader() {
    }

    public static synchronized JsonNode section(String key) {
        if (root == null) {
            root = load();
        }
        JsonNode node = root.get(key);
        return node == null ? MAPPER.createObjectNode() : node;
    }

    private static JsonNode load() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IOException("Minigames data resource not found: " + RESOURCE);
            }
            return MAPPER.readTree(in);
        } catch (IOException e) {
            System.err.println("[MinigamesDataLoader] " + e.getMessage());
            return MAPPER.createObjectNode();
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
