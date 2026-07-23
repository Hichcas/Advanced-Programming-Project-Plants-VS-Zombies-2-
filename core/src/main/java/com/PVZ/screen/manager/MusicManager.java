package com.PVZ.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicManager {
    private static MusicManager instance;

    private Music currentMusic;
    private Music previousMusic;

    private boolean isMuted = false;
    private float masterVolume = 0.6f;
    private String currentTrackName = "";
    private enum FadeState {
        NONE, FADE_IN, CROSS_FADE
    }

    private FadeState fadeState = FadeState.NONE;

    private float fadeTimer = 0f;
    private float fadeInDuration = 3.0f;
    private float crossFadeDuration = 1.5f;
    private float prevMusicVolumeStart = 0f;

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void update(float delta) {
        if (fadeState == FadeState.NONE)
            return;

        fadeTimer += delta;

        if (fadeState == FadeState.FADE_IN) {
            float progress = fadeTimer / fadeInDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                fadeState = FadeState.NONE;
            }

            if (currentMusic != null) {
                float targetVol = progress * masterVolume;
                currentMusic.setVolume(isMuted ? 0f : targetVol);
            }

        } else if (fadeState == FadeState.CROSS_FADE) {
            float progress = fadeTimer / crossFadeDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                fadeState = FadeState.NONE;
                safelyDisposePrevious();
            }

            if (previousMusic != null) {
                float prevVol = (1.0f - progress) * prevMusicVolumeStart;
                previousMusic.setVolume(isMuted ? 0f : prevVol);
            }

            if (currentMusic != null) {
                float targetVol = progress * masterVolume;
                currentMusic.setVolume(isMuted ? 0f : targetVol);
            }
        }
    }

    public void playMusic(String fileName) {
        if (currentTrackName.equals(fileName) && currentMusic != null) {
            if (!currentMusic.isPlaying()) {
                currentMusic.play();
            }
            return;
        }

        if (currentMusic != null) {
            safelyDisposePrevious();

            previousMusic = currentMusic;
            prevMusicVolumeStart = previousMusic.getVolume();

            fadeState = FadeState.CROSS_FADE;
        } else {
            fadeState = FadeState.FADE_IN;
        }

        try {
            currentTrackName = fileName;
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(fileName));
            currentMusic.setLooping(true);
            currentMusic.setVolume(0f);
            currentMusic.play();

            fadeTimer = 0f;
        } catch (Exception e) {
            Gdx.app.error("MusicManager", "Error playing music file: " + fileName, e);
            fadeState = FadeState.NONE;
        }
    }


    public void setVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(volume, 1f));
        if (fadeState == FadeState.NONE && currentMusic != null) {
            currentMusic.setVolume(isMuted ? 0f : masterVolume);
        }
    }

    public float getVolume() {
        return masterVolume;
    }

    public void setMuted(boolean mute) {
        this.isMuted = mute;

        if (currentMusic != null) {
            if (fadeState == FadeState.NONE) {
                currentMusic.setVolume(mute ? 0f : masterVolume);
            } else {
                if (mute)
                    currentMusic.setVolume(0f);
            }
        }

        if (previousMusic != null && mute) {
            previousMusic.setVolume(0f);
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void stopMusic() {
        fadeState = FadeState.NONE;
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        safelyDisposePrevious();
        currentTrackName = "";
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
        if (previousMusic != null && previousMusic.isPlaying()) {
            previousMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
        if (previousMusic != null && !previousMusic.isPlaying()) {
            previousMusic.play();
        }
    }

    private void safelyDisposePrevious() {
        if (previousMusic != null) {
            previousMusic.stop();
            previousMusic.dispose();
            previousMusic = null;
        }
    }

    public void dispose() {
        stopMusic();
    }

    public boolean getMute() {
        return isMuted;
    }
}
