package model.status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TimedEffect implements Effect {
    private final EffectType type;
    private final double durationSeconds;
    private double remainingSeconds;
    private final Map<String, Object> params = new LinkedHashMap<>();

    protected TimedEffect(EffectType type, double durationSeconds) {
        this.type = type == null ? EffectType.UNKNOWN : type;
        this.durationSeconds = Math.max(0.0, durationSeconds);
        this.remainingSeconds = this.durationSeconds;
    }

    protected TimedEffect(TimedEffect other) {
        this.type = other.type;
        this.durationSeconds = other.durationSeconds;
        this.remainingSeconds = other.remainingSeconds;
        this.params.putAll(other.params);
    }

    @Override
    public EffectType getType() {
        return type;
    }

    @Override
    public double getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public double getRemainingSeconds() {
        return remainingSeconds;
    }

    @Override
    public void tick(double deltaTimeSeconds) {
        if (deltaTimeSeconds <= 0.0 || isExpired()) {
            return;
        }

        remainingSeconds = Math.max(0.0, remainingSeconds - deltaTimeSeconds);
    }

    @Override
    public boolean isExpired() {
        return remainingSeconds <= 0.0;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public void putParam(String key, Object value) {
        if (key != null) {
            params.put(key, value);
        }
    }

    public Object getParam(String key) {
        return params.get(key);
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

    protected void setRemainingSeconds(double remainingSeconds) {
        this.remainingSeconds = Math.max(0.0, remainingSeconds);
    }

    protected void resetTimer() {
        this.remainingSeconds = this.durationSeconds;
    }
}