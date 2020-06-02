package com.example.myfinalproject.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

//declared this object to behave as a singleton
object ImageUtils {
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String){
        //stream is created to hold my image data
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        ImageUtils.saveBytesToFile(context, bytes, filename)
    }

    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(bytes)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //loading an image from a file
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap?{
        val filepath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filepath)
    }
}