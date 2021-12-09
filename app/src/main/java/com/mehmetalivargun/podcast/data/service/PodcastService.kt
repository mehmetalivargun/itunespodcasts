package com.mehmetalivargun.podcast.data.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.mehmetalivargun.podcast.MainActivity
import com.mehmetalivargun.podcast.data.PodcastMediaSource
import com.mehmetalivargun.podcast.data.exoplayer.MediaPlaybackPreparer
import com.mehmetalivargun.podcast.data.exoplayer.MediaPlayerNotificationListener
import com.mehmetalivargun.podcast.data.exoplayer.MediaPlayerNotificationManager
import com.mehmetalivargun.podcast.data.exoplayer.MediaPlayerQueueNavigator
import com.mehmetalivargun.podcast.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class PodcastService : MediaBrowserServiceCompat() {
    @Inject
    lateinit var dataSourceFactory: CacheDataSource.Factory
    @Inject
    lateinit var mediaSource: PodcastMediaSource
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer
    private var isPlayerInitialized = false
    private lateinit var mediaPlayerNotificationManager: MediaPlayerNotificationManager
    var isForegroundService: Boolean = false
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var currentPlayingMedia: MediaMetadataCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaPlayerNotificationListener: MediaPlayerNotificationListener
    companion object {
        private const val TAG = "MediaPlayerService"

        var currentDuration: Long = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate called")
        val activityPendingIntent = Intent(this, MainActivity::class.java)
            .apply {
                action ="ACTION_PODCAST_NOTIFICATION_CLICK"
            }
            .let {
                PendingIntent.getActivity(
                    this,
                    0,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        mediaSession = MediaSessionCompat(baseContext, TAG).apply {
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            setSessionActivity(activityPendingIntent)

            isActive = true
        }

        val mediaPlaybackPreparer = MediaPlaybackPreparer(mediaSource) { mediaMetadata ->
            currentPlayingMedia = mediaMetadata
            preparePlayer(mediaSource.mediaMetadataEpisodes, mediaMetadata, true)
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(mediaPlaybackPreparer)
            setQueueNavigator(MediaPlayerQueueNavigator(mediaSession, mediaSource))
            setPlayer(exoPlayer)
        }

        this.sessionToken = mediaSession.sessionToken

        mediaPlayerNotificationManager = MediaPlayerNotificationManager(
            this,
            mediaSession.sessionToken,
            MediaPlayerNotificationListener(this)
        ) {
            currentDuration = exoPlayer.duration
        }
        mediaPlayerNotificationManager.showNotification(exoPlayer)
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
       return BrowserRoot("TUVESUFfUk9PVF9JRA",null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            "TUVESUFfUk9PVF9JRA"->{
                val resultSent= mediaSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(mediaSource.asMediaItems())
                        if(!isPlayerInitialized && mediaSource.mediaMetadataEpisodes.isNotEmpty()){
                            isPlayerInitialized=true
                        }
                    }else{
                        result.sendResult(null)
                    }
                }
                if(!resultSent){
                    result.detach()
                }
            }
            else->Unit
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.release()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        super.onCustomAction(action, extras, result)
        when (action) {
            Constants.START_MEDIA_PLAYBACK_ACTION -> {
                //mediaPlayerNotificationManager.showNotification(exoPlayer)
            }
            Constants.REFRESH_MEDIA_BROWSER_CHILDREN -> {
                mediaSource.refresh()
                notifyChildrenChanged(Constants.MEDIA_ROOT_ID)
            }
            else -> Unit
        }
    }

    private fun preparePlayer(
        mediaMetaData: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean
    ) {
        val indexToPlay = if (currentPlayingMedia == null) 0 else mediaMetaData.indexOf(itemToPlay)
        exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(indexToPlay, 0L)
        exoPlayer.playWhenReady = playWhenReady
    }
}