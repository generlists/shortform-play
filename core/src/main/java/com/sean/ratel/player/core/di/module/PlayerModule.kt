package com.sean.ratel.player.core.di.module

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.player.core.data.player.media.MediaExoStreamPlayer
import com.sean.ratel.player.core.domain.MediaStreamPlayer
import com.sean.ratel.player.core.domain.api.UserAgentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityRetainedComponent::class) // ActivityComponent로 변경
object PlayerModule {

    @OptIn(UnstableApi::class)
    @Provides
    fun provideTExoPlayer(
        @ApplicationContext context: Context,
        userAgentProvider: UserAgentProvider,
    ): MediaStreamPlayer {
        return MediaExoStreamPlayer(context, userAgentProvider)
    }

    @Provides
    fun provideIFramePlayerOption(
    ): IFramePlayerOptions {
        return IFramePlayerOptions.Builder().controls(0).build()
    }
    @Provides
    fun provideYouTubePlayerTracker(
    ): YouTubePlayerTracker {
        return YouTubePlayerTracker()
    }

}
