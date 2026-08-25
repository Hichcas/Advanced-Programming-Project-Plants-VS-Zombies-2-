package com.PVZ.network.server;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnlineMatchSession {

    private static final long SELECTION_TIMEOUT_MS = 30_000L;
    private static final long COUNTDOWN_MS = 3_000L;

    private final ClientSession plantSession;
    private final ClientSession zombieSession;
    private final int levelId;
    private final String roomId;

    private boolean plantReady = false;
    private boolean zombieReady = false;
    private boolean countdownStarted = false;
    private boolean finished = false;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public OnlineMatchSession(ClientSession plantSession, ClientSession zombieSession, int levelId, String roomId) {
        this.plantSession = plantSession;
        this.zombieSession = zombieSession;
        this.levelId = levelId;
        this.roomId = roomId;

        // اگر یکی از بازیکن‌ها قطع شود، به دیگری اعلام برد
        plantSession.onDisconnect(() -> handlePlayerDisconnect(plantSession));
        zombieSession.onDisconnect(() -> handlePlayerDisconnect(zombieSession));

        // تایمر ۳۰ ثانیه‌ای برای انتخاب
        scheduler.schedule(this::forceReady, SELECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private synchronized void handlePlayerDisconnect(ClientSession disconnected) {
        if (finished) return;
        finished = true;

        ClientSession winner = (disconnected == plantSession) ? zombieSession : plantSession;
        if (winner != null && winner.isInGame()) {
            winner.setInGame(false);
            NetworkMessage gameOver = NetworkMessage.push(MessageType.GAME_OVER)
                .with("winner", winner.getCurrentRole())
                .with("reason", "OPPONENT_DISCONNECTED");
            winner.send(gameOver);
        }

        if (disconnected != null) {
            disconnected.setInGame(false);
        }
        cleanup();
    }

    private synchronized void forceReady() {
        if (!plantReady) plantReady = true;
        if (!zombieReady) zombieReady = true;
        checkStartCountdown();
    }

    public synchronized void markReady(String username) {
        if (finished) return;
        if (username.equals(plantSession.getUsername())) {
            plantReady = true;
        } else if (username.equals(zombieSession.getUsername())) {
            zombieReady = true;
        }
        checkStartCountdown();
    }

    private void checkStartCountdown() {
        if (countdownStarted || finished) return;
        if (plantReady && zombieReady) {
            countdownStarted = true;
            long startAt = System.currentTimeMillis() + COUNTDOWN_MS;

            NetworkMessage plantMsg = NetworkMessage.push(MessageType.GAME_START_SYNC)
                .with("startAt", startAt);
            plantSession.send(plantMsg);

            NetworkMessage zombieMsg = NetworkMessage.push(MessageType.GAME_START_SYNC)
                .with("startAt", startAt);
            zombieSession.send(zombieMsg);

            scheduler.shutdown();
        }
    }

    public void markGameFinished() {
        if (finished) return;
        finished = true;
        plantSession.setInGame(false);
        zombieSession.setInGame(false);
        cleanup();
    }

    private void cleanup() {
        scheduler.shutdownNow();
        MatchmakingManager.getInstance().removeSession(roomId);
    }
}
