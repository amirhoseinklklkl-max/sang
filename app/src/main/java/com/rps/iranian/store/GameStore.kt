package com.rps.iranian.store

import android.content.Context
import android.content.SharedPreferences
import com.rps.iranian.model.HandSkin

/**
 * مدیریت ذخیره‌سازی داده‌های بازی با SharedPreferences:
 * - تعداد سکه‌های کاربر
 * - اسکین‌های خریداری‌شده
 * - اسکین انتخاب‌شده فعلی
 * - آمار بازی (برد/باخت/مساوی)
 */
class GameStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val coins: Int
        get() = prefs.getInt(KEY_COINS, INITIAL_COINS)

    val selectedSkinId: Int
        get() = prefs.getInt(KEY_SELECTED_SKIN, HandSkin.BASIC.id)

    val wins: Int
        get() = prefs.getInt(KEY_WINS, 0)

    val losses: Int
        get() = prefs.getInt(KEY_LOSSES, 0)

    val draws: Int
        get() = prefs.getInt(KEY_DRAWS, 0)

    val selectedSkin: HandSkin
        get() = HandSkin.fromId(selectedSkinId)

    /**
     * بررسی اینکه آیا اسکین خاص خریداری شده یا خیر.
     * اسکین پایه همیشه مالک آن داریم.
     */
    fun isSkinOwned(skin: HandSkin): Boolean {
        if (skin == HandSkin.BASIC) return true
        return prefs.getBoolean("${KEY_OWNED_PREFIX}${skin.id}", false)
    }

    /**
     * خرید یک اسکین.
     * @return true اگر خرید موفق بود، false اگر سکه کافی نبود
     */
    fun buySkin(skin: HandSkin): Boolean {
        if (isSkinOwned(skin)) return true
        val currentCoins = coins
        if (currentCoins < skin.price) return false

        prefs.edit()
            .putInt(KEY_COINS, currentCoins - skin.price)
            .putBoolean("${KEY_OWNED_PREFIX}${skin.id}", true)
            .apply()
        return true
    }

    /**
     * انتخاب یک اسکین (اگر خریداری شده باشد).
     */
    fun selectSkin(skin: HandSkin): Boolean {
        if (!isSkinOwned(skin)) return false
        prefs.edit().putInt(KEY_SELECTED_SKIN, skin.id).apply()
        return true
    }

    /**
     * افزودن سکه برای پاداش بردن بازی.
     */
    fun addCoins(amount: Int) {
        prefs.edit().putInt(KEY_COINS, coins + amount).apply()
    }

    /**
     * ثبت نتیجه بازی در آمار.
     */
    fun recordResult(result: GameResult) {
        val editor = prefs.edit()
        when (result) {
            GameResult.WIN -> editor.putInt(KEY_WINS, wins + 1).putInt(KEY_COINS, coins + REWARD_WIN)
            GameResult.LOSE -> editor.putInt(KEY_LOSSES, losses + 1)
            GameResult.DRAW -> editor.putInt(KEY_DRAWS, draws + 1).putInt(KEY_COINS, coins + REWARD_DRAW)
        }
        editor.apply()
    }

    /**
     * ریست کامل پیشرفت (برای تست یا گزینه بازنشانی).
     */
    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "rps_iranian_prefs"
        private const val KEY_COINS = "coins"
        private const val KEY_SELECTED_SKIN = "selected_skin"
        private const val KEY_OWNED_PREFIX = "owned_skin_"
        private const val KEY_WINS = "wins"
        private const val KEY_LOSSES = "losses"
        private const val KEY_DRAWS = "draws"

        const val INITIAL_COINS = 1000
        const val REWARD_WIN = 50
        const val REWARD_DRAW = 5
    }
}

enum class GameResult {
    WIN, LOSE, DRAW
}
