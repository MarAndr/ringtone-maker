package com.example.ringtonemaker.viewmodel


import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.ringtonemaker.AudioTrimmer
import com.example.ringtonemaker.R
import com.example.ringtonemaker.state.CuttingState
import com.example.ringtonemaker.utils.getPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File


class RingtoneViewModel @ViewModelInject constructor(
        private val context: Application,
) : ViewModel() {

    private val _cuttingState = MutableStateFlow<CuttingState>(CuttingState.EMPTY)
    val cuttingState: StateFlow<CuttingState> = _cuttingState
    private val _fileChoosingState = MutableLiveData(false)
    private val _folderChoosingState = MutableLiveData(false)
    private val _nameChoosingState = MutableLiveData(false)
    private val _folderPathName = MutableLiveData<String>()
    val folderPathName: LiveData<String> = _folderPathName
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
            changeFileChoosingState(false)
            _cuttingState.value = CuttingState.FILE_NOT_CHOSEN
            return
        } else {
            Timber.d("uri = $uri")
            _originalPath.value = getPath(uri)!!
            changeFileChoosingState(true)
        }
    }

    fun handleSelectedFolderUri(selectedFolderUri: Uri?) {
        if (selectedFolderUri == null) {
            changeRingtoneFolderChoosingState(false)
            _cuttingState.value = CuttingState.FOLDER_NOT_CHOSEN
            return
        } else {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    selectedFolderUri, DocumentsContract.getTreeDocumentId(selectedFolderUri)
            )
            changeRingtoneFolderChoosingState(true)
            _folderPathName.value = getPath(docUri)!!
        }
    }

    private fun createFileForRingtone(ringtoneFolderPathName: String, ringtoneName: String) {
        val file = File(ringtoneFolderPathName, "$ringtoneName.mp3")
        _ringtoneUri.value = Uri.fromFile(file)
        _ringtonePath.value = file.path
    }

    fun getRingtoneName(string: String?) {
        _ringtoneName.value = string.orEmpty()
    }

    fun changeRingtoneNameChoosingState(ringtoneNameChoosingState: Boolean) {
        _nameChoosingState.value = ringtoneNameChoosingState
        if (isRingtoneReadyToMake()) {
            _cuttingState.value = CuttingState.READY
        }
    }

    private fun changeFileChoosingState(choosingState: Boolean) {
        _fileChoosingState.value = choosingState
        if (isRingtoneReadyToMake()) {
            _cuttingState.value = CuttingState.READY
        }
    }

    private fun changeRingtoneFolderChoosingState(choosingState: Boolean) {
        _folderChoosingState.value = choosingState
        if (isRingtoneReadyToMake()) {
            _cuttingState.value = CuttingState.READY
        }
    }

    fun trimAudio(
            startTimeMinutes: String,
            startTimeSeconds: String,
            endTimeMinutes: String,
            endTimeSeconds: String
    ) {
        createFileForRingtone(
                _folderPathName.value.orEmpty(),
                _ringtoneName.value.orEmpty()
        )

        _cuttingState.value = CuttingState.LOADING

        AudioTrimmer.with()
                .setFile(_originalPath.value.orEmpty())
                .setStartTime(startTimeMinutes, startTimeSeconds)
                .setEndTime(endTimeMinutes, endTimeSeconds)
                .setRingtonePath(_ringtonePath.value.orEmpty())
                .trim { isCuttingSuccessful ->
                    when {
                        isCuttingSuccessful -> {
                            _cuttingState.value = CuttingState.SUCCESSFUL
                        }
                        isTimeEmpty(startTimeMinutes, startTimeSeconds, endTimeMinutes, endTimeSeconds) -> {
                            _cuttingState.value =
                                    CuttingState.ERROR(context.getString(R.string.enterTimeIntervalError))
                        }
                        else -> _cuttingState.value = CuttingState.ERROR(context.getString(R.string.unknowError))
                    }
                }
    }

    private fun isTimeEmpty(
            startTimeMinutes: String,
            startTimeSeconds: String,
            endTimeMinutes: String,
            endTimeSeconds: String
    ) = startTimeMinutes.toInt() == 0 && startTimeSeconds.toInt() == 0
            && endTimeMinutes.toInt() == 0 && endTimeSeconds.toInt() == 0

    private fun isRingtoneReadyToMake() = _fileChoosingState.value == true
            && _folderChoosingState.value == true
            && _nameChoosingState.value == true

}