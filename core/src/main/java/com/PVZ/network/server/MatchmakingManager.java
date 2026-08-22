package com.PVZ.network.server;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * مدیریت صف تصادفی و دعوت‌نامه‌های چالش.
 * Thread-safe است و توسط Handlerهای مختلف صدا زده می‌شود.
 */
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
        randomQueue.remove(session); // جلوگیری از ورود تکراری
        randomQueue.add(session);

        // اگر نفر دیگری در صف باشد، هر دو را از صف خارج و match کن
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

    public void removeFromAll(ClientSession session) {
        randomQueue.remove(session);
        // حذف دعوت‌های معلق این کاربر
        String username = session.getUsername();
        if (username != null) {
            pendingInvitations.remove(username);
        }
    }

    // ===================== Challenge =====================

    /**
     * @return null در صورت موفقیت، در غیر این صورت پیام خطا.
     */
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

        // آیا کاربر هدف اصلاً وجود دارد؟
        com.PVZ.model.user.User targetUser = com.PVZ.model.user.UserRegistry.getUser(targetUsername);
        if (targetUser == null) {
            return "User does not exist.";
        }

        // آیا آنلاین است؟
        ClientSession targetSession = SessionRegistry.get(targetUsername);
        if (targetSession == null) {
            return "User is offline.";
        }

        // اگر دعوت قبلی از این چلنجر به همین نفر وجود دارد، فقط update
        pendingInvitations.put(targetUsername, challenger);

        // ارسال دعوت‌نامه به هدف
        NetworkMessage invitation = NetworkMessage.push(MessageType.CHALLENGE_INVITATION)
            .with("from", challenger.getUsername());
        targetSession.send(invitation);

        return null; // موفق
    }

    public synchronized String respondToInvitation(ClientSession target, boolean accept) {
        String targetUsername = target.getUsername();
        ClientSession challenger = pendingInvitations.remove(targetUsername);
        if (challenger == null) {
            return "No invitation found.";
        }

        if (accept) {
            startMatch(challenger, target);
        } else {
            // به چلنجر اطلاع ندهیم؛ فعلاً فقط برای دعوت‌شده پاسخ می‌فرستیم
            // در صورت نیاز می‌توان پیام جداگانه فرستاد
        }
        return null; // موفق
    }

    // ===================== Match =====================

    private void startMatch(ClientSession a, ClientSession b) {
        // فعلاً فقط MATCH_FOUND را به هر دو بفرست
        NetworkMessage matchFoundA = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", b.getUsername())
            .with("roomId", a.getUsername() + "-" + b.getUsername() + "-" + System.currentTimeMillis());
        NetworkMessage matchFoundB = NetworkMessage.push(MessageType.MATCH_FOUND)
            .with("opponent", a.getUsername())
            .with("roomId", a.getUsername() + "-" + b.getUsername() + "-" + System.currentTimeMillis());

        a.send(matchFoundA);
        b.send(matchFoundB);
        System.out.println("[Matchmaking] Match found: " + a.getUsername() + " vs " + b.getUsername());
    }
}
