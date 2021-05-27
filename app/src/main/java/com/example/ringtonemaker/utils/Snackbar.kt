package com.example.ringtonemaker.utils

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.createSnackBar(message: String){
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
}