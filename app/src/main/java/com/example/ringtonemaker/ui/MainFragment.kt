package com.example.ringtonemaker

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ringtonemaker.databinding.FragmentMainBinding
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.getPath
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
    private lateinit var selectDocumentDirectoryLauncher: ActivityResultLauncher<Uri>
    private lateinit var audioPicker: AudioPicker
    private val viewModel: RingtoneViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSelectDocumentLauncher()
        audioPicker = AudioPicker(requireActivity().activityResultRegistry) {
            originalPath = getPath(requireContext(), it)!!
            binding.textViewMainFragmentFileName.text = receiveFileNameFromTheFilePath(originalPath)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf<String>(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_SETTINGS
                ), 2222
            )
        } else if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf<String>(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_SETTINGS
                ), 2222
            )
        }
        binding.buttonMainFragmentChooseFile.setOnClickListener {
            audioPicker.pickAudio()
            viewModel.changeFileChoosingState(true)
        }

        binding.buttonMainFragmentChooseTheFolder.setOnClickListener {
            chooseFolder()
        }

        binding.buttonMainFragmentCreateRingtone.setOnClickListener {
//            trimAudio(5, 10, "trimMy.mp3")
            val startTime: String = binding.etMainFragmentStartTime.text.toString()
            val endTime: String = binding.etMainFragmentEndTime.text.toString()
            ringtonePath = getFilePath()
            viewModel.trimAudio(originalPath, endTime, startTime, getFilePath())
        }

        binding.buttonMainFragmentPlayRingtone.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToExoFragment(ringtonePath)
            findNavController().navigate(action)
        }

        binding.buttonMainFragmentSetAsRingtone.setOnClickListener {
            Timber.d("ringtoneUri = $ringtoneUri")
            setRingtone2(ringtoneUri)
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

    private fun chooseFolder() {
        selectDocumentDirectoryLauncher.launch(null)
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initSelectDocumentLauncher() {
        selectDocumentDirectoryLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { selectedRingtoneFolderUri ->
            ringtoneFolderUri = selectedRingtoneFolderUri
            handleSelectDirectory(selectedRingtoneFolderUri)
            viewModel.changeRingtoneFolderChoosingState(true)
        }
    }

//    "fade=in:st=0:d=5",
//    "fade=in:5:8",


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleSelectDirectory(uri: Uri?) {
        if (uri == null) {
            Timber.d("directory not selected")
            return
        }
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri, DocumentsContract.getTreeDocumentId(uri)
        )
        ringtoneFolderPathName = getPath(requireContext(), docUri)
        Timber.d("ringtoneFolderPathName = $ringtoneFolderPathName")
        binding.textViewMainFragmentChoosedFolder.text = ringtoneFolderPathName
    }

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

    private fun getFilePath(): String {
        val musicDir = ringtoneFolderPathName
        val file = File(musicDir, "testRRingtoneFile_${System.currentTimeMillis()}.mp3")
        ringtoneUri = Uri.fromFile(file)
        return file.path
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setRingtone(ringtonePath: String){
        val k: File = File(ringtonePath) // path is a file playing


        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
        values.put(MediaStore.MediaColumns.TITLE, "My Song title") //You will have to populate

        values.put(MediaStore.MediaColumns.SIZE, 215454)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
        values.put(MediaStore.Audio.Media.ARTIST, "Band Name") //You will have to populate this

        values.put(MediaStore.Audio.Media.DURATION, 230)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, false)

//Insert it into the database

//Insert it into the database
        val uri = MediaStore.Audio.Media.getContentUriForPath(k.absolutePath)
        val newUri: Uri? = requireContext().contentResolver.insert(uri!!, values)
        if (Settings.System.canWrite(requireContext())){
            createSnackBar("canWrite = true")
            RingtoneManager.setActualDefaultRingtoneUri(
                requireContext(),
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
        } else {
            openAndroidPermissionsMenu()
            createSnackBar("canWrite = false")
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setRingtone2(uri: Uri?){
        if (Settings.System.canWrite(requireContext())){
            createSnackBar("canWrite = true")
            RingtoneManager.setActualDefaultRingtoneUri(
                requireContext(),
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
        } else {
            openAndroidPermissionsMenu()
            createSnackBar("canWrite = false")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        startActivity(intent)
    }


}
