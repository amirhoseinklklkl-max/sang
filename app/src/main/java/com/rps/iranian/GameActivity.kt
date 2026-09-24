package com.rps.iranian

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rps.iranian.audio.AudioManager
import com.rps.iranian.model.GameAI
import com.rps.iranian.model.HandType
import com.rps.iranian.model.HandSkin
import com.rps.iranian.store.GameResult
import com.rps.iranian.store.GameStore
import com.rps.iranian.view.HandView
import java.text.NumberFormat
import java.util.Locale

class GameActivity : AppCompatActivity() {

    private lateinit var store: GameStore
    private lateinit var audio: AudioManager
    private lateinit var ai: GameAI

    private lateinit var playerHand: HandView
    private lateinit var aiHand: HandView
    private lateinit var tvPlayerChoice: TextView
    private lateinit var tvAiChoice: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var tvCoins: TextView
    private lateinit var btnRock: Button
    private lateinit var btnPaper: Button
    private lateinit var btnScissors: Button

    private var countdownTimer: CountDownTimer? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        store = GameStore(this)
        audio = (applicationContext as? RPSApplication)?.audioManager ?: AudioManager(this)
        ai = GameAI()

        bindViews()
        setupClickListeners()
        updateCoinsDisplay()
        resetHands()
    }

    private fun bindViews() {
        playerHand = findViewById(R.id.playerHand)
        aiHand = findViewById(R.id.aiHand)
        tvPlayerChoice = findViewById(R.id.tvPlayerChoice)
        tvAiChoice = findViewById(R.id.tvAiChoice)
        tvResult = findViewById(R.id.tvResult)
        tvCountdown = findViewById(R.id.tvCountdown)
        tvCoins = findViewById(R.id.tvCoins)
        btnRock = findViewById(R.id.btnRock)
        btnPaper = findViewById(R.id.btnPaper)
        btnScissors = findViewById(R.id.btnScissors)

        // اسکین انتخاب‌شده برای بازیکن، اسکین پایه برای حریف
        playerHand.setSkin(store.selectedSkin)
        playerHand.setHandType(HandType.ROCK)
        playerHand.setMirrored(false)

        aiHand.setSkin(HandSkin.BASIC)
        aiHand.setHandType(HandType.ROCK)
        aiHand.setMirrored(true)  // دست حریف آینه می‌شود
    }

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            audio.playClick()
            finish()
        }

        btnRock.setOnClickListener { if (!isPlaying) startRound(HandType.ROCK) }
        btnPaper.setOnClickListener { if (!isPlaying) startRound(HandType.PAPER) }
        btnScissors.setOnClickListener { if (!isPlaying) startRound(HandType.SCISSORS) }
    }

    private fun resetHands() {
        playerHand.setHandType(HandType.ROCK)
        aiHand.setHandType(HandType.ROCK)
        tvPlayerChoice.text = ""
        tvAiChoice.text = ""
        tvResult.visibility = View.GONE
        enableButtons(true)
    }

    private fun enableButtons(enabled: Boolean) {
        btnRock.isEnabled = enabled
        btnPaper.isEnabled = enabled
        btnScissors.isEnabled = enabled
        btnRock.alpha = if (enabled) 1f else 0.5f
        btnPaper.alpha = if (enabled) 1f else 0.5f
        btnScissors.alpha = if (enabled) 1f else 0.5f
    }

    /**
     * شروع یک دور بازی پس از انتخاب بازیکن.
     * ۱. هر دو دست مشت می‌شوند و انیمیشن مچ زدن شروع می‌شود.
     * ۲. شمارش معکوس: ۳، ۲، ۱ (با افکت صوتی).
     * ۳. نمایش دست بازیکن و حرکت هوشمند حریف.
     * ۴. تعیین برنده و اعطای پاداش.
     */
    private fun startRound(playerMove: HandType) {
        isPlaying = true
        enableButtons(false)
        audio.playSelect()

        // پنهان کردن نتیجه قبلی
        tvResult.visibility = View.GONE

        // هر دو دست به حالت سنگ (مشت) برمی‌گردند
        playerHand.setHandType(HandType.ROCK)
        aiHand.setHandType(HandType.ROCK)
        playerHand.startShake()
        aiHand.startShake()

        tvPlayerChoice.text = ""
        tvAiChoice.text = ""

        // شمارش معکوس: سه → دو → یک (هر کدام ۶۰۰ms)
        tvCountdown.visibility = View.VISIBLE
        val countTexts = arrayOf(
            getString(R.string.count_three),
            getString(R.string.count_two),
            getString(R.string.count_one)
        )
        countdownTimer = object : CountDownTimer(2100, 700) {
            private var tickIndex = 0
            override fun onTick(millisUntilFinished: Long) {
                if (tickIndex < countTexts.size) {
                    tvCountdown.text = countTexts[tickIndex]
                    tvCountdown.startAnimation(
                        AnimationUtils.loadAnimation(this@GameActivity, R.anim.count_pop)
                    )
                    audio.playCount()
                    tickIndex++
                }
            }

            override fun onFinish() {
                tvCountdown.visibility = View.GONE
                revealHands(playerMove)
            }
        }.start()
    }

    private fun revealHands(playerMove: HandType) {
        // توقف انیمیشن مچ زدن
        playerHand.stopShake()
        aiHand.stopShake()

        // حرکت هوشمند حریف
        val aiMove = ai.chooseMove()
        ai.recordPlayerMove(playerMove)

        // نمایش دست‌ها
        playerHand.setHandType(playerMove)
        aiHand.setHandType(aiMove)
        playerHand.revealAnimation()
        aiHand.revealAnimation()

        // نمایش برچسب انتخاب
        tvPlayerChoice.text = getString(playerMove.displayResId)
        tvAiChoice.text = getString(aiMove.displayResId)

        // تعیین نتیجه بعد از ۴۰۰ms برای دیدن انیمیشن
        tvResult.postDelayed({
            determineResult(playerMove, aiMove)
        }, 400)
    }

    private fun determineResult(playerMove: HandType, aiMove: HandType) {
        val result: GameResult
        val resultText: String
        val resultBg: Int

        when {
            playerMove == aiMove -> {
                result = GameResult.DRAW
                resultText = getString(R.string.result_draw) + "\n+" + persianNum(GameStore.REWARD_DRAW) + " " + getString(R.string.coins_label)
                resultBg = R.drawable.result_banner_draw
                audio.playDraw()
            }
            playerMove.beats(aiMove) -> {
                result = GameResult.WIN
                resultText = getString(R.string.result_win) + "\n+" + persianNum(GameStore.REWARD_WIN) + " " + getString(R.string.coins_label)
                resultBg = R.drawable.result_banner_win
                audio.playWin()
                audio.playCoin()
            }
            else -> {
                result = GameResult.LOSE
                resultText = getString(R.string.result_lose)
                resultBg = R.drawable.result_banner_lose
                audio.playLose()
            }
        }

        store.recordResult(result)
        tvResult.text = resultText
        tvResult.background = ContextCompat.getDrawable(this, resultBg)
        tvResult.visibility = View.VISIBLE
        tvResult.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))

        updateCoinsDisplay()

        // اجازه بازی مجدد بعد از ۱.۵ ثانیه
        tvResult.postDelayed({
            isPlaying = false
            resetHands()
        }, 1500)
    }

    private fun updateCoinsDisplay() {
        val formatted = NumberFormat.getNumberInstance(Locale("fa", "IR")).format(store.coins)
        tvCoins.text = formatted
    }

    private fun persianNum(n: Int): String {
        return NumberFormat.getNumberInstance(Locale("fa", "IR")).format(n)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        countdownTimer?.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        playerHand.stopShake()
        aiHand.stopShake()
    }
}
