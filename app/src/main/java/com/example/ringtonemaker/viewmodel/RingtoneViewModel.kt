package com.example.ringtonemaker.viewmodel


import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.ringtonemaker.FolderPicker
import com.example.ringtonemaker.repository.Repository
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.createSnackBar
import com.example.ringtonemaker.utils.getPath
import com.example.ringtonemaker.utils.receiveFileNameFromTheFilePath
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


class RingtoneViewModel @ViewModelInject constructor(private val context: Application, private val repository: Repository): ViewModel() {

    private val _ringtoneCuttingState = MutableStateFlow<CuttingState>(CuttingState.Empty)
    val ringtoneCuttingState: StateFlow<CuttingState> = _ringtoneCuttingState
    private val _fileChoosingState = MutableLiveData(false)
    private val _ringtoneFolderChoosingState = MutableLiveData(false)
    private val _ringtoneNameChoosingState = MutableLiveData(false)
    private val _ringtoneFolderPathName = MutableLiveData<String>()
    val ringtoneFolderPathName: LiveData<String> = _ringtoneFolderPathName
    private val _ringtoneName = MutableLiveData<String>()
    val ringtoneName: LiveData<String> = _ringtoneName
    private val _ringtoneUri = MutableLiveData<Uri>()
    val ringtoneUri: LiveData<Uri> = _ringtoneUri
    private val _ringtonePath = MutableLiveData<String>()
    val ringtonePath: LiveData<String> = _ringtonePath
    private val _originalPath = MutableLiveData<String>()
    val originalPath: LiveData<String> = _originalPath

    fun handleSelectFile(uri: Uri?) {
        if (uri == null) {
            _ringtoneFolderChoosingState.value = false
            //snackbar
            return
        } else {
            _originalPath.value = getPath(context, uri)!!
            _ringtoneFolderChoosingState.value = false
//            binding.textViewMainFragmentFileName.text =
//                receiveFileNameFromTheFilePath(originalFilePath)
        }
    }

    fun handleSelectedFolderUri(selectedFolderUri: Uri?){
        if (selectedFolderUri == null) {
            _ringtoneFolderChoosingState.value = false
            return
        } else {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                selectedFolderUri, DocumentsContract.getTreeDocumentId(selectedFolderUri)
            )
            _ringtoneFolderChoosingState.value = true
            _ringtoneFolderPathName.value = getPath(context, docUri)!!
        }
    }

    fun createFileForRingtone(ringtoneFolderPathName: String, ringtoneName: String){
        val file = File(ringtoneFolderPathName, "$ringtoneName.mp3")
        _ringtoneUri.value = Uri.fromFile(file)
        _ringtonePath.value =  file.path
    }

    fun getRingtoneName(string: String?){
        _ringtoneName.value = string.orEmpty()
    }

    fun changeRingtoneNameChoosingState(ringtoneNameChoosingState: Boolean) {
        _ringtoneNameChoosingState.value = ringtoneNameChoosingState
        if (isRingtoneReadyToMake()) {
            _ringtoneCuttingState.value = CuttingState.READY
        }
    }

    fun changeFileChoosingState(choosingState: Boolean) {
        _fileChoosingState.value = choosingState
        if (isRingtoneReadyToMake()) {
            _ringtoneCuttingState.value = CuttingState.READY
        }
    }

    fun changeRingtoneFolderChoosingState(choosingState: Boolean) {
        _ringtoneFolderChoosingState.value = choosingState
        if (isRingtoneReadyToMake()) {
            _ringtoneCuttingState.value = CuttingState.READY
        }
    }

    fun trimAudio(originalPath: String, startTime: String, endTime: String, ringtonePath: String) {
        createFileForRingtone(_ringtoneFolderPathName.value.orEmpty(), _ringtoneName.value.orEmpty())
        _ringtoneCuttingState.value = CuttingState.LOADING

//    "fade=in:st=0:d=5",
//    "fade=in:5:8",
        val cmd = arrayOf(
                "-i",
                _originalPath.value.orEmpty(),
                "-ss",
                "00:00:$startTime",
                "-t",
                "00:00:$endTime",
                "-c",
                "copy",
                _ringtonePath.value.orEmpty()
        )

        viewModelScope.launch {
            repository.execFFMpegBinary(cmd) { isCuttingSuccessful ->
                when {
                    isCuttingSuccessful -> {
                        _ringtoneCuttingState.value = CuttingState.SUCCESSFUL
                    }
                    isTimeEmpty(startTime, endTime) -> {
                        _ringtoneCuttingState.value = CuttingState.Error("Введите временной интервал")
                    }
                    else -> _ringtoneCuttingState.value = CuttingState.Error("Неизвестная ошибка!")
                }
            }
        }
    }

    private fun isTimeEmpty(startTime: String?, endTime: String?) = startTime?.isBlank() == true || endTime?.isBlank() == true
    private fun isRingtoneReadyToMake() = _fileChoosingState.value == true
            && _ringtoneFolderChoosingState.value == true
            && _ringtoneNameChoosingState.value == true

}