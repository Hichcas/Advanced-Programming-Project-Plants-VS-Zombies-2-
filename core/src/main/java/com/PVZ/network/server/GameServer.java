package com.PVZ.network.server;

import com.PVZ.database.UserDatabase;
import com.PVZ.network.server.handlers.AuthHandlers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * فرآیند سرور. جدا از اپلیکیشن Client اجرا می‌شود (JVM جداگانه).
 *
 * مدل concurrency: یک Thread به ازای هر Client متصل (thread-per-connection).
 * برای این مقیاس (چند ده کاربر همزمان، نه هزاران) این ساده‌ترین و
 * قابل‌اطمینان‌ترین مدل است؛ نیازی به NIO/Netty نداریم.
 *
 * نکته‌ی مهم درباره‌ی مسیر داده: {@link UserDatabase} با
 * {@code findProjectRoot()} دنبال فایل settings.gradle می‌گردد تا مسیر
 * resources/data را پیدا کند. یعنی این کلاس (GameServer) باید از داخل
 * ریشه‌ی پروژه (یا هر زیرپوشه‌ای از آن) اجرا شود، وگرنه در همان ابتدا
 * Exception می‌خورد. اگر پروژه‌ی نهایی gradle نیست/ساختار عوض شده،
 * DATA_DIR در UserDatabase را متناسب با ساختار واقعی asset‌بندی کنید.
 */
public class GameServer {

    private static final int DEFAULT_PORT = 5050;

    private final int port;
    private final RequestDispatcher dispatcher = new RequestDispatcher();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public GameServer(int port) {
        this.port = port;
        AuthHandlers.registerAll(dispatcher);
        // نفرات دیگر تیم اینجا Handlerهای خودشان را ثبت می‌کنند، مثلا:
        //   MatchmakingHandlers.registerAll(dispatcher);
        //   GameSyncHandlers.registerAll(dispatcher);
        //   ReactionHandlers.registerAll(dispatcher);
        //   LeaderboardHandlers.registerAll(dispatcher);
    }

    /** برای این‌که بخش‌های دیگر تیم بتوانند از بیرون هم Handler اضافه کنند. */
    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public void start() throws IOException {
        UserDatabase.init();
        UserRegistryWarmup.loadExistingUsers();

        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("[GameServer] Listening on port " + port);

        while (running) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if (!running) break; // stop() صدا زده شده، خطا مورد انتظار است
                System.err.println("[GameServer] Accept failed: " + e.getMessage());
                continue;
            }
            try {
                ClientSession session = new ClientSession(clientSocket, dispatcher);
                clientPool.submit(session);
                System.out.println("[GameServer] Client connected: " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("[GameServer] Failed to set up client session: " + e.getMessage());
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        clientPool.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.err.println("Invalid port argument, using default " + DEFAULT_PORT);
            }
        }
        GameServer server = new GameServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }

    /** بارگیری اولیه‌ی کاربران موجود در دیتابیس به داخل کش UserRegistry. */
    private static final class UserRegistryWarmup {
        static void loadExistingUsers() {
            com.PVZ.model.user.UserRegistry.loadAllFromDatabase();
            System.out.println("[GameServer] Loaded existing users into registry.");
        }
    }
}
