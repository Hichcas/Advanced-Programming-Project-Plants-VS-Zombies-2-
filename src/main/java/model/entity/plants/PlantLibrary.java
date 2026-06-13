package model.entity.plants;

import model.enums.PlantType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PlantLibrary {
    private static final Map<PlantType, PlantDefinition> BY_TYPE = new EnumMap<>(PlantType.class);
    private static final Map<Integer, PlantDefinition> BY_ID = new HashMap<>();
    private static final Map<String, PlantDefinition> BY_NAME = new HashMap<>();

    private PlantLibrary() {
    }

    public static synchronized void register(PlantDefinition definition) {
        if (definition == null) {
            return;
        }

        PlantType type = definition.getType();
        BY_TYPE.put(type, definition);
        BY_ID.put(definition.getId(), definition);
        BY_NAME.put(normalize(definition.getName()), definition);
    }

    public static synchronized void registerAll(Collection<PlantDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        for (PlantDefinition definition : definitions) {
            register(definition);
        }
    }

    public static Optional<PlantDefinition> findByType(PlantType type) {
        return Optional.ofNullable(BY_TYPE.get(type));
    }

    public static Optional<PlantDefinition> findById(int id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<PlantDefinition> findByName(String name) {
        return Optional.ofNullable(BY_NAME.get(normalize(name)));
    }

    public static PlantDefinition getByType(PlantType type) {
        return findByType(type).orElseThrow(() ->
                new IllegalArgumentException("Unknown plant type: " + type));
    }

    public static PlantDefinition getByName(String name) {
        return findByName(name).orElseThrow(() ->
                new IllegalArgumentException("Unknown plant name: " + name));
    }

    public static List<PlantDefinition> all() {
        return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
    }

    public static int size() {
        return BY_ID.size();
    }

    public static boolean isRegistered(PlantType type) {
        return BY_TYPE.containsKey(type);
    }

    public static void clear() {
        BY_TYPE.clear();
        BY_ID.clear();
        BY_NAME.clear();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}