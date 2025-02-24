package com.lingfenglong.videoeditor

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Util {
    companion object {
        val gson: Gson = Gson()
    }
}

fun Any.toJson(): String = Util.gson.toJson(this)

fun <T> String.toObject(classOfT: Class<T>): T = Util.gson.fromJson(this, classOfT)

fun getFileNameAndExtFromUri(context: Context, uri: Uri): String {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Video.Media.DISPLAY_NAME), null, null, null)
    val fileNameIndex = cursor?.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
    val cursorHasValue = cursor?.moveToFirst()
    if (cursorHasValue == true) {
        fileName = fileNameIndex?.let { cursor.getString(it) }
    }
    cursor?.close()
    if (!fileName.isNullOrEmpty()) {
        return fileName
    }
    return "null"
}

fun Float.withPrecision(precision: Int) = "%.${precision}f".format(this)

fun Long.timeFormat(): String = LocalTime.of(0,0,0,0)
    .plusSeconds(this / 1000)
    .format(DateTimeFormatter.ofPattern("HH:mm:ss"))


fun View.makeSnackBar(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, text, duration).show()
}