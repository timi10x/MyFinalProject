package com.example.myfinalproject.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

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

    //this method is used to calculate the optimum inSampleSize that can be used to resize
    //an image to a specified width and height
    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int{
        var inSampleSize = 1
        if(height>reqHeight || width > reqWidth){
            val halfHeight = height/2
            val halfWidth = width/2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth){
                inSampleSize *=2
            }
        }
        return inSampleSize
    }

    //this helper method generates unique image filename
    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File{
        val timeStamp = SimpleDateFormat("yyyMMddHHmmss").format(Date())
        val filename = "BookMarkApp_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    //loading an image from a file
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap?{
        val filepath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filepath)
    }
}