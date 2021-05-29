package com.example.ringtonemaker.state

sealed class CuttingState {
    object LOADING: CuttingState()
    object SUCCESSFUL: CuttingState()
    object READY: CuttingState()
    class ERROR(val message: String): CuttingState()
    object EMPTY: CuttingState()
    object NOTCHOSEN: CuttingState()
}