package com.PVZ.model.game.reaction;

/** Central catalog for all multiplayer reactions. */
public final class ReactionCatalog {
    private ReactionCatalog() {}

    public enum Kind { TEXT, EMOJI, STICKER }

    public record Reaction(
            String id,
            Kind kind,
            String label,
            String plantIconType,
            String pamPath,
            String pamClip,
            float pamLifetimeSeconds) {}

    public static final Reaction[] ALL = {
        new Reaction("text_nice_move", Kind.TEXT, "Nice move!", null, null, null, 0f),
        new Reaction("text_good_game", Kind.TEXT, "Good game!", null, null, null, 0f),
        new Reaction("text_watch_out", Kind.TEXT, "Watch out!", null, null, null, 0f),

        new Reaction("emoji_happy", Kind.EMOJI, "Happy", "SUNFLOWER", null, null, 0f),
        new Reaction("emoji_taunt", Kind.EMOJI, "Boom!", "POTATO_MINE", null, null, 0f),
        new Reaction("emoji_oops", Kind.EMOJI, "Oops", "WALL_NUT", null, null, 0f),
        new Reaction("emoji_pea", Kind.EMOJI, "Pew pew!", "PEASHOOTER", null, null, 0f),
        new Reaction("emoji_cherry", Kind.EMOJI, "Kaboom!", "CHERRY_BOMB", null, null, 0f),
        new Reaction("emoji_sun", Kind.EMOJI, "Big brain", "SUNFLOWER", null, null, 0f),

        new Reaction("sticker_confetti", Kind.STICKER, "Party!", null,
                "768/FULL/UI/LEVELOFTHEDAY/CONFETTI/CONFETTI.PAM", "medium", 3.2f),
        new Reaction("sticker_pop", Kind.STICKER, "POP!", null,
                "768/INITIAL/EFFECTS/PUFFBALL_EXPLOSION_CONFETTI/PUFFBALL_EXPLOSION_CONFETTI.PAM", "animation", 1.0f),
        new Reaction("sticker_coin", Kind.STICKER, "Cha-ching!", null,
                "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM", "animation", 0.7f),
        new Reaction("sticker_crown", Kind.STICKER, "Champion!", null,
                "768/FULL/UI/JOUST/MATCH_RESULTS/CROWN_COLLECT_ANIM/CROWN_COLLECT_ANIM.PAM", "bump", 0.3f),
        new Reaction("sticker_laugh", Kind.STICKER, "LOL!", null,
                "768/FULL/WORLDMAP/ZOMBOSS_NODE_HOLOGRAM/ZOMBOSS_NODE_HOLOGRAM.PAM", "laugh", 2.0f),
        new Reaction("sticker_nope", Kind.STICKER, "NOPE!", null,
                "768/INITIAL/UI/GENERIC/ENDSCREEN_X_MARK_ANIM/ENDSCREEN_X_MARK_ANIM.PAM", "check_anim", 1.5f),
        new Reaction("sticker_smack", Kind.STICKER, "Ouch!", null,
                "768/FULL/EFFECTS/ZOMBOSS_DAMAGE_CHUNKS/ZOMBOSS_DAMAGE_CHUNKS.PAM", "animation", 1.2f),
        new Reaction("sticker_breakdance", Kind.STICKER, "Dance!", null,
                "768/FULL/ZOMBIE/ZOMBIE_80S_BREAKDANCER/ZOMBIE_80S_BREAKDANCER.PAM", "jam_spin", 2.2f),
        new Reaction("sticker_disco", Kind.STICKER, "Disco!", null,
                "768/FULL/ZOMBIE/ZOMBIE_MECH_DISCO/ZOMBIE_MECH_DISCO.PAM", "dance_idle", 2.0f),
        new Reaction("sticker_chicken", Kind.STICKER, "Bawk!", null,
                "768/FULL/ZOMBIE/CHICKEN/CHICKEN.PAM", "feather_burst", 1.0f),
        new Reaction("sticker_surf", Kind.STICKER, "Cowabunga!", null,
                "768/FULL/ZOMBIE/ZOMBIE_BEACH_SURFER/ZOMBIE_BEACH_SURFER.PAM", "surf_idle", 1.8f),
        new Reaction("sticker_fireworks", Kind.STICKER, "Boom!", null,
                "768/FULL/FIREBREAKER/VASE_GREEN_FIREWORKS/VASE_GREEN_FIREWORKS.PAM", "break2", 1.4f),
        new Reaction("sticker_pinata", Kind.STICKER, "Smashed it!", null,
                "768/FULL/UI/LEVELOFTHEDAY/PRIZE_EGG_PINATA/PRIZE_EGG_PINATA.PAM", "explode", 1.1f),
        new Reaction("sticker_star_win", Kind.STICKER, "GG!", null,
                "768/INITIAL/EFFECTS/STAR_OBJECTIVE_FLOWER/STAR_OBJECTIVE_FLOWER.PAM", "win", 1.6f),
        new Reaction("sticker_sparkle", Kind.STICKER, "Shiny!", null,
                "768/INITIAL/UI/STORE/CARD_SPARKLE/CARD_SPARKLE.PAM", "animation", 0.9f),
        new Reaction("sticker_balloon", Kind.STICKER, "Float away!", null,
                "768/FULL/ZOMBIE/ZOMBIE_BIGHEAD_BALLOON/ZOMBIE_BIGHEAD_BALLOON.PAM", "fly", 1.6f),
    };

    public static final Reaction[] TEXT_MESSAGES = slice(0, 3);
    public static final Reaction[] EMOJIS = slice(3, 9);
    public static final Reaction[] STICKERS = slice(9, ALL.length);

    private static Reaction[] slice(int from, int to) {
        Reaction[] out = new Reaction[to - from];
        System.arraycopy(ALL, from, out, 0, out.length);
        return out;
    }

    public static Reaction findById(String id) {
        if (id == null) return null;
        for (Reaction r : ALL) {
            if (r.id().equals(id)) return r;
        }
        return null;
    }
}
