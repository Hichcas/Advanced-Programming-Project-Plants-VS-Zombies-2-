package com.PVZ.network.server;

import com.PVZ.network.common.MessageChannel;
import com.PVZ.network.common.NetworkMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * یک اتصال Client روی سرور. هر Client یک Thread اختصاصی دارد که در حلقه
 * منتظر پیام می‌ماند ({@link #run()}) و هر پیام را به {@link RequestDispatcher}
 * می‌سپارد.
 *
 * {@code username} بعد از LOGIN موفق ست می‌شود؛ قبل از آن null است -
 * Handlerهای بخش‌های دیگر (مثلا بازی/matchmaking) باید قبل از پردازش
 * درخواست بررسی کنند {@code session.getUsername() != null} تا کاربر
 * لاگین‌نشده نتواند وارد بازی/صف بشود.
 */
public class ClientSession implements Runnable {

    private final MessageChannel channel;
    private final RequestDispatcher dispatcher;
    private volatile String username; // null تا وقتی لاگین موفق انجام نشده
    private final List<Runnable> disconnectListeners = new CopyOnWriteArrayList<>();

    private volatile boolean inGame = false;
    private volatile ClientSession opponentSession;
    private volatile String currentRoomId;
    private volatile String currentRole;

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public ClientSession getOpponentSession() {
        return opponentSession;
    }

    private volatile long lastActivity = System.currentTimeMillis();
    private final ScheduledExecutorService heartbeatChecker = Executors.newSingleThreadScheduledExecutor();
    private static final long HEARTBEAT_TIMEOUT_MS = 5000; // 5 ثانیه

    private void startHeartbeatChecker() {
        heartbeatChecker.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (now - lastActivity > HEARTBEAT_TIMEOUT_MS) {
                // قطع شده است
                try {
                    channel.close();
                } catch (Exception ignored) {}
            }
        }, HEARTBEAT_TIMEOUT_MS, HEARTBEAT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    public void updateLastActivity() {
        lastActivity = System.currentTimeMillis();
    }

    public void setOpponentSession(ClientSession opponentSession) {
        this.opponentSession = opponentSession;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public ClientSession(Socket socket, RequestDispatcher dispatcher) throws IOException {
        this.channel = new MessageChannel(socket);
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try {
            NetworkMessage message;
            while ((message = channel.receive()) != null) {
                updateLastActivity(); // ← این خط اضافه شود
                dispatcher.dispatch(this, message);
            }
        } catch (IOException e) {
            // اتصال به‌صورت غیرمنتظره قطع شد (کاربر برنامه را بست، شبکه قطع شد و ...)
            // - این حالت عادی است، خطا لاگ نمی‌کنیم که کنسول سرور شلوغ نشود.
        } finally {
            handleDisconnect();
        }
    }

    private void handleDisconnect() {
        if (username != null) {
            SessionRegistry.markOffline(username);
            setUsername(null);
        }
        for (Runnable listener : disconnectListeners) {
            try {
                listener.run();
            } catch (Exception ignored) {
            }
        }
        channel.close();
        heartbeatChecker.shutdownNow();
    }

    /**
     * زیرسیستم‌های دیگر (matchmaking queue، اتاق بازی فعال) با این متد
     * ثبت می‌کنند که وقتی این کاربر قطع شد، خودشان را پاکسازی کنند
     * (مثلا از صف انتظار حذفش کنند یا به حریفش اطلاع بدهند بازی رها شده).
     */
    public void onDisconnect(Runnable listener) {
        disconnectListeners.add(listener);
    }

    public void send(NetworkMessage message) {
        if (!channel.isClosed()) {
            channel.send(message);
        }
    }

    public String getUsername() {
        return username;
    }

    /** فقط باید توسط Handler لاگین/ثبت‌نام بعد از احراز هویت موفق صدا زده شود. */
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAuthenticated() {
        return username != null;
    }
}
