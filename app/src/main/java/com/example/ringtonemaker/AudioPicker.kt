package com.example.ringtonemaker

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ringtonemaker.const.Constants

class AudioPicker(
    activityResultRegistry: ActivityResultRegistry,
    callback: (audioUri: Uri?) -> Unit
){
    private val getContent: ActivityResultLauncher<String> = activityResultRegistry.register(
        REGISTRY_KEY, ActivityResultContracts.GetContent(), callback)

    fun pickAudio(){
        getContent.launch(Constants.MIMETYPE_MUSIC)
    }


    private companion object{
        private const val REGISTRY_KEY = "AudioPicker"
    }
}