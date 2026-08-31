package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pvz.libpvz.pam.ClipRef;
import java.util.List;
import java.util.Map;

final class PlantRendering {

    private PlantRendering() {}

    static ClipRef getClip(EntityRenderer er, String plantType, String state) {
        if (plantType == null || state == null) return null;
        String key = plantType + "#" + state;
        Map<String, ClipRef> cache = er.plantClips();
        if (cache.containsKey(key)) return cache.get(key);
        if (Boolean.TRUE.equals(er.plantPamFailed().get(plantType))) return null;

        String pamPath = com.PVZ.model.entity.PlantTexturePaths.getPamPath(plantType);
        if (pamPath == null) {
            er.plantPamFailed().put(plantType, true);
            return null;
        }
        try {
            er.getPamPlayer().loadSync(pamPath);
            String resolved = com.PVZ.model.entity.PamAnimationCatalog
                .resolveClip(pamPath, state);
            ClipRef clip = resolved != null ? er.getPamPlayer().getClip(pamPath, resolved) : null;
            if (clip == null) {
                List<String> available = er.getPamPlayer().clips(pamPath);
                if (available != null && !available.isEmpty()) {
                    for (String name : available) {
                        if (name.toLowerCase().contains(state.toLowerCase())) {
                            clip = er.getPamPlayer().getClip(pamPath, name);
                            if (clip != null) break;
                        }
                    }
                    if (clip == null) clip = er.getPamPlayer().getClip(pamPath, available.get(0));
                }
            }
            if (clip != null) cache.put(key, clip);
            else er.plantPamFailed().put(plantType, true);
            return clip;
        } catch (Exception e) {
            er.plantPamFailed().put(plantType, true);
            return null;
        }
    }

    static ClipRef getClipExact(EntityRenderer er, String plantType, String exactClip) {
        if (plantType == null || exactClip == null) return null;
        String key = plantType + "#exact#" + exactClip;
        Map<String, ClipRef> cache = er.plantClips();
        if (cache.containsKey(key)) return cache.get(key);
        String pamPath = com.PVZ.model.entity.PlantTexturePaths.getPamPath(plantType);
        if (pamPath == null) return null;
        try {
            er.getPamPlayer().loadSync(pamPath);
            ClipRef clip = er.getPamPlayer().getClip(pamPath, exactClip);
            cache.put(key, clip);
            return clip;
        } catch (Exception e) {
            return null;
        }
    }

    static boolean renderExact(EntityRenderer er, SpriteBatch batch, String plantType,
                               String exactClip, float time, float x, float y) {
        er.update();
        ClipRef clip = getClipExact(er, plantType, exactClip);
        if (clip == null) return false;
        drawScaled(er, batch, clip, time, x, y, scaleFor(plantType));
        return true;
    }

    static boolean render(EntityRenderer er, SpriteBatch batch, String plantType,
                          String state, float time, float x, float y) {
        er.update();
        ClipRef clip = getClip(er, plantType, state);
        if (clip == null) return false;
        drawScaled(er, batch, clip, time, x, y, scaleFor(plantType));
        return true;
    }

    private static float scaleFor(String plantType) {
        if ("THREEPEATER".equalsIgnoreCase(plantType)) return 1.35f;
        return 1f;
    }

    private static void drawScaled(EntityRenderer er, SpriteBatch batch, ClipRef clip,
                                   float time, float x, float y, float scale) {
        er.getPamPlayer().draw(batch, clip, time, x, y, true);
    }
}
