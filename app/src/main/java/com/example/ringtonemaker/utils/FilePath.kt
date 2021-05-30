package com.example.ringtonemaker.utils

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

fun getPath(uri: Uri): String? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":").toTypedArray()
    val type = split[0]
    if ("primary".equals(type, ignoreCase = true)) {
        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
    }
    return null
}

fun receiveFileNameFromTheFilePath(filePath: String): String {
    return filePath.substringAfterLast("/")
}
