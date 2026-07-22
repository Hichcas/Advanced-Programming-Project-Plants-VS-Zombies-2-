package com.PVZ.model.user;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;

import java.time.LocalDate;
import java.util.Set;

public class ShopDaily {
    private PlantType offerPlant;   // null یعنی هنوز پیشنهادی ساخته نشده
    private LocalDate offerDate;    // تاریخی که پیشنهاد تولید شده (null = هیچ پیشنهادی)
    private boolean purchased;      // وضعیت خرید

    public ShopDaily() {
        // مقدار اولیه: خالی
        this.offerPlant = null;
        this.offerDate = null;
        this.purchased = false;
    }

    // ---------- Getter & Setter ----------
    public PlantType getOfferPlant() {
        return offerPlant;
    }

    public void setOfferPlant(PlantType offerPlant) {
        this.offerPlant = offerPlant;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(LocalDate offerDate) {
        this.offerDate = offerDate;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    // ---------- منطق خودکار (بدون نیاز به پارامتر تاریخ) ----------

    /** آیا پیشنهاد ذخیره‌شده متعلق به امروز است؟ */
    public boolean isForToday() {
        return offerDate != null && offerDate.equals(LocalDate.now()) && offerPlant != null;
    }

    /** آیا امروز می‌توان خرید کرد؟ (تاریخ امروز باشد و هنوز خریده نشده باشد) */
    public boolean isAvailableToday() {
        return isForToday() && !purchased;
    }

    /** آیا نیاز به تولید پیشنهاد جدید داریم؟ (تاریخ امروز نباشد یا اصلاً پیشنهادی ساخته نشده) */
    public boolean needsNewOffer() {
        return offerDate == null || !offerDate.equals(LocalDate.now()) || offerPlant == null;
    }

    /** ثبت خرید (فقط در صورت معتبر بودن) */
    public boolean markAsPurchased() {
        if (isAvailableToday()) {
            purchased = true;
            return true;
        }
        return false;
    }

    /** تولید یک پیشنهاد جدید برای امروز (مهم نیست قبلاً چه بوده، تاریخ امروز می‌گیرد) */
    public void generateOfferForToday(PlantType plant) {
        this.offerPlant = plant;
        this.offerDate = LocalDate.now();
        this.purchased = false;
    }

    public boolean generateIfNeeded() {
        if (!needsNewOffer()) {
            return false;   // امروز پیشنهاد معتبری داریم، تولید دوباره لازم نیست
        }

        // دسترسی به کاربر فعلی و گیاهان آنلاک‌شده
        User currentUser = AppStatus.getCurrentUser();
        if (currentUser == null || currentUser.collectionState == null) {
            System.err.println("Error in " + getClass().getName() + ".generateIfNeeded(): No user logged in.");
            return false;
        }

        Set<PlantType> unlocked = currentUser.collectionState.getUnlockedPlants();
        if (unlocked == null || unlocked.isEmpty()) {
            System.err.println("Error in " + getClass().getName() +
                    ".generateIfNeeded(): No unlocked plants available.");
            return false;
        }

        // انتخاب تصادفی یک گیاه از مجموعه
        PlantType[] unlockedArray = unlocked.toArray(new PlantType[0]);
        PlantType randomPlant = unlockedArray[new java.util.Random().nextInt(unlockedArray.length)];

        // تولید پیشنهاد جدید برای امروز
        generateOfferForToday(randomPlant);
        return true;
    }
}
