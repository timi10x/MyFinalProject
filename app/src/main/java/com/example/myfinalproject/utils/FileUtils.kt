package com.example.myfinalproject.utils

import android.content.Context
import java.io.File

object FileUtils {
    fun deleteFile(context: Context, filename: String) {
        val dir = context.filesDir
        val file = File(dir, filename)
        file.delete()
    }

}