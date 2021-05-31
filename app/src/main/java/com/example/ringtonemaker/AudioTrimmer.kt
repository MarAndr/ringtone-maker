package com.example.ringtonemaker

import android.content.Context
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.ringtonemaker.utils.extractSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber


class AudioTrimmer private constructor(){

    private var originalFilePath: String? = null

    private var startTime = "00:00:00"
    private var endTime = "00:00:00"
    private var ringtonePath = ""
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    fun setFile(originalFilePath: String): AudioTrimmer {
        this.originalFilePath = originalFilePath
        return this
    }

    fun setStartTime(startTimeMinutes: String, startTimeSeconds: String): AudioTrimmer {
        this.startTime = "00:$startTimeMinutes:$startTimeSeconds"
        return this
    }

    fun setEndTime(endTimeMinutes: String, endTimeSeconds: String): AudioTrimmer {
        this.endTime = "00:$endTimeMinutes:$endTimeSeconds"
        return this
    }

    fun setRingtonePath(ringtonePath: String): AudioTrimmer {
        this.ringtonePath = ringtonePath
        return this
    }

    fun trim(listener: suspend (isOk: Boolean) -> Unit){

        val fadeInTime = extractSeconds(startTime)
        val fadeOutTime = extractSeconds(endTime) - 10

        val cmd = arrayOf(
                "-i",
                originalFilePath.orEmpty(),
                "-ss",
                this.startTime,
                "-to",
                this.endTime,
                "-af",
                "afade=t=in:st=$fadeInTime:d=10, afade=t=out:st=$fadeOutTime:d=10",
                ringtonePath
        )

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


    companion object {
        fun with(): AudioTrimmer {
            return AudioTrimmer()
        }
    }

}