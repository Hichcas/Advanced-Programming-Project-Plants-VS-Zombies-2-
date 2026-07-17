package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.greenhouse.GreenhouseState;
import com.PVZ.model.greenhouse.Pot;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.GreenhouseInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.Random;
import java.util.Set;

public class GreenhouseMenuController {

    private final Random random = new Random();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof GreenhouseInputDTO dto))
            return new OutputDTO(false, "Invalid input.");
        if (dto.getCommand() == null)
            return new OutputDTO(false, "Invalid Command.");
        return switch (dto.getCommand()) {
            case SHOW_GREENHOUSE -> showGreenhouse();
            case PLANT_POT_AT     -> plantPot(dto.getX(), dto.getY());
            case COLLECT          -> collect(dto.getX(), dto.getY());
            case GROW             -> grow(dto.getX(), dto.getY());
            case ENTER_SHOP       -> enterShop();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT             -> exitToGameMenu();
        };
    }

    private User currentUser() {
        return AppStatus.currentUser;
    }

    // ---------- نمایش گلخانه ----------
    private OutputDTO showGreenhouse() {
        User user = currentUser();
        if (user == null || user.greenhouseState == null)
            return new OutputDTO(false, "You must be logged in.");

        GreenhouseState state = user.greenhouseState;
        long now = System.currentTimeMillis();
        StringBuilder matrix = new StringBuilder();
        StringBuilder details = new StringBuilder();

        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 5; x++) {
                Pot pot = state.getPot(x, y);
                if (!pot.isUnlocked()) {
                    matrix.append("[X] ");
                } else if (pot.isEmpty()) {
                    matrix.append("[O] ");
                } else {
                    matrix.append("[●] ");
                    String name = pot.isMarigold() ? "Marigold" : pot.getPlantType().getDisplayName();
                    details.append(String.format("Pot (%d,%d): %s", x, y, name));
                    double remaining = state.getRemainingHours(x, y, now);
                    if (remaining <= 0) {
                        details.append(" - READY\n");
                    } else {
                        long sec = (long)(remaining * 3600);
                        long h = sec / 3600;
                        long m = (sec % 3600) / 60;
                        long s = sec % 60;
                        details.append(String.format(" - %dh %dm %ds remaining\n", h, m, s));
                    }
                }
            }
            matrix.append("\n");
        }

        String out = matrix.toString().trim();
        if (details.length() > 0)
            out += "\n\nDetails:\n" + details.toString().trim();
        return new OutputDTO(true, out);
    }

    // ---------- کاشت گیاه ----------
    private OutputDTO plantPot(int x, int y) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null || user.collectionState == null)
            return new OutputDTO(false, "You must be logged in.");

        try {
            Pot pot = user.greenhouseState.getPot(x, y);
            if (!pot.isReadyForPlanting())
                return new OutputDTO(false, "Cannot plant here. Pot is either locked or occupied.");

            long now = System.currentTimeMillis();
            Set<PlantType> unlocked = user.collectionState.getUnlockedPlants();

            // 50% marigold, یا اگر گیاه آنلاک‌شده‌ای وجود ندارد، قطعاً marigold
            if (unlocked.isEmpty() || random.nextDouble() < 0.5) {
                user.greenhouseState.plantMarigold(x, y, now);
                UserRegistry.markDirty(user.profile.getUsername());
                return new OutputDTO(true, "Planted Marigold in pot (" + x + "," + y + ").");
            } else {
                PlantType[] arr = unlocked.toArray(new PlantType[0]);
                PlantType chosen = arr[random.nextInt(arr.length)];
                user.greenhouseState.plantInPot(x, y, chosen, now);
                UserRegistry.markDirty(user.profile.getUsername());
                return new OutputDTO(true, String.format("Planted %s in pot (%d,%d).", chosen.getDisplayName(), x, y));
            }
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    // ---------- برداشت ----------
    private OutputDTO collect(int x, int y) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null || user.collectionState == null)
            return new OutputDTO(false, "You must be logged in.");

        long now = System.currentTimeMillis();
        try {
            Pot pot = user.greenhouseState.getPot(x, y);
            if (pot.isEmpty())
                return new OutputDTO(false, "Nothing to collect. Pot is empty.");
            if (!user.greenhouseState.isPlantReady(x, y, now))
                return new OutputDTO(false, "Plant is not ready yet. Use 'grow' to accelerate.");

            boolean isMarigold = user.greenhouseState.isMarigold(x, y);
            PlantType harvested = user.greenhouseState.collectFromPot(x, y);   // marigold → null

            if (isMarigold) {
                user.userStats.addCoins(500);
                UserRegistry.markDirty(user.profile.getUsername());
                return new OutputDTO(true, "Collected Marigold. +500 coins.");
            } else {
                // harvested حتماً یک گیاه آنلاک‌شده است
                if (user.collectionState.hasGreenhouseBoost(harvested)) {
                    UserRegistry.markDirty(user.profile.getUsername());
                    return new OutputDTO(true, String.format(
                        "Collected %s, but boost already stored. No additional boost added.",
                        harvested.getDisplayName()));
                } else {
                    user.collectionState.addGreenhouseBoost(harvested);
                    UserRegistry.markDirty(user.profile.getUsername());
                    return new OutputDTO(true, String.format(
                        "Collected %s. Greenhouse boost stored!", harvested.getDisplayName()));
                }
            }
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    // ---------- تسریع ----------
    private OutputDTO grow(int x, int y) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null || user.userStats == null)
            return new OutputDTO(false, "You must be logged in.");

        long now = System.currentTimeMillis();
        try {
            Pot pot = user.greenhouseState.getPot(x, y);
            if (pot.isEmpty())
                return new OutputDTO(false, "No plant in this pot to accelerate.");
            if (user.greenhouseState.isPlantReady(x, y, now))
                return new OutputDTO(false, "Plant is already ready to harvest.");

            double remainingHours = user.greenhouseState.getRemainingHours(x, y, now);
            int cost = (int) Math.ceil(remainingHours);
            if (user.userStats.getDiamonds() < cost) {
                return new OutputDTO(false, String.format(
                    "Not enough diamonds. Need %d diamonds but have %d.", cost, user.userStats.getDiamonds()));
            }
            user.userStats.spendDiamonds(cost);
            user.greenhouseState.accelerateGrowth(x, y);
            UserRegistry.markDirty(user.profile.getUsername());
            return new OutputDTO(true, String.format(
                "Growth accelerated for %d diamonds. Plant is now ready.", cost));
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    private OutputDTO enterShop() {
        AppStatus.currentMenuType = MenuType.SHOP;
        return new OutputDTO(true, "Entered Shop Menu.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Exited to Game Menu.");
    }
}
