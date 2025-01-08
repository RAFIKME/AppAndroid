package com.example.myapplication.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // MutableState to hold shopName
    var shopName: MutableState<String> = mutableStateOf("")

    // Private MutableState to hold headerName
    private val _headerName = mutableStateOf("")
    val headerName: String get() = _headerName.value

    // Function to set the headerName
    fun setHeaderName(name: String) {
        _headerName.value = name
    }
}
