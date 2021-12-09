package com.mehmetalivargun.podcast.remote

import com.mehmetalivargun.podcast.data.model.PodcastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesService {

    @GET("/search?media=podcast")
    suspend fun  getPodcasts(@Query("term") term:String) : Response<PodcastResponse>
    @GET("/lookup?")
    suspend fun  getPodcastbyId(@Query("id") id:Int) : Response<PodcastResponse>

}