package com.example.ringtonemaker

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mylab.const.Constants
import com.example.ringtonemaker.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }




}