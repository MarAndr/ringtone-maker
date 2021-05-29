package com.example.ringtonemaker.utils

fun generateTimeList(): MutableList<String>{
    val list = mutableListOf<String>()
    (0 .. 59).forEach { timeItem ->
        list.add(timeItem.toString())
    }
    return list
}