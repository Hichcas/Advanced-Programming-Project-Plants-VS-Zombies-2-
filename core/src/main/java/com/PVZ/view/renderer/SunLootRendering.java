package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.LootDrop;

final class SunLootRendering {

    private SunLootRendering() {}

    static boolean renderSun(EntityRenderer er, SpriteBatch batch, Sun.SunType type,
                             float animationTime, boolean falling,
                             boolean reachedGround, float x, float y) {
        String pamPath = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
        String clip = "animation";
        float localTime = animationTime;
        if (type == Sun.SunType.SPECIAL && falling && !reachedGround) {
            if (animationTime < 0.5333f) clip = "transition_blue";
            else { clip = "blue"; localTime = animationTime - 0.5333f; }
        } else if (type == Sun.SunType.RADIOACTIVE && falling && !reachedGround) {
            if (animationTime < 0.5333f) clip = "transition_red";
            else { clip = "red"; localTime = animationTime - 0.5333f; }
        }
        return er.renderPam(batch, pamPath, clip, localTime, x, y);
    }

    static boolean renderLoot(EntityRenderer er, SpriteBatch batch,
                              LootDrop.LootType type, float animationTime,
                              float x, float y) {
        if (type == null) return false;
        String pamPath, clip;
        switch (type) {
            case COIN: pamPath = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM"; clip = "animation"; break;
            case DIAMOND: pamPath = "768/INITIAL/EFFECTS/TUTORIAL_DIAMOND/TUTORIAL_DIAMOND.PAM"; clip = "idle"; break;
            case PLANT_FOOD: pamPath = "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM"; clip = "idle"; break;
            default: return false;
        }
        return er.renderPam(batch, pamPath, clip, animationTime, x, y);
    }
}
