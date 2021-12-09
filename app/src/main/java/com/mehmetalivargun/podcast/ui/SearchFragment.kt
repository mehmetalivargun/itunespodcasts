package com.mehmetalivargun.podcast.ui

import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mehmetalivargun.podcast.adapters.PodcastAdapter
import com.mehmetalivargun.podcast.base.BaseFragment
import com.mehmetalivargun.podcast.databinding.FragmentSearchBinding
import com.mehmetalivargun.podcast.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {
    private val viewModel: SearchViewModel by viewModels()
    private val adapter = PodcastAdapter()


    override fun FragmentSearchBinding.initialize() {

        viewModel.podcasts.observe(
            viewLifecycleOwner, {
                adapter.submitList(it)
                adapter.setOnItemClickListener {
                    findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToPodcastDetailFragment(it.collectionId))
                }
                binding.podcastRV.adapter=adapter

            }
        )

        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchQuery = query
                    doSearch()
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    searchQuery = query
                    doSearch()
                    return true
                }
            })
        }

    }

    //appbar search textchange listener
    /*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        (searchItem.actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchQuery = query
                    doSearch()
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    searchQuery = query
                    doSearch()
                    return true
                }
            })
        }

        super.onCreateOptionsMenu(menu, inflater)
    }*/

    fun doSearch() {
        searchQuery?.apply {
            if (length > 2) {
                viewModel.apply {
                    getSearchResult(searchQuery!!)
                }
            } else {

            }
        }
        }
    companion object {
        var searchQuery: String? = ""
    }

}







