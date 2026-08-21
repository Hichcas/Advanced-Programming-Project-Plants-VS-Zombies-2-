package com.PVZ.network.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * لایه‌ی ارتباطی خام روی یک {@link Socket}: هر پیام دقیقا یک خط JSON است
 * (newline-delimited)، هم Server و هم Client از همین کلاس استفاده
 * می‌کنند تا منطق فریم‌بندی پیام دو بار پیاده‌سازی نشود.
 *
 * Thread-safety: چون هم Thread شنونده (که مدام receive() صدا می‌زند) و
 * هم Threadهای دیگر (که می‌خواهند send() کنند) ممکن است هم‌زمان به این
 * آبجکت دسترسی داشته باشند، نوشتن روی Socket را synchronized کرده‌ایم.
 * خواندن نیازی به قفل ندارد چون همیشه فقط یک Thread می‌خواند.
 */
public class MessageChannel implements AutoCloseable {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public MessageChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(socket.getOutputStream(), false, StandardCharsets.UTF_8);
    }

    /** یک پیام می‌فرستد. Thread-safe - چند Thread می‌توانند هم‌زمان صدا بزنند. */
    public synchronized void send(NetworkMessage message) {
        String line = JsonCodec.encode(message);
        writer.println(line);
        writer.flush();
    }

    /**
     * منتظر می‌ماند تا یک پیام کامل برسد و آن را برمی‌گرداند.
     * اگر طرف مقابل اتصال را ببندد {@code null} برمی‌گرداند.
     * این متد Blocking است - باید همیشه از یک Thread اختصاصی صدا زده شود.
     */
    public NetworkMessage receive() throws IOException {
        String line = reader.readLine();
        if (line == null) return null;
        line = line.trim();
        if (line.isEmpty()) return receive();
        return JsonCodec.decode(line);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
