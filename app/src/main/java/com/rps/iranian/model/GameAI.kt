package com.rps.iranian.model

/**
 * هوش مصنوعی سبک برای حریف کامپیوتری.
 * الگوی حرکات بازیکن رو یاد می‌گیره و بر اساس اون بهترین حرکت رو انتخاب می‌کنه.
 *
 * استراتژی:
 * - 30٪ مواقع کاملا تصادفی (برای غیرقابل پیش‌بینی بودن)
 * - 70٪ مواقع بر اساس آخرین N حرکت بازیکن، حرکتی که می‌تواند او را شکست دهد انتخاب می‌کند.
 */
class GameAI {

    private val history = mutableListOf<HandType>()
    private val maxHistorySize = 10

    /**
     * ثبت حرکت بازیکن برای یادگیری
     */
    fun recordPlayerMove(move: HandType) {
        history.add(move)
        if (history.size > maxHistorySize) {
            history.removeAt(0)
        }
    }

    /**
     * پیش‌بینی حرکت بعدی بازیکن و انتخاب حرکت متقابل.
     */
    fun chooseMove(): HandType {
        // 30% کاملاً تصادفی
        if (history.isEmpty() || Math.random() < 0.3) {
            return HandType.random()
        }

        // پیش‌بینی بر اساس فراوانی حرکات قبلی بازیکن
        val predictedMove = predictNextMove()

        // انتخاب حرکتی که می‌تواند پیش‌بینی رو شکست بده
        return counterMove(predictedMove)
    }

    /**
     * پیش‌بینی حرکت بعدی بازیکن:
     * - اگر ۳ حرکت اخیر یکسان بوده، احتمال تکرار زیاد است
     * - در غیر این صورت، پرتکرارترین حرکت رو برمی‌گردانیم
     */
    private fun predictNextMove(): HandType {
        if (history.size >= 3) {
            val lastThree = history.takeLast(3)
            if (lastThree.distinct().size == 1) {
                return lastThree[0]
            }
        }

        // شمارش فراوانی
        val rockCount = history.count { it == HandType.ROCK }
        val paperCount = history.count { it == HandType.PAPER }
        val scissorsCount = history.count { it == HandType.SCISSORS }

        return when {
            rockCount >= paperCount && rockCount >= scissorsCount -> HandType.ROCK
            paperCount >= scissorsCount -> HandType.PAPER
            else -> HandType.SCISSORS
        }
    }

    /**
     * حرکتی که حرکت داده شده رو شکست می‌دهد:
     * - ROCK → PAPER (کاغذ سنگ را می‌پوشاند)
     * - PAPER → SCISSORS (قیچی کاغذ را می‌برد)
     * - SCISSORS → ROCK (سنگ قیچی را خرد می‌کند)
     */
    private fun counterMove(move: HandType): HandType {
        return when (move) {
            HandType.ROCK -> HandType.PAPER
            HandType.PAPER -> HandType.SCISSORS
            HandType.SCISSORS -> HandType.ROCK
        }
    }

    fun reset() {
        history.clear()
    }
}
