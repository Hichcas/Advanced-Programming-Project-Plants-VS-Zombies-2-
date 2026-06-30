package com.PVZ.model.user;

/**
 * کلاس پروفایل کاربر شامل اطلاعات هویتی، امنیتی و آماری.
 * این کلاس به‌عنوان رکورد اصلی کاربر در دیتابیس استفاده می‌شود.
 */
public class Profile {
    // ---------- اطلاعات هویتی ----------
    private String username;
    private String nickname;
    private String email;
    private String gender;
    
    // ---------- اطلاعات امنیتی ----------
    private String passwordHash;          // هش رمز عبور (SHA-256)
    private String securityQuestion;      // متن سوال امنیتی
    private String securityAnswerHash;    // هش پاسخ سوال امنیتی (برای ذخیره‌سازی امن)
    
    // ---------- سازنده‌ها ----------
    public Profile() {
        // سازندهٔ پیش‌فرض (برای Gson یا استفاده‌های دیگر)
    }

    /**
     * سازندهٔ اصلی برای ساخت یک پروفایل جدید.
     * آمار با مقادیر پیش‌فرض صفر و سختی روی ۳ تنظیم می‌شود.
     */
    public Profile(String username, String passwordHash, String nickname,
                   String email, String gender, String securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
    }

    // ---------- Getter و Setter ----------
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }


}
