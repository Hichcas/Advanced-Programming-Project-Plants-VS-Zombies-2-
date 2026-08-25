package com.PVZ.network.server;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * مدیریت یک مسابقه‌ی آنلاین بعد از Matchmaking.
 * فاز انتخاب (۳۰ ثانیه) و سپس فاز شمارش معکوس (۳ ثانیه) و شروع بازی.
 */
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

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public OnlineMatchSession(ClientSession plantSession, ClientSession zombieSession, int levelId, String roomId) {
        this.plantSession = plantSession;
        this.zombieSession = zombieSession;
        this.levelId = levelId;
        this.roomId = roomId;

        // شروع تایمر ۳۰ ثانیه‌ای برای انتخاب
        scheduler.schedule(this::forceReady, SELECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private void forceReady() {
        // اگر بعد از ۳۰ ثانیه یکی آماده نشد، به‌عنوان ready علامت بزن
        if (!plantReady) plantReady = true;
        if (!zombieReady) zombieReady = true;
        checkStartCountdown();
    }

    /** وقتی بازیکن دکمه‌ی LET'S ROCK را زد صدا زده می‌شود. */
    public synchronized void markReady(String username) {
        if (username.equals(plantSession.getUsername())) {
            plantReady = true;
        } else if (username.equals(zombieSession.getUsername())) {
            zombieReady = true;
        }
        checkStartCountdown();
    }

    private void checkStartCountdown() {
        if (countdownStarted) return;
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
}
