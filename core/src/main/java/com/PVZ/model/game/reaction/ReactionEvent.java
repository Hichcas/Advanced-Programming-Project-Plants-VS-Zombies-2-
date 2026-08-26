package com.PVZ.model.game.reaction;

/** A reaction waiting for presentation in the local UI. */
public record ReactionEvent(String reactionId, boolean mine, String senderName) {}
