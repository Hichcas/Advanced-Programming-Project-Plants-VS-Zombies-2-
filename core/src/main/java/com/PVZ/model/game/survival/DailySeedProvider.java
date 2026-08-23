package com.PVZ.model.game.survival;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * طبق سند فاز سه («این بازی در هر روز برای همه‌ی کاربران با الگوریتم
 * یکسانی زامبی تولید می‌کند»)، تولید زامبی‌های بازی امتیازی باید برای
 * همه‌ی کاربران در یک روز مشخص، دقیقا یکسان باشد - نه فقط "شبیه به هم"،
 * بلکه از نظر توالی و زمان‌بندی کاملا یکی.
 *
 * راه‌حل: به‌جای {@code new Random()} با seed تصادفی، از تاریخ امروز
 * (UTC، تا سطح روز - نه ساعت/دقیقه) به‌عنوان seed استفاده می‌کنیم. همه‌ی
 * کاربرانی که همان روز بازی می‌کنند، دقیقا همان دنباله‌ی اعداد تصادفی
 * (و در نتیجه همان دنباله‌ی زامبی‌ها) را می‌گیرند؛ فردا که روز عوض شود،
 * seed هم عوض می‌شود و یک چینش کاملا جدید تولید می‌شود.
 */
public final class DailySeedProvider {

    /** یک نمک ثابت دلخواه، فقط برای اینکه seed نهایی به یک الگوی قابل‌حدس ساده (مثل خودِ epoch day) نچسبد. */
    private static final long SALT = 0x50565A5F4D594F4CL; // "PVZ_MYOL" به‌صورت hex، فقط یک عدد ثابت دلخواه

    private DailySeedProvider() {
    }

    /** seed امروز (UTC) را برمی‌گرداند - برای تمام کاربران در یک روز یکسان است. */
    public static long todaySeed() {
        long epochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        return epochDay ^ SALT;
    }

    public static java.util.Random newTodayRandom() {
        return new java.util.Random(todaySeed());
    }
}
