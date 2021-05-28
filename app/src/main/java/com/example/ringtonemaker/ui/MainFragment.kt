package com.example.ringtonemaker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ringtonemaker.AudioPicker
import com.example.ringtonemaker.FolderPicker
import com.example.ringtonemaker.ViewBindingFragment
import com.example.ringtonemaker.databinding.FragmentMainBinding
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.getPath
import com.example.ringtonemaker.utils.onTextChanged
import com.example.ringtonemaker.utils.receiveFileNameFromTheFilePath
import com.example.ringtonemaker.viewmodel.RingtoneViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


class MainFragment : ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    private var ringtoneFolderUri: Uri? = null
    private var ringtoneFolderPathName: String? = null
    private var originalPath = ""
    private var ringtonePath = ""
    private lateinit var ringtoneUri: Uri
    private var ringtoneName = "ringtone_${System.currentTimeMillis()}"
    private lateinit var audioPicker: AudioPicker
    private lateinit var folderPicker: FolderPicker
    private val viewModel: RingtoneViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFolderPicker()
        initFilePicker()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), 2222
            )
        } else if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), 2222
            )
        }
        binding.buttonMainFragmentChooseFile.setOnClickListener {
            audioPicker.pickAudio()
        }

        binding.buttonMainFragmentChooseTheFolder.setOnClickListener {
            chooseFolder()
        }

        binding.buttonMainFragmentCreateRingtone.setOnClickListener {
            val startTime: String = binding.etMainFragmentStartTime.text.toString()
            val endTime: String = binding.etMainFragmentEndTime.text.toString()
            ringtonePath = createFileForRingtone(ringtoneFolderPathName!!, ringtoneName)
            viewModel.trimAudio(originalPath, endTime, startTime, ringtonePath)
        }


        binding.etMainFragmentSetRingtoneName.onTextChanged { inputString: String? ->
            ringtoneName = inputString.toString()
            viewModel.changeRingtoneNameChoosingState(inputString?.isNotBlank() ?: false)
        }
        observeData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ringtoneCuttingState.collect { cuttingState ->
                when (cuttingState) {
                    is CuttingState.LOADING -> {
                        isLoading(true)
                    }
                    is CuttingState.SUCCESSFUL -> {
                        isLoading(false)
                        val action = MainFragmentDirections.actionMainFragmentToFinalFragment(ringtoneUri, ringtoneName)
                        findNavController().navigate(action)
                        createSnackBar("рингтон успешно создан")
                    }
                    is CuttingState.Error -> {
                        isLoading(false)
                        createSnackBar(cuttingState.message)
                    }
                    is CuttingState.READY -> {
                        isCuttingRingtoneButtonEnable(true)
                    }
                    else -> Unit
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun chooseFolder() {
        folderPicker.chooseFolder()
    }

    private fun isLoading(boolean: Boolean) {
        binding.progressBarMainFragment.isVisible = boolean
        binding.buttonMainFragmentCreateRingtone.isEnabled = boolean.not()
    }

    private fun isCuttingRingtoneButtonEnable(isEnable: Boolean) {
        binding.buttonMainFragmentCreateRingtone.isEnabled = isEnable
        binding.textInputLayoutMainFragmentEndTime.isEnabled = isEnable
        binding.textInputLayoutMainFragmentStartTime.isEnabled = isEnable
    }

    private fun initFilePicker(){
        audioPicker = AudioPicker(requireActivity().activityResultRegistry) { audioFileUri ->
            handleSelectFile(audioFileUri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initFolderPicker() {
        folderPicker = FolderPicker(requireActivity().activityResultRegistry) { selectedRingtoneFolderUri ->
            handleSelectDirectory(selectedRingtoneFolderUri)
        }
    }

    private fun handleSelectFile(uri: Uri?){
        if (uri == null) {
            viewModel.changeFileChoosingState(false)
            createSnackBar("Файл не выбран")
            return
        } else{
            originalPath = getPath(requireContext(), uri)!!
            viewModel.changeFileChoosingState(true)
            binding.textViewMainFragmentFileName.text = receiveFileNameFromTheFilePath(originalPath)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleSelectDirectory(uri: Uri?) {
        if (uri == null) {
            viewModel.changeRingtoneFolderChoosingState(false)
            createSnackBar("Папка не выбрана")
            return
        } else{
            ringtoneFolderUri = uri
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    uri, DocumentsContract.getTreeDocumentId(uri)
            )
            viewModel.changeRingtoneFolderChoosingState(true)
            ringtoneFolderPathName = getPath(requireContext(), docUri)
            binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2222) {
            chooseFolder()
        }
    }

    private fun createFileForRingtone(ringtoneFolderPath: String, ringtoneName: String): String {
        val file = File(ringtoneFolderPath, "$ringtoneName.mp3")
        ringtoneUri = Uri.fromFile(file)
        return file.path
    }
}
