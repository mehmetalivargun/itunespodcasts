package com.mehmetalivargun.podcast.data.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mehmetalivargun.podcast.data.PodcastMediaSource
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import com.mehmetalivargun.podcast.util.Constants


class MediaPlayerServiceConnection(
    context: Context,
    private val mediaSource: PodcastMediaSource,
) {

    var playbackState = MutableLiveData<PlaybackStateCompat?>(null)
    var currentPlayingEpisode = MutableLiveData<RSSFeedResponse.EpisodeResponse?>(null)

    lateinit var mediaController: MediaControllerCompat

    private var isConnected: Boolean = false

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, PodcastService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    fun playPodcast(episodes: List<RSSFeedResponse.EpisodeResponse>) {
        mediaSource.setEpisodes(episodes)
        mediaBrowser.sendCustomAction(Constants.START_MEDIA_PLAYBACK_ACTION, null, null)
    }

    fun fastForward(seconds: Int = 10) {
        playbackState.value?.position?.let { currentPosition ->
            transportControls.seekTo(currentPosition + seconds * 1000)
        }
    }

    fun rewind(seconds: Int = 10) {
        playbackState.value?.position?.let { currentPosition ->
            transportControls.seekTo(currentPosition - seconds * 1000)
        }
    }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun refreshMediaBrowserChildren() {
        mediaBrowser.sendCustomAction(Constants.REFRESH_MEDIA_BROWSER_CHILDREN, null, null)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            isConnected = true
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            isConnected = false
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            isConnected = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            currentPlayingEpisode.value = metadata?.let {
                mediaSource.podcastEpisodes.find {
                    it.guid == metadata.description?.mediaId
                }
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}