package com.example.ringtonemaker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ringtonemaker.AudioPicker
import com.example.ringtonemaker.FolderPicker
import com.example.ringtonemaker.R
import com.example.ringtonemaker.ViewBindingFragment
import com.example.ringtonemaker.databinding.FragmentMainBinding
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.getPath
import com.example.ringtonemaker.utils.onTextChanged
import com.example.ringtonemaker.utils.receiveFileNameFromTheFilePath
import com.example.ringtonemaker.viewmodel.RingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class MainFragment : ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

//    private var ringtoneFolderPathName: String? = null
//    private var originalFilePath = ""
//    private var ringtonePath = ""
    private lateinit var ringtoneUri: Uri
    private var ringtoneName = "ringtone_${System.currentTimeMillis()}"

    private lateinit var audioPicker: AudioPicker
    private lateinit var folderPicker: FolderPicker
    private val viewModel: RingtoneViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFolderPicker()
        initFilePicker()
    }


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
            folderPicker.chooseFolder()
        }

        binding.buttonMainFragmentCreateRingtone.setOnClickListener {
            val startTime: String = binding.etMainFragmentStartTime.text.toString()
            val endTime: String = binding.etMainFragmentEndTime.text.toString()
//            ringtonePath = createFileForRingtone(ringtoneFolderPathName!!, ringtoneName)
            viewModel.trimAudio(endTime, startTime)
        }


        binding.etMainFragmentSetRingtoneName.onTextChanged { inputString: String? ->
//            ringtoneName = inputString.toString()
            viewModel.getRingtoneName(inputString)
            viewModel.changeRingtoneNameChoosingState(inputString?.isNotBlank() ?: false)
        }
        observeData()
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            viewModel.ringtoneCuttingState.collect { cuttingState ->
                when (cuttingState) {
                    is CuttingState.LOADING -> {
                        isLoading(true)
                    }
                    is CuttingState.SUCCESSFUL -> {
                        isLoading(false)
                        val action = MainFragmentDirections.actionMainFragmentToFinalFragment(
                            ringtoneUri,
                            ringtoneName
                        )
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

        viewModel.ringtoneFolderPathName.observe(viewLifecycleOwner) { ringtoneFolderPathName ->
            binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
        }
        viewModel.originalPath.observe(viewLifecycleOwner) { originalFilePath ->
            binding.textViewMainFragmentFileName.text =
                receiveFileNameFromTheFilePath(originalFilePath)
        }
        viewModel.ringtoneUri.observe(viewLifecycleOwner){ringtoneUri ->
            this.ringtoneUri = ringtoneUri
        }
        viewModel.ringtoneName.observe(viewLifecycleOwner){ringtoneName ->
            this.ringtoneName = ringtoneName
        }
    }

    private fun isLoading(boolean: Boolean) {
        binding.progressBarMainFragment.isVisible = boolean
        binding.buttonMainFragmentCreateRingtone.isEnabled = boolean.not()
    }

    private fun isCuttingRingtoneButtonEnable(isEnable: Boolean) {
        binding.buttonMainFragmentCreateRingtone.isEnabled = isEnable
        binding.textInputLayoutMainFragmentEndTime.isEnabled = isEnable
        binding.textInputLayoutMainFragmentStartTime.isEnabled = isEnable
        if (isEnable) {
            binding.textViewMainFragmentChooseTimeLabel.setTextColor(Color.BLACK)
            binding.textViewMainFragmentChooseTimeLabel.setText(R.string.chooseTimeHeaderActive)
        }
    }

    private fun initFilePicker() {
        audioPicker = AudioPicker(requireActivity().activityResultRegistry) { audioFileUri ->
//            handleSelectFile(audioFileUri)
            viewModel.handleSelectFile(audioFileUri)
        }
    }

    private fun initFolderPicker() {
        folderPicker =
            FolderPicker(requireActivity().activityResultRegistry) { selectedRingtoneFolderUri ->
//                handleSelectDirectory(selectedRingtoneFolderUri)
                viewModel.handleSelectedFolderUri(selectedRingtoneFolderUri)
            }
    }

//    private fun handleSelectFile(uri: Uri?) {
//        if (uri == null) {
//            viewModel.changeFileChoosingState(false)
//            createSnackBar("Файл не выбран")
//            return
//        } else {
////            originalFilePath = getPath(requireContext(), uri)!!
//            viewModel.changeFileChoosingState(true)
////            binding.textViewMainFragmentFileName.text =
////                receiveFileNameFromTheFilePath(originalFilePath)
//        }
//    }

//    private fun handleSelectDirectory(uri: Uri?) {
//        if (uri == null) {
//            viewModel.changeRingtoneFolderChoosingState(false)
//            createSnackBar("Папка не выбрана")
//            return
//        } else {
//            val docUri = DocumentsContract.buildDocumentUriUsingTree(
//                uri, DocumentsContract.getTreeDocumentId(uri)
//            )
//            viewModel.changeRingtoneFolderChoosingState(true)
//            ringtoneFolderPathName = getPath(requireContext(), docUri)
//            binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2222) {
            folderPicker.chooseFolder()
        }
    }

//    private fun createFileForRingtone(ringtoneFolderPath: String, ringtoneName: String): String {
//        val file = File(ringtoneFolderPath, "$ringtoneName.mp3")
//        ringtoneUri = Uri.fromFile(file)
//        return file.path
//    }
}
