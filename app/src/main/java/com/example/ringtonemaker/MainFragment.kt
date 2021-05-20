package com.example.ringtonemaker

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blackbox.ffmpeg.examples.callback.FFMpegCallback
import com.example.mylab.const.Constants
import com.example.ringtonemaker.databinding.FragmentMainBinding
import timber.log.Timber
import java.io.File


class MainFragment : ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate),
    FFMpegCallback {

    private var filePath: String? = ""
    private var ringtoneFolderPathName: String? = ""
    private var ringtoneFolderUri: Uri? = null
    private lateinit var ringtone: File
    private lateinit var selectDocumentDirectoryLauncher: ActivityResultLauncher<Uri>
    private val getContent: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { audioUri ->
        filePath = audioUri.path
        val name = filePath?.substringAfterLast('/')?.substringAfterLast('/')
        binding.textViewMainFragmentFileName.text = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSelectDocumentLauncher()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonMainFragmentChooseFile.setOnClickListener {
            getContent.launch(Constants.MIMETYPE_MUSIC)
        }

        binding.buttonMainFragmentChooseTheFolder.setOnClickListener {
            selectDir()
        }
        binding.buttonMainFragmentCreateRingtone.setOnClickListener {
            setUpResources()
        }

    }

    private fun selectDir() {
        selectDocumentDirectoryLauncher.launch(null)
    }

    private fun initSelectDocumentLauncher() {
        selectDocumentDirectoryLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { selectedRingtoneFolderUri ->
            ringtoneFolderUri = selectedRingtoneFolderUri
            handleSelectDirectory(selectedRingtoneFolderUri)
        }
    }

    private fun handleSelectDirectory(uri: Uri?) {
        if (uri == null) {
            Timber.d("directory not selected")
            return
        }
        ringtoneFolderPathName = uri.path?.substringAfter(":")
        binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
    }

    private fun setUpResources() {
        ringtone = Utils.copyFileToExternalStorage(ringtoneFolderUri, requireContext())
    }

    override fun onProgress(progress: String) {
        Timber.d("onProgress")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Timber.d("onSuccess")
    }

    override fun onFailure(error: Exception) {
        Timber.d(error)
    }

    override fun onNotAvailable(error: Exception) {
        Timber.d(error)
    }

    override fun onFinish() {
        Timber.d("onFinish")
    }


}