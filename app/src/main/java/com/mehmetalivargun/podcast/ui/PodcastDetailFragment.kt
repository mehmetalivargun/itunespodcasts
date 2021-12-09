package com.mehmetalivargun.podcast.ui

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mehmetalivargun.podcast.adapters.EpisodeAdapter
import com.mehmetalivargun.podcast.base.BaseFragment
import com.mehmetalivargun.podcast.databinding.FragmentPodcastDetailBinding
import com.mehmetalivargun.podcast.load
import com.mehmetalivargun.podcast.viewmodel.PodcastDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_episode.*

@AndroidEntryPoint
class PodcastDetailFragment : BaseFragment<FragmentPodcastDetailBinding>(FragmentPodcastDetailBinding::inflate) {
    private val viewModel: PodcastDetailViewModel by viewModels()
    private val adapter = EpisodeAdapter()
    override fun FragmentPodcastDetailBinding.initialize() {

        viewModel.episodes.observe(viewLifecycleOwner,{
            adapter.submitList(it.episodes)
            binding.episodesRV.adapter=adapter
            binding.episodeSize.text= it.episodes?.size.toString()+" Episode"
            binding.summary.text=it.summary
        })


        adapter.onItemClickListener={
            if(!viewModel.podcastIsPlaying)
                viewModel.playClickedPodcast(it)
            else
                viewModel.pausePodcast()
        }

        viewModel.podcast.observe(viewLifecycleOwner,{
            binding.imageLogo.load(it.last().artworkUrl600)
            binding.title.text=it.last().collectionName
        })
    }


}