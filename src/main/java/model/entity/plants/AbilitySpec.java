package model.entity.plants;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbilitySpec {
    @JsonAlias({"id", "abilityId", "key"})
    private String id;

    @JsonAlias({"kind", "abilityKind", "abilityType", "type"})
    private String kind;

    @JsonAlias({"raw", "text", "description"})
    private String raw;

    @JsonAlias({"behaviorId", "behaviorKey"})
    private String behaviorId;

    private String note;

    @JsonAlias({"params", "parameters"})
    private Map<String, Object> params = new LinkedHashMap<>();

    public AbilitySpec() {
    }

    public String getId() {
        return id;
    }

    public AbilitySpec setId(String id) {
        this.id = id;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public AbilitySpec setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getNormalizedKind() {
        if (kind == null) {
            return null;
        }
        return kind.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    public String getRaw() {
        return raw;
    }

    public AbilitySpec setRaw(String raw) {
        this.raw = raw;
        return this;
    }

    public String getBehaviorId() {
        return behaviorId;
    }

    public AbilitySpec setBehaviorId(String behaviorId) {
        this.behaviorId = behaviorId;
        return this;
    }

    public String getResolvedBehaviorId() {
        if (behaviorId != null && !behaviorId.isBlank()) {
            return behaviorId;
        }
        if (id != null && !id.isBlank()) {
            return id;
        }
        if (kind != null && !kind.isBlank()) {
            return kind;
        }
        return null;
    }

    public String getNote() {
        return note;
    }

    public AbilitySpec setNote(String note) {
        this.note = note;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public AbilitySpec setParams(Map<String, Object> params) {
        this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
        return this;
    }

    public AbilitySpec putParam(String key, Object value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    public String getStringParam(String key, String defaultValue) {
        Object value = params.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    public int getIntParam(String key, int defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public double getDoubleParam(String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBooleanParam(String key, boolean defaultValue) {
        Object value = params.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
    }
}
