package com.sean.ratel.player.core


import android.content.Context
import com.sean.player.utils.log.RLog


/**
 * Set the necessary data when initializing the player.
 * Set playback related data and logging related data.
 */
data class Configurations(
    val loggingConfig: LoggingConfiguration,
    val videoConfig: PlayConfiguration
)

/**
 * Set play Logging related data
 */
data class LoggingConfiguration(
    val enableAllLogger: Boolean? = false,

    val enableShowLogWithLinkToSource: Boolean? = false,

    val enableUdpLogger: Boolean? = false,

    val ipAddress: String? = null
)

/**
 * Set play related data
 */
data class PlayConfiguration(
    var bufferForPlaybackAfterRebufferMs: Int? = 2_000,
    var bufferForPlaybackMs: Int? = 24_0000,
    var maxBufferMs: Int? = 2_000,
    var minBufferMs: Int? = 2_000,
    var maximumVideoQuality: Int? = Int.MAX_VALUE
)

/**
 * Configure data required for playback.
 * Reference[com.tving.player.core.data.player.TExoPlayer.setPlayerConfig].
 */

private object PlayConfigurationBuilder {
    private var bufferForPlaybackAfterRebufferMs: Int? = 2_000
    private var bufferForPlaybackMs: Int? = 24_0000
    private var maxBufferMs: Int? = 2_000
    private var minBufferMs: Int? = 2_000
    private var maximumVideoQuality: Int? = Int.MAX_VALUE

    fun bufferForPlaybackAfterRebufferMs(bufferForPlaybackAfterRebufferMs: Int?) = apply {
        this.bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs
    }

    fun bufferForPlaybackMs(bufferForPlaybackMs: Int?) = apply {
        this.bufferForPlaybackMs = bufferForPlaybackMs
    }

    fun maxBufferMs(maxBufferMs: Int?) = apply {
        this.maxBufferMs = maxBufferMs
    }

    fun minBufferMs(minBufferMs: Int?) = apply {
        this.minBufferMs = minBufferMs
    }

    fun maximumVideoQuality(maximumVideoQuality: Int?) = apply {
        this.maximumVideoQuality = maximumVideoQuality
    }

    fun build() = PlayConfiguration(
        bufferForPlaybackAfterRebufferMs,
        bufferForPlaybackMs,
        maxBufferMs,
        minBufferMs,
        maximumVideoQuality
    )
}

private object LoggingConfigurationBuilder {

    private var enableAllLogger: Boolean = true
    private var enableShowLogWithLinkToSource: Boolean = false
    private var enableUdpLogger: Boolean = false
    private var ipAddress: String? = null

    fun enableAllLogger(enableAllLogger: Boolean) = apply {
        this.enableAllLogger = enableAllLogger
    }

    fun enableShowLogWithLinkToSource(enableShowLogWithLinkToSource: Boolean) = apply {
        this.enableShowLogWithLinkToSource = enableShowLogWithLinkToSource
    }

    fun enableUdpLogger(enableUdpLogger: Boolean) = apply {
        this.enableUdpLogger = enableUdpLogger
    }

    fun ipAddress(ipAddress: String?) = apply {
        this.ipAddress = ipAddress
    }

    fun build(context: Context) = RLog.init(
        context = context,
        enableAllLogger = enableAllLogger,
        enableShowLogWithLinkToSource = enableShowLogWithLinkToSource,
        enableUdpLogger = enableUdpLogger,
        ipAddress = ipAddress
    ).let {
        LoggingConfiguration(
            enableAllLogger = enableAllLogger,
            enableShowLogWithLinkToSource = enableShowLogWithLinkToSource,
            enableUdpLogger = enableUdpLogger,
            ipAddress = ipAddress
        )
    }
}

/**
 * play configurations Builder
 */
class ConfigurationBuilder(private val context: Context) {

    private var playConfiguration = PlayConfiguration()
    private var loggingConfiguration = LoggingConfiguration()

    fun playConfiguration(
        bufferForPlaybackAfterRebufferMs: Int?,
        bufferForPlaybackMs: Int?,
        maxBufferMs: Int?,
        minBufferMs: Int?,
        maximumVideoQuality: Int? = null
    ) = apply {
        playConfiguration = PlayConfigurationBuilder
            .bufferForPlaybackAfterRebufferMs(bufferForPlaybackAfterRebufferMs)
            .bufferForPlaybackMs(bufferForPlaybackMs)
            .maxBufferMs(maxBufferMs)
            .minBufferMs(minBufferMs)
            .maximumVideoQuality(maximumVideoQuality)
            .build()
    }

    fun loggingConfiguration(
        enableAllLogger: Boolean,
        enableShowLogWithLinkToSource: Boolean,
        enableUdpLogger: Boolean,
        ipAddress: String? = null

    ) = apply {
        loggingConfiguration =
            LoggingConfigurationBuilder
                .enableAllLogger(enableAllLogger)
                .enableShowLogWithLinkToSource(enableShowLogWithLinkToSource)
                .enableUdpLogger(enableUdpLogger)
                .ipAddress(ipAddress)
                .build(context)
    }

    fun build() = Configurations(loggingConfiguration, playConfiguration)
}

fun configurations(context: Context, lambda: ConfigurationBuilder.() -> Unit): Configurations {
    return ConfigurationBuilder(context).apply(lambda).build()
}
