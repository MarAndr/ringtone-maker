package com.example.ringtonemaker

import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.ringtonemaker.const.Constants

class FolderPicker(
        activityResultRegistry: ActivityResultRegistry,
        callback: (folderUri: Uri?) -> Unit
) {
    private val selectDocumentDirectoryLauncher: ActivityResultLauncher<Uri> = activityResultRegistry.register(
            REGISTRY_KEY, ActivityResultContracts.OpenDocumentTree(), callback)

    fun chooseFolder() {
        selectDocumentDirectoryLauncher.launch(null)
    }

    private companion object {
        private const val REGISTRY_KEY = "FolderPicker"
    }
}