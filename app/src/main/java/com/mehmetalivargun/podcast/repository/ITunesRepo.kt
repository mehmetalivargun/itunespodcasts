package com.mehmetalivargun.podcast.repository

import com.mehmetalivargun.podcast.data.model.PodcastResponse
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import com.mehmetalivargun.podcast.remote.ITunesService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject

class ITunesRepo @Inject constructor(private val api : ITunesService) {

    suspend fun getSearchResult(term:String): Flow<Any>  = flow{
        emit(PodcastResult.Loading)
        val response = try {
            api.getPodcasts(term)
        }catch (e:Exception){
            null
        }
        when(response?.code()){
            200->emit(PodcastResult.Succes(response.body()!!))
            else->emit(PodcastResult.Failure)
        }

    }
    suspend fun getLookupResult(id:Int): Flow<Any>  = flow{
        emit(PodcastResult.Loading)
        val response = try {
            api.getPodcastbyId(id)
        }catch (e:Exception){
            null
        }
        when(response?.code()){
            200->emit(PodcastResult.Succes(response.body()!!))
            else->emit(PodcastResult.Failure)
        }

    }

    sealed class PodcastResult{
        class Succes(val response:PodcastResponse):PodcastResult()
        object Loading : PodcastResult()
        object Failure: PodcastResult()
    }
}