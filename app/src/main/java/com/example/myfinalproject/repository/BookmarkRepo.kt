package com.example.myfinalproject.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myfinalproject.db.AppDatabase
import com.example.myfinalproject.db.BookmarkDao
import com.example.myfinalproject.model.Bookmark


class BookmarkRepo(context: Context) {
    private var db = AppDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

    fun addBookMark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookMark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
        return bookmarkDao.loadLiveBookmark(bookmarkId)
    }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }
}