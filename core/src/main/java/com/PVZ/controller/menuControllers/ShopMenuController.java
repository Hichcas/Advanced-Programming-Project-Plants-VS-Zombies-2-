
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.ShopCommand;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.ShopInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class ShopMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof ShopInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case LIST -> new OutputDTO(true, shopList());
            case DAILY -> new OutputDTO(true, dailyOffer());
            case BUY -> buy(dto.getItemId(), dto.getCount(), dto.getPlantType());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGreenhouse();
        };
    }

    private String shopList() {
        return String.join("\n",
                "1) Pot",
                "2) Food Plant",
                "3) Random Seed Pack",
                "4) Selected Seed Pack",
                "5) Currency Exchange");
    }

    private String dailyOffer() {
        return "Daily offer is available.";
    }

    private OutputDTO buy(String itemId, Integer count, String plantType) {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (itemId == null || count == null || count <= 0) {
            return new OutputDTO(false, "Invalid buy command.");
        }
        switch (itemId.toLowerCase()) {
            case "pot" -> {
                int cost = 2000 * count;
                if (!user.userStats.spendCoins(cost)) {
                    return new OutputDTO(false, "Not enough coins.");
                }
                return new OutputDTO(true, "Bought pot(s).");
            }
            case "food" -> {
                int cost = 3 * count;
                if (!user.userStats.spendDiamonds(cost)) {
                    return new OutputDTO(false, "Not enough diamonds.");
                }
                return new OutputDTO(true, "Bought food plant(s).");
            }
            case "random-seed" -> {
                int cost = 1000 * count;
                if (!user.userStats.spendCoins(cost)) {
                    return new OutputDTO(false, "Not enough coins.");
                }
                return new OutputDTO(true, "Bought random seed pack(s).");
            }
            case "selected-seed" -> {
                if (plantType == null) {
                    return new OutputDTO(false, "Plant type is required.");
                }
                if (!user.userStats.spendDiamonds(5 * count)) {
                    return new OutputDTO(false, "Not enough diamonds.");
                }
                return new OutputDTO(true, "Bought selected seed pack(s) for " + plantType + ".");
            }
            case "exchange" -> {
                if (!user.userStats.spendDiamonds(5 * count)) {
                    return new OutputDTO(false, "Not enough diamonds.");
                }
                user.userStats.addCoins(500 * count);
                return new OutputDTO(true, "Currency exchanged.");
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
