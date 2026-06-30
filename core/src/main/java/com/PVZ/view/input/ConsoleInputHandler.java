package com.PVZ.view.input;

import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsoleInputHandler {
    private final Queue<String> commandQueue = new ConcurrentLinkedQueue<>();
    private Thread inputThread;
    private volatile boolean running = false;

    public void start() {
        if (running) return;
        running = true;
        inputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (running) {
                    // این خوندن بلاک کننده است، اما چون توی یه thread جدا است، بازی قفل نمیشه
                    String line = reader.readLine();
                    if (line == null) { // اگر stream بسته بشه (مثل Ctrl+D)
                        break;
                    }
                    commandQueue.offer(line.trim()); // خط کامل رو توی صف میذاریم
                }
            } catch (IOException e) {
                Gdx.app.error("Console", "خطا در خواندن ورودی", e);
            }
        }, "ConsoleInputThread");
        inputThread.setDaemon(true); // با بسته شدن برنامه تموم بشه
        inputThread.start();
    }

    public void stop() {
        running = false;
        if (inputThread != null) {
            inputThread.interrupt(); // برای خروج از readLine موقع بستن برنامه
        }
    }

    /**
     * توی رندر loop صداش بزن.
     * فقط فرمان‌های کامل و آماده رو برمی‌گردونه، در غیر این صورت null.
     */
    public String pollCommand() {
        return commandQueue.poll(); // اگه خالی باشه null برمیگرده -> بازی ادامه میده
    }

    public boolean hasCommand() {
        return !commandQueue.isEmpty();
    }
}
