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
    // چون ClientSession.onDisconnect چندین listener رو قبول می‌کنه (لیست)، اگه
    // هر بار joinRandomQueue صدا زده بشه بدون این چک، هر بار یه listener جدید
    // اضافه می‌شه (نشتی حافظه‌ی کوچیک روی سشن‌های طولانی). این Set فقط برای
    // اینه که هر session فقط یک بار cleanup ثبت کنه.
    private final java.util.Set<ClientSession> disconnectHookRegistered =
            java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private record PendingChallenge(ClientSession challenger, String challengerRole, int levelId) {}
    private final Map<String, PendingChallenge> pendingInvitations = new ConcurrentHashMap<>();
    private final Map<String, OnlineMatchSession> activeSessions = new ConcurrentHashMap<>();

    private MatchmakingManager() {
    }

    public static MatchmakingManager getInstance() {
        return INSTANCE;
    }

    public OnlineMatchSession getSession(String roomId) {
        return activeSessions.get(roomId);
    }

    // ===================== Random Match =====================

    public synchronized void joinRandomQueue(ClientSession session) {
        if (session.isInGame()) return; // از قبل توی یه بازی‌ست - دوباره صف نره که match تداخل نکنه

        // اگه کسی وسط انتظار توی صف قطع بشه (بست برنامه/افتادن نت)، باید از صف
        // حذف بشه؛ وگرنه یه بازیکن واقعی می‌تونه با یه session مرده match بشه و
        // برای همیشه منتظر بمونه (چون طرف مقابل هیچ‌وقت GAME_START_SYNC نمی‌فرسته).
        if (disconnectHookRegistered.add(session)) {
            session.onDisconnect(() -> leaveRandomQueue(session));
        }

        randomQueue.remove(session);
        randomQueue.add(session);

        if (randomQueue.size() >= 2) {
            ClientSession a = randomQueue.poll();
            ClientSession b = randomQueue.poll();
            if (a != null && b != null) {
                boolean aIsPlant = new java.util.Random().nextBoolean();
                String roleA = aIsPlant ? "PLANT" : "ZOMBIE";
                String roleB = aIsPlant ? "ZOMBIE" : "PLANT";
                int levelId = new java.util.Random().nextInt(3) + 1;
                startMatchWithRoles(a, roleA, b, roleB, levelId);
            }
        }
    }

    public synchronized void leaveRandomQueue(ClientSession session) {
        randomQueue.remove(session);
    }

    // ===================== Challenge =====================

    public synchronized String challenge(ClientSession challenger, String targetUsername, String requestedRole, int levelId) {
        if (!challenger.isAuthenticated()) return "You must be logged in.";
        if (targetUsername == null || targetUsername.isBlank()) return "Please enter a username.";
        if (targetUsername.equals(challenger.getUsername())) return "You cannot challenge yourself.";

        com.PVZ.model.user.User targetUser = com.PVZ.model.user.UserRegistry.getUser(targetUsername);
        if (targetUser == null) return "User does not exist.";

        ClientSession targetSession = SessionRegistry.get(targetUsername);
        if (targetSession == null) return "User is offline.";
        if (targetSession.isInGame()) return "User is in a game and cannot play now.";

        String role = (requestedRole != null && requestedRole.equalsIgnoreCase("ZOMBIE")) ? "ZOMBIE" : "PLANT";
        String targetRole = role.equals("PLANT") ? "ZOMBIE" : "PLANT";
        int validLevelId = (levelId >= 1 && levelId <= 3) ? levelId : 1;

        pendingInvitations.put(targetUsername, new PendingChallenge(challenger, role, validLevelId));

        NetworkMessage invitation = NetworkMessage.push(MessageType.CHALLENGE_INVITATION)
            .with("from", challenger.getUsername())
            .with("challengerRole", role)
            .with("targetRole", targetRole)
            .with("levelId", validLevelId);
        targetSession.send(invitation);

        return null;
    }

    public synchronized String respondToInvitation(ClientSession target, boolean accept) {
        String targetUsername = target.getUsername();
        PendingChallenge pending = pendingInvitations.remove(targetUsername);
        if (pending == null) return "No invitation found.";

        if (accept) {
            String challengerRole = pending.challengerRole();
            String targetRole = challengerRole.equals("PLANT") ? "ZOMBIE" : "PLANT";
            startMatchWithRoles(pending.challenger(), challengerRole, target, targetRole, pending.levelId());
        }
        return null;
    }

    // ===================== Match =====================

    private void startMatchWithRoles(ClientSession a, String roleA, ClientSession b, String roleB, int levelId) {
        a.setInGame(true);
        a.setOpponentSession(b);
        a.setCurrentRole(roleA);

        b.setInGame(true);
        b.setOpponentSession(a);
        b.setCurrentRole(roleB);

        String roomId = a.getUsername() + "-" + b.getUsername() + "-" + System.currentTimeMillis();
        a.setCurrentRoomId(roomId);
        b.setCurrentRoomId(roomId);

        long startAt = System.currentTimeMillis() + 5000L; // This will be replaced by OnlineMatchSession countdown

        NetworkMessage matchFoundA = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", b.getUsername())
            .with("roomId", roomId)
            .with("role", roleA)
            .with("levelId", levelId)
            .with("startAt", startAt); // not used directly now

        NetworkMessage matchFoundB = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", a.getUsername())
            .with("roomId", roomId)
            .with("role", roleB)
            .with("levelId", levelId)
            .with("startAt", startAt);

        a.send(matchFoundA);
        b.send(matchFoundB);

        // ساخت session جدید برای همگام‌سازی selection و countdown
        OnlineMatchSession matchSession = new OnlineMatchSession(a, b, levelId, roomId);
        activeSessions.put(roomId, matchSession);

        System.out.println("[Matchmaking] Match started: " + a.getUsername() + " (" + roleA + ") vs "
            + b.getUsername() + " (" + roleB + ") in room " + roomId + " at level " + levelId);
    }

    public void removeSession(String roomId) {
        activeSessions.remove(roomId);
    }
}
