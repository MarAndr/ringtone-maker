package com.example.ringtonemaker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtonemaker.repository.Repository
import com.example.ringtonemaker.state.CuttingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RingtoneViewModel: ViewModel() {
    private val _ringtoneCuttingState = MutableStateFlow<CuttingState>(CuttingState.Empty)
    val ringtoneCuttingState: StateFlow<CuttingState> = _ringtoneCuttingState
    private val repository = Repository()
    private val _fileChoosingState = MutableLiveData(false)
    val fileChoosingState: LiveData<Boolean> = _fileChoosingState
    private val _ringtoneFolderChoosingState = MutableLiveData(false)
    val ringtoneFolderChoosingState: LiveData<Boolean> = _ringtoneFolderChoosingState

    fun changeFileChoosingState(choosingState: Boolean){
        _fileChoosingState.value = choosingState
        if (_fileChoosingState.value == true && _ringtoneFolderChoosingState.value == true){
            _ringtoneCuttingState.value = CuttingState.READY
        }
    }

    fun changeRingtoneFolderChoosingState(choosingState: Boolean){
        _ringtoneFolderChoosingState.value = choosingState
        if (_fileChoosingState.value == true && _ringtoneFolderChoosingState.value == true){
            _ringtoneCuttingState.value = CuttingState.READY
        }
    }

    fun trimAudio(originalPath: String, startTime: String, endTime: String, ringtonePath: String){
        _ringtoneCuttingState.value = CuttingState.LOADING
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
            repository.execFFMpegBinary(cmd){ isCuttingSuccessful ->
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
    private fun isTimeFormatFalse(startTime: String?, endTime: String?) = startTime?.toInt() == null || endTime?.toInt() == null
    private fun isRingtoneFolderNotChoosed(ringtonePath: String?) = ringtonePath == null
    private fun isOriginalFileNotChoosed(originalPath: String?) = originalPath == null

}