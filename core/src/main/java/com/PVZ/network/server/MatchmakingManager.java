package com.PVZ.network.server;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchmakingManager {

    private static final MatchmakingManager INSTANCE = new MatchmakingManager();

    private final Queue<ClientSession> randomQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, ClientSession> pendingInvitations = new ConcurrentHashMap<>();

    private MatchmakingManager() {
    }

    public static MatchmakingManager getInstance() {
        return INSTANCE;
    }

    // ===================== Random Match =====================

    public synchronized void joinRandomQueue(ClientSession session) {
        randomQueue.remove(session);
        randomQueue.add(session);

        if (randomQueue.size() >= 2) {
            ClientSession a = randomQueue.poll();
            ClientSession b = randomQueue.poll();
            if (a != null && b != null) {
                startMatch(a, b);
            }
        }
    }

    public synchronized void leaveRandomQueue(ClientSession session) {
        randomQueue.remove(session);
    }

    // ===================== Challenge =====================

    public synchronized String challenge(ClientSession challenger, String targetUsername) {
        if (!challenger.isAuthenticated()) {
            return "You must be logged in.";
        }
        if (targetUsername == null || targetUsername.isBlank()) {
            return "Please enter a username.";
        }
        if (targetUsername.equals(challenger.getUsername())) {
            return "You cannot challenge yourself.";
        }

        com.PVZ.model.user.User targetUser =
            com.PVZ.model.user.UserRegistry.getUser(targetUsername);
        if (targetUser == null) {
            return "User does not exist.";
        }

        ClientSession targetSession = SessionRegistry.get(targetUsername);
        if (targetSession == null) {
            return "User is offline.";
        }

        if (targetSession.isInGame()) {
            return "User is in a game and cannot play now.";
        }

        pendingInvitations.put(targetUsername, challenger);

        NetworkMessage invitation = NetworkMessage.push(MessageType.CHALLENGE_INVITATION)
            .with("from", challenger.getUsername());
        targetSession.send(invitation);

        return null;
    }

    public synchronized String respondToInvitation(ClientSession target, boolean accept) {
        String targetUsername = target.getUsername();
        ClientSession challenger = pendingInvitations.remove(targetUsername);
        if (challenger == null) {
            return "No invitation found.";
        }

        if (accept) {
            startMatch(challenger, target);
        }
        return null;
    }

    // ===================== Match =====================

    private void startMatch(ClientSession a, ClientSession b) {
        a.setInGame(true);
        b.setInGame(true);

        String roomId = a.getUsername() + "-" + b.getUsername() + "-" + System.currentTimeMillis();

        NetworkMessage matchFoundA = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", b.getUsername())
            .with("roomId", roomId);

        NetworkMessage matchFoundB = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", a.getUsername())
            .with("roomId", roomId);

        a.send(matchFoundA);
        b.send(matchFoundB);

        System.out.println("[Matchmaking] Match found: " + a.getUsername() + " vs " + b.getUsername());
    }
}
