package com.mehmetalivargun.podcast.viewmodel

import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetalivargun.podcast.data.model.Podcast
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import com.mehmetalivargun.podcast.data.service.MediaPlayerServiceConnection
import com.mehmetalivargun.podcast.remote.ITunesService
import com.mehmetalivargun.podcast.remote.RssService
import com.mehmetalivargun.podcast.repository.ITunesRepo
import com.mehmetalivargun.podcast.util.isPlaying
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcastDetailViewModel @Inject constructor(
    private val serviceConnection: MediaPlayerServiceConnection,
    private val repository: ITunesRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val id: Int? = savedStateHandle["podcastID"]
    private val rssService = RssService()
    val podcast: MutableLiveData<List<Podcast>> by lazy {
        MutableLiveData<List<Podcast>>()
    }
    val episodes: MutableLiveData<RSSFeedResponse> by lazy {
        MutableLiveData<RSSFeedResponse>()
    }

    val currentPlayingEpisode = serviceConnection.currentPlayingEpisode
    private val playbackState = serviceConnection.playbackState

    val podcastIsPlaying: Boolean
        get() = playbackState.value?.isPlaying == true

    init {
        getEpisodes(id)
    }

    private fun getEpisodes(id: Int?) = viewModelScope.launch {
        if (id != null) {
            repository.getLookupResult(id).collect {
                when (it) {
                    is ITunesRepo.PodcastResult.Succes -> onSucces(it.response.podcasts)
                    is ITunesRepo.PodcastResult.Loading -> onLoading()
                    is ITunesRepo.PodcastResult.Failure -> onFailure()
                }
            }
        }
    }



    private fun onFailure() {
    }

    private fun onLoading() {

    }

    private fun onSucces(list: List<Podcast>) {
        podcast.postValue(list)
        var feed : RSSFeedResponse? = null
        viewModelScope.launch {
            list[0].let {
                var feedUrl = ""
                feedUrl = if (it.feedUrl.startsWith("https")) {
                    it.feedUrl
                } else {
                    it.feedUrl.replace("http", "https")
                }
                 feed = rssService.getFeed(feedUrl)
                episodes.postValue(feed)
            }
        }


    }

    fun playClickedPodcast(currentEpisode: RSSFeedResponse.EpisodeResponse){
        Log.e("Play","play")
        episodes.value?.episodes?.let {
            Log.e("Play","notnull")
            playPodcast(it,currentEpisode) }

    }

    fun isPlaying():Boolean{
       return podcastIsPlaying
    }
    fun pausePodcast(){
        serviceConnection.transportControls.stop()
    }
    fun playPodcast(episodes: List<RSSFeedResponse.EpisodeResponse>, currentEpisode: RSSFeedResponse.EpisodeResponse) {
        serviceConnection.playPodcast(episodes)
        if (currentEpisode.guid == currentPlayingEpisode.value?.guid) {
            if (podcastIsPlaying) {
                serviceConnection.transportControls.pause()
            } else {
                serviceConnection.transportControls.play()
            }
        } else {
            serviceConnection.transportControls.playFromMediaId(currentEpisode.guid, null)
        }
    }



}