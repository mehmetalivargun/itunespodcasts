package com.mehmetalivargun.podcast.remote

import android.text.format.DateUtils
import com.mehmetalivargun.podcast.data.model.RSSFeedResponse
import okhttp3.*
import org.w3c.dom.Node
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class RssService {
    suspend fun getFeed(xmlFileURL: String,/* callback: (RSSFeedResponse?) -> Unit*/) :RSSFeedResponse?{
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(xmlFileURL)
            .build()
        val response = client.newCall(request).await()
        response.body?.let { responseBody ->
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dbBuilder = dbFactory.newDocumentBuilder()
            val doc = try {
                dbBuilder.parse(responseBody.byteStream())
            }catch (e:Exception){
                null
            }


            val rssFeedResponse = RSSFeedResponse(episodes = mutableListOf())
            if (doc != null) {
                domToRssFeedResponse(doc, rssFeedResponse)
            }
            //callback(rssFeedResponse)
            println(rssFeedResponse)
            return rssFeedResponse

        }
        return null
    }
/*
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException){
                callback(null)
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response){
                if(response.isSuccessful){
                    response.body?.let{ responseBody ->
                        //println(responseBody.string())
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dbBuilder = dbFactory.newDocumentBuilder()
                        val doc = dbBuilder.parse(responseBody.byteStream())
                        val rssFeedResponse = RSSFeedResponse(episodes = mutableListOf())
                        domToRssFeedResponse(doc, rssFeedResponse)
                        callback(rssFeedResponse)
                        println(rssFeedResponse)
                        return
                    }
                }
                callback(null)
            }

        })*/



    private fun domToRssFeedResponse(node : Node, rssFeedResponse : RSSFeedResponse){
        if(node.nodeType == Node.ELEMENT_NODE){
            val nodeName = node.nodeName
            val parentName = node.parentNode.nodeName
            val grandParentName = node.parentNode.parentNode?.nodeName

            if(parentName == "item" && grandParentName == "channel"){
                val currentItem = rssFeedResponse.episodes?.last()
                if(currentItem != null){
                    when (nodeName) {
                        "title" -> currentItem.title = node.textContent
                        "description" -> currentItem.description = node.textContent
                        "itunes:duration" -> currentItem.duration = node.textContent
                        "guid" -> currentItem.guid = node.textContent
                        "pubDate" -> currentItem.pubDate = node.textContent
                        "link" -> currentItem.link = node.textContent
                        "enclosure" -> {
                            currentItem.url = node.attributes.getNamedItem("url")
                                .textContent
                            currentItem.type = node.attributes.getNamedItem("type")
                                .textContent
                        }
                    }
                }
            }

            if(parentName == "channel"){
                when (nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description = node.textContent
                    "itunes:summary" -> rssFeedResponse.summary = node.textContent
                    "item" -> rssFeedResponse.episodes?.add(RSSFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated =
                       xmlDateToDate(node.textContent)
                }
            }
        }
        val nodeList = node.childNodes
        for( i in 0 until nodeList.length){
            val childNode = nodeList.item(i)
            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }
    private fun xmlDateToDate(date : String?) : Date {
        val date = date?:return Date()
        val inFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        return inFormat.parse(date)
    }

}