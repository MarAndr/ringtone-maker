package com.example.ringtonemaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

//    AudioTrimmer.with(context!!)
//    .setFile(audio2) //Audio File
//    .setStartTime("00:00:05") //Start at 5 seconds
//    .setEndTime("00:00:10") //End at 10 seconds
//    .setOutputPath("PATH_TO_OUTPUT_AUDIO")
//    .setOutputFileName("trimmed_" + System.currentTimeMillis() + ".mp3")
//    .setCallback(this@MainActivity)
//    .trim()
}