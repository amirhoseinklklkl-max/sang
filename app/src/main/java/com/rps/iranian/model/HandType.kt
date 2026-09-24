package com.rps.iranian.model

/**
 * نوع حرکت دست: سنگ، کاغذ یا قیچی
 */
enum class HandType(val displayResId: Int) {
    ROCK(com.rps.iranian.R.string.rock),
    PAPER(com.rps.iranian.R.string.paper),
    SCISSORS(com.rps.iranian.R.string.scissors);

    /**
     * تعیین برنده بین دو حرکت
     * ROCK beats SCISSORS
     * SCISSORS beats PAPER
     * PAPER beats ROCK
     */
    fun beats(other: HandType): Boolean {
        return when (this) {
            ROCK -> other == SCISSORS
            PAPER -> other == ROCK
            SCISSORS -> other == PAPER
        }
    }

    companion object {
        fun random(): HandType = values()[(Math.random() * values().size).toInt()]
    }
}
