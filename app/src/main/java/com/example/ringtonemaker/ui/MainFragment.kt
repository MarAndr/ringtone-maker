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
import com.example.ringtonemaker.databinding.FragmentMainBinding
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.generateTimeList
import com.example.ringtonemaker.utils.onTextChanged
import com.example.ringtonemaker.utils.receiveFileNameFromTheFilePath
import com.example.ringtonemaker.viewmodel.RingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

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

        binding.scrollChoiceStartSeconds.addItems(generateTimeList(),0)
        binding.scrollChoiceStartMinutes.addItems(generateTimeList(),0)
        binding.scrollChoiceStartSeconds.visibleItemCount = 2
        binding.scrollChoiceStartMinutes.visibleItemCount = 2
        binding.scrollChoiceStartSeconds.setOnItemSelectedListener { scrollChoice, position, name ->
            startTimeMinutes = name
            Timber.d("startTime = $startTimeMinutes")
        }
        binding.scrollChoiceStartMinutes.setOnItemSelectedListener { scrollChoice, position, name ->
            startTimeSeconds = name
            Timber.d("startTime = $startTimeMinutes")
        }

        binding.scrollChoiceEndSeconds.addItems(generateTimeList(),0)
        binding.scrollChoiceEndMinutes.addItems(generateTimeList(),0)
        binding.scrollChoiceEndSeconds.visibleItemCount = 2
        binding.scrollChoiceEndMinutes.visibleItemCount = 2
        binding.scrollChoiceEndSeconds.setOnItemSelectedListener { scrollChoice, position, name ->
            endTimeMinutes = name
            Timber.d("startTime = $startTimeMinutes")
        }
        binding.scrollChoiceStartMinutes.setOnItemSelectedListener { scrollChoice, position, name ->
            endTimeSeconds = name
            Timber.d("startTime = $startTimeMinutes")
        }

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
//            val startTime: String = binding.etMainFragmentStartTime.text.toString()
//            val endTime: String = binding.etMainFragmentEndTime.text.toString()
            viewModel.trimAudio(endTimeMinutes, startTimeMinutes)
        }


        binding.etMainFragmentSetRingtoneName.onTextChanged { inputString: String? ->
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
                                ringtoneName,
                                ringtonePath
                        )
                        findNavController().navigate(action)
                        createSnackBar("рингтон успешно создан")
                    }
                    is CuttingState.ERROR -> {
                        isLoading(false)
                        createSnackBar(cuttingState.message)
                    }
                    is CuttingState.READY -> {
                        isCuttingRingtoneButtonEnable(true)
                    }
                    is CuttingState.NOTCHOSEN -> {
                        createSnackBar("Файл или папка не выбраны!")
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
            binding.textViewMainFragmentChooseTimeLabel.setTextColor(Color.BLACK)
            binding.textViewMainFragmentChooseTimeLabel.setText(R.string.chooseTimeHeaderActive)
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
}
