package com.PVZ.database;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.PVZ.model.user.User;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {

    private static final Path DATA_DIR = findProjectRoot().resolve("core/src/main/resources/data").normalize();

    private static Path findProjectRoot() {
        Path dir = Paths.get(System.getProperty("user.dir"));
        while (dir != null) {
            if (Files.exists(dir.resolve("settings.gradle"))) return dir;
            dir = dir.getParent();
        }
        throw new RuntimeException("Cannot find project root (settings.gradle)");
    }
    private static final Path USERS_DIR = DATA_DIR.resolve("users");
    private static final Path INDEX_FILE = DATA_DIR.resolve("users_index.json");

    private static final ObjectMapper mapper = new ObjectMapper()
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static void init() throws Exception {
        Files.createDirectories(USERS_DIR);
        if (!Files.exists(INDEX_FILE)) {
            Files.writeString(INDEX_FILE, "[]");
        }
    }

    public static List<String> loadIndex() throws Exception {
        if (!Files.exists(INDEX_FILE)) return new ArrayList<>();
        String json = Files.readString(INDEX_FILE);
        return mapper.readValue(json, new TypeReference<List<String>>() {});
    }

    public static void addToIndex(String username) throws Exception {
        List<String> list = loadIndex();
        if (!list.contains(username)) {
            list.add(username);
            Files.writeString(INDEX_FILE, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        }
    }

    public static void save(String username, User user) throws Exception {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        Files.writeString(USERS_DIR.resolve(username + ".dat"), json);
    }

    public static User load(String username) throws Exception {
        Path file = USERS_DIR.resolve(username + ".dat");
        if (!Files.exists(file)) return null;
        String json = Files.readString(file);
        return mapper.readValue(json, User.class);
    }

    public static boolean exists(String username) throws Exception {
        return loadIndex().contains(username);
    }

    public static void delete(String username) throws Exception {
        Files.deleteIfExists(USERS_DIR.resolve(username + ".dat"));
        List<String> list = loadIndex();
        list.remove(username);
        Files.writeString(INDEX_FILE, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
    }
}
