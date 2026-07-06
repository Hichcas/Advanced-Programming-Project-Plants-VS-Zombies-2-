package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.enums.DamageType;

public class StatusEffect {
    private DamageType type;
    private float duration;

    public StatusEffect(DamageType type, float duration) {
        this.type = type;
        this.duration = duration;
    }

    public boolean update(float delta) {
        duration -= delta;
        return duration <= 0;
    }

    public DamageType getType() { return type; }
    public float getDuration() { return duration; }
}
