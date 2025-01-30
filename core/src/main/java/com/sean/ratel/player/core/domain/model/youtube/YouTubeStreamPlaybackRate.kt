package com.sean.ratel.player.core.domain.model.youtube

enum class YouTubeStreamPlaybackRate(val rate: Float) {
    UNKNOWN(rate = 1f), RATE_0_25(0.25f), RATE_0_5(0.5f), RATE_0_75(0.75f), RATE_1(1f), RATE_1_25(
        1.25f
    ),
    RATE_1_5(1.5f), RATE_1_75(1.75f), RATE_2(2f)
}