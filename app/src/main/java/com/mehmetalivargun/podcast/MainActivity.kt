package com.mehmetalivargun.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.mehmetalivargun.podcast.remote.RssService
import com.mehmetalivargun.podcast.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home->{
                onBackPressed()
                return true
            }
        }
        return  super.onOptionsItemSelected(item)
    }*/
}