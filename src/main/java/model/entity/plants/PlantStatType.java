package model.entity.plants;

import java.util.Locale;


public enum PlantStatType {
    COST,               // هزینه کاشت (Sun Cost)
    HP,                 // مقدار جان یا سلامتی گیاه
    DAMAGE,             // مقدار آسیب یا دمیج
    PRODUCTION_TIME,    // زمان/سرعت تولید (مثلاً برای Sunflower)
    GROWTH_TIME,        // زمان رشد گیاه (برای گیاهان مرحله‌ای)
    CHARGE_TIME,        // زمان شارژ شدن توانایی خاص
    RECHARGE,           // زمان کول‌داون کارت گیاه برای کاشت مجدد (Cooldown)
    ATTACK_SPEED,       // سرعت ضربه زدن یا شلیک (Attack Speed)
    PLANT_FOOD_CHANCE,  // شانس افکت‌های خاص پلنت فود
    FREEZE_TIME,        // مدت زمان یخ زدن یا کند شدن زامبی‌ها
    SUN_AMOUNT,         // مقدار خورشیدی که در هر بار تولید می‌دهد
    DURATION,           // مدت زمان ماندگاری گیاه روی زمین (مثل نعناع‌ها)
    UNKNOWN;            // برای مقادیر ناشناخته یا پیش‌بینی نشده

    public static PlantStatType fromRaw(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }

        String value = raw.trim().toLowerCase(Locale.ROOT).replace(".", "").replace(" ", "");

        return switch (value) {
            case "cost" -> COST;
            case "hp" -> HP;
            case "damage"-> DAMAGE;
            case  "productiontime" -> PRODUCTION_TIME;
            case  "growthtime" -> GROWTH_TIME;
            case "chargetime" -> CHARGE_TIME;
            case  "cooldown" -> RECHARGE;
            case "attackspeed" -> ATTACK_SPEED;
            case "plantfoodchance" -> PLANT_FOOD_CHANCE;
            case "freezetime" -> FREEZE_TIME;
            case "sunamount" -> SUN_AMOUNT;
            case "duration" -> DURATION;
            default -> UNKNOWN;
        };
    }
}