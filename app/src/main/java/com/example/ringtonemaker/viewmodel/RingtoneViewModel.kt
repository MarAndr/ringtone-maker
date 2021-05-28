package com.example.ringtonemaker.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtonemaker.repository.Repository
import com.example.ringtonemaker.state.CuttingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RingtoneViewModel : ViewModel() {
    private val _ringtoneCuttingState = MutableStateFlow<CuttingState>(CuttingState.Empty)
    val ringtoneCuttingState: StateFlow<CuttingState> = _ringtoneCuttingState
    private val repository = Repository()
    private val _fileChoosingState = MutableLiveData(false)
    private val _ringtoneFolderChoosingState = MutableLiveData(false)
    private val _ringtoneNameChoosingState = MutableLiveData(false)


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
        _ringtoneCuttingState.value = CuttingState.LOADING

//    "fade=in:st=0:d=5",
//    "fade=in:5:8",
        val cmd = arrayOf(
                "-i",
                originalPath,
                "-ss",
                "00:00:$startTime",
                "-t",
                "00:00:$endTime",
                "-c",
                "copy",
                ringtonePath
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
                    else -> _ringtoneCuttingState.value = CuttingState.Error("Неопознанная ошибка!")
                }
            }
        }
    }

    private fun isTimeEmpty(startTime: String?, endTime: String?) = startTime?.isBlank() == true || endTime?.isBlank() == true
    private fun isRingtoneReadyToMake() = _fileChoosingState.value == true
            && _ringtoneFolderChoosingState.value == true
            && _ringtoneNameChoosingState.value == true

}