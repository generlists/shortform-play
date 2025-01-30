package com.sean.ratel.player.core.domain.model.track

import java.util.TreeMap

data class VideoTrack(
    val id: String? = null,
    val bitrate: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Float? = null,
    val language: String? = null
) {

    /**
     * 높이가 존재하지 않는 경우 추정된 높이.
     */
    val heightMaybeEstimated: Int
        get() = height ?: estimatedHeightFromBitrate

    /**
     * 비트레이트로부터 추정된 높이 (품질).
     */
    val estimatedHeightFromBitrate: Int
        get() = estimatedHeightMap.floorEntry(bitrate)?.value ?: 0

    companion object {
        private val estimatedHeightMap = TreeMap<Int, Int>().apply {
            put(5_000_000, 1080)
            put(1_500_000, 720)
            put(1_000_000, 360)
            put(200_000, 270)
        }
    }
}