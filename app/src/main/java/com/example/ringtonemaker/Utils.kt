package com.example.ringtonemaker

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber
import java.io.*

object Utils {

    fun copyFileToExternalStorage(initialFileUri: Uri?, ringtonePath: String, context: Context): File {
        try {
            val inputStream = initialFileUri?.let { context.contentResolver.openInputStream(it) }
            initialFileUri?.path?.let { inputStream?.toFile(it) }
        } catch (e: FileNotFoundException) {
            Timber.d(e)
            e.printStackTrace()
        } catch (e: IOException) {
            Timber.d(e)
            e.printStackTrace()
        }
        return File(ringtonePath)
    }

    private fun InputStream.toFile(ringtonePath: String) {
        File(ringtonePath).outputStream().use { this.copyTo(it) }
    }

    fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdirs()

        return File(f.path + File.separator + fileName)
    }

    fun refreshGallery(path: String, context: Context) {

        val file = File(path)
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}