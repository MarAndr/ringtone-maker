package com.example.ringtonemaker.repository

import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Repository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun execFFMpegBinary(cmd: Array<String>, listener: suspend (isOk: Boolean) -> Unit) {
        FFmpeg.executeAsync(cmd)
        { _, returnCode ->
            if (returnCode == 0){
                scope.launch {
                    listener(true)
                }
            } else{
                scope.launch {
                    listener(false)
                }
            }
        }
    }
}


