package com.example.myfinalproject.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.myfinalproject.R
import com.example.myfinalproject.ui.MapsActivity
import com.example.myfinalproject.viewmodel.MapsViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookmarkInfoAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {
    private val contents: View = context.layoutInflater.inflate(
        R.layout.content_bookmark_info, null, false
    )

    override fun getInfoContents(marker: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker.title ?: ""

        val phoneView =
            contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker.snippet ?: ""

        val imageView = contents.findViewById<ImageView>(R.id.photo)
        when(marker
            .tag){
            //if the marker.tag is a MapsActivity.placeInfo, i wanna set the imageView bitmap
            //directly from the PlaceInfo.image object
            is MapsActivity.PlaceInfo->{
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
            }
            //if the marker.tag us a MapViewModel ish, i wanna set the imageView bitmap from the BookmarkView
            is MapsViewModel.BookmarkMarkerView->{
                val bookMarkView = marker.tag as MapsViewModel.BookmarkMarkerView
            }
        }
        return contents
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}