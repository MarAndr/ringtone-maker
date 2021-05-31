package com.example.ringtonemaker.utils

fun generateTimeList(): MutableList<String>{
    val list = mutableListOf<String>()
    (0 .. 59).forEach { timeItem ->
        list.add(timeItem.toString())
    }
    return list
}

fun extractSeconds(fullTime: String): Int{
    val minutesAndSeconds = fullTime.substringAfter(':')
    val minutes = minutesAndSeconds.substringBefore(':')
    val seconds = minutesAndSeconds.substringAfter(':')
    return (minutes.toInt())*60 + seconds.toInt()
}