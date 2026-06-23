package model.entity.plants;

import java.util.Locale;


public enum PlantCategory {
    SUN_PRODUCER,     // تولیدکنندگان آفتاب مثل Sunflower
    SHOOTER,          // شلیک‌کننده‌های مستقیم مثل Peashooter
    LOBBER,           // پرتاب‌کننده‌های خمپاره‌ای مثل Cabbage-pult
    EXPLOSIVE,        // بمب‌ها مثل Cherry Bomb
    MELEE,            // مبارزان نزدیک‌زن مثل Bonk Choy
    WALL,             // مدافعان پرجان مثل Wall-Nut
    MODIFIER,         // تغییردهنده‌های وضعیت محیط یا زامبی‌ها
    THROUGH_STRIKE,   // شلیک‌های نافذ و ردشونده مثل Laser Bean
    HOMING,           // تیرهای تعقیب‌کننده مثل Homing Thistle
    UNKNOWN;          // برای مواقعی که دیتای ناشناخته وارد شود


    public static PlantCategory fromRaw(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }

        String value = raw.trim().toLowerCase(Locale.ROOT);

        return switch (value) {
            case "sun producer" -> SUN_PRODUCER;
            case "shooter" -> SHOOTER;
            case "lobber" -> LOBBER;
            case "explosive" -> EXPLOSIVE;
            case "melee" -> MELEE;
            case  "wall-nut" -> WALL;
            case "modifier" -> MODIFIER;
            case "strike-through" -> THROUGH_STRIKE;
            case "homing" -> HOMING;
            default -> UNKNOWN;
        };
    }
}