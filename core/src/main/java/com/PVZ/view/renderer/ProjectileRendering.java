package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pvz.libpvz.pam.ClipRef;
import java.util.List;
import java.util.Map;

final class ProjectileRendering {

    private ProjectileRendering() {}

    static boolean render(EntityRenderer er, SpriteBatch batch, String visualKey,
                          float time, float x, float y) {
        if (visualKey == null) return false;
        er.update();
        String clipName = clipName(visualKey);
        String cacheKey = visualKey + "#" + (clipName == null ? "<first>" : clipName);
        Map<String, ClipRef> cache = er.projectileClips();
        ClipRef clip = cache.get(cacheKey);
        if (clip == null) {
            if (Boolean.TRUE.equals(er.projectilePamFailed().get(cacheKey))) return false;
            String pamPath = com.PVZ.model.entity.plants.behavior.impl.ProjectileVisuals
                .getPath(visualKey);
            if (pamPath == null) {
                er.projectilePamFailed().put(cacheKey, true);
                return false;
            }
            try {
                er.getPamPlayer().loadSync(pamPath);
                List<String> available = er.getPamPlayer().clips(pamPath);
                String chosen = null;
                if (clipName != null && available.contains(clipName)) chosen = clipName;
                else if (!available.isEmpty()) chosen = available.get(0);
                if (chosen != null) clip = er.getPamPlayer().getClip(pamPath, chosen);
                if (clip == null) {
                    er.projectilePamFailed().put(cacheKey, true);
                    return false;
                }
                cache.put(cacheKey, clip);
            } catch (Exception e) {
                er.projectilePamFailed().put(cacheKey, true);
                return false;
            }
        }
        drawScaled(er, batch, clip, time, x, y, scale(visualKey));
        return true;
    }

    private static String clipName(String visualKey) {
        switch (visualKey) {
            case "CITRON": return "Citron_Citrus_Orb";
            case "CITRON_PF": return "Plantfood_Citron_Plasma_Orb";
            case "CAULIPOWER": return "animation";
            case "ELECTRIC_BLUEBERRY": return "attack";
            case "CACTUS": return "idle";
            case "FUME": return "special";
            case "STARFRUIT": case "STARFRUIT_PF": return "animation";
            case "BOWLING_BULB_1": case "BOWLING_BULB_2": case "BOWLING_BULB_3": return "animation";
            case "MEGA_GATLING": return "animation";
            case "ROTOBAGA_1": case "ROTOBAGA_2": return "animation";
            case "GRAPESHOT_FORWARD": return "animation_forward";
            case "GRAPESHOT_BACKWARD": return "animation_backward";
            case "GRAPESHOT_UP": return "animation_verticle_up";
            case "GRAPESHOT_DOWN": return "animation_verticle_down";
            case "GRAPESHOT": return "animation_forward";
            default: return null;
        }
    }

    private static float scale(String visualKey) {
        switch (visualKey) {
            case "CITRON": case "CITRON_PF": return 1.25f;
            case "CAULIPOWER": return 1.20f;
            case "ELECTRIC_BLUEBERRY": return 1.25f;
            case "CACTUS": return 0.85f;
            case "FUME": return 1.10f;
            case "STARFRUIT": return 0.95f;
            case "STARFRUIT_PF": return 1.05f;
            case "BOWLING_BULB_1": case "BOWLING_BULB_2": case "BOWLING_BULB_3": return 1.0f;
            case "MEGA_GATLING": return 0.95f;
            case "ROTOBAGA_1": case "ROTOBAGA_2": return 1.45f;
            case "GRAPESHOT_FORWARD": case "GRAPESHOT_BACKWARD":
            case "GRAPESHOT_UP": case "GRAPESHOT_DOWN": case "GRAPESHOT": return 1.25f;
            default: return 1.0f;
        }
    }

    private static void drawScaled(EntityRenderer er, SpriteBatch batch, ClipRef clip,
                                   float time, float x, float y, float scale) {
        er.getPamPlayer().draw(batch, clip, time, x, y, true);
    }
}
