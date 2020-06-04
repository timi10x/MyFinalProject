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

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(
        getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>?
            = null

    fun addBookmark(latLng: LatLng) : Long? {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name = "Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {

        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        bookmark.category = getPlaceCategory(place)

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let { bookmark.setImage(it, getApplication()) }
        Timber.tag(TAG).i("New bookmark $newId added to the database.")
    }

    fun getBookmarkViews() :
            LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun getPlaceCategory(place: Place): String {

        var category = "Other"
        val placeTypes = place.types

        placeTypes?.let {
            if (it.size > 0) {
                val placeType = it[0]
                category = bookmarkRepo.placeTypeToCategory(placeType)
            }
        }

        return category
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark):
            MapsViewModel.BookmarkView {
        return MapsViewModel.BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepo.getCategoryResourceId(bookmark.category))
    }

    data class BookmarkView(val id: Long? = null,
                            val location: LatLng = LatLng(0.0, 0.0),
                            val name: String = "",
                            val phone: String = "",
                            val categoryResourceId: Int? = null) {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context,
                    Bookmark.generateImageFilename(it))
            }
            return null
        }
    }
}