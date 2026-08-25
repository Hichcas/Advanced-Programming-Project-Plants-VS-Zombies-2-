package com.PVZ.model.game.reaction;

/**
 * فهرست ثابت واکنش‌های قابل ارسال در حین بازی دونفره‌ی «من، زامبی» (فاز شبکه) -
 * دقیقا مثل سیستم ری‌اکشن کلش‌آف‌کلنز بین دو بازیکن.
 *
 * سه دسته داریم:
 * <ul>
 *   <li>{@link #TEXT_MESSAGES} - سه پیام متنی از پیش تعریف‌شده</li>
 *   <li>{@link #EMOJIS} - سه ایموجی؛ چون اسکین بازی گلیف ایموجی یونیکد ندارد،
 *       هرکدام را با آیکون یک گیاه واقعی و شناخته‌شده از بازی نشان می‌دهیم</li>
 *   <li>{@link #STICKERS} - استیکرهای متحرک؛ هرکدام یک انیمیشن PAM واقعی و
 *       موجود در pam_animations.json که روی زمین بازی پخش می‌شود</li>
 * </ul>
 *
 * هر واکنش یک {@code id} پایدار دارد که همان چیزی است که روی شبکه
 * (SEND_REACTION / REACTION_RECEIVED) رد و بدل می‌شود؛ کلاینت گیرنده از
 * روی همین id، نوع و ظاهر واکنش را دوباره پیدا می‌کند.
 */
public final class ReactionCatalog {

    private ReactionCatalog() {
    }

    public enum Kind { TEXT, EMOJI, STICKER }

    public record Reaction(String id, Kind kind, String label,
                            String plantIconType, String pamPath, String pamClip) {
    }

    public static final Reaction[] TEXT_MESSAGES = {
        new Reaction("text_nice_move", Kind.TEXT, "Nice move!", null, null, null),
        new Reaction("text_good_game", Kind.TEXT, "Good game!", null, null, null),
        new Reaction("text_watch_out", Kind.TEXT, "Watch out!", null, null, null),
    };

    public static final Reaction[] EMOJIS = {
        new Reaction("emoji_happy", Kind.EMOJI, ":)", "SUNFLOWER", null, null),
        new Reaction("emoji_taunt", Kind.EMOJI, ">:)", "POTATO_MINE", null, null),
        new Reaction("emoji_oops", Kind.EMOJI, ":S", "WALL_NUT", null, null),
    };

    /** استیکرهای متحرک - هرکدام یک انیمیشن PAM واقعی که روی زمین بازی پخش می‌شود. */
    public static final Reaction[] STICKERS = {
        new Reaction("sticker_confetti", Kind.STICKER, "Party!", null,
            "768/FULL/UI/LEVELOFTHEDAY/CONFETTI/CONFETTI.PAM", "medium"),
        new Reaction("sticker_puffball", Kind.STICKER, "Pop!", null,
            "768/INITIAL/EFFECTS/PUFFBALL_EXPLOSION_CONFETTI/PUFFBALL_EXPLOSION_CONFETTI.PAM", "animation"),
        new Reaction("sticker_coin", Kind.STICKER, "Cha-ching!", null,
            "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM", "animation"),
        new Reaction("sticker_crown", Kind.STICKER, "Champion!", null,
            "768/FULL/UI/JOUST/MATCH_RESULTS/CROWN_COLLECT_ANIM/CROWN_COLLECT_ANIM.PAM", "bump"),
    };

    public static Reaction findById(String id) {
        if (id == null) return null;
        for (Reaction r : TEXT_MESSAGES) if (r.id().equals(id)) return r;
        for (Reaction r : EMOJIS) if (r.id().equals(id)) return r;
        for (Reaction r : STICKERS) if (r.id().equals(id)) return r;
        return null;
    }
}
