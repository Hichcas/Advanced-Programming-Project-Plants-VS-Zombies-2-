package database;

import java.util.Optional;

public interface DatabaseConnection {

    // ۱. متدهای مدیریت رکوردهای کاربران (CRUD خالص و عمومی)
    public void addRecord(DatabaseRecord record);
    public Optional<DatabaseRecord> getUser(String username);
    public void updateRecord(DatabaseRecord record);
    public void removeRecord(DatabaseRecord record);
    public boolean containsUser(String username);

    // ۲. متدهای عمومی برای همگام‌سازی با هارد دیسک (I/O)
    // این متد تمام رکوردهای موجود در رم را یکجا در فایل اصلی بنویسد
    public void saveAllData();
    
    // این متد برای مدیریت فایل تک‌یوزری (یوزر لاگین مانده) است؛ 
    // کاملاً جنرال است و فقط یک استرینگ ساده را ذخیره/بازیابی می‌کند
    public void saveActiveSession(DatabaseRecord activeInfo);
    public Optional<DatabaseRecord> getActiveSession();
    public void clearActiveSession();
    public boolean isThereAnyCurrentSession();
}