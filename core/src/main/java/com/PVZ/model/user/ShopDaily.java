package com.PVZ.model.user;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.status.AppStatus;

import java.time.LocalDate;
import java.util.Set;

public class ShopDaily {
    private PlantType offerPlant;
    private LocalDate offerDate;
    private boolean purchased;

    public ShopDaily() {

        this.offerPlant = null;
        this.offerDate = null;
        this.purchased = false;
    }

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

    public boolean isForToday() {
        return offerDate != null && offerDate.equals(LocalDate.now()) && offerPlant != null;
    }

    public boolean isAvailableToday() {
        return isForToday() && !purchased;
    }

    public boolean needsNewOffer() {
        return offerDate == null || !offerDate.equals(LocalDate.now()) || offerPlant == null;
    }

    public boolean markAsPurchased() {
        if (isAvailableToday()) {
            purchased = true;
            return true;
        }
        return false;
    }

    public void generateOfferForToday(PlantType plant) {
        this.offerPlant = plant;
        this.offerDate = LocalDate.now();
        this.purchased = false;
    }

    public boolean generateIfNeeded() {
        if (!needsNewOffer()) {
            return false;
        }

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

        PlantType[] unlockedArray = unlocked.toArray(new PlantType[0]);
        PlantType randomPlant = unlockedArray[new java.util.Random().nextInt(unlockedArray.length)];

        generateOfferForToday(randomPlant);
        return true;
    }
}
