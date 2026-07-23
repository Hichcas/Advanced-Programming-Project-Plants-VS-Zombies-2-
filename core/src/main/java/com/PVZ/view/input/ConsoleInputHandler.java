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
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    commandQueue.offer(line.trim());
                }
            } catch (IOException e) {
                Gdx.app.error("Console", "خطا در خواندن ورودی", e);
            }
        }, "ConsoleInputThread");
        inputThread.setDaemon(true);
        inputThread.start();
    }

    public void stop() {
        running = false;
        if (inputThread != null) {
            inputThread.interrupt();
        }
    }


    public String pollCommand() {
        return commandQueue.poll();
    }

    public boolean hasCommand() {
        return !commandQueue.isEmpty();
    }
}
