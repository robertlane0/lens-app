package com.openscan.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

fun copyToInternal(context: Context, uri: Uri, subDir: String): File {
    val dir = File(context.filesDir, subDir)
    dir.mkdirs()
    val file = File(dir, "${UUID.randomUUID()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap, subDir: String): File {
    val dir = File(context.cacheDir, subDir)
    dir.mkdirs()
    val file = File(dir, "edit_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
    return file
}

fun loadBitmapFromFile(path: String): Bitmap? {
    return try {
        BitmapFactory.decodeFile(path)
    } catch (_: Exception) {
        null
    }
}

fun bitmapToFile(bitmap: Bitmap, file: File, quality: Int = 95): File {
    file.parentFile?.mkdirs()
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }
    return file
}
