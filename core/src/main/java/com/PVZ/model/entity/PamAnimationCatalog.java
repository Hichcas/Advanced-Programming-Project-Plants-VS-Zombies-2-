package com.PVZ.model.entity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PamAnimationCatalog {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Map<String, String[]> SYNONYMS = new LinkedHashMap<>();
    static {
        SYNONYMS.put("idle", new String[]{"idle", "idle1", "idle_stage3", "loop", "plant_idle"});
        SYNONYMS.put("shooting", new String[]{"attack", "attack1", "attack_loop", "bite", "special", "pf_attack"});
        SYNONYMS.put("producing", new String[]{"special", "transition", "growth"});
        SYNONYMS.put("plantfood", new String[]{"plantfood_on",
            "plantfood", "plantfood_loop", "pf", "pf_loop", "special"});
        SYNONYMS.put("water", new String[]{"water"});
        SYNONYMS.put("damage", new String[]{"damage", "damage2", "damage3", "idle_damage"});
        SYNONYMS.put("planting", new String[]{"plant", "intro"});
        SYNONYMS.put("walk", new String[]{"walk", "walk1",
            "walk2", "walk_stage1", "walk_stage2", "run", "walk_forward", "idle"});
        SYNONYMS.put("eat", new String[]{"eat", "eat1", "chew", "attack", "bite", "eat_norm"});
        SYNONYMS.put("die", new String[]{"death", "die", "fall", "burn", "ash"});
        SYNONYMS.put("zombie_idle", new String[]{"idle", "idle1", "groan", "stand", "idle_norm"});
        SYNONYMS.put("smash", new String[]{"smash_left", "smash_right", "attack"});
        SYNONYMS.put("power", new String[]{"power", "power_up", "break_power"});
        SYNONYMS.put("stomp", new String[]{"stomp", "fire_attack", "charge"});
        SYNONYMS.put("portal", new String[]{"zombie_portal_start", "summoning", "spawn", "zombie_drop"});
        SYNONYMS.put("walk_up", new String[]{"walk_up", "float_up"});
        SYNONYMS.put("walk_down", new String[]{"walk_down", "float_down"});
        SYNONYMS.put("throw", new String[]{"throw", "snowball_throw"});
        SYNONYMS.put("fly", new String[]{"fly_loop", "fly_start", "fly"});
        SYNONYMS.put("push", new String[]{"push", "push_block"});
        SYNONYMS.put("weasel_attack", new String[]{"weasel_attack", "release"});
        SYNONYMS.put("wind", new String[]{"wind_1", "wind_2", "wind_3", "slingshot"});
        SYNONYMS.put("cast", new String[]{"cast", "cast_loop", "throw"});
        SYNONYMS.put("reel", new String[]{"reel", "pull", "idle"});
        SYNONYMS.put("toss", new String[]{"toss", "throw", "attack"});
        SYNONYMS.put("kick", new String[]{"kick", "tackle", "attack", "smash"});
        SYNONYMS.put("run", new String[]{"run", "charge", "walk_fast", "walk"});
        SYNONYMS.put("tackle", new String[]{"tackle", "kick", "attack"});
        SYNONYMS.put("particles", new String[]{"particles", "particle", "idle"});
        SYNONYMS.put("play", new String[]{"play", "play2", "idle"});
        SYNONYMS.put("blastoff", new String[]{"blastoff", "fly", "land", "walk"});
        SYNONYMS.put("newspaper_defeat", new String[]{"newspaper_defeat", "roar", "walk"});
        SYNONYMS.put("walk_newspaper", new String[]{"walk_newspaper", "walk"});
        SYNONYMS.put("eat_newspaper", new String[]{"eat_newspaper", "eat"});
        SYNONYMS.put("spin", new String[]{"spin", "spin_walk", "spinup", "walk"});
        SYNONYMS.put("sheep", new String[]{"sheep", "cast", "attack"});
    }

    private static Map<String, Entry> byPath;
    private static boolean loaded = false;

    private PamAnimationCatalog() {
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        byPath = new LinkedHashMap<>();
        try {
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("data/pam_animations.json");
            if (inputStream == null) {
                System.err.println("PamAnimationCatalog: data/pam_animations.json not found on classpath");
                return;
            }
            try (InputStream in = inputStream) {
                Root root = MAPPER.readValue(in, Root.class);
                if (root.animations != null) {
                    for (Entry e : root.animations) {
                        if (e.path != null) {
                            byPath.put(normalize(e.path), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("PamAnimationCatalog: failed to load pam_animations.json: " + e.getMessage());
        }
    }

    private static String normalize(String path) {
        return path.replace('\\', '/').toUpperCase();
    }

    public static java.util.Set<String> clipNames(String pamPath) {
        ensureLoaded();
        Entry e = byPath.get(normalize(pamPath));
        return (e == null || e.clips == null) ? null : e.clips.keySet();
    }
    public static Double clipDuration(String pamPath, String clipName) {
        ensureLoaded();
        Entry e = byPath.get(normalize(pamPath));
        return (e == null || e.clips == null) ? null : e.clips.get(clipName);
    }

    public static String resolveClip(String pamPath, String canonicalState) {
        ensureLoaded();
        Entry e = byPath.get(normalize(pamPath));
        if (e == null || e.clips == null || e.clips.isEmpty()) {
            return null;
        }
        String[] candidates = SYNONYMS.get(canonicalState);
        if (candidates != null) {
            for (String name : candidates) {
                if (e.clips.containsKey(name)) {
                    return name;
                }
            }
        }
        if (canonicalState != null) {
            String needle = canonicalState.toLowerCase();
            for (String name : e.clips.keySet()) {
                if (name.toLowerCase()
                    .contains(needle)) {
                    return name;
                }
            }
        }
        if (e.clips.
            containsKey("idle")) {
            return "idle";
        }
        return e.clips.keySet().
            iterator().next();
    }

    private static class Root {
        public int count;
        public List<Entry> animations;
    }

    private static class Entry {
        public String name;
        public String path;
        public List<Integer> canvas;
        public Map<String, Double> clips;
    }
}
