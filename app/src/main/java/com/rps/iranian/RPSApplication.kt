package com.rps.iranian

import android.app.Application
import com.rps.iranian.audio.AudioManager

/**
 * کلاس Application برای مدیریت نمونه‌ی سراسری AudioManager.
 * موسیقی پس‌زمینه در سراسر چرخه حیات اکتیویتی‌ها پابرجا می‌ماند.
 */
class RPSApplication : Application() {

    lateinit var audioManager: AudioManager
        private set

    override fun onCreate() {
        super.onCreate()
        audioManager = AudioManager(this)
        audioManager.startBackgroundMusic()
    }
}
