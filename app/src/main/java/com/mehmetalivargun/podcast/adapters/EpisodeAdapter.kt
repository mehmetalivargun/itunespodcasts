package com.mehmetalivargun.podcast.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mehmetalivargun.podcast.R
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import com.mehmetalivargun.podcast.databinding.ItemEpisodeBinding
import com.mehmetalivargun.podcast.getDate


class EpisodeAdapter :
    ListAdapter<RSSFeedResponse.EpisodeResponse, EpisodeAdapter.PodcastHolder>(Companion) {
    var onItemClickListener: ((RSSFeedResponse.EpisodeResponse) -> Unit)? = null



    companion object : DiffUtil.ItemCallback<RSSFeedResponse.EpisodeResponse>() {
        override fun areItemsTheSame(
            oldItem: RSSFeedResponse.EpisodeResponse,
            newItem: RSSFeedResponse.EpisodeResponse
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: RSSFeedResponse.EpisodeResponse,
            newItem: RSSFeedResponse.EpisodeResponse
        ): Boolean {
            return oldItem.url == newItem.url
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEpisodeBinding.inflate(inflater, parent, false)
        return PodcastHolder(binding)
    }

    override fun onBindViewHolder(holder: PodcastHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PodcastHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var isPlaying:Boolean=false
        fun bind(item: RSSFeedResponse.EpisodeResponse) = binding.apply {
            val date = item.pubDate?.replace(",","")
            title.text = item.title
            releaseDate.text = date.getDate()
            descriptionText.text = Html.fromHtml(item.description.toString(), Html.FROM_HTML_MODE_COMPACT)
            playButton.text = item.duration
            playButton.setOnClickListener {
                if(!isPlaying){
                    onItemClickListener?.invoke(item)
                    playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_button,0,0,0)
                    isPlaying=true
                }
                else{
                    onItemClickListener?.invoke(item)
                    playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_button,0,0,0)
                    isPlaying=false
                }
            }
        }
    }
}