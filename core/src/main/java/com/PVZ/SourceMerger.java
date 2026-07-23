package com.PVZ;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class SourceMerger {

    public static void main(String[] args) {
        Path currentDir = Paths.get(".");
        Path outputFile = Paths.get("result.txt");

        try {
            Files.deleteIfExists(outputFile);
        } catch (IOException e) {
            System.err.println("خطا در حذف فایل نتیجه قبلی: " + e.getMessage());
        }

        System.out.println("در حال جمع‌آوری تمام سورس فایل‌ها در result.txt...");

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            Files.walk(currentDir)
                .filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString().equals("result.txt"))
                .filter(path -> !path.getFileName().toString().equals("SourceMerger.java"))
                .filter(path -> !path.toString().contains(".git"))
                .filter(path -> !path.toString().contains(".idea"))
                .filter(path -> !path.toString().contains("/build/") && !path.toString().contains("\\build\\"))
                .forEach(filePath -> appendFileContent(filePath, writer));

            System.out.println("عملیات با موفقیت تمام شد! فایل result.txt آماده است.");

        } catch (IOException e) {
            System.err.println("خطا در ساخت فایل خروجی: " + e.getMessage());
        }
    }

    private static void appendFileContent(Path filePath, BufferedWriter writer) {
        try {
            writer.write("\n=================================================================\n");
            writer.write("FILE: " + filePath.toString() + "\n");
            writer.write("=================================================================\n\n");

            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.newLine();

            System.out.println("اضافه شد: " + filePath);

        } catch (IOException e) {
            System.err.println("فایل متنی نبود یا خوانده نشد (نادیده گرفته شد): " + filePath.getFileName());
        }
    }
}
