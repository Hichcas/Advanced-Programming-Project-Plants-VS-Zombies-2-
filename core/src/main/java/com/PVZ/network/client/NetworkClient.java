package com.PVZ.network.client;

import com.PVZ.network.common.MessageChannel;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * لایه‌ی ارتباط سمت Client با {@link com.PVZ.network.server.GameServer}.
 *
 * دو نوع پیام از سرور می‌رسد و این کلاس هر دو را همزمان پشتیبانی
 * می‌کند (طبق سند: باید بتوانیم درخواست بفرستیم *و* پیام ناخواسته از
 * سرور (مثل دعوت‌نامه یا snapshot بازی) دریافت کنیم):
 *
 * ۱. پاسخ به یک درخواست قبلی (پیام دارای requestId که در
 *    {@link #pendingRequests} منتظرش هستیم) -> {@link #sendRequest} را
 *    که صدا زده Complete می‌کند.
 * ۲. پیام Push بدون درخواست قبلی (دعوت‌نامه، snapshot بازی، ری‌اکشن
 *    دریافتی) -> به تمام Listenerهایی که با {@link #on(MessageType, Consumer)}
 *    برای همان نوع پیام ثبت شده‌اند تحویل داده می‌شود.
 *
 * تمام کدهای این کلاس روی Thread شنونده‌ی اختصاصی اجرا می‌شوند، به جز
 * چیزی که خودتان صدا می‌زنید (connect/sendRequest/sendFireAndForget) که
 * از Thread فراخوان (مثلا Thread رندر UI) اجرا می‌شود - پس اگر داخل
 * یک Listener push می‌خواهید مستقیم UI/libGDX را تغییر بدهید، حتما آن
 * را به Thread اصلی رندر پاس بدهید (posted runnable)، چون این Listener
 * روی Thread شبکه اجرا می‌شود نه روی render thread.
 */
public class NetworkClient {

    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    private Socket socket;
    private MessageChannel channel;
    private Thread listenerThread;
    private volatile boolean connected = false;

    private final Map<String, CompletableFuture<NetworkMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<MessageType, List<Consumer<NetworkMessage>>> pushListeners = new ConcurrentHashMap<>();
    private final List<Runnable> disconnectListeners = new CopyOnWriteArrayList<>();

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        channel = new MessageChannel(socket);
        connected = true;
        startHeartbeat();

        listenerThread = new Thread(this::listenLoop, "NetworkClient-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private Thread heartbeatThread;
    private volatile boolean heartbeatRunning = false;

    private void startHeartbeat() {
        heartbeatRunning = true;
        heartbeatThread = new Thread(() -> {
            while (connected && heartbeatRunning) {
                try {
                    Thread.sleep(2000);
                    if (connected) {
                        channel.send(NetworkMessage.push(MessageType.PING));
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private void stopHeartbeat() {
        heartbeatRunning = false;
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
    }

    private void listenLoop() {
        try {
            NetworkMessage message;
            while (connected && (message = channel.receive()) != null) {
                handleIncoming(message);
            }
        } catch (IOException e) {
            // اتصال قطع شد (سرور خاموش شد یا شبکه قطع شد)
        } finally {
            handleDisconnect();
        }
    }

    private void handleIncoming(NetworkMessage message) {
        String requestId = message.getRequestId();
        if (requestId != null) {
            CompletableFuture<NetworkMessage> pending = pendingRequests.remove(requestId);
            if (pending != null) {
                pending.complete(message);
                return;
            }
        }
        // یا requestId نداشت (پیام push) یا برایش کسی منتظر نبود -> به Listenerها بده
        List<Consumer<NetworkMessage>> listeners = pushListeners.get(message.getType());
        if (listeners != null) {
            for (Consumer<NetworkMessage> listener : listeners) {
                try {
                    listener.accept(message);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void handleDisconnect() {
        connected = false;
        stopHeartbeat();
        // به تمام درخواست‌های معلق خطا بده تا کسی برای همیشه منتظر نماند
        for (CompletableFuture<NetworkMessage> future : pendingRequests.values()) {
            future.completeExceptionally(new IOException("Connection to server lost."));
        }
        pendingRequests.clear();
        for (Runnable listener : disconnectListeners) {
            try {
                listener.run();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * یک درخواست می‌فرستد و {@link CompletableFuture} پاسخ را برمی‌گرداند.
     * استفاده‌ی معمول (بلاک‌کننده، مثلا داخل یک Controller که همین الان
     * جواب می‌خواهد):
     * <pre>{@code
     * NetworkMessage response = client.sendRequest(
     *     NetworkMessage.request(MessageType.LOGIN)
     *         .with("username", username)
     *         .with("password", password)
     * ).get(10, TimeUnit.SECONDS);
     * }</pre>
     * یا به‌صورت async با {@code .thenAccept(...)}.
     */
    public CompletableFuture<NetworkMessage> sendRequest(NetworkMessage request) {
        if (!connected) {
            CompletableFuture<NetworkMessage> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IOException("Not connected to server."));
            return failed;
        }
        if (request.getRequestId() == null) {
            throw new IllegalArgumentException("Use NetworkMessage.request(type) to create a request (needs a requestId).");
        }
        CompletableFuture<NetworkMessage> future = new CompletableFuture<>();
        pendingRequests.put(request.getRequestId(), future);
        channel.send(request);
        return future;
    }

    /** نسخه‌ی بلاک‌کننده‌ی ساده با timeout پیش‌فرض - برای جاهایی که async لازم نیست. */
    public NetworkMessage sendRequestBlocking(NetworkMessage request) throws IOException, TimeoutException {
        try {
            return sendRequest(request).get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingRequests.remove(request.getRequestId());
            throw e;
        } catch (Exception e) {
            throw new IOException("Request failed: " + e.getMessage(), e);
        }
    }

    /** برای پیام‌هایی که پاسخ فوری نمی‌خواهند (مثلا ری‌اکشن حین بازی). */
    public void sendFireAndForget(NetworkMessage message) {
        if (connected) {
            channel.send(message);
        }
    }

    /** ثبت Listener برای پیام‌های Push (دعوت‌نامه، snapshot بازی، ری‌اکشن دریافتی و ...). */
    public void on(MessageType type, Consumer<NetworkMessage> listener) {
        pushListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void onDisconnect(Runnable listener) {
        disconnectListeners.add(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        if (channel != null) channel.close();
        if (listenerThread != null) listenerThread.interrupt();
    }
}
