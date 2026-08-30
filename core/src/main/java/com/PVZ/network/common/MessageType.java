package com.PVZ.network.common;

/**
 * تمام انواع پیام‌هایی که بین Client و Server رد و بدل می‌شود.
 * این فایل به‌عمد شامل انواعی هم هست که بخش‌های دیگر تیم (matchmaking،
 * سینک بازی، واکنش‌ها) قرار است پیاده‌سازی کنند، تا هیچ‌کس مجبور نباشد
 * این enum مرکزی را تغییر بدهد و باعث Merge Conflict شود. هر کسی فقط
 * برای نوع پیام خودش یک Handler در RequestDispatcher ثبت می‌کند.
 */
public enum MessageType {

    // ===== زیرساخت / حساب کاربری (این بخش) =====
    /** Client -> Server: درخواست ثبت‌نام کاربر جدید. */
    REGISTER,
    /** Server -> Client: نتیجه‌ی ثبت‌نام. */
    REGISTER_RESULT,

    /** Client -> Server: درخواست ورود. */
    LOGIN,
    /** Server -> Client: نتیجه‌ی ورود (شامل کل User در صورت موفقیت). */
    LOGIN_RESULT,

    /** Client -> Server: اعلام خروج از حساب کاربری. */
    LOGOUT,
    /** Server -> Client: تایید خروج. */
    LOGOUT_RESULT,

    /** Client -> Server: ذخیره‌ی نسخه‌ی به‌روز User در سرور (هنگام خروج یا تغییر مهم). */
    SYNC_USER,
    /** Server -> Client: تایید ذخیره‌سازی. */
    SYNC_USER_RESULT,

    /** Client -> Server: بارگیری آخرین نسخه‌ی User از سرور (مثلاً بعد از ورود از دستگاه دیگر). */
    FETCH_USER,
    /** Server -> Client: پاسخ به FETCH_USER. */
    FETCH_USER_RESULT,

    /** Client -> Server: بررسی این‌که یک username آزاد است یا نه (پیش از ثبت‌نام). */
    CHECK_USERNAME,
    /** Server -> Client: نتیجه‌ی بررسی. */
    CHECK_USERNAME_RESULT,

    // ===== عمومی =====
    /** Client <-> Server: پینگ برای زنده نگه‌داشتن اتصال و تست تاخیر. */
    PING,
    PONG,

    /** Server -> Client: خطای عمومی که به هیچ درخواست خاصی مرتبط نیست (مثلا قطعی داخلی). */
    SERVER_ERROR,

    SURRENDER,
    DRAW_OFFER,
    DRAW_RESPONSE,

    // ===== Matchmaking / انتخاب رقیب (برای هم‌تیمی که این بخش را می‌زند) =====
    CHALLENGE_USER,
    CHALLENGE_INVITATION,
    CHALLENGE_RESPONSE,
    JOIN_RANDOM_QUEUE,
    LEAVE_RANDOM_QUEUE,
    MATCH_FOUND,

    // ===== بازی «من، زامبی» آنلاین (برای هم‌تیمی که این بخش را می‌زند) =====
    GAME_PLANT_INPUT,
    GAME_DEPLOY_INPUT,
    GAME_STATE_SNAPSHOT,
    GAME_OVER,

    // ===== واکنش‌ها در حین بازی =====
    SEND_REACTION,
    REACTION_RECEIVED,

    SELECTION_READY,
    GAME_START_SYNC,

    // ===== لیدربورد =====
    FETCH_LEADERBOARD,
    FETCH_LEADERBOARD_RESULT,

    // ===== بازی امتیازی تحت شبکه (فاز شبکه) =====
    /** Client -> Server: ارسال امتیاز پایان‌دور بازی امتیازی («میوپوینت»). */
    SUBMIT_SCORE,
    /** Server -> Client: نتیجه، شامل رکورد نهایی (بعد از مقایسه با رکورد قبلی). */
    SUBMIT_SCORE_RESULT

}
