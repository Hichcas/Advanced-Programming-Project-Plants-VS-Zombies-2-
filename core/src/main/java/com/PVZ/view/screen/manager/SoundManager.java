package com.PVZ.view.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {
    private static SoundManager instance;

    // هر مسیر می‌تونه چند نمونه Sound داشته باشه تا صداها با هم اورلپ بشن
    private final ObjectMap<String, List<Sound>> soundCache;
    private final ObjectMap<String, Integer> soundIndex;

    // حداکثر تعداد هم‌زمان برای هر افکت
    private static final int MAX_SIMULTANEOUS = 4;

    private boolean isMuted = false;
    private float masterVolume = 1f;

    private SoundManager() {
        soundCache = new ObjectMap<>();
        soundIndex = new ObjectMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private float getCalculatedVolume() {
        if (isMuted) return 0f;
        return masterVolume * masterVolume;
    }

    /**
     * پخش صدا با استخر نمونه‌ها.
     * اگه صدا تکراری و هم‌زمان پخش بشه، منتظر تموم شدن قبلی نمی‌مونه.
     */
    public void playSFX(String filePath) {
        if (isMuted) return;

        List<Sound> sounds = soundCache.get(filePath);
        if (sounds == null) {
            sounds = new ArrayList<>();
            soundCache.put(filePath, sounds);
            soundIndex.put(filePath, 0);
        }

        Sound sound;
        if (sounds.size() < MAX_SIMULTANEOUS) {
            try {
                sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
                sounds.add(sound);
            } catch (Exception e) {
                Gdx.app.error("SoundManager", "Error loading sound file: " + filePath, e);
                return;
            }
        } else {
            int idx = soundIndex.get(filePath);
            sound = sounds.get(idx);
            soundIndex.put(filePath, (idx + 1) % MAX_SIMULTANEOUS);
        }

        sound.play(getCalculatedVolume());
    }

    public void playSound(Sound sound) {
        if (isMuted || sound == null) return;
        sound.play(getCalculatedVolume());
    }

    public void setVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(volume, 1f));
        if (this.masterVolume > 0) isMuted = false;
    }

    public float getVolume() {
        return masterVolume;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public boolean getMute() {
        return isMuted;
    }

    public void dispose() {
        for (List<Sound> sounds : soundCache.values()) {
            for (Sound sound : sounds) {
                sound.dispose();
            }
        }
        soundCache.clear();
        soundIndex.clear();
        instance = null;
    }
}
