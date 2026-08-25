package com.PVZ.model.game.reaction;

/** یک واکنش در انتظار نمایش - یا از طرف خودمان (mine=true) یا حریف (mine=false). */
public record ReactionEvent(String reactionId, boolean mine) {
}
