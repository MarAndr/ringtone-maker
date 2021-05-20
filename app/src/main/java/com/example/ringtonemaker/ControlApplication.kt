package com.example.ringtonemaker

import android.app.Application
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import timber.log.Timber

class ControlApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG){
        Timber.plant(Timber.DebugTree())
        }
        //Load FFMpeg library
        try {
            FFmpeg.getInstance(this).loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {
                    Log.e("FFMpeg", "Failed to load FFMpeg library.")
                }

                override fun onSuccess() {
                    Log.i("FFMpeg", "FFMpeg Library loaded!")
                }

                override fun onStart() {
                    Log.i("FFMpeg", "FFMpeg Started")
                }

                override fun onFinish() {
                    Log.i("FFMpeg", "FFMpeg Stopped")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //            AudioTrimmer.with(requireContext())
//                .setFile(ringtone) //Audio File
//                .setStartTime("00:00:05") //Start at 5 seconds
//                .setEndTime("00:00:10") //End at 10 seconds
//                .setOutputPath(Utils.outputPath + "audio")
//                .setOutputFileName("trimmed_" + System.currentTimeMillis() + ".mp3")
//                .setCallback(this)
//                .trim()
}