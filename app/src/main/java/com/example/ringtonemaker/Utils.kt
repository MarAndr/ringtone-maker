package com.example.ringtonemaker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.example.mylab.const.Constants
import timber.log.Timber
import java.io.*

object Utils {

    fun copyFileToExternalStorage(uri: Uri?, context: Context): File {
        try {
            val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
            uri?.path?.let { inputStream?.toFile(it) }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return File(uri?.path)
    }

    private fun InputStream.toFile(ringtonePath: String) {
        File(ringtonePath).outputStream().use { this.copyTo(it) }
    }

//    fun getConvertedFile(folder: String, fileName: String): File {
//        val f = File(folder)
//
//        if (!f.exists())
//            f.mkdirs()
//
//        return File(f.path + File.separator + fileName)
//    }
//
//    fun refreshGallery(path: String, context: Context) {
//
//        val file = File(path)
//        try {
//            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            val contentUri = Uri.fromFile(file)
//            mediaScanIntent.data = contentUri
//            context.sendBroadcast(mediaScanIntent)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
}