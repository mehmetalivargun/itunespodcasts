package com.mehmetalivargun.podcast.data

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.mehmetalivargun.podcast.data.model.Podcast
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import com.mehmetalivargun.podcast.repository.ITunesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastMediaSource @Inject constructor(

) {


    var mediaMetadataEpisodes: List<MediaMetadataCompat> = emptyList()
    var podcastEpisodes: List<RSSFeedResponse.EpisodeResponse> = emptyList()
    var episodes: List<RSSFeedResponse.EpisodeResponse> = emptyList()
        private set
    private val onReadyListeners = mutableListOf<OnReadyListener>()
    private val isReady: Boolean
        get() = state == PodcastState.INITIALIZED


    private var state: PodcastState =
        PodcastState.CREATED
        set(value) {
            if (value == PodcastState.INITIALIZED || value == PodcastState.ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(isReady)
                    }
                }
            } else {
                field = value
            }
        }


    fun setEpisodes(data: List<RSSFeedResponse.EpisodeResponse>) {
        state = PodcastState.INITIALIZING
        podcastEpisodes = data
        mediaMetadataEpisodes = data.map { episode->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, episode.guid)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, episode.url)
                .build()

        }
        state = PodcastState.INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        mediaMetadataEpisodes.forEach { metadata ->
            val mediaItem = MediaItem.fromUri(
                metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()
            )
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = mediaMetadataEpisodes.map { metadata ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(metadata.description.mediaId)
            .setTitle(metadata.description.title)
            .setSubtitle(metadata.description.subtitle)
            .setIconUri(metadata.description.iconUri)
            .setMediaUri(metadata.description.mediaUri)
            .build()
        MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    fun whenReady(listener: OnReadyListener): Boolean {
        return if (state == PodcastState.CREATED || state == PodcastState.INITIALIZING) {
            onReadyListeners += listener
            false
        } else {
            listener(isReady)
            true
        }
    }

    fun refresh() {
        onReadyListeners.clear()
        state = PodcastState.CREATED
    }
}


typealias OnReadyListener = (Boolean) -> Unit

enum class PodcastState {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    ERROR
}