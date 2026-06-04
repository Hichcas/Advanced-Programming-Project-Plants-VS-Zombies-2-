package model.user;

import model.enums.PlantType;
import model.enums.ZombieType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * نگه‌دارندهٔ وضعیت کلکسیون کاربر:
 * گیاهان آنلاک‌شده، زامبی‌های دیده‌شده، بسته‌های بذر،
 * سطوح ارتقای گیاهان و بوست‌های ذخیره‌شده از گلخانه.
 */
public class CollectionState {
    private Set<PlantType> unlockedPlants;
    private Set<ZombieType> seenZombies;
    private Map<PlantType, Integer> seedPackets;   // plant -> count
    private Map<PlantType, Integer> plantLevels;    // plant -> level (default 0 if not present)
    private Set<PlantType> greenhouseBoosts;        // plants with a stored boost from greenhouse

    // ---------- سازنده ----------
    public CollectionState() {
        unlockedPlants = new HashSet<>();
        seenZombies = new HashSet<>();
        seedPackets = new HashMap<>();
        plantLevels = new HashMap<>();
        greenhouseBoosts = new HashSet<>();
        // No need to pre-populate plantLevels with zeros;
        // getPlantLevel() returns 0 as default for missing keys.
    }
    

    // ---------- Getter و Setter (برای سریالایز) ----------
    public Set<PlantType> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(Set<PlantType> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public Set<ZombieType> getSeenZombies() {
        return seenZombies;
    }

    public void setSeenZombies(Set<ZombieType> seenZombies) {
        this.seenZombies = seenZombies;
    }

    public Map<PlantType, Integer> getSeedPackets() {
        return seedPackets;
    }

    public void setSeedPackets(Map<PlantType, Integer> seedPackets) {
        this.seedPackets = seedPackets;
    }

    public Map<PlantType, Integer> getPlantLevels() {
        return plantLevels;
    }

    public void setPlantLevels(Map<PlantType, Integer> plantLevels) {
        this.plantLevels = plantLevels;
    }

    public Set<PlantType> getGreenhouseBoosts() {
        return greenhouseBoosts;
    }

    public void setGreenhouseBoosts(Set<PlantType> greenhouseBoosts) {
        this.greenhouseBoosts = greenhouseBoosts;
    }

    // ---------- متدهای کمکی برای دسترسی امن ----------

    /** بررسی آنلاک بودن یک گیاه */
    public boolean isPlantUnlocked(PlantType plant) {
        return unlockedPlants.contains(plant);
    }

    /** آنلاک کردن یک گیاه (در صورت نبودن قبلی) */
    public void unlockPlant(PlantType plant) {
        unlockedPlants.add(plant);
    }

    /** ثبت مشاهدهٔ یک زامبی */
    public void seeZombie(ZombieType zombie) {
        seenZombies.add(zombie);
    }

    /** تعداد بسته‌های بذر یک گیاه */
    public int getSeedPacketCount(PlantType plant) {
        return seedPackets.getOrDefault(plant, 0);
    }

    /** افزودن بسته‌های بذر به یک گیاه */
    public void addSeedPackets(PlantType plant, int amount) {
        if (amount <= 0) return;
        seedPackets.merge(plant, amount, Integer::sum);
    }

    /** مصرف بسته‌های بذر (در صورت کافی بودن)؛ true در صورت موفقیت */
    public boolean spendSeedPackets(PlantType plant, int amount) {
        int current = getSeedPacketCount(plant);
        if (current < amount) return false;
        seedPackets.put(plant, current - amount);
        return true;
    }

    /** دریافت سطح فعلی یک گیاه (۰ برای گیاهانی که هرگز ارتقا نیافته‌اند) */
    public int getPlantLevel(PlantType plant) {
        return plantLevels.getOrDefault(plant, 0);
    }

    /** تنظیم سطح یک گیاه (سطوح مثبت) */
    public void setPlantLevel(PlantType plant, int level) {
        if (level < 0) level = 0;
        plantLevels.put(plant, level);
    }

    /** افزایش سطح یک گیاه به‌اندازهٔ یک واحد (و بازگشت سطح جدید) */
    public int upgradePlant(PlantType plant) {
        int newLevel = getPlantLevel(plant) + 1;
        plantLevels.put(plant, newLevel);
        return newLevel;
    }

    // ---------- بوست‌های گلخانه ----------

    /** آیا برای این گیاه بوست ذخیره‌شده وجود دارد؟ */
    public boolean hasGreenhouseBoost(PlantType plant) {
        return greenhouseBoosts.contains(plant);
    }

    /** افزودن بوست (حداکثر یک بوست برای هر گیاه نگه داشته می‌شود) */
    public void addGreenhouseBoost(PlantType plant) {
        greenhouseBoosts.add(plant);
    }

    /** مصرف بوست (و حذف آن) در صورت موجود بودن؛ true در صورت وجود بوست */
    public boolean consumeGreenhouseBoost(PlantType plant) {
        return greenhouseBoosts.remove(plant);
    }
}