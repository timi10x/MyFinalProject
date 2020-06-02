package com.example.myfinalproject.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.myfinalproject.model.Bookmark
import com.example.myfinalproject.repository.BookmarkRepo
import com.example.myfinalproject.utils.ImageUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import timber.log.Timber

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MapsViewModel"
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

    fun addBookmarkFomPlace(place: Place, image: Bitmap?) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()

        val newId = bookmarkRepo.addBookMark(bookmark)
        //set image method here is used to save the image to the bookmark
        image?.let {
            bookmark.setImage(it, getApplication())
        }
        Timber.tag(TAG).i("New bookmark $newId added to the database")
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkView {
        return BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone
        )
    }

    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    //method to return the LiveData object that will be observed by MapsActivity
    fun getBookmarkViews():
            LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }


    data class BookmarkView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        var phone: String = ""
    ) {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(
                    context,
                    Bookmark.generateImageFilename(it)
                )
            }
            return null
        }
    }
}