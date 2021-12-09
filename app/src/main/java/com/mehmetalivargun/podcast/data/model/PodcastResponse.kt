package com.mehmetalivargun.podcast.data.model


import com.google.gson.annotations.SerializedName

data class PodcastResponse(
    @SerializedName("resultCount")
    val resultCount: Int,
    @SerializedName("results")
    val podcasts: List<Podcast>
)