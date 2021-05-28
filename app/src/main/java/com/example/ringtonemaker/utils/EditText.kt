package com.example.ringtonemaker.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.onTextChanged(listener: (String?) -> Unit){
    val textChangedListener = object: TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener(s.toString())
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    this.addTextChangedListener(textChangedListener)
}