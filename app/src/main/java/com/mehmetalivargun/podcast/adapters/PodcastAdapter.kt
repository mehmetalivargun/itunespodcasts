package com.mehmetalivargun.podcast.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mehmetalivargun.podcast.data.model.Podcast
import com.mehmetalivargun.podcast.databinding.ItemSearchBinding
import com.mehmetalivargun.podcast.load


class PodcastAdapter : ListAdapter<Podcast, PodcastAdapter.PodcastHolder>(Companion) {
    private var onItemClickListener: ((Podcast) -> Unit)? = null
    fun setOnItemClickListener(listener: (Podcast) -> Unit) {
        onItemClickListener = listener
    }
    companion object : DiffUtil.ItemCallback<Podcast>() {
        override fun areItemsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return  oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return  oldItem.collectionId == newItem.collectionId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchBinding.inflate(inflater, parent, false)
        return PodcastHolder(binding)
    }

    override fun onBindViewHolder(holder: PodcastHolder, position: Int) {
       holder.bind(getItem(position))
    }

    inner class PodcastHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Podcast) = binding.apply {
            item.artworkUrl100.let { artWork.load(it) }
            collectionName.text = item.trackName
            releaseDate.text = item.releaseDate.toString()


            root.setOnClickListener {
                onItemClickListener?.invoke(item)

            }

        }
    }
}


