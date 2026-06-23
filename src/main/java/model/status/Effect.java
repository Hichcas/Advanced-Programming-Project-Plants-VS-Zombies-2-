package model.status;

public interface Effect {
    EffectType getType();

    double getDurationSeconds();

    double getRemainingSeconds();

    void tick(double deltaTimeSeconds);

    boolean isExpired();

    Effect copy();
}