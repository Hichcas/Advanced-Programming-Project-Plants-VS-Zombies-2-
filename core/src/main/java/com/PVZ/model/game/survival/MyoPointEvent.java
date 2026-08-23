package com.PVZ.model.game.survival;

/**
 * یک رویداد «دریافت میوپوینت». وقتی یکی از ۵ الگوی امتیازگیری فعال
 * می‌شود، یک نمونه از این کلاس ساخته و هم به {@code AppStatus.showAnnouncement}
 * (اطلاع‌رسانی فوری متنی) و هم به‌عنوان یک {@code TimedPamEffect} گربه‌ای
 * (انیمیشن گرافیکی خاص) تحویل داده می‌شود.
 *
 * فیلد {@code x}/{@code y} مختصات صفحه‌ای است که پیشنهاد می‌شود
 * انیمیشن/اعلان دقیقا همان‌جا (روی همان کاشی‌ای که زامبی کشته شد)
 * نمایش داده شود.
 */
public final class MyoPointEvent {

    private final MyoPointPattern pattern;
    private final int points;
    private final float x;
    private final float y;

    public MyoPointEvent(MyoPointPattern pattern, int points, float x, float y) {
        this.pattern = pattern;
        this.points = points;
        this.x = x;
        this.y = y;
    }

    public MyoPointPattern getPattern() {
        return pattern;
    }

    public int getPoints() {
        return points;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String describe() {
        return "+" + points + " MyoPoint - " + pattern.getDisplayName();
    }
}
