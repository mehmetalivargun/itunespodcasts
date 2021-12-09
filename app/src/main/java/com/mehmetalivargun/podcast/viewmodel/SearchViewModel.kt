package com.mehmetalivargun.podcast.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetalivargun.podcast.data.model.Podcast
import com.mehmetalivargun.podcast.remote.RssService
import com.mehmetalivargun.podcast.repository.ITunesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository : ITunesRepo ) : ViewModel(){
    val podcasts: MutableLiveData<List<Podcast>> by lazy {
        MutableLiveData<List<Podcast>>()
    }


    fun getSearchResult(term:String)  = viewModelScope.launch {


        repository.getSearchResult(term).collect {
            when(it){
                is ITunesRepo.PodcastResult.Succes->onSucces(it.response.podcasts)
                is ITunesRepo.PodcastResult.Loading->onLoading()
                is ITunesRepo.PodcastResult.Failure->onFailure()
            }
        }
    }



    private fun onFailure() {
    }

    private fun onLoading() {

    }

    private fun onSucces(list: List<Podcast>) {
        podcasts.postValue(list)



    }
}