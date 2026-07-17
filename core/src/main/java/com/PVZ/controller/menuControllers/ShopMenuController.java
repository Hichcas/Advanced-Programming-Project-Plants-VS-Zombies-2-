
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.ShopDaily;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.ShopInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.Random;

public class ShopMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof ShopInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        if (AppStatus.getCurrentUser().shopDaily == null) {
            AppStatus.getCurrentUser().shopDaily = new ShopDaily();
        }
        // todo line above must be earased later.
        AppStatus.getCurrentUser().shopDaily.generateIfNeeded();
        return switch (dto.getCommand()) {
            case LIST -> new OutputDTO(true, shopList());
            case DAILY -> new OutputDTO(true, dailyOffer());
            case BUY -> buy(dto.getItemId(), dto.getCount(), dto.getPlantType());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGreenhouse();
            case COINS -> new OutputDTO(true, showCoins());
            case UNLOCK_ALL_PLANTS -> new OutputDTO(true, unlockAllPlants());
        };
    }

    private String unlockAllPlants () {
        AppStatus.getCurrentUser().collectionState.setAllPlantsUnlocked();
        return "all plants are unlocked now, boro halesho bebar.";
    }

    private String showCoins() {
        return "coins : " + AppStatus.getCurrentUser().userStats.getCoins() + " and diamonds :" + AppStatus.getCurrentUser().userStats.getDiamonds();
    }

    private String shopList() {
        return String.join("\n",
            "1) Pot (Price: 2000 Coins) - Unlocks a greenhouse slot (Max 20)",
            "2) Food Plant (Price: 3 Diamonds) - Max storage limit is 3",
            "3) Random Seed Pack (Price: 1000 Coins) - 5 Seed packs for a random unlocked plant",
            "4) Selected Seed Pack (Price: 5 Diamonds) - 10 Seed packs for a chosen unlocked plant",
            "5) Currency Exchange (5 Diamonds -> 500 Coins) - Convert Diamonds to Coins");
    }

    private String dailyOffer() {
        User user = AppStatus.currentUser;
        if (user == null) return "Not logged in.";

        // اگر پیشنهاد امروز وجود ندارد یا تاریخ گذشته است، خودکار تولید کن
        if (user.shopDaily.needsNewOffer()) {
            if (user.collectionState.getUnlockedPlants().isEmpty()) {
                return "No unlocked plants for a daily offer.";
            }
            PlantType[] unlocked = user.collectionState.getUnlockedPlants().toArray(new PlantType[0]);
            PlantType randomPlant = unlocked[new Random().nextInt(unlocked.length)];
            user.shopDaily.generateOfferForToday(randomPlant);
        }

        // حالا نمایش وضعیت
        if (!user.shopDaily.isForToday()) {
            return "No daily offer available (unexpected error).";
        }
        if (user.shopDaily.isPurchased()) {
            return "Today's offer (10 seed packets for " + user.shopDaily.getOfferPlant().getDisplayName()
                + " at 1600 coins) has already been purchased.";
        } else {
            return "Today's offer: 10 seed packets for " + user.shopDaily.getOfferPlant().getDisplayName()
                + " at 1600 coins (20% off). Use 'shop buy -i daily -n 1' to purchase.";
        }
    }

    private OutputDTO buy(String itemId, Integer count, String plantType) {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (itemId == null || count == null || count <= 0) {
            return new OutputDTO(false, "Invalid buy command.");
        }

        StringBuilder log = new StringBuilder();
        log.append("");

        switch (itemId.toLowerCase()) {
            case "pot" -> {
                int cost = 2000 * count;
                // 1. بررسی ظرفیت
                if (user.greenhouseState.getNumberOfLockedPots() < count) {
                    return new OutputDTO(false,
                        "Not enough locked pots. Max 20 total. You have " +
                            user.greenhouseState.getNumberOfLockedPots() + " locked pots.");
                }
                // 2. بررسی موجودی سکه
                if (user.userStats.getCoins() < cost) {
                    return new OutputDTO(false, "Not enough coins.");
                }
                // 3. انجام عملیات (کسر سکه + باز کردن گلدان)
                user.userStats.spendCoins(cost);
                user.greenhouseState.unlockPots(count);
                UserRegistry.markDirty(user.profile.getUsername());
                log.append("Bought ").append(count).append(" pot(s) for ").append(cost).append(" coins.");
                return new OutputDTO(true, log.toString());
            }

            case "food" -> {
//                int cost = 3 * count;
//                // 1. بررسی ظرفیت غذای گیاه (حداکثر ۳)
//                if (user.collectionState.getPlantFoodCount() + count > 3) {
//                    return new OutputDTO(false, "Plant food storage full (max 3).");
//                }
//                // 2. بررسی موجودی الماس
//                if (user.userStats.getDiamonds() < cost) {
//                    return new OutputDTO(false, "Not enough diamonds.");
//                }
//                // 3. کسر الماس و افزودن غذا
//                user.userStats.spendDiamonds(cost);
//                user.collectionState.addPlantFood(count);
//                log.append("Bought ").append(count).append(" plant food(s) for ").append(cost).append(" diamonds.");
                return new OutputDTO(true, log.toString());
            }

            case "random-seed" -> {
                // 1. بررسی وجود گیاه آنلاک‌شده
                java.util.Set<PlantType> unlockedSet = user.collectionState.getUnlockedPlants();
                if (unlockedSet.isEmpty()) {
                    return new OutputDTO(false, "No unlocked plants to receive seed packets.");
                }
                int cost = 1000 * count;
                // 2. بررسی موجودی سکه
                if (user.userStats.getCoins() < cost) {
                    return new OutputDTO(false, "Not enough coins.");
                }
                // 3. کسر سکه و توزیع بسته‌های تصادفی
                user.userStats.spendCoins(cost);
                PlantType[] unlocked = unlockedSet.toArray(new PlantType[0]);
                Random rand = new Random();
                log.append("Bought ").append(count).append(" random seed pack(s) for ").append(cost).append(" coins:\n");
                for (int i = 0; i < count; i++) {
                    PlantType randomPlant = unlocked[rand.nextInt(unlocked.length)];
                    user.collectionState.addSeedPackets(randomPlant, 5);
                    log.append(" - 5 seed packets for ").append(randomPlant.getDisplayName()).append("\n");
                }
                UserRegistry.markDirty(user.profile.getUsername());
                return new OutputDTO(true, log.toString().trim());
            }

            case "selected-seed" -> {
                // 1. بررسی الزامی بودن plantType
                if (plantType == null) {
                    return new OutputDTO(false, "Plant type is required (-t <plant_type>).");
                }
                PlantType selected;
                try {
                    selected = PlantType.valueof(plantType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return new OutputDTO(false, "Invalid plant type: " + plantType);
                }
                // 2. بررسی آنلاک بودن گیاه
                if (!user.collectionState.isPlantUnlocked(selected)) {
                    return new OutputDTO(false, "Plant " + plantType + " is not unlocked yet.");
                }
                int cost = 5 * count;
                // 3. بررسی موجودی الماس
                if (user.userStats.getDiamonds() < cost) {
                    return new OutputDTO(false, "Not enough diamonds.");
                }
                // 4. کسر الماس و افزودن بسته‌ها
                user.userStats.spendDiamonds(cost);
                int totalPackets = 10 * count;
                user.collectionState.addSeedPackets(selected, totalPackets);
                UserRegistry.markDirty(user.profile.getUsername());
                log.append("Bought ").append(count).append(" selected seed pack(s) for ")
                    .append(selected.getDisplayName()).append(" (").append(totalPackets)
                    .append(" packets) for ").append(cost).append(" diamonds.");
                return new OutputDTO(true, log.toString());
            }

            case "exchange" -> {
                int diamondsCost = 5 * count;
                // 1. بررسی موجودی الماس
                if (user.userStats.getDiamonds() < diamondsCost) {
                    return new OutputDTO(false, "Not enough diamonds.");
                }
                // 2. تبدیل
                user.userStats.spendDiamonds(diamondsCost);
                user.userStats.addCoins(500 * count);
                UserRegistry.markDirty(user.profile.getUsername());
                log.append("Exchanged ").append(diamondsCost).append(" diamonds for ")
                    .append(500 * count).append(" coins.");
                return new OutputDTO(true, log.toString());
            }

            case "daily" -> {
                // 1. بررسی پیشنهاد روزانه (موجود بودن و خریداری‌نشده)
                if (!user.shopDaily.isAvailableToday()) {
                    return new OutputDTO(false, "No available daily offer to purchase.");
                }
                int cost = 1600;
                // 2. بررسی موجودی سکه
                if (user.userStats.getCoins() < cost) {
                    return new OutputDTO(false, "Not enough coins.");
                }
                // 3. کسر سکه و اعمال خرید
                user.userStats.spendCoins(cost);
                user.collectionState.addSeedPackets(user.shopDaily.getOfferPlant(), 10);
                user.shopDaily.markAsPurchased();
                UserRegistry.markDirty(user.profile.getUsername());
                log.append("Purchased daily offer: 10 seed packets for ")
                    .append(user.shopDaily.getOfferPlant().name())
                    .append(" for ").append(cost).append(" coins.");
                return new OutputDTO(true, log.toString());
            }

            default -> {
                return new OutputDTO(false, "Unknown shop item.");
            }
        }
    }

    private OutputDTO exitToGreenhouse() {
        AppStatus.currentMenuType = MenuType.GREENHOUSE;
        return new OutputDTO(true, "Entered Greenhouse Menu.");
    }
}
