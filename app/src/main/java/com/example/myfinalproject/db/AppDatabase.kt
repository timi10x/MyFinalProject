package com.example.myfinalproject.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myfinalproject.model.Bookmark

@Database(entities = arrayOf(Bookmark::class), version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun bookmarkDao() : BookmarkDao
    companion object{
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase{
            if (instance == null){
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase"
                ).build()
            }
            return instance as AppDatabase
        }
    }
}