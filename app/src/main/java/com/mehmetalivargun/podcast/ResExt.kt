package com.mehmetalivargun.podcast

import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*


fun ImageView.load(imgUrl: String) {
    if (imgUrl.isEmpty())
        return
    Glide.with(context)
        .load(imgUrl)
        .into(this)
}

fun String?.getDate(): String {

    this?.apply {
        val sdf = SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss ZZZZ", Locale.ENGLISH)
        val parsedDate = sdf.parse(this)
        val systemSdp = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
        return systemSdp.format(parsedDate!!)
    }

    return ""
}

