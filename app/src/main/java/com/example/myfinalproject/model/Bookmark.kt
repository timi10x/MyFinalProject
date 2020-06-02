package com.example.myfinalproject.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myfinalproject.utils.ImageUtils

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var placeId: String? = null,
    var name: String = "",
    var address: String ="",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = ""
){
    //the function setImage provides the public interface for saving an image for a bookmark
    fun setImage(image: Bitmap, context: Context){
        //if the image has an id, then it gets saved to a file
        //the filename is incoporated with the bookmark ID to make sure it is unique
        id?.let {
            ImageUtils.saveBitmapToFile(context, image, generateImageFilename(it))
        }
    }


    //this function returns a filename based on the bookmark ID
    companion object {
        fun generateImageFilename(id: Long): String {
            return "bookmark$id.png"
        }
    }

}