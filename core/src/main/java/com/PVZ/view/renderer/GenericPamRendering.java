package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import pvz.libpvz.pam.ClipRef;
import java.util.List;
import java.util.Map;

final class GenericPamRendering {

    private GenericPamRendering() {}

    static boolean render(EntityRenderer er, SpriteBatch batch, String pamPath,
                          String clipName, float time, float x, float y) {
        return renderInternal(er, batch, pamPath, clipName, time, x, y, 1f);
    }

    static boolean renderScaled(EntityRenderer er, SpriteBatch batch, String pamPath,
                                String clipName, float time, float x, float y, float scale) {
        if (scale <= 0f || scale == 1f) return renderInternal(er, batch, pamPath, clipName, time, x, y, 1f);
        Matrix4 old = batch.getTransformMatrix().cpy();
        batch.getTransformMatrix().translate(x, y, 0).scale(scale, scale, 1f).translate(-x, -y, 0);
        boolean res = renderInternal(er, batch, pamPath, clipName, time, x, y, 1f);
        batch.setTransformMatrix(old);
        return res;
    }

    private static boolean renderInternal(EntityRenderer er, SpriteBatch batch, String pamPath,
                                          String clipName, float time, float x, float y, float scale) {
        if (pamPath == null) return false;
        er.update();
        String cacheKey = clipName == null ? pamPath : pamPath + "#" + clipName;
        Map<String, ClipRef> cache = er.genericPamClips();
        ClipRef clip = cache.get(cacheKey);
        if (clip == null) {
            if (Boolean.TRUE.equals(er.genericPamFailed().get(cacheKey))) return false;
            try {
                er.getPamPlayer().loadSync(pamPath);
                List<String> available = er.getPamPlayer().clips(pamPath);
                String chosen = null;
                if (clipName != null && available.contains(clipName)) chosen = clipName;
                else if (!available.isEmpty()) chosen = available.get(0);
                if (chosen != null) clip = er.getPamPlayer().getClip(pamPath, chosen);
                if (clip == null) {
                    er.genericPamFailed().put(cacheKey, true);
                    return false;
                }
                cache.put(cacheKey, clip);
            } catch (Exception e) {
                er.genericPamFailed().put(cacheKey, true);
                return false;
            }
        }
        er.getPamPlayer().draw(batch, clip, time, x, y, true);
        return true;
    }
}
