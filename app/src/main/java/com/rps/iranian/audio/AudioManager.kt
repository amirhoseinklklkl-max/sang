package com.rps.iranian.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import com.rps.iranian.R

/**
 * مدیریت افکت‌های صوتی و موسیقی پس‌زمینه.
 * از SoundPool برای افکت‌ها (با تأخیر کم) و MediaPlayer برای موسیقی استفاده می‌کند.
 */
class AudioManager(private val context: Context) {

    private val soundPool: SoundPool
    private var bgMusic: android.media.MediaPlayer? = null
    private var soundEnabled = true
    private var musicEnabled = true

    private val soundIds = mutableMapOf<String, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundIds[SOUND_CLICK] = soundPool.load(context, R.raw.click, 1)
            soundIds[SOUND_COUNT] = soundPool.load(context, R.raw.count, 1)
            soundIds[SOUND_WIN] = soundPool.load(context, R.raw.win, 1)
            soundIds[SOUND_LOSE] = soundPool.load(context, R.raw.lose, 1)
            soundIds[SOUND_DRAW] = soundPool.load(context, R.raw.draw, 1)
            soundIds[SOUND_COIN] = soundPool.load(context, R.raw.coin, 1)
            soundIds[SOUND_SELECT] = soundPool.load(context, R.raw.select, 1)
            soundIds[SOUND_PURCHASE] = soundPool.load(context, R.raw.purchase, 1)
        } catch (e: Exception) {
            // اگر فایل‌های صوتی بارگذاری نشدند، بازی همچنان کار می‌کند
        }
    }

    fun playClick() = play(SOUND_CLICK)
    fun playCount() = play(SOUND_COUNT)
    fun playWin() = play(SOUND_WIN)
    fun playLose() = play(SOUND_LOSE)
    fun playDraw() = play(SOUND_DRAW)
    fun playCoin() = play(SOUND_COIN)
    fun playSelect() = play(SOUND_SELECT)
    fun playPurchase() = play(SOUND_PURCHASE)

    private fun play(name: String) {
        if (!soundEnabled) return
        val id = soundIds[name] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    /**
     * شروع موسیقی پس‌زمینه (لوپ).
     */
    fun startBackgroundMusic() {
        if (!musicEnabled) return
        try {
            if (bgMusic == null) {
                bgMusic = android.media.MediaPlayer.create(context, R.raw.bg_music)?.apply {
                    isLooping = true
                    setVolume(0.3f, 0.3f)
                }
            }
            bgMusic?.let {
                if (!it.isPlaying) it.start()
            }
        } catch (e: Exception) {
            // نادیده گرفتن خطا اگر فایل موسیقی وجود ندارد
        }
    }

    fun pauseBackgroundMusic() {
        try {
            bgMusic?.let {
                if (it.isPlaying) it.pause()
            }
        } catch (e: Exception) {}
    }

    fun resumeBackgroundMusic() {
        if (!musicEnabled) return
        try {
            bgMusic?.let {
                if (!it.isPlaying) it.start()
            }
        } catch (e: Exception) {}
    }

    fun stopBackgroundMusic() {
        try {
            bgMusic?.stop()
            bgMusic?.release()
            bgMusic = null
        } catch (e: Exception) {}
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) pauseBackgroundMusic() else resumeBackgroundMusic()
    }

    fun release() {
        soundPool.release()
        stopBackgroundMusic()
    }

    companion object {
        const val SOUND_CLICK = "click"
        const val SOUND_COUNT = "count"
        const val SOUND_WIN = "win"
        const val SOUND_LOSE = "lose"
        const val SOUND_DRAW = "draw"
        const val SOUND_COIN = "coin"
        const val SOUND_SELECT = "select"
        const val SOUND_PURCHASE = "purchase"
    }
}
