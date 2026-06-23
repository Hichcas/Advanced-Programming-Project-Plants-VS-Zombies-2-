package model.status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class EffectInstance {
    private final Effect effect;
    private final String sourceId;
    private final String targetId;
    private final long createdTick;
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    public EffectInstance(Effect effect, String sourceId, String targetId, long createdTick) {
        this.effect = Objects.requireNonNull(effect, "effect");
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.createdTick = createdTick;
    }

    public Effect getEffect() {
        return effect;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public long getCreatedTick() {
        return createdTick;
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public void putMetadata(String key, Object value) {
        if (key != null) {
            metadata.put(key, value);
        }
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public void tick(double deltaTimeSeconds) {
        effect.tick(deltaTimeSeconds);
    }

    public boolean isExpired() {
        return effect.isExpired();
    }

    public EffectInstance copy() {
        EffectInstance copy = new EffectInstance(effect.copy(), sourceId, targetId, createdTick);
        copy.metadata.putAll(metadata);
        return copy;
    }

    @Override
    public String toString() {
        return "EffectInstance{"
                + "type=" + effect.getType()
                + ", sourceId='" + sourceId + '\''
                + ", targetId='" + targetId + '\''
                + ", remaining=" + effect.getRemainingSeconds()
                + '}';
    }
}