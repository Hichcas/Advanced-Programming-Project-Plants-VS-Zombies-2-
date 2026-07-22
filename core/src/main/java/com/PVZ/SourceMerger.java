

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class SourceMerger {

    public static void main(String[] args) {
        Path currentDir = Paths.get(".");
        Path outputFile = Paths.get("result.txt");

        // حذف فایل نتیجه قبلی در صورت وجود، برای جلوگیری از اضافه شدن مجدد به خودش
        try {
            Files.deleteIfExists(outputFile);
        } catch (IOException e) {
            System.err.println("خطا در حذف فایل نتیجه قبلی: " + e.getMessage());
        }

        System.out.println("در حال جمع‌آوری تمام سورس فایل‌ها در result.txt...");

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            // پیمایش تمام فایل‌ها به صورت ریکرسیو
            Files.walk(currentDir)
                .filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString().equals("result.txt")) // نادیده گرفتن فایل خروجی
                .filter(path -> !path.getFileName().toString().equals("SourceMerger.java")) // نادیده گرفتن خود این کد
                .filter(path -> !path.toString().contains(".git")) // نادیده گرفتن کش گیت
                .filter(path -> !path.toString().contains(".idea")) // نادیده گرفتن تنظیمات آی‌دی‌ای
                // نادیده گرفتن فایل‌های کامپایل شده گریدل
                .filter(path -> !path.toString().contains("/build/") && !path.toString().contains("\\build\\"))
                .forEach(filePath -> appendFileContent(filePath, writer));

            System.out.println("عملیات با موفقیت تمام شد! فایل result.txt آماده است.");

        } catch (IOException e) {
            System.err.println("خطا در ساخت فایل خروجی: " + e.getMessage());
        }
    }

    private static void appendFileContent(Path filePath, BufferedWriter writer) {
        try {
            // نوشتن هدر برای مشخص شدن اینکه این کد مربوط به کدام فایل است
            writer.write("\n=================================================================\n");
            writer.write("FILE: " + filePath.toString() + "\n");
            writer.write("=================================================================\n\n");

            // خواندن خط به خط و نوشتن در فایل مقصد
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.newLine(); // یک خط خالی اضافه برای فاصله بین فایل‌ها

            System.out.println("اضافه شد: " + filePath);

        } catch (IOException e) {
            // بعضی فایل‌ها مثل عکس‌ها یا فایل‌های باینری متنی نیستند و خطا می‌دهند که عادی است
            System.err.println("فایل متنی نبود یا خوانده نشد (نادیده گرفته شد): " + filePath.getFileName());
        }
    }
}
