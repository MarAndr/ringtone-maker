package com.example.ringtonemaker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import com.example.ringtonemaker.const.Constants
import com.example.ringtonemaker.databinding.FragmentMainBinding
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.generateTimeList
import com.example.ringtonemaker.utils.onTextChanged
import com.example.ringtonemaker.utils.receiveFileNameFromTheFilePath
import com.example.ringtonemaker.viewmodel.RingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class MainFragment : ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    private var ringtonePath = ""
    private lateinit var ringtoneUri: Uri
    private var ringtoneName = "ringtone_${System.currentTimeMillis()}"
    private var startTimeMinutes = "0"
    private var startTimeSeconds = "0"
    private var endTimeMinutes = "0"
    private var endTimeSeconds = "0"

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

        binding.apply {
            scrollChoiceStartSeconds.apply {
                addItems(generateTimeList(), Constants.DEFAULT_INDEX)
                visibleItemCount = Constants.VISIBLE_ITEM_COUNT
                setOnItemSelectedListener { _, _, name ->
                    startTimeSeconds = name
                }
            }
            scrollChoiceStartMinutes.apply {
                addItems(generateTimeList(), Constants.DEFAULT_INDEX)
                visibleItemCount = Constants.VISIBLE_ITEM_COUNT
                setOnItemSelectedListener { _, _, name ->
                    startTimeMinutes = name
                }
            }
            scrollChoiceEndSeconds.apply {
                addItems(generateTimeList(), Constants.DEFAULT_INDEX)
                visibleItemCount = Constants.VISIBLE_ITEM_COUNT
                setOnItemSelectedListener { _, _, name ->
                    endTimeSeconds = name
                }
            }
            scrollChoiceEndMinutes.apply {
                addItems(generateTimeList(), Constants.DEFAULT_INDEX)
                visibleItemCount = Constants.VISIBLE_ITEM_COUNT
                setOnItemSelectedListener { _, _, name ->
                    endTimeMinutes = name
                }
            }
        }

        if (!isPermissionReadStorageGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), getPermissionsArray(), Constants.READ_WRITE_PERMISSION_REQUEST_CODE
            )
        } else if (!isPermissionWriteStorageGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), getPermissionsArray(), Constants.READ_WRITE_PERMISSION_REQUEST_CODE
            )
        }
        binding.buttonMainFragmentChooseFile.setOnClickListener {
            audioPicker.pickAudio()
        }

        binding.buttonMainFragmentChooseTheFolder.setOnClickListener {
            folderPicker.chooseFolder()
        }

        binding.buttonMainFragmentCreateRingtone.setOnClickListener {
            viewModel.trimAudio(
                startTimeMinutes = startTimeMinutes,
                startTimeSeconds = startTimeSeconds,
                endTimeMinutes = endTimeMinutes,
                endTimeSeconds = endTimeSeconds
            )
        }


        binding.etMainFragmentSetRingtoneName.onTextChanged { inputString: String? ->
            viewModel.getRingtoneName(inputString)
            viewModel.changeRingtoneNameChoosingState(inputString?.isNotBlank() ?: false)
        }
        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.cuttingState.collect { cuttingState ->
                when (cuttingState) {
                    is CuttingState.LOADING -> {
                        isLoading(true)
                    }
                    is CuttingState.SUCCESSFUL -> {
                        isLoading(false)
                        val action = MainFragmentDirections.actionMainFragmentToFinalFragment(
                            ringtoneUri,
                            ringtoneName,
                            ringtonePath
                        )
                        findNavController().navigate(action)
                        createSnackBar(getString(R.string.ringtoneMakingSuccess))
                    }
                    is CuttingState.ERROR -> {
                        isLoading(false)
                        createSnackBar(cuttingState.message)
                    }
                    is CuttingState.READY -> {
                        isCuttingRingtoneButtonEnable(true)
                    }
                    is CuttingState.FILE_NOT_CHOSEN -> {
                        createSnackBar(getString(R.string.fileNotChosen))
                    }
                    is CuttingState.FOLDER_NOT_CHOSEN -> {
                        createSnackBar(getString(R.string.folderNotChosen))
                    }

                    else -> Unit
                }
            }

        }

        viewModel.folderPathName.observe(viewLifecycleOwner) { ringtoneFolderPathName ->
            binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
        }
        viewModel.originalPath.observe(viewLifecycleOwner) { originalFilePath ->
            binding.textViewMainFragmentFileName.text =
                receiveFileNameFromTheFilePath(originalFilePath)
        }
        viewModel.ringtoneUri.observe(viewLifecycleOwner) { ringtoneUri ->
            this.ringtoneUri = ringtoneUri
        }
        viewModel.ringtoneName.observe(viewLifecycleOwner) { ringtoneName ->
            this.ringtoneName = ringtoneName
        }
        viewModel.ringtonePath.observe(viewLifecycleOwner) { ringtonePath ->
            this.ringtonePath = ringtonePath
        }
    }

    private fun isLoading(boolean: Boolean) {
        binding.progressBarMainFragment.isVisible = boolean
        binding.buttonMainFragmentCreateRingtone.isEnabled = boolean.not()
    }

    private fun isCuttingRingtoneButtonEnable(isEnable: Boolean) {
        binding.buttonMainFragmentCreateRingtone.isEnabled = isEnable

        if (isEnable) {
            binding.apply {
                scrollChoiceStartSeconds.selectedItemTextColor = R.color.purple_700
                scrollChoiceStartMinutes.selectedItemTextColor = R.color.purple_700
                scrollChoiceStartMinutes.selectedItemTextColor = R.color.purple_700
                scrollChoiceEndMinutes.selectedItemTextColor = R.color.purple_700
                scrollChoiceEndSeconds.selectedItemTextColor = R.color.purple_700
                textViewMainFragmentChooseTimeLabel.setTextColor(Color.BLACK)
                textViewMainFragmentChooseTimeLabel.setText(R.string.chooseTimeHeaderActive)
            }
        }
    }

    private fun initFilePicker() {
        audioPicker = AudioPicker(requireActivity().activityResultRegistry) { audioFileUri ->
            viewModel.handleSelectFile(audioFileUri)
        }
    }

    private fun initFolderPicker() {
        folderPicker =
            FolderPicker(requireActivity().activityResultRegistry) { selectedRingtoneFolderUri ->
                viewModel.handleSelectedFolderUri(selectedRingtoneFolderUri)
            }
    }

    private fun getPermissionsArray() = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun isPermissionReadStorageGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED


    private fun isPermissionWriteStorageGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_WRITE_PERMISSION_REQUEST_CODE) {
            folderPicker.chooseFolder()
        }
    }
}
