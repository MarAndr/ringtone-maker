package com.example.ringtonemaker.state

sealed class CuttingState {
    object LOADING: CuttingState()
    object SUCCESSFUL: CuttingState()
    object READY: CuttingState()
    class Error(val message: String): CuttingState()
    object Empty: CuttingState()
}