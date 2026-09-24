package com.rps.iranian

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rps.iranian.audio.AudioManager
import com.rps.iranian.model.HandSkin
import com.rps.iranian.model.HandType
import com.rps.iranian.store.GameStore
import com.rps.iranian.view.HandView
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var store: GameStore
    private lateinit var audio: AudioManager
    private lateinit var handPreview: HandView
    private lateinit var tvCoins: TextView
    private lateinit var tvStats: TextView

    private var previewIndex = 0
    private val previewSequence = listOf(HandType.ROCK, HandType.PAPER, HandType.SCISSORS)

    private val cycleHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val cycleRunnable = object : Runnable {
        override fun run() {
            previewIndex = (previewIndex + 1) % previewSequence.size
            handPreview.setHandType(previewSequence[previewIndex])
            handPreview.revealAnimation()
            cycleHandler.postDelayed(this, 1500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = GameStore(this)
        audio = (applicationContext as? RPSApplication)?.audioManager ?: AudioManager(this)

        bindViews()
        setupClickListeners()
        startPreviewCycle()
    }

    private fun bindViews() {
        handPreview = findViewById(R.id.handPreview)
        tvCoins = findViewById(R.id.tvCoins)
        tvStats = findViewById(R.id.tvStats)

        // استفاده از اسکین انتخاب‌شده برای پیش‌نمایش
        handPreview.setSkin(store.selectedSkin)
        handPreview.setHandType(HandType.ROCK)
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            audio.playClick()
            startActivity(Intent(this, GameActivity::class.java))
            overridePendingTransition(R.anim.slide_up_fade, R.anim.slide_up_fade)
        }

        findViewById<Button>(R.id.btnShop).setOnClickListener {
            audio.playClick()
            startActivity(Intent(this, ShopActivity::class.java))
            overridePendingTransition(R.anim.slide_up_fade, R.anim.slide_up_fade)
        }

        findViewById<Button>(R.id.btnExit).setOnClickListener {
            audio.playClick()
            showExitConfirm()
        }
    }

    private fun showExitConfirm() {
        AlertDialog.Builder(this)
            .setMessage(R.string.exit_confirm)
            .setPositiveButton(R.string.exit_yes) { _, _ ->
                audio.stopBackgroundMusic()
                finishAffinity()
            }
            .setNegativeButton(R.string.exit_no, null)
            .show()
    }

    private fun startPreviewCycle() {
        cycleHandler.postDelayed(cycleRunnable, 2000)
    }

    private fun stopPreviewCycle() {
        cycleHandler.removeCallbacks(cycleRunnable)
    }

    override fun onResume() {
        super.onResume()
        updateCoinsDisplay()
        updateStatsDisplay()
        handPreview.setSkin(store.selectedSkin)
        audio.resumeBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        stopPreviewCycle()
        audio.pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPreviewCycle()
    }

    private fun updateCoinsDisplay() {
        val formatted = NumberFormat.getNumberInstance(Locale("fa", "IR")).format(store.coins)
        tvCoins.text = formatted
    }

    private fun updateStatsDisplay() {
        val fmt = NumberFormat.getNumberInstance(Locale("fa", "IR"))
        tvStats.text = "برد: ${fmt.format(store.wins)} | باخت: ${fmt.format(store.losses)} | مساوی: ${fmt.format(store.draws)}"
    }
}
