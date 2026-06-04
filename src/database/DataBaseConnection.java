package database;

import java.util.Optional;

public interface DataBaseConnection {

    // ۱. متدهای مدیریت رکوردهای کاربران (CRUD خالص و عمومی)
    public void addRecord(DataBaseRecord record);
    public Optional<DataBaseRecord> getUser(String username);
    public void updateRecord(DataBaseRecord record);
    public void removeRecord(DataBaseRecord record);
    public boolean containsUser(String username);

    // ۲. متدهای عمومی برای همگام‌سازی با هارد دیسک (I/O)
    // این متد تمام رکوردهای موجود در رم را یکجا در فایل اصلی بنویسد
    public void saveAllData();
    
    // این متد برای مدیریت فایل تک‌یوزری (یوزر لاگین مانده) است؛ 
    // کاملاً جنرال است و فقط یک استرینگ ساده را ذخیره/بازیابی می‌کند
    public void saveActiveSession(DataBaseRecord activeInfo);
    public Optional<DataBaseRecord> getActiveSession();
    public void clearActiveSession();
    public boolean isThereAnyCurrentSession();
}